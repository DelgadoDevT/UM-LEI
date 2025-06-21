/** 
 * @file metadata.h
 * @brief Defines structures and functions for handling media metadata.
 *
 * This module provides functionality to create, manipulate, and access metadata entries
 * for media files, including validation and memory management operations.
 *
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef METADATA_H
 #define METADATA_H
 
 /** 
  * @def MAX_TITLE_LENGTH
  * @brief Maximum length for media title (including null terminator)
  */
 #define MAX_TITLE_LENGTH 201
 
 /** 
  * @def MAX_AUTHOR_LENGTH
  * @brief Maximum length for author name (including null terminator)
  */
 #define MAX_AUTHOR_LENGTH 201
 
 /** 
  * @def MAX_PATH_LENGTH
  * @brief Maximum length for file system path (including null terminator)
  */
 #define MAX_PATH_LENGTH 65
 
 /** 
  * @def MAX_YEAR_LENGTH
  * @brief Maximum length for publication year (including null terminator)
  */
 #define MAX_YEAR_LENGTH 5
 
 #include <stdint.h>
 
 /**
  * @struct metadata
  * @brief Represents metadata for a media file
  */
 typedef struct metadata {
     char title[MAX_TITLE_LENGTH];      /**< Title of the media */
     char author[MAX_AUTHOR_LENGTH];    /**< Author/creator of the media */
     char year[MAX_YEAR_LENGTH];        /**< Publication/release year */
     char path[MAX_PATH_LENGTH];        /**< File system path to media */
     unsigned int identifier;           /**< Unique numeric identifier */
     uint8_t valid;                     /**< Validation flag (1 = valid, 0 = invalid) */
 } metadata;
 
 /**
  * @brief Creates a new metadata entry with dynamic memory allocation
  * 
  * @param title Media title (max 200 chars)
  * @param author Media author (max 200 chars)
  * @param year Release year (4-digit string)
  * @param path File system path (max 64 chars)
  * @param identifier Unique numeric ID
  * @param valid Initial validation flag
  * @return Pointer to allocated metadata, NULL on failure
  */
 metadata* createMetadata(const char *title, const char *author, const char *year, 
                         const char *path, unsigned int identifier, uint8_t valid);
 
 /**
  * @brief Deallocates memory for a metadata entry
  * 
  * @param meta Metadata object to free
  */
 void freeMetadata(metadata *meta);
 
 /* Getter functions */
 
 /**
  * @brief Retrieves title from metadata
  * @param meta Metadata object to query
  * @return Pointer to title string
  */
 const char* getTitle(const metadata *meta);
 
 /**
  * @brief Retrieves author from metadata
  * @param meta Metadata object to query
  * @return Pointer to author string
  */
 const char* getAuthor(const metadata *meta);
 
 /**
  * @brief Retrieves publication year from metadata
  * @param meta Metadata object to query
  * @return Pointer to year string
  */
 const char* getYear(const metadata *meta);
 
 /**
  * @brief Retrieves file path from metadata
  * @param meta Metadata object to query
  * @return Pointer to path string
  */
 const char* getPath(const metadata *meta);
 
 /**
  * @brief Retrieves numeric identifier
  * @param meta Metadata object to query
  * @return Unique identifier value
  */
 int getIdentifier(const metadata *meta);
 
 /**
  * @brief Retrieves validation status
  * @param meta Metadata object to query
  * @return Current valid flag (1 = valid, 0 = invalid)
  */
 uint8_t getValid(const metadata *meta);
 
 /* Setter functions */
 
 /**
  * @brief Updates media title
  * @param meta Metadata object to modify
  * @param title New title (max 200 chars)
  */
 void setTitle(metadata *meta, const char *title);
 
 /**
  * @brief Updates media author
  * @param meta Metadata object to modify
  * @param author New author (max 200 chars)
  */
 void setAuthor(metadata *meta, const char *author);
 
 /**
  * @brief Updates publication year
  * @param meta Metadata object to modify
  * @param year New year (4-digit string)
  */
 void setYear(metadata *meta, const char *year);
 
 /**
  * @brief Updates file system path
  * @param meta Metadata object to modify
  * @param path New path (max 64 chars)
  */
 void setPath(metadata *meta, const char *path);
 
 /**
  * @brief Updates numeric identifier
  * @param meta Metadata object to modify
  * @param identifier New unique ID
  */
 void setIdentifier(metadata *meta, unsigned int identifier);
 
 /**
  * @brief Updates validation status
  * @param meta Metadata object to modify
  * @param valid New validation flag (1 = valid, 0 = invalid)
  */
 void setValid(metadata *meta, uint8_t valid);
 
 #endif // METADATA_H