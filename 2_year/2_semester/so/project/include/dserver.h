/**
 * @file dserver.h
 * @brief Server communication and operation processing.
 *
 * This module provides the server-side functionality for listening to client requests, 
 * processing operations, and managing server resources like FIFOs and cache.
 * 
 * It includes functions for initializing the server, processing operations, and 
 * responding to clients through the server and client-specific FIFOs.
 * 
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef DSERVER_H
 #define DSERVER_H

 #include "metadata.h"
 
 /** 
  * @def FIFO_PATH
  * @brief Path for the server FIFO (named pipe) for communication.
  */
 #define FIFO_PATH "tmp/serverChannel"
 
 /** 
  * @def CLIENT_BASE_PATH
  * @brief Base path for the client communication channel.
  */
 #define CLIENT_BASE_PATH "tmp/clientChannel"
 
 /**
  * @brief Processes an operation and sends a response to the client.
  * 
  * This function handles the operation and sends the processed result back to
  * the client through the corresponding FIFO channel.
  * 
  * @param op The operation to process.
  * @param document_folder The folder where the documents are stored.
  * @param metadata Metadata retrieved from cache used for some queries (null if not used)
  */
 void processAndRespond(operation op, const char *document_folder, metadata *m);
 
 /**
  * @brief Listens for incoming operations from clients.
  * 
  * This function listens for operations on the given FIFO file descriptor.
  * It returns the operation that has been received.
  * 
  * @param fifo_fd The file descriptor of the FIFO channel to listen on.
  * @return A pointer to the operation received from the FIFO.
  */
 operation *serverListen(int fifo_fd);
 
 /**
  * @brief Runs the server, handling operations and managing cache.
  * 
  * Initializes cache and other components, then enters a loop to handle
  * incoming operations. The loop continues until a close operation (-f) is received.
  * 
  * @param document_folder The directory where documents are located.
  * @param cache_size The size of the cache to initialize.
  * @return 0 on success, 1 on failure.
  */
 int serverRun(const char *document_folder, unsigned int cache_size);
 
 #endif // DSERVER_H 