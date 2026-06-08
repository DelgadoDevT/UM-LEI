#define FUSE_USE_VERSION 31

#include <fuse.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <stdbool.h>

#include "dedup_fs/metadata.h"
#include "dedup_fs/persistence.h"
#include "dedup_fs/hashing.h"
#include "dedup_fs/chunk_operations.h"

// --- Helper Functions ---

/**
 * @brief Helper to securely free a FileMeta value.
 * 
 * Called automatically by GHashTable when an entry is removed or the table is destroyed.
 */
static void destroy_file_meta(gpointer data) {
    FileMeta* meta = (FileMeta*)data;
    if (meta != NULL) {
        if (meta->block_hashes != NULL) {
            for (guint i = 0; i < meta->block_hashes->len; i++) {
                g_free(g_array_index(meta->block_hashes, char*, i));
            }
            g_array_free(meta->block_hashes, TRUE);
        }
        g_free(meta);
    }
}

/**
 * @brief Initializes a new Context structure.
 *
 * Allocates and initializes all fields of the Context struct, including
 * GLib data structures and the mutex.
 *
 * @param root_dir The root directory path for the filesystem storage.
 * @return Pointer to initialized Context, or NULL on failure.
 */
static Context* context_init(const char* root_dir) {
    Context* context = malloc(sizeof(Context));
    if (context == NULL) {
        return NULL;
    }
// Duplica a string root_dir para armazenar no contexto
    context->root_dir = strdup(root_dir);
    if (context->root_dir == NULL) {
        free(context);
        return NULL;
    }

    context->metaindex = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, destroy_file_meta);
    context->chunk_index = g_hash_table_new_full(g_str_hash, g_str_equal, g_free, g_free);
    context->free_list = g_array_new(FALSE, FALSE, sizeof(FreeSlot));

    context->active_container_id = 0;
    context->active_container_fd = -1;

    context->total_logical_size = 0;
    context->total_physical_size = 0;

    context->cache_path = NULL;
    context->cache_data = NULL;
    context->cache_size = 0;

    pthread_mutex_init(&context->mutex, NULL);

    return context;
}

/**
 * @brief Destroys a Context structure and frees all resources.
 *
 * Frees all memory allocated for the Context, including GLib structures.
 *
 * @param context The context to destroy.
 */
static void context_destroy(Context* context) {
    if (context == NULL) {
        return;
    }

    g_hash_table_destroy(context->metaindex);
    g_hash_table_destroy(context->chunk_index);
    g_array_free(context->free_list, TRUE);

    if (context->cache_path != NULL) free(context->cache_path);
    if (context->cache_data != NULL) free(context->cache_data);

    if (context->active_container_fd >= 0) {
        close(context->active_container_fd);
    }

    free(context->root_dir);

    pthread_mutex_destroy(&context->mutex);

    free(context);
}
// --- FUSE Operation Stubs ---

static void* dedup_init(struct fuse_conn_info* conn, struct fuse_config* cfg) {
    (void)conn;
    cfg->use_ino = 1;
    cfg->entry_timeout = 0;
    cfg->attr_timeout = 0;
    cfg->negative_timeout = 0;

    const char* root_dir = (const char*)fuse_get_context()->private_data;

    Context* context = context_init(root_dir);
    if (context == NULL) {
        fprintf(stderr, "Failed to initialize context\n");
        return NULL;
    }

    if (mkdir(root_dir, 0755) == -1 && errno != EEXIST) {
        fprintf(stderr, "Failed to create root_dir: %s\n", strerror(errno));
        context_destroy(context);
        return NULL;
    }

    load_metadata(context);

    return context;
}

/**
 * @brief FUSE callback to clean up the filesystem on unmount.
 * 
 * Saves the current metadata state to persistent storage and frees all
 * memory associated with the global Context.
 * 
 * @param private_data Pointer to the global Context structure.
 */
static void dedup_destroy(void* private_data) {
    Context* context = (Context*)private_data;
    if (context == NULL) {
        return;
    }
    
    save_metadata(context);
    context_destroy(context);
}

/**
 * @brief FUSE callback to get file or directory attributes.
 * 
 * Populates the stat structure for a given path. It specifically handles
 * the virtual "/stats.txt" file, the root directory "/", and retrieves
 * logical sizes and permissions for regular files from the metaindex.
 * 
 * @param path The path of the file or directory.
 * @param stbuf The stat structure to populate.
 * @param fi FUSE file information structure.
 * @return 0 on success, or -ENOENT if the path is not found.
 */
static int dedup_getattr(const char* path, struct stat* stbuf, struct fuse_file_info* fi) {
    (void)fi;
    memset(stbuf, 0, sizeof(struct stat));

    // 1. Raiz do filesystem
    if (strcmp(path, "/") == 0) {
        stbuf->st_mode = S_IFDIR | 0755;
        stbuf->st_nlink = 2;
        return 0;
    }

    // 2. Ficheiro virtual de estatísticas
    if (strcmp(path, "/stats.txt") == 0) {
        stbuf->st_mode = S_IFREG | 0444;
        stbuf->st_nlink = 1;
        stbuf->st_size = 512;
        return 0;
    }

    // 3. Qualquer outro path — procurar no metaindex
    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);
    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    pthread_mutex_unlock(&context->mutex);

    if (meta == NULL) {
        return -ENOENT;
    }

    stbuf->st_mode = meta->mode;
    stbuf->st_nlink = (meta->type == TYPE_DIRECTORY) ? 2 : 1;
    stbuf->st_size = meta->logical_size;

    return 0;
}

/**
 * @brief FUSE callback to read the contents of a directory.
 * 
 * Iterates through the metaindex to find all entries that are children of
 * the specified directory path and adds them to the directory listing.
 * 
 * @param path The path of the directory to read.
 * @param buf The buffer to hold the directory entries.
 * @param filler The FUSE filler function used to add entries to the buffer.
 * @param offset The directory stream offset.
 * @param fi FUSE file information structure.
 * @param flags FUSE readdir flags.
 * @return 0 on success.
 */
static int dedup_readdir(const char* path, void* buf, fuse_fill_dir_t filler, off_t offset, struct fuse_file_info* fi, enum fuse_readdir_flags flags) {
    (void)offset;
    (void)fi;
    (void)flags;

   
    filler(buf, ".", NULL, 0, 0);
    filler(buf, "..", NULL, 0, 0);

  
    if (strcmp(path, "/") == 0) {
        filler(buf, "stats.txt", NULL, 0, 0);
    }


    Context* context = (Context*)fuse_get_context()->private_data;
    size_t path_len = strlen(path);

    pthread_mutex_lock(&context->mutex);

    GHashTableIter iter;
    gpointer key, value;
    g_hash_table_iter_init(&iter, context->metaindex);

    while (g_hash_table_iter_next(&iter, &key, &value)) {
        const char* entry_path = (const char*)key;

        // O entry_path tem de começar com path
        if (strncmp(entry_path, path, path_len) != 0) {
            continue;
        }

        // O que vem a seguir tem de ser '/' + nome (sem mais '/')
        const char* remainder = entry_path + path_len;

        // Se path é "/", o remainder começa com "/"
        // Se path é "/dir", o remainder começa com "/"
        if (path_len > 1 && remainder[0] != '/') {
            continue;
        }

        // Avançar o '/' inicial
        const char* name = (path_len == 1) ? remainder : remainder + 1;

        // Garantir que é filho direto (sem '/' no nome)
        if (strlen(name) == 0 || strchr(name, '/') != NULL) {
            continue;
        }

        filler(buf, name, NULL, 0, 0);
    }

    pthread_mutex_unlock(&context->mutex);

    return 0;
}

/**
 * @brief Creates a new file entry in the file system.
 * 
 * Inserts a new FileMeta structure into the metaindex for the specified path,
 * initializing its properties for an empty regular file. Returns -EEXIST if
 * the path is already in use.
 * 
 * @param path The path of the new file to create.
 * @param mode The permissions and type mode for the file.
 * @param rdev Device number (unused in this file system).
 * @return 0 on success, or a negative error code (e.g., -EEXIST).
 */
static int dedup_mknod(const char* path, mode_t mode, dev_t rdev) {
    (void)rdev;

    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    if (g_hash_table_contains(context->metaindex, path)) {
        pthread_mutex_unlock(&context->mutex);
        return -EEXIST; // Error: File already exists
    }

    FileMeta* meta = g_new0(FileMeta, 1);
    meta->type = TYPE_FILE;
    meta->logical_size = 0;
    
    // If the OS didn't specify it's a regular file, force it to be one
    if (!S_ISREG(mode)) {
        mode = (mode & 0777) | S_IFREG;  // Clear type bits, keep permissions, set regular file
    }
    meta->mode = mode;
    
    meta->block_hashes = g_array_new(FALSE, FALSE, sizeof(char*));

    g_hash_table_insert(context->metaindex, g_strdup(path), meta);

    pthread_mutex_unlock(&context->mutex);

    return 0;
}

/**
 * @brief FUSE callback to create a new directory.
 * 
 * Creates a new FileMeta entry of type TYPE_DIRECTORY and inserts it into
 * the metaindex.
 * 
 * @param path The path of the new directory.
 * @param mode The permissions for the new directory.
 * @return 0 on success, or a negative error code.
 */
static int dedup_mkdir(const char* path, mode_t mode) {
    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    if (g_hash_table_contains(context->metaindex, path)) {
        pthread_mutex_unlock(&context->mutex);
        return -EEXIST;
    }

    FileMeta* meta = g_new0(FileMeta, 1);
    meta->type = TYPE_DIRECTORY;
    meta->logical_size = 0;
    meta->mode = (mode & 0777) | S_IFDIR;
    meta->block_hashes = g_array_new(FALSE, FALSE, sizeof(char*));

    g_hash_table_insert(context->metaindex, g_strdup(path), meta);

    pthread_mutex_unlock(&context->mutex);

    return 0;
}


/**
 * @brief FUSE callback to delete a file.
 * 
 * Locks the context mutex to ensure thread safety and delegates the deletion
 * logic to the engine_process_delete function.
 * 
 * @param path The path of the file to delete.
 * @return 0 on success, or a negative error code.
 */
static int dedup_unlink(const char* path) {
    Context* context = (Context*)fuse_get_context()->private_data;
    
    pthread_mutex_lock(&context->mutex);
    int result = engine_process_delete(context, path);
    pthread_mutex_unlock(&context->mutex);
    
    return result;
}

/**
 * @brief FUSE callback to remove a directory.
 * 
 * Checks if the directory is empty and removes its entry from the metaindex.
 * 
 * @param path The path of the directory to remove.
 * @return 0 on success, or a negative error code.
 */
static int dedup_rmdir(const char* path) {
    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    if (meta == NULL) {
        pthread_mutex_unlock(&context->mutex);
        return -ENOENT;
    }

    if (meta->type != TYPE_DIRECTORY) {
        pthread_mutex_unlock(&context->mutex);
        return -ENOTDIR;
    }


    size_t path_len = strlen(path);
    GHashTableIter iter;
    gpointer key, value;
    g_hash_table_iter_init(&iter, context->metaindex);

    while (g_hash_table_iter_next(&iter, &key, &value)) {
        const char* entry_path = (const char*)key;

        if (strncmp(entry_path, path, path_len) != 0) continue;

        const char* remainder = entry_path + path_len;
        if (remainder[0] != '/') continue;

        const char* name = remainder + 1;
        if (strlen(name) > 0) {
            // Encontrou um filho (direto ou indireto) — diretório não está vazio
            pthread_mutex_unlock(&context->mutex);
            return -ENOTEMPTY;
        }
    }

    g_hash_table_remove(context->metaindex, path);

    pthread_mutex_unlock(&context->mutex);

    return 0;
}

/**
 * @brief FUSE callback to rename a file or directory.
 * 
 * Updates the path key in the metaindex for the specified entry.
 * 
 * @param from The current path of the entry.
 * @param to The new path for the entry.
 * @param flags FUSE rename flags.
 * @return 0 on success, or a negative error code.
 */
static int dedup_rename(const char* from, const char* to, unsigned int flags) {
    if (flags) return -EINVAL;

    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, from);
    if (meta == NULL) {
        pthread_mutex_unlock(&context->mutex);
        return -ENOENT;
    }

    if (meta->type == TYPE_DIRECTORY) {
        // Collect all child entries starting with 'from/'
        GPtrArray* keys_to_rename = g_ptr_array_new();
        size_t from_len = strlen(from);
        
        GHashTableIter iter;
        gpointer key, value;
        g_hash_table_iter_init(&iter, context->metaindex);
        while (g_hash_table_iter_next(&iter, &key, &value)) {
            const char* entry_path = (const char*)key;
            if (strncmp(entry_path, from, from_len) == 0 && entry_path[from_len] == '/') {
                g_ptr_array_add(keys_to_rename, (gpointer)entry_path);
            }
        }
        
        // Rename all children
        for (guint i = 0; i < keys_to_rename->len; i++) {
            const char* old_path = (const char*)g_ptr_array_index(keys_to_rename, i);
            char new_path[PATH_MAX];
            snprintf(new_path, PATH_MAX, "%s%s", to, old_path + from_len);
            
            FileMeta* child_meta = (FileMeta*)g_hash_table_lookup(context->metaindex, old_path);
            g_hash_table_steal(context->metaindex, old_path);
            g_hash_table_insert(context->metaindex, g_strdup(new_path), child_meta);
        }
        g_ptr_array_free(keys_to_rename, TRUE);
    }

    if (g_hash_table_contains(context->metaindex, to)) {
        FileMeta* to_meta = (FileMeta*)g_hash_table_lookup(context->metaindex, to);
        if (to_meta->type == TYPE_DIRECTORY) {
            pthread_mutex_unlock(&context->mutex);
            return -EISDIR;
        }
        // Correctly garbage collect the physical chunks of the file being overwritten
        engine_process_delete(context, to);
    }

    g_hash_table_steal(context->metaindex, from);
    g_hash_table_insert(context->metaindex, g_strdup(to), meta);

    pthread_mutex_unlock(&context->mutex);

    return 0;
}


/**
 * @brief FUSE callback to change the size of a file.
 * 
 * Locks the context mutex to ensure thread safety and delegates the truncation
 * logic to the engine_process_truncate function.
 * 
 * @param path The path of the file to truncate.
 * @param size The new size of the file.
 * @param fi FUSE file information structure.
 * @return 0 on success, or a negative error code.
 */
static int dedup_truncate(const char* path, off_t size, struct fuse_file_info* fi) {
    (void)fi;
    
    Context* context = (Context*)fuse_get_context()->private_data;
    
    pthread_mutex_lock(&context->mutex);
    int result = engine_process_truncate(context, path, size);
    pthread_mutex_unlock(&context->mutex);
    
    return result;
}

/**
 * @brief Opens a file and caches its content into memory for reading.
 * 
 * If the file is opened for reading (O_RDONLY or O_RDWR) and is not already
 * in the read cache, it reads the entire file content from the underlying
 * chunk store into the global context's cache buffer.
 * 
 * @param path The path of the file to open.
 * @param fi FUSE file information structure containing open flags.
 * @return 0 on success, or a negative error code (e.g., -ENOENT, -ENOMEM).
 */
static int dedup_open(const char* path, struct fuse_file_info* fi) {
    if (strcmp(path, "/stats.txt") == 0) {
        return 0;
    }
    
    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    if (meta == NULL) {
        pthread_mutex_unlock(&context->mutex);
        return -ENOENT;
    }

    int accmode = fi->flags & O_ACCMODE;
    
    if (accmode == O_RDONLY || accmode == O_RDWR) {
        bool is_already_cached = (context->cache_path != NULL && strcmp(context->cache_path, path) == 0);

        if (!is_already_cached) {
            if (context->cache_path != NULL) {
                free(context->cache_path);
                context->cache_path = NULL;
            }
            if (context->cache_data != NULL) {
                free(context->cache_data);
                context->cache_data = NULL;
            }
            context->cache_size = 0;

            if (meta->logical_size > 0) {
                context->cache_data = malloc(meta->logical_size);
                if (context->cache_data == NULL) {
                    pthread_mutex_unlock(&context->mutex);
                    return -ENOMEM; // Out of memory
                }

                int read_res = engine_process_read(context, path, context->cache_data, meta->logical_size, 0);
                
                if (read_res < 0) {
                    free(context->cache_data);
                    context->cache_data = NULL;
                    free(context->cache_path);
                    context->cache_path = NULL;
                    pthread_mutex_unlock(&context->mutex);
                    return read_res;
                }
                
                context->cache_size = meta->logical_size;
            }

            context->cache_path = strdup(path);
        }
    }

    pthread_mutex_unlock(&context->mutex);
    return 0;
}

/**
 * @brief Closes a file and cleans up its associated read cache.
 * 
 * If the file being released is currently held in the global read cache,
 * this function frees the cached memory and clears the cache state.
 * 
 * @param path The path of the file being closed.
 * @param fi FUSE file information structure.
 * @return 0 on success.
 */
static int dedup_release(const char* path, struct fuse_file_info* fi) {
    (void)fi;

    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    if (context->cache_path != NULL && strcmp(context->cache_path, path) == 0) {
        
        free(context->cache_path);
        context->cache_path = NULL;

        if (context->cache_data != NULL) {
            free(context->cache_data);
            context->cache_data = NULL;
        }

        context->cache_size = 0;
    }

    pthread_mutex_unlock(&context->mutex);

    return 0;
}

/**
 * @brief FUSE callback to read data from a file or pseudo-file.
 * 
 * Intercepts reads to the virtual "/stats.txt" file to dynamically generate
 * filesystem statistics. For regular files, it acquires the context mutex and
 * delegates the read logic to engine_process_read.
 * 
 * @param path The path of the file to read.
 * @param buf The buffer to populate with read data.
 * @param size The number of bytes to read.
 * @param offset The offset to read from.
 * @param fi FUSE file information structure.
 * @return The number of bytes read, or a negative error code.
 */
static int dedup_read(const char* path, char* buf, size_t size, off_t offset, struct fuse_file_info* fi) {
    (void)fi;
    
    // 1. Handle the virtual file "/stats.txt" separately
    if (strcmp(path, "/stats.txt") == 0) {
        char stats_buf[4096];
        int stats_len = generate_stats_string(
            (Context*)fuse_get_context()->private_data, 
            stats_buf, 
            sizeof(stats_buf)
        );
        
        if (stats_len < 0) {
            return stats_len;  // Error
        }
        
        // Handle offset and size to return the correct portion
        if (offset >= stats_len) {
            return 0;  // Reading past EOF
        }
        
        if (offset + (off_t)size > stats_len) {
            size = stats_len - offset;  // Truncate to EOF
        }
        
        memcpy(buf, stats_buf + offset, size);
        return (int)size;
    }
    
    // 2. For regular files
    Context* context = (Context*)fuse_get_context()->private_data;
    
    pthread_mutex_lock(&context->mutex);
    int result = engine_process_read(context, path, buf, size, offset);
    pthread_mutex_unlock(&context->mutex);
    
    return result;
}

/**
 * @brief Writes data to a file by delegating to the deduplication engine.
 * 
 * Invokes engine_process_write() to chunk the input buffer, perform
 * deduplication checks, write new unique blocks, and update the file's
 * metadata. Thread-safe.
 * 
 * @param path The path of the file to write to.
 * @param buf The data buffer to be written.
 * @param size The number of bytes to write.
 * @param offset The byte offset in the file to begin writing at.
 * @param fi FUSE file information structure.
 * @return The number of bytes written, or a negative error code.
 */
static int dedup_write(const char* path, const char* buf, size_t size, off_t offset, struct fuse_file_info* fi) {
    // (void)fi prevents compiler warnings about unused variables,
    (void)fi;
    Context* context = (Context*)fuse_get_context()->private_data;

    pthread_mutex_lock(&context->mutex);

    int result = engine_process_write(context, path, buf, size, offset);

    pthread_mutex_unlock(&context->mutex);

    return result;
}


// --- FUSE Operation Table ---

static const struct fuse_operations dedup_oper = {
    .init       = dedup_init,
    .destroy    = dedup_destroy,
    .getattr    = dedup_getattr,
    .readdir    = dedup_readdir,
    .mknod      = dedup_mknod,
    .mkdir      = dedup_mkdir,
    .unlink     = dedup_unlink,
    .rmdir      = dedup_rmdir,
    .rename     = dedup_rename,
    .truncate   = dedup_truncate,
    .open       = dedup_open,
    .release    = dedup_release,
    .read       = dedup_read,
    .write      = dedup_write,
};


// --- Main Function ---

/**
 * @brief Entry point for the deduplication file system.
 * 
 * Parses custom command-line arguments (e.g., --root_dir) and passes the
 * remaining standard arguments, along with the FUSE operations table,
 * to fuse_main to start the file system daemon.
 * 
 * @param argc The number of command-line arguments.
 * @param argv The array of command-line arguments.
 * @return 0 on successful file system unmount, or an error code on failure.
 */
int main(int argc, char* argv[]) {
    umask(0);

    int new_argc = 0;
    char* new_argv[argc + 1]; 
    
    char* root_dir = "/tmp/dedup_storage"; 

    for (int i = 0; i < argc; i++) {
        if (strcmp(argv[i], "--root_dir") == 0) {
            if (i + 1 < argc) {
                root_dir = argv[i + 1];
                i++; 
            } else {
                fprintf(stderr, "Error: --root_dir requires a path argument.\n");
                return 1;
            }
        } else {
            new_argv[new_argc++] = argv[i];
        }
    }

    new_argv[new_argc] = NULL;

    return fuse_main(new_argc, new_argv, &dedup_oper, root_dir);
}
