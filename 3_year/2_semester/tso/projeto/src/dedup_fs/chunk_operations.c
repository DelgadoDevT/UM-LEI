#include "dedup_fs/chunk_operations.h"
#include "dedup_fs/hashing.h"
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <limits.h>

// --- Chunk Storage Functions ---

/**
 * @brief Writes a single 4KB chunk to a physical container file.
 * 
 * Prioritizes reusing abandoned slots from the free_list. If none exist,
 * it appends to the active container, automatically creating a new container
 * if the active one reaches the size limit (CONTAINER_SIZE).
 * 
 * @param context The global filesystem context.
 * @param chunk_data A buffer containing exactly BLOCK_SIZE (4KB) bytes of data.
 * @param container_id A pointer where the ID of the chosen container will be written.
 * @param offset A pointer where the byte offset of the written chunk will be written.
 * @return 0 on success, or a negative error code (e.g., -errno or -EIO).
 */
int chunk_store_write(Context* context, const char* chunk_data, 
                      uint32_t* container_id, uint64_t* offset) {
    int target_fd;
    uint32_t target_container_id;
    uint64_t target_offset;
    int temp_fd;

    if (context->free_list != NULL && context->free_list->len > 0) {
        guint last_index = context->free_list->len - 1;
        FreeSlot slot = g_array_index(context->free_list, FreeSlot, last_index);
        
        target_container_id = slot.container_id;
        target_offset = slot.offset;
        
        g_array_remove_index(context->free_list, last_index);
    } else {
        if (context->active_container_fd == -1) {
            temp_fd = open_container(context, context->active_container_id, O_RDWR | O_CREAT);
            if (temp_fd < 0) {
                return temp_fd;
            }
            context->active_container_fd = temp_fd;
        }

        target_offset = lseek(context->active_container_fd, 0, SEEK_END);
        if (target_offset == (off_t)-1) {
            return -errno;
        }

        if (target_offset >= CONTAINER_SIZE) {
            close(context->active_container_fd);
            context->active_container_fd = -1;
            
            context->active_container_id++;
            temp_fd = open_container(context, context->active_container_id, O_RDWR | O_CREAT);
            
            if (temp_fd < 0) {
                return temp_fd;
            }
            context->active_container_fd = temp_fd;
            target_offset = 0;
        }
        
        target_container_id = context->active_container_id;
    }

    if (target_container_id == context->active_container_id) {
        target_fd = context->active_container_fd;
    } else {
        target_fd = open_container(context, target_container_id, O_RDWR);
        if (target_fd < 0) {
            return target_fd;
        }
    }

    ssize_t bytes_written = pwrite(target_fd, chunk_data, BLOCK_SIZE, target_offset);
    if (bytes_written != BLOCK_SIZE) {
        int err = (bytes_written == -1) ? -errno : -EIO;
        
        if (target_fd != context->active_container_fd) {
            close(target_fd);
        }
        return err;
    }

    if (target_fd != context->active_container_fd) {
        close(target_fd);
    }

    *container_id = target_container_id;
    *offset = target_offset;

    return 0;
}

/**
 * @brief Reads a 4KB chunk from a physical container file.
 * 
 * Opens the specified container file, seeks to the offset, and reads exactly
 * one block of data into the provided buffer.
 * 
 * @param context The global filesystem context.
 * @param container_id The ID of the container file.
 * @param offset The byte offset within the container file.
 * @param chunk_data Buffer to store the read data (must be at least 4KB).
 * @return 0 on success, or a negative error code (e.g., -errno or -EIO).
 */
int chunk_store_read(Context* context, uint32_t container_id,
                     uint64_t offset, char* chunk_data) {
    int fd = open_container(context, container_id, O_RDONLY);
    if (fd < 0) {
        return fd;  // Error
    }

    ssize_t bytes_read = pread(fd, chunk_data, BLOCK_SIZE, offset);
    
    close(fd);
    
    if (bytes_read != BLOCK_SIZE) {
        return (bytes_read == -1) ? -errno : -EIO;
    }

    return 0;
}

/**
 * @brief Marks a physical chunk slot as free for reuse.
 * 
 * Adds the container ID and offset of a deleted chunk to the global free_list,
 * allowing subsequent write operations to reuse this physical space instead of
 * allocating new space. Decrements the total physical size counter.
 * 
 * @param context The global filesystem context.
 * @param container_id The ID of the container file where the chunk resides.
 * @param offset The byte offset of the chunk within the container.
 * @return 0 on success, or a negative error code on failure.
 */
int chunk_store_delete(Context* context, uint32_t container_id, uint64_t offset) {
    if (context == NULL || context->free_list == NULL) {
        errno = EINVAL;
        return -errno;
    }

    if (context->total_physical_size < BLOCK_SIZE) {
        errno = EOVERFLOW;
        return -errno;
    }

    FreeSlot slot;
    slot.container_id = container_id;
    slot.offset = offset;
    
    // Append the FreeSlot to the context's free_list (GArray)
    g_array_append_val(context->free_list, slot);
    
    // Update total_physical_size by subtracting BLOCK_SIZE
    context->total_physical_size -= BLOCK_SIZE;
    
    return 0;
}


// --- Engine Functions ---

/**
 * @brief The core deduplication engine logic for writing data.
 * 
 * Splits a file's input buffer into 4KB chunks, calculates the SHA-256 hash
 * for each chunk, and queries the chunk_index. If the chunk exists, its
 * ref_count is incremented. If not, it is physically written via
 * chunk_store_write(). It then appends the hashes to the file's metadata.
 * 
 * @param context The global filesystem context.
 * @param path The path of the file being written to.
 * @param buf The input data buffer from the user.
 * @param size The number of bytes to write.
 * @param offset The byte offset in the file to begin writing at.
 * @return The number of bytes successfully processed, or a negative error code.
 */
int engine_process_write(Context* context, const char* path, const char* buf,
                        size_t size, off_t offset) {
    if (size == 0) {
        return 0; 
    }

    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    if (meta == NULL) {
        return -ENOENT;
    }

    // Validate append-only assumption: writes must always extend the file
    uint64_t expected_offset = (meta->block_hashes->len) * BLOCK_SIZE;
    if (offset != expected_offset) {
        return -EINVAL;
    }

    uint64_t start_block = offset / BLOCK_SIZE;
    uint64_t end_block = (offset + size - 1) / BLOCK_SIZE;

    const char* current_buf = buf;
    size_t remaining_size = size;

    for (uint64_t block = start_block; block <= end_block; block++) {
        char chunk[BLOCK_SIZE] = {0};
        size_t chunk_size = (remaining_size > BLOCK_SIZE) ? BLOCK_SIZE : remaining_size;
        memcpy(chunk, current_buf, chunk_size);
        
        char hash[SHA256_HEX_LEN];
        calculate_sha256(chunk, BLOCK_SIZE, hash);

        ChunkInfo* info = (ChunkInfo*)g_hash_table_lookup(context->chunk_index, hash);

        if (info != NULL) {
            info->ref_count++;
        } else {
            uint32_t cid;
            uint64_t coff;
            int err = chunk_store_write(context, chunk, &cid, &coff);
            if (err < 0) {
                return err;
            }

            info = g_new0(ChunkInfo, 1);
            strcpy(info->hash, hash);
            info->container_id = cid;
            info->offset = coff;
            info->ref_count = 1;

            g_hash_table_insert(context->chunk_index, g_strdup(hash), info);
            
            context->total_physical_size += BLOCK_SIZE;
        }

        // We assume strictly append-only workload, so we just append the hash
        char* hash_copy = g_strdup(hash);
        g_array_append_val(meta->block_hashes, hash_copy);

        current_buf += chunk_size;
        remaining_size -= chunk_size;
    }

    if (offset + size > meta->logical_size) {
        uint64_t size_diff = (offset + size) - meta->logical_size;
        meta->logical_size = offset + size;
        context->total_logical_size += size_diff;
    }

    return size;
}

/**
 * @brief Handles the deletion of a file and its associated metadata.
 * 
 * Decrements the reference counts for all data chunks associated with the file.
 * If a chunk's reference count drops to zero, its physical storage space is
 * reclaimed by adding it to the free list. Finally, removes the file from the metaindex.
 * 
 * @param context The global filesystem context.
 * @param path The path of the file being deleted.
 * @return 0 on success, or a negative error code if the file is not found.
 */
int engine_process_delete(Context* context, const char* path) {
    // This handles file deletion with garbage collection.

    // 1. Look up the file in context->metaindex
    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    if (meta == NULL) {
        return -ENOENT;
    }

    // 2. Get the FileMeta struct for the file (already done above)
    
    // 3. Iterate through the file's block_hashes GArray
    for (guint i = 0; i < meta->block_hashes->len; i++) {
        char* hash = g_array_index(meta->block_hashes, char*, i);
        
        // a. Look up the hash in context->chunk_index to get the ChunkInfo
        ChunkInfo* info = (ChunkInfo*)g_hash_table_lookup(context->chunk_index, hash);
        if (info == NULL) {
            continue;  // Skip if corrupted
        }
        
        // b. Decrement the ChunkInfo's ref_count
        info->ref_count--;
        
        // c. If ref_count reaches 0
        if (info->ref_count == 0) {
            // - Call chunk_store_delete() to add the location to free_list
            int err = chunk_store_delete(context, info->container_id, info->offset);
            if (err < 0) {
                return err;
            }
            
            // - Remove the ChunkInfo from context->chunk_index
            g_hash_table_remove(context->chunk_index, hash);
        }
    }

    // 4. Update context->total_logical_size by subtracting the file's logical_size
    context->total_logical_size -= meta->logical_size;

    // 5. Remove the file entry from context->metaindex
    // This automatically calls destroy_file_meta to free the array and hash strings.
    g_hash_table_remove(context->metaindex, path);

    return 0;
}

/**
 * @brief Reconstructs file data by reading its deduplicated chunks.
 * 
 * Resolves the requested file offset and size to the corresponding block hashes.
 * Retrieves the physical location of each block from the chunk index and reads
 * the data from the container files to satisfy the read request.
 * 
 * @param context The global filesystem context.
 * @param path The path of the file to read.
 * @param buf The output buffer where the read data will be stored.
 * @param size The number of bytes to read.
 * @param offset The byte offset in the file to start reading from.
 * @return The number of bytes successfully read, or a negative error code.
 */
int engine_process_read(Context* context, const char* path, char* buf,
                       size_t size, off_t offset) {
    // This reconstructs file data from deduplicated chunks.
    
    // 1. Look up the file in context->metaindex - If not found, return -ENOENT.
    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    if (meta == NULL) {
        return -ENOENT;
    }

    // 2. Check if the requested offset + size exceeds the file's logical_size
    //    Adjust size if necessary to avoid reading past end of file
    if (offset >= (off_t)meta->logical_size) {
        return 0;  // Reading past EOF
    }
    
    if (offset + (off_t)size > (off_t)meta->logical_size) {
        size = meta->logical_size - offset;  // Truncate to EOF
    }

    // 3. Calculate which blocks need to be read
    uint64_t start_block = offset / BLOCK_SIZE;
    uint64_t end_block = (offset + size - 1) / BLOCK_SIZE;

    // 4. For each needed block
    size_t bytes_read = 0;
    char chunk[BLOCK_SIZE];
    
    for (uint64_t block = start_block; block <= end_block; block++) {
        // a. Get the hash string from the file's block_hashes GArray at index
        if (block >= meta->block_hashes->len) {
            break;  // No more blocks
        }
        
        char* hash = g_array_index(meta->block_hashes, char*, block);
        
        // b. Look up the hash in context->chunk_index to get ChunkInfo
        ChunkInfo* info = (ChunkInfo*)g_hash_table_lookup(context->chunk_index, hash);
        if (info == NULL) {
            return -EIO;  // Corrupted metadata
        }
        
        // c. Call chunk_store_read() to retrieve the 4KB chunk
        int err = chunk_store_read(context, info->container_id, info->offset, chunk);
        if (err < 0) {
            return err;
        }
        
        // d. Copy the relevant portion of the chunk to the output buffer 'buf'
        //    Handle partial reads at the start and end blocks
        size_t chunk_offset = (block == start_block) ? (offset % BLOCK_SIZE) : 0;
        size_t chunk_size_to_copy = BLOCK_SIZE - chunk_offset;
        
        if (bytes_read + chunk_size_to_copy > size) {
            chunk_size_to_copy = size - bytes_read;
        }
        
        memcpy(buf + bytes_read, chunk + chunk_offset, chunk_size_to_copy);
        bytes_read += chunk_size_to_copy;
    }

    return (int)bytes_read;
}

/**
 * @brief Adjusts the logical size of a file, performing garbage collection if necessary.
 * 
 * If the file is truncated to a smaller size, it identifies the orphaned blocks,
 * decrements their reference counts, and reclaims physical space for any blocks
 * that are no longer referenced. Extending a file beyond its current size is not
 * fully implemented and returns an error.
 * 
 * @param context The global filesystem context.
 * @param path The path of the file to truncate.
 * @param new_size The desired new size of the file in bytes.
 * @return 0 on success, or a negative error code.
 */
int engine_process_truncate(Context* context, const char* path, off_t new_size) {
    // This handles file truncation with chunk cleanup.
    // 1. Look up the file in context->metaindex
    FileMeta* meta = (FileMeta*)g_hash_table_lookup(context->metaindex, path);
    if (meta == NULL) {
        return -ENOENT;
    }

    // 2. Calculate the old number of blocks and new number of blocks
    uint64_t old_blocks = (meta->logical_size + BLOCK_SIZE - 1) / BLOCK_SIZE;
    uint64_t new_blocks = (new_size + BLOCK_SIZE - 1) / BLOCK_SIZE;

    // 3. If new_size < current logical_size (truncating down)
    if (new_size < (off_t)meta->logical_size) {
        // a. For each block from old_blocks - 1 down to new_blocks
        for (uint64_t block = old_blocks; block > new_blocks; block--) {
            uint64_t idx = block - 1;
            if (idx >= meta->block_hashes->len) {
                continue;
            }
            
            char* hash = g_array_index(meta->block_hashes, char*, idx);
            
            // - Look up ChunkInfo in chunk_index
            ChunkInfo* info = (ChunkInfo*)g_hash_table_lookup(context->chunk_index, hash);
            if (info != NULL) {
                // - Decrement ref_count
                info->ref_count--;
                
                // - If ref_count == 0, call chunk_store_delete() and remove from chunk_index
                if (info->ref_count == 0) {
                    chunk_store_delete(context, info->container_id, info->offset);
                    g_hash_table_remove(context->chunk_index, hash);
                }
            }
            
            // - Remove the hash from the block_hashes GArray and free the string
            g_array_remove_index(meta->block_hashes, idx);
            g_free(hash);
        }
        
        // b. Update context->total_logical_size
        context->total_logical_size -= (meta->logical_size - new_size);
    }
    // 4. If new_size > current logical_size (extending):
    else if (new_size > (off_t)meta->logical_size) {
        // This is more complex; for now, we can return -ENOSYS
        // A full implementation would need to add zero-filled chunks
        return -ENOSYS;
    }

    // 5. Update the file's logical_size in FileMeta
    meta->logical_size = new_size;

    return 0;
}


// --- Helper Functions ---

/**
 * @brief Constructs the full file path for a given container ID.
 * 
 * Uses the root directory of the context to generate a string pointing to
 * the exact `.dat` file for the specific container.
 * 
 * @param context The global filesystem context.
 * @param container_id The ID of the container.
 * @param path_buf The buffer to write the constructed path into.
 * @return 0 on success, or a negative error code on failure.
 */
int get_container_path(Context* context, uint32_t container_id, char* path_buf) {
    if (context == NULL || context->root_dir == NULL || path_buf == NULL) {
        return -EINVAL;
    }

    int written = snprintf(path_buf, PATH_MAX, "%s/containers/container_%u.dat",
                           context->root_dir, container_id);

    if (written < 0 || written >= PATH_MAX) {
        return -ENAMETOOLONG;
    }

    return 0;
}

/**
 * @brief Helper function to open or create a specific data container file.
 * 
 * Determines the file path for the given container_id, ensures the parent
 * "containers" directory exists, and opens the file with the specified flags.
 * 
 * @param context The global filesystem context.
 * @param container_id The ID of the container file to open (e.g., 0 for container_0.dat).
 * @param flags The file access mode flags (e.g., O_RDWR | O_CREAT).
 * @return An open file descriptor for the container, or a negative error code (e.g., -errno).
 */
int open_container(Context* context, uint32_t container_id, int flags) {
    char path[PATH_MAX];
    char dir_path[PATH_MAX];

    int err = get_container_path(context, container_id, path);
    if (err < 0) {
        return err;
    }

    snprintf(dir_path, PATH_MAX, "%s/containers", context->root_dir);
    
    if (mkdir(dir_path, 0755) == -1) {
        if (errno != EEXIST) {
            return -errno;
        }
    }

    int fd;
    // In C, if you use the O_CREAT flag to create a file, you MUST provide a third 
    // argument to open() specifying the file permissions (mode). 
    if (flags & O_CREAT) {
        fd = open(path, flags, 0666);
    } else {
        fd = open(path, flags);
    }

    if (fd < 0) {
        return -errno;
    }

    return fd;
}

/**
 * @brief Generates a formatted string containing filesystem statistics.
 * 
 * Calculates the deduplication ratio and writes a human-readable text block
 * with logical size, physical size, and savings into the provided buffer.
 * 
 * @param context The global filesystem context.
 * @param buf The buffer to write the formatted string into.
 * @param buf_size The maximum capacity of the output buffer.
 * @return The number of bytes written, or a negative error code on failure.
 */
int generate_stats_string(Context* context, char* buf, size_t buf_size) {
    if (context == NULL || buf == NULL) {
        return -EINVAL;
    }

    double ratio;
    if (context->total_physical_size == 0) {
        ratio = 1.0;  // Evitar divisão por zero
    } else {
        ratio = (double)context->total_logical_size / (double)context->total_physical_size;
    }

    int written = snprintf(buf, buf_size,
        "=== Deduplication File System Statistics ===\n"
        "Logical Size:   %llu bytes (%.2f MB)\n"
        "Physical Size:  %llu bytes (%.2f MB)\n"
        "Dedup Ratio:    %.2fx\n"
        "Savings:        %.2f MB\n",
        (unsigned long long)context->total_logical_size,
        (double)context->total_logical_size / (1024.0 * 1024.0),
        (unsigned long long)context->total_physical_size,
        (double)context->total_physical_size / (1024.0 * 1024.0),
        ratio,
        (double)((int64_t)context->total_logical_size - (int64_t)context->total_physical_size) / (1024.0 * 1024.0)
    );

    if (written < 0 || (size_t)written >= buf_size) {
        return -ENOBUFS;
    }

    return written;
}