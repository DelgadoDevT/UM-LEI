#ifndef CHUNK_OPERATIONS_H
#define CHUNK_OPERATIONS_H

#include "dedup_fs/metadata.h"
#include <stddef.h>

// --- Constants ---

#define BLOCK_SIZE 4096  // 4KB fixed block size
#define CONTAINER_SIZE (1024 * 1024 * 1024)  // 1GB container file size
#define BLOCKS_PER_CONTAINER (CONTAINER_SIZE / BLOCK_SIZE)  // Number of 4KB blocks per container


// --- Chunk Storage Functions ---

/**
 * @brief Writes a unique data chunk to physical storage.
 *
 * This function writes a 4KB chunk to a data container file. It handles
 * container management (creating new containers when needed) and updates
 * the active container state in the context.
 *
 * @param context The global filesystem context.
 * @param chunk_data The 4KB data buffer to write.
 * @param container_id Output parameter: the container ID where the chunk was written.
 * @param offset Output parameter: the byte offset within the container.
 * @return 0 on success, a negative error code on failure.
 */
int chunk_store_write(Context* context, const char* chunk_data, 
                      uint32_t* container_id, uint64_t* offset);

/**
 * @brief Reads a data chunk from physical storage.
 *
 * This function reads a 4KB chunk from a data container file based on
 * the provided container ID and offset.
 *
 * @param context The global filesystem context.
 * @param container_id The container ID to read from.
 * @param offset The byte offset within the container.
 * @param chunk_data Output buffer to store the read chunk (must be at least BLOCK_SIZE bytes).
 * @return 0 on success, a negative error code on failure.
 */
int chunk_store_read(Context* context, uint32_t container_id, 
                     uint64_t offset, char* chunk_data);

/**
 * @brief Marks a chunk location as free for reuse.
 *
 * This function adds a freed chunk location to the free_list in the context.
 * The physical data is not erased, but the slot can be reused for new chunks.
 *
 * @param context The global filesystem context.
 * @param container_id The container ID of the freed chunk.
 * @param offset The byte offset within the container.
 * @return 0 on success, a negative error code on failure.
 */
int chunk_store_delete(Context* context, uint32_t container_id, uint64_t offset);


// --- Engine Functions ---

/**
 * @brief Processes a write operation with deduplication logic.
 *
 * This is the main "brain" for write operations. It:
 * 1. Chunks the input data into 4KB blocks
 * 2. Calculates SHA-256 hash for each block
 * 3. Checks if the block already exists (deduplication)
 * 4. For existing blocks: increments reference count
 * 5. For new blocks: writes to storage via chunk_store_write()
 * 6. Updates the file's block_hashes array and metadata
 * 7. Updates global statistics
 *
 * @param context The global filesystem context.
 * @param path The file path being written to.
 * @param buf The data buffer to write.
 * @param size The size of the data to write.
 * @param offset The offset within the file to start writing.
 * @return Number of bytes written on success, a negative error code on failure.
 */
int engine_process_write(Context* context, const char* path, const char* buf,
                        size_t size, off_t offset);

/**
 * @brief Processes a delete/unlink operation with garbage collection.
 *
 * This function handles the deduplication-aware deletion of files:
 * 1. Retrieves the file's block_hashes array
 * 2. For each chunk hash:
 *    a. Decrements the reference count in chunk_index
 *    b. If ref_count reaches 0, calls chunk_store_delete() to free the slot
 * 3. Updates global statistics (logical and physical size)
 * 4. Removes the file entry from metaindex
 *
 * @param context The global filesystem context.
 * @param path The file path being deleted.
 * @return 0 on success, a negative error code on failure.
 */
int engine_process_delete(Context* context, const char* path);

/**
 * @brief Processes a read operation with chunk reconstruction.
 *
 * This function reconstructs file data from its constituent chunks:
 * 1. Looks up the file in metaindex to get block_hashes
 * 2. Calculates which chunks are needed based on offset and size
 * 3. For each needed chunk:
 *    a. Looks up ChunkInfo in chunk_index
 *    b. Calls chunk_store_read() to retrieve the chunk data
 * 4. Assembles the chunks into the output buffer
 *
 * @param context The global filesystem context.
 * @param path The file path being read from.
 * @param buf Output buffer to store the read data.
 * @param size The number of bytes to read.
 * @param offset The offset within the file to start reading.
 * @return Number of bytes read on success, a negative error code on failure.
 */
int engine_process_read(Context* context, const char* path, char* buf,
                       size_t size, off_t offset);

/**
 * @brief Processes a truncate operation with chunk cleanup.
 *
 * This function handles truncating a file to a new size:
 * 1. If size == 0, behaves similar to delete but keeps the file entry
 * 2. If size < current size, removes chunks beyond the new size:
 *    a. Decrements reference counts for removed chunks
 *    b. Calls chunk_store_delete() for chunks with ref_count == 0
 * 3. If size > current size, may need to add zero-filled chunks
 * 4. Updates file metadata and global statistics
 *
 * @param context The global filesystem context.
 * @param path The file path being truncated.
 * @param new_size The new file size in bytes.
 * @return 0 on success, a negative error code on failure.
 */
int engine_process_truncate(Context* context, const char* path, off_t new_size);


// --- Helper Functions ---

/**
 * @brief Gets the path for a container file.
 *
 * Constructs the full path for a data container file given its ID.
 * Format: {root_dir}/containers/container_{id}.dat
 *
 * @param context The global filesystem context.
 * @param container_id The container ID.
 * @param path_buf Output buffer for the path (should be at least PATH_MAX bytes).
 * @return 0 on success, a negative error code on failure.
 */
int get_container_path(Context* context, uint32_t container_id, char* path_buf);

/**
 * @brief Opens or creates a container file.
 *
 * Opens an existing container file or creates a new one if it doesn't exist.
 * The file descriptor is returned for use in read/write operations.
 *
 * @param context The global filesystem context.
 * @param container_id The container ID to open.
 * @param flags Open flags (O_RDONLY, O_WRONLY, O_RDWR, etc.).
 * @return File descriptor on success, a negative error code on failure.
 */
int open_container(Context* context, uint32_t container_id, int flags);

/**
 * @brief Generates the statistics string for /stats.txt.
 *
 * Creates a formatted string with current filesystem statistics including
 * total logical size, total physical size, and deduplication ratio.
 *
 * @param context The global filesystem context.
 * @param buf Output buffer for the stats string.
 * @param buf_size Size of the output buffer.
 * @return Number of bytes written to buf, or negative error code.
 */
int generate_stats_string(Context* context, char* buf, size_t buf_size);

#endif // CHUNK_OPERATIONS_H
