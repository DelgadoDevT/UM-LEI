#ifndef METADATA_H
#define METADATA_H

#include <glib.h>
#include <pthread.h>
#include <sys/types.h>
#include <stdint.h>

/**
 * @brief The global context for the entire file system instance.
 * 
 * This structure holds all the shared, in-memory state required for the
 * deduplication file system to operate, including indexes, caching, and
 * global statistics.
 */
typedef struct context {
    char* root_dir;               /**< Path to the root directory for storage. */
    GHashTable* metaindex;        /**< Hash table mapping file paths to FileMeta structs. */
    GHashTable* chunk_index;      /**< Hash table mapping chunk hashes to ChunkInfo structs. */
    GArray* free_list;            /**< Array of FreeSlot structs for reusable space. */
    uint32_t active_container_id; /**< ID of the container currently being written to. */
    int active_container_fd;      /**< File descriptor of the active container. */
    uint64_t total_logical_size;  /**< Total logical size of all files combined. */
    uint64_t total_physical_size; /**< Total physical size of all unique chunks combined. */
    char* cache_path;             /**< Path of the file currently loaded in the read cache. */
    char* cache_data;             /**< Pointer to the cached file data. */
    size_t cache_size;            /**< Size of the cached file data. */
    pthread_mutex_t mutex;        /**< Global mutex to ensure thread-safe operations. */
} Context;

/**
 * @brief Defines the type of an entry in the file system namespace.
 */
typedef enum {
    TYPE_FILE,      /**< Represents a regular file. */
    TYPE_DIRECTORY  /**< Represents a directory. */
} EntryType;

/**
 * @brief Metadata for a file or directory in the file system.
 * 
 * Acts as the "recipe" for a file, storing its attributes and the sequence
 * of chunks that make up its content.
 */
typedef struct {
    EntryType type;         /**< The type of the entry (file or directory). */
    GArray* block_hashes;   /**< Array of SHA-256 hash strings representing the file's blocks. */
    size_t logical_size;    /**< The logical size of the file in bytes. */
    mode_t mode;            /**< The permissions and mode of the file/directory. */
} FileMeta;

/**
 * @brief Describes a unique 4KB data chunk stored on disk.
 */
typedef struct {
    char hash[65];          /**< The SHA-256 hash of the chunk (64 hex chars + null). */
    guint32 container_id;   /**< The ID of the container file where this chunk is stored. */
    guint64 offset;         /**< The byte offset within the container file. */
    guint32 ref_count;      /**< The number of times this chunk is referenced by files. */
} ChunkInfo;

/**
 * @brief Describes a 4KB slot in a container file that is available for reuse.
 */
typedef struct {
    guint32 container_id;   /**< The ID of the container file with the free slot. */
    guint64 offset;         /**< The byte offset of the free slot within the container. */
} FreeSlot;

#endif // METADATA_H
