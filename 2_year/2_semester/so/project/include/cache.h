/**
 * @file cache.h
 * @brief Provides caching functionality for metadata entries using a clock replacement policy.
 *
 * This module handles metadata caching operations such as initialization, searching,
 * replacement, and memory cleanup. It interacts with the metadata binary file and 
 * uses GLib hash tables for fast access.
 *
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef CACHE_H
 #define CACHE_H
 
 #include "metadata.h"

/** 
 * @def METADATA_PATH
 * @brief Path to the binary metadata file.
 */
 #define METADATA_PATH "bin/metadata.bin"
 #define CACHE_STATS_PATH "bin/cacheStats.bin"
/** 
 * @def PERCENTAGE
 * @brief Percentage of cache entries to remove during the replacement operation.
 */
 #define PERCENTAGE (0.25f)

 /**
  * @struct cache_entry
  * @brief Represents a single entry in the metadata cache.
  */
 typedef struct cache_entry {
     metadata *metadata;  /**< Pointer to metadata structure */
     uint8_t valid;       /**< Flag indicating if the entry is recently accessed (used in Clock algorithm) */
 } *cache_entry;
 
 typedef struct cache_stats{
     unsigned int cache_hits;
     unsigned int cache_total_tries;
 } *cache_stats;
 /**
  * @brief Initializes the metadata cache with a specified number of entries.
  * 
  * @param N Number of entries the cache should support.
  */
 void initializeCache(unsigned int N);
 
 /**
  * @brief Calculates the maximum number of attempts to replace metadata in the cache.
  */
 void calculateMaxAttempts();
 
 /**
  * @brief Searches for metadata in the cache by its identifier.
  * 
  * @param identifier Unique identifier of the metadata.
  * @return Pointer to metadata if found, otherwise NULL.
  */
 metadata* searchCache(unsigned int identifier);
 
 /**
  * @brief Applies the Clock Algorithm to remove entries from the cache.
  */
 void replacementPolicyClockAlgorithm();
 
 /**
  * @brief Repopulates the cache from the metadata file after removal.
  */
 void rePopulateCache();
 
 /**
  * @brief Creates a new cache entry with the given metadata and valid flag.
  * 
  * @param m Pointer to a metadata structure.
  * @param valid Initial valid flag state (1 = recently used, 0 = not used).
  * @return A new cache_entry struct pointer.
  */
 cache_entry createCacheEntry(metadata *m, uint8_t valid);
 
 /**
  * @brief Removes a metadata entry from the cache by its identifier.
  * 
  * @param identifier Unique identifier of the metadata to remove.
  */
 void removeFromCache(unsigned int identifier);
 
 /**
  * @brief Frees all memory used by the cache and its entries.
  */
 void freeCache();
 
 /**
  * @brief Frees a single cache entry.
  * 
  * @param data Pointer to the cache entry to free.
  */
 void freeCacheEntry(void *data);
 void saveCacheStats(cache_stats stats);
 cache_stats loadCacheStats();
 char* getCacheHitRate();
 
 #endif // CACHE_H 