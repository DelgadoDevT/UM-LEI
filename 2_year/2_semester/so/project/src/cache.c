/**
 * @file cache.c
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <glib.h>
#include <math.h>
#include "cache.h"
#include "utils.h"

static off_t readPosition; // Tracks the last read position in the metadata file
static GHashTable *cache;  // Hash table to store cache entries
static unsigned int maxAttempts; // Maximum attempts for cache replacement
static unsigned int cacheSize; // Size of the cache

cache_stats loadCacheStats(){
    int file = open(CACHE_STATS_PATH, O_RDONLY);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Cache Stats file could not be opened.\n");
        return NULL;
    }
    cache_stats stats = malloc(sizeof(struct cache_stats));
    ssize_t bytes_read = read(file, stats, sizeof(struct cache_stats));
    if (bytes_read == -1) {
        writeToTerminal("[DEBUG]: Error read from cache Stats file.\n");
        close(file);
        return NULL;
    }
    close(file);
    return stats;
}

void saveCacheStats(cache_stats stats){
    int file = open(CACHE_STATS_PATH, O_CREAT | O_WRONLY | O_TRUNC, 0600);  
    if (file == -1) {
        writeToTerminal("[DEBUG]: Erro ao abrir o ficheiro para salvar os contadores.\n");
        return;
        }
    ssize_t bytes_written = write(file, stats, sizeof(cache_stats));
    if (bytes_written == -1) {
        writeToTerminal("[DEBUG]: Error writing in cache stats file.\n");
        close(file);
        return;
    }
    
    close(file);
}

void fillCache() {
    int cacheUsed = g_hash_table_size(cache);
    if (cacheUsed >= cacheSize) return; // Exit if cache is already full

    int file = open(METADATA_PATH, O_CREAT | O_RDONLY, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        return;
    }

    // Calculate how many entries we still need to load into the cache
    size_t bufferSize = cacheSize - cacheUsed;
    metadata *buffer = malloc(bufferSize * sizeof(metadata));
    if (!buffer) {
        writeToTerminal("[DEBUG]: Memory allocation failed for metadata buffer\n");
        close(file);
        return;
    }

    ssize_t readenBytes = read(file, buffer, sizeof(metadata) * (cacheSize - cacheUsed));
    if (readenBytes < 0) {
        writeToTerminal("[DEBUG]: Cannot read from metadata file\n");
        free(buffer);
        close(file);
        return;
    }

    int itemsRead = readenBytes / sizeof(metadata);
    for (int i = 0; i < itemsRead; i++) {
        metadata *m = &buffer[i];
        char *key_str = uint_to_string(getIdentifier(m));
        if (g_hash_table_lookup(cache, key_str) == NULL) {
            metadata *new = createMetadata(getTitle(m), getAuthor(m), getYear(m), getPath(m), getIdentifier(m), getValid(m));
            cache_entry entry = createCacheEntry(new, 0); 
            g_hash_table_insert(cache, key_str, entry);
        }
    }
    readPosition = itemsRead * sizeof(metadata);

    free(buffer);
    close(file);
}

void initializeCache(unsigned int N) {
    if (cache != NULL) return;
    cache = g_hash_table_new_full(g_str_hash, g_str_equal, free, freeCacheEntry);
    cacheSize = N;
    calculateMaxAttempts(); // Calculate the maximum replacement attempts based on the cache size
    char buffer[256];
    sprintf(buffer, "[DEBUG]: Cache Starting with %d slots, and %u max attempts for replacement\n", cacheSize, maxAttempts);
    writeToTerminal(buffer); 
    cache_stats cacheStats = malloc(sizeof(struct cache_stats));
    cacheStats->cache_hits=0;
    cacheStats->cache_total_tries=0;
    saveCacheStats(cacheStats);
    fillCache(); // Populate cache with initial entries
}

void calculateMaxAttempts() {
    int file = open(METADATA_PATH, O_CREAT | O_RDONLY, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        return;
    }
    off_t file_size = lseek(file, 0, SEEK_END);
    if (file_size == -1) {
        writeToTerminal("[DEBUG]: Error: lseek in calculateMaxAttempts Failed\n");
        close(file);
        return;
    }
    close(file);

    if (file_size / sizeof(metadata) < cacheSize) {
        maxAttempts = 0;
        return;
    }
    maxAttempts = ceil((file_size / sizeof(metadata) - cacheSize) / (ceil(cacheSize * PERCENTAGE)));
}

metadata *searchCache(unsigned int identifier) {
    cache_stats stats = loadCacheStats();
    unsigned int cache_hits = 0;
    unsigned int cache_total_tries = 0;
    cache_entry entry = NULL;
    unsigned int attempts = 0;
    // If cache size is smaller than required, fill the cache
    if (g_hash_table_size(cache) < cacheSize)
        fillCache();

    // Search metadata in cache by identifier
    char *key_str = uint_to_string(identifier);
    entry = (cache_entry)g_hash_table_lookup(cache, key_str);
    cache_total_tries++;
    if (entry != NULL) {
        entry->valid = 1;
        free(key_str);
        cache_hits++;
        stats->cache_hits += cache_hits;
        stats->cache_total_tries += cache_total_tries;
        saveCacheStats(stats);
        return entry->metadata;
    }

    // If not found, attempt to replace entries based on the clock algorithm
    calculateMaxAttempts();
    while (attempts < maxAttempts) {
        replacementPolicyClockAlgorithm();
        entry = (cache_entry)g_hash_table_lookup(cache, key_str);
        cache_total_tries++;
        if (entry != NULL) {
            entry->valid = 1;
            cache_hits++;
            stats->cache_hits += cache_hits;
            stats->cache_total_tries += cache_total_tries;
            saveCacheStats(stats);
            free(key_str);
            return entry->metadata;
        }
        attempts++;
    }

    free(key_str);
    return NULL;
} 

/* metadata *searchCache(unsigned int identifier) {
    int file = open(METADATA_PATH, O_CREAT | O_RDONLY, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        return NULL;
    }

    metadata temp;
    metadata *result = NULL;

    while (read(file, &temp, sizeof(temp)) > 0)
        if (getIdentifier(&temp) == identifier) {
            result = malloc(sizeof(metadata));
            if (result != NULL)
                *result = temp;
            break;
        }

    close(file);
    return result;
}  */

// Implements substitution policy by Clock Algorithm
void replacementPolicyClockAlgorithm() {
    int num_to_remove = (int) ceil(cacheSize * PERCENTAGE); // Calculate how many entries to remove

    GHashTableIter iter;
    gpointer key, value;

    int removed = 0;
    while (removed < num_to_remove) {
        g_hash_table_iter_init(&iter, cache);

        while (g_hash_table_iter_next(&iter, &key, &value)) {
            cache_entry entry = (cache_entry)value;
            if (entry->valid == 0) {  
                g_hash_table_iter_remove(&iter);
                removed++;
                if (removed >= num_to_remove)
                    break;
            } else {
                entry->valid = 0;
            }
        }
    }

    rePopulateCache(); // Repopulate cache after removing entries
}

void rePopulateCache() {
    int file = open(METADATA_PATH, O_CREAT | O_RDONLY, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        return;
    }
    if (lseek(file, readPosition, SEEK_SET) == -1) {
        writeToTerminal("[DEBUG]: Error: lseek in rePopulateCache Failed\n");
        close(file);
        return;
    }

    int batchSize = (int) ceil(cacheSize * PERCENTAGE);
    metadata *buffer = malloc(batchSize * sizeof(metadata));
    if (!buffer) {
        writeToTerminal("[DEBUG]: Memory allocation failed for metadata buffer\n");
        close(file);
        return;
    }
    ssize_t readenBytes = 0;
    int totalInserted = 0;

    while (totalInserted < batchSize) {
        readenBytes = read(file, &buffer[totalInserted], sizeof(metadata) * (batchSize - totalInserted));

        if (readenBytes < 0) {
            writeToTerminal("[DEBUG]: Error reading from metadata file\n");
            free(buffer);
            close(file);
            return;
        }

        if (readenBytes == 0) {  // If we reach the end of the file, restart from the beginning
            if (lseek(file, 0, SEEK_SET) == -1) {
                writeToTerminal("[DEBUG]: Error: lseek in rePopulateCache Failed\n");
                free(buffer);
                close(file);
                return;
            }
            readPosition = 0;
            continue;
        }

        int itemsRead = readenBytes / sizeof(metadata);
        totalInserted += itemsRead;

        for (int i = 0; i < itemsRead; i++) {
            metadata *m = &buffer[totalInserted - itemsRead + i];
            char *key_str = uint_to_string(getIdentifier(m));

            if (g_hash_table_lookup(cache, key_str) == NULL) {
                metadata *new = createMetadata(getTitle(m), getAuthor(m), getYear(m), getPath(m), getIdentifier(m), getValid(m));
                cache_entry entry = createCacheEntry(new, 0);
                g_hash_table_insert(cache, key_str, entry);
            }
        }

        if (totalInserted >= batchSize)
            break;
    }

    readPosition = lseek(file, 0, SEEK_CUR);
    if (readPosition == -1) {
        writeToTerminal("[DEBUG]: Error: lseek in rePopulateCache Failed\n");
        free(buffer);
        close(file);
        return;
    }
    free(buffer);
    close(file);
}

void removeFromCache(unsigned int identifier) {
    char *key_str = uint_to_string(identifier);
    g_hash_table_remove(cache, key_str); 
}

cache_entry createCacheEntry(metadata *m, uint8_t valid) {
    cache_entry entry = malloc(sizeof(*entry));
    if (entry == NULL) {
        writeToTerminal("[DEBUG]: Error: Failed to allocate memory for cache entry\n");
        return NULL; 
    }

    entry->metadata = m;
    entry->valid = valid;

    return entry; 
}

char* getCacheHitRate()
{
    cache_stats stats = loadCacheStats();
    if (stats->cache_total_tries == 0) {
        return NULL;
    }
    float hitrate = (float)stats->cache_hits/stats->cache_total_tries;
    char* buffer = malloc(sizeof(char)*60);
    sprintf(buffer, "Hit rate ---> %.2f\n cache_hits -> %d, total_tries -> %d", hitrate, stats->cache_hits, stats->cache_total_tries);
    return buffer;
}



void freeCache() {
    g_hash_table_destroy(cache); // Clean up the hash table
}

void freeCacheEntry(void *data) {
    cache_entry entry = (cache_entry) data;
    if (entry != NULL) {
        freeMetadata(entry->metadata);
        free(entry);
    }
}