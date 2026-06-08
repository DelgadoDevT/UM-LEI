#include "dedup_fs/persistence.h"
#include "dedup_fs/hashing.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/stat.h>
#include <limits.h>

/**
 * @brief Saves all in-memory metadata to disk.
 *
 * This function serializes the metadata indexes and free list and writes them
 * to persistent storage in the root_dir.
 *
 * @param context The global filesystem context.
 * @return 0 on success, a negative error code on failure.
 */
int save_metadata(Context* context) {
    if (context == NULL) return -EINVAL;

    char meta_dir[PATH_MAX];
    snprintf(meta_dir, PATH_MAX, "%s/.meta", context->root_dir);
    if (mkdir(meta_dir, 0755) == -1 && errno != EEXIST) return -errno;

    pthread_mutex_lock(&context->mutex);

    //1. Guardar metaindex
    char path[PATH_MAX];
    snprintf(path, PATH_MAX, "%s/.meta/metaindex.bin", context->root_dir);
    FILE* f = fopen(path, "wb");
    if (!f) { pthread_mutex_unlock(&context->mutex); return -errno; }

    guint n_entries = g_hash_table_size(context->metaindex);
    fwrite(&n_entries, sizeof(guint), 1, f);

    GHashTableIter iter;
    gpointer key, value;
    g_hash_table_iter_init(&iter, context->metaindex);

    while (g_hash_table_iter_next(&iter, &key, &value)) {
        const char* entry_path = (const char*)key;
        FileMeta* meta = (FileMeta*)value;

        // Escrever o path (tamanho + string)
        guint32 path_len = strlen(entry_path) + 1;
        fwrite(&path_len, sizeof(guint32), 1, f);
        fwrite(entry_path, 1, path_len, f);

        // Escrever os campos do FileMeta
        fwrite(&meta->type, sizeof(EntryType), 1, f);
        fwrite(&meta->logical_size, sizeof(size_t), 1, f);
        fwrite(&meta->mode, sizeof(mode_t), 1, f);

        // Escrever o array de hashes
        guint n_hashes = meta->block_hashes->len;
        fwrite(&n_hashes, sizeof(guint), 1, f);
        for (guint i = 0; i < n_hashes; i++) {
            char* hash = g_array_index(meta->block_hashes, char*, i);
            fwrite(hash, 1, SHA256_HEX_LEN, f);
        }
    }
    fclose(f);

    //2. Guardar chunk_index
    snprintf(path, PATH_MAX, "%s/.meta/chunk_index.bin", context->root_dir);
    f = fopen(path, "wb");
    if (!f) { pthread_mutex_unlock(&context->mutex); return -errno; }

    n_entries = g_hash_table_size(context->chunk_index);
    fwrite(&n_entries, sizeof(guint), 1, f);

    g_hash_table_iter_init(&iter, context->chunk_index);
    while (g_hash_table_iter_next(&iter, &key, &value)) {
        ChunkInfo* info = (ChunkInfo*)value;
        fwrite(info, sizeof(ChunkInfo), 1, f);
    }
    fclose(f);

    //3. Guardar free_list
    snprintf(path, PATH_MAX, "%s/.meta/free_list.bin", context->root_dir);
    f = fopen(path, "wb");
    if (!f) { pthread_mutex_unlock(&context->mutex); return -errno; }

    guint n_slots = context->free_list->len;
    fwrite(&n_slots, sizeof(guint), 1, f);
    for (guint i = 0; i < n_slots; i++) {
        FreeSlot slot = g_array_index(context->free_list, FreeSlot, i);
        fwrite(&slot, sizeof(FreeSlot), 1, f);
    }
    fclose(f);

    //4. Guardar contadores e estado
    snprintf(path, PATH_MAX, "%s/.meta/stats.bin", context->root_dir);
    f = fopen(path, "wb");
    if (!f) { pthread_mutex_unlock(&context->mutex); return -errno; }

    fwrite(&context->total_logical_size, sizeof(uint64_t), 1, f);
    fwrite(&context->total_physical_size, sizeof(uint64_t), 1, f);
    fwrite(&context->active_container_id, sizeof(uint32_t), 1, f);
    fclose(f);

    pthread_mutex_unlock(&context->mutex);
    return 0;
}

/**
 * @brief Loads all metadata from disk into memory.
 *
 * This function reads the serialized metadata from the root_dir,
 * deserializes it, and populates the context's indexes and free list.
 *
 * @param context The global filesystem context.
 * @return 0 on success, a negative error code on failure.
 */
int load_metadata(Context* context) {
    if (context == NULL) return -EINVAL;

    char path[PATH_MAX];

    //1. Carregar metaindex
    snprintf(path, PATH_MAX, "%s/.meta/metaindex.bin", context->root_dir);
    FILE* f = fopen(path, "rb");
    if (!f) return 0;  // Montagem fresh, sem metadados ainda

    guint n_entries;
    fread(&n_entries, sizeof(guint), 1, f);

    for (guint i = 0; i < n_entries; i++) {
        // Ler o path
        guint32 path_len;
        fread(&path_len, sizeof(guint32), 1, f);
        char* entry_path = g_malloc(path_len);
        fread(entry_path, 1, path_len, f);

        // Ler os campos do FileMeta
        FileMeta* meta = g_new0(FileMeta, 1);
        fread(&meta->type, sizeof(EntryType), 1, f);
        fread(&meta->logical_size, sizeof(size_t), 1, f);
        fread(&meta->mode, sizeof(mode_t), 1, f);

        // Ler o array de hashes
        guint n_hashes;
        fread(&n_hashes, sizeof(guint), 1, f);
        meta->block_hashes = g_array_new(FALSE, FALSE, sizeof(char*));
        for (guint j = 0; j < n_hashes; j++) {
            char* hash = g_malloc(SHA256_HEX_LEN);
            fread(hash, 1, SHA256_HEX_LEN, f);
            g_array_append_val(meta->block_hashes, hash);
        }

        g_hash_table_insert(context->metaindex, entry_path, meta);
    }
    fclose(f);

    //2. Carregar chunk_index
    snprintf(path, PATH_MAX, "%s/.meta/chunk_index.bin", context->root_dir);
    f = fopen(path, "rb");
    if (!f) return 0;

    fread(&n_entries, sizeof(guint), 1, f);
    for (guint i = 0; i < n_entries; i++) {
        ChunkInfo* info = g_new0(ChunkInfo, 1);
        fread(info, sizeof(ChunkInfo), 1, f);
        g_hash_table_insert(context->chunk_index, g_strdup(info->hash), info);
    }
    fclose(f);

    //3. Carregar free_list
    snprintf(path, PATH_MAX, "%s/.meta/free_list.bin", context->root_dir);
    f = fopen(path, "rb");
    if (!f) return 0;

    guint n_slots;
    fread(&n_slots, sizeof(guint), 1, f);
    for (guint i = 0; i < n_slots; i++) {
        FreeSlot slot;
        fread(&slot, sizeof(FreeSlot), 1, f);
        g_array_append_val(context->free_list, slot);
    }
    fclose(f);

    //4. Carregar contadores e estado
    snprintf(path, PATH_MAX, "%s/.meta/stats.bin", context->root_dir);
    f = fopen(path, "rb");
    if (!f) return 0;

    fread(&context->total_logical_size, sizeof(uint64_t), 1, f);
    fread(&context->total_physical_size, sizeof(uint64_t), 1, f);
    fread(&context->active_container_id, sizeof(uint32_t), 1, f);
    fclose(f);

    return 0;
}
