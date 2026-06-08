#ifndef PERSISTENCE_H
#define PERSISTENCE_H

#include "dedup_fs/metadata.h"

/**
 * @brief Saves all in-memory metadata to disk.
 *
 * This function serializes the metadata indexes and free list and writes them
 * to persistent storage in the root_dir.
 *
 * @param context The global filesystem context.
 * @return 0 on success, a negative error code on failure.
 */
int save_metadata(Context* context);

/**
 * @brief Loads all metadata from disk into memory.
 *
 * This function reads the serialized metadata from the root_dir,
 * deserializes it, and populates the context's indexes and free list.
 *
 * @param context The global filesystem context.
 * @return 0 on success, a negative error code on failure.
 */
int load_metadata(Context* context);

#endif // PERSISTENCE_H
