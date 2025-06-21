#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "metadata.h"
#include "utils.h"

// Allocates and initializes a metadata structure.
// Returns NULL on allocation failure.
metadata* createMetadata(const char *title, const char *author, const char *year, const char *path, unsigned int identifier, uint8_t valid) {
    metadata *meta = malloc(sizeof(metadata));
    if (meta == NULL) {
        // Debug message for critical failure 
        writeToTerminal("[DEBUG]: Failed to allocate memory for metadata\n");
        return NULL;
    }

    // Using strncpy to prevent buffer overflow, followed by manual null termination.
    // Note: If source string exceeds limit, it will be silently truncated.
    strncpy(meta->title, title, MAX_TITLE_LENGTH - 1);
    meta->title[MAX_TITLE_LENGTH - 1] = '\0';  // Ensures null termination even after truncation

    strncpy(meta->author, author, MAX_AUTHOR_LENGTH - 1);
    meta->author[MAX_AUTHOR_LENGTH - 1] = '\0';

    strncpy(meta->year, year, MAX_YEAR_LENGTH - 1);
    meta->year[MAX_YEAR_LENGTH - 1] = '\0';

    // Path is stored as fixed-size array - beware of long paths!
    strncpy(meta->path, path, MAX_PATH_LENGTH - 1);
    meta->path[MAX_PATH_LENGTH - 1] = '\0';

    // Unique identifier - assumed to be managed externally
    meta->identifier = identifier;
    
    // 'valid' is a 1-byte flag (0/1). Could be extended for bitwise flags.
    meta->valid = valid;

    return meta;
}

// Releases memory for the metadata structure.
// Note: Fields are static arrays - no individual freeing needed.
void freeMetadata(metadata *meta) {
    if (meta != NULL) {
        free(meta);  // Frees entire block (includes all fields)
    }
}

// Getters: Return pointers to internal data. Caution: mutable through pointers!
// 'const' qualifier prevents accidental modification via returned pointer.
const char* getTitle(const metadata *meta) {
    return meta->title;
}

const char* getAuthor(const metadata *meta) {
    return meta->author;
}

const char* getYear(const metadata *meta) {
    return meta->year;
}

const char* getPath(const metadata *meta) {
    return meta->path;
}

// Returns numeric identifier for external reference
int getIdentifier(const metadata *meta) {
    return meta->identifier;
}

// 'valid' as uint8_t allows future expansion into multi-flag system
uint8_t getValid(const metadata *meta) {
    return meta->valid;
}

// Setters: Update fields with size validation.
// Overwrite previous values completely.
void setTitle(metadata *meta, const char *title) {
    strncpy(meta->title, title, MAX_TITLE_LENGTH - 1);
    meta->title[MAX_TITLE_LENGTH - 1] = '\0';  // Defense against overflow
}

void setAuthor(metadata *meta, const char *author) {
    strncpy(meta->author, author, MAX_AUTHOR_LENGTH - 1);
    meta->author[MAX_AUTHOR_LENGTH - 1] = '\0';
}

// 'year' stored as string for format flexibility (e.g., "2023", "c. 1500")
void setYear(metadata *meta, const char *year) {
    strncpy(meta->year, year, MAX_YEAR_LENGTH - 1);
    meta->year[MAX_YEAR_LENGTH - 1] = '\0';
}

// Path updates require filesystem synchronization considerations
void setPath(metadata *meta, const char *path) {
    strncpy(meta->path, path, MAX_PATH_LENGTH - 1);
    meta->path[MAX_PATH_LENGTH - 1] = '\0';
}

// Identifier uniqueness must be enforced externally
void setIdentifier(metadata *meta, unsigned int identifier) {
    meta->identifier = identifier;
}

// 'valid' could represent states beyond boolean (e.g., bitmask flags)
void setValid(metadata *meta, uint8_t valid) {
    meta->valid = valid;
}