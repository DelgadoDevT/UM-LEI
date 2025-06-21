/**
 * @file operation_handler.h
 * @brief Provides functions for handling operations on metadata.
 * 
 * This file contains function declarations for indexing, consulting,
 * deleting metadata entries, and other operations related to document 
 * management.
 * 
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef OPERATION_HANDLER_H
 #define OPERATION_HANDLER_H
 
 #include "metadata.h"

 /** 
  * @def METADATA_PATH
  * @brief Path to the metadata file.
  */
 #define METADATA_PATH "bin/metadata.bin"
 
 /** 
  * @def IDENTIFIER_PATH
  * @brief Path to the identifier file.
  */
 #define IDENTIFIER_PATH "bin/identifier.bin"
 
 /** 
  * @def TEMPORARY_METADATA_PATH
  * @brief Path to the temporary metadata file.
  */
 #define TEMPORARY_METADATA_PATH "tmp/metadataT.bin"
 
 /** 
  * @def GARBAGE_LIMIT
  * @brief Maximum number of deletes before performing garbage collection.
  */
 #define GARBAGE_LIMIT 16
 
 /** 
  * @def CHUNK_SIZE
  * @brief The chunk size used when reading or writing metadata.
  */
 #define CHUNK_SIZE 64
 
 /**
  * @brief Handles the operation based on the operation type.
  * 
  * This function determines the type of operation (e.g., index, consult,
  * delete, etc.) and calls the corresponding handler function.
  * 
  * @param op The operation struct for response.
  * @param document_folder The folder where documents are stored.
  * @param metadata Metadata retrieved from cache used for some queries
  * @return The result of the operation.
  */
 operation handleOperation(operation op, const char *document_folder, metadata *m);
 
 /**
  * @brief Indexes new metadata into the system.
  * 
  * This function indexes new metadata by reading and updating the identifier
  * file and writing the metadata to the metadata file.
  * 
  * @param vector A vector of arguments for the metadata.
  * @param op The operation struct for response.
  * @return 0 on success, non-zero on failure.
  */
 int indexNewMetaData(char **vector, operation *op);
 
 /**
  * @brief Consults metadata based on an identifier.
  * 
  * This function retrieves and returns metadata information based on the 
  * provided identifier.
  * 
  * @param vector A vector of arguments (including the identifier).
  * @param op The operation struct for response.
  * @param metadata Metadata retrieved from cache
  * @return 0 on success, non-zero on failure.
  */
 int consultMetaData(char **vector, operation *op, metadata *m);
 
 /**
  * @brief Deletes metadata based on an identifier.
  * 
  * This function marks a metadata entry as invalid and moves it to the 
  * garbage collection queue (freeQueue).
  * 
  * @param vector A vector of arguments (including the identifier).
  * @param op The operation struct for response.
  * @return 0 on success, non-zero on failure.
  */
 int deleteMetaData(char **vector, operation *op);
 
 /**
  * @brief Counts the number of lines in a document based on a keyword.
  * 
  * This function counts the number of occurrences of a keyword in a 
  * document using `grep`.
  * 
  * @param vector A vector of arguments (including the identifier and keyword).
  * @param op The operation struct for response.
  * @param directoryPath The path to the document directory.
  * @param metadata Metadata retrieved from cache
  * @return 0 on success, non-zero on failure.
  */
 int documentLines(char **vector, operation *op, const char* directoryPath, metadata *m);
 
 /**
  * @brief Lists documents containing a specific keyword.
  * 
  * This function lists all documents that contain a specified keyword.
  * It may spawn multiple processes for parallel search.
  * 
  * @param vector A vector of arguments (including the keyword and max procs).
  * @param op The operation struct for response.
  * @param document_folder The folder where documents are stored.
  * @return 0 on success, non-zero on failure.
  */
 int listDocKeyword(char **vector, operation *op, const char *document_folder);
 
 /**
  * @brief Sends a shutdown message when the server is closing.
  * 
  * This function generates a message indicating the server is shutting down.
  * 
  * @param op The operation struct for response.
  * @return 0 on success, non-zero on failure.
  */
 int serverCloseMessage(operation *op);
 
 /**
  * @brief Performs garbage collection to clean up invalid metadata.
  * 
  * This function scans the metadata file, removes invalid entries, and 
  * rewrites the file with only valid metadata.
  * 
  * @return 0 on success, non-zero on failure.
  */
 int garbageCollector();
 
 #endif // OPERATION_HANDLER_H
 