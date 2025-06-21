/**
 * @file dserver.c
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <unistd.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "operation.h"
#include "operation_handler.h"
#include "dserver.h"
#include "freeQueue.h"
#include "metadata.h"
#include "cache.h"
#include "utils.h" 

void processAndRespond(operation op, const char *document_folder, metadata *m) {
    operation newOp = handleOperation(op, document_folder, m);
    if (getOperationPid(&newOp) == 0) {
        writeToTerminal("[DEBUG]: Error: operation failed.\n");
        return;
    }

    char clientChannel[256];
    sprintf(clientChannel, "%s%d", CLIENT_BASE_PATH, getOperationPid(&newOp));
    
    int fifo_c = open(clientChannel, O_WRONLY);
    if (fifo_c == -1) {
        writeToTerminal("[DEBUG]: Error: Client response channel cannot be opened for write\n");
        return;
    }

    // Sending operation result back to the client
    if (write(fifo_c, &newOp, sizeof(newOp)) < 0) 
        writeToTerminal("[DEBUG]: Error: Server cannot send response to client\n");

    close(fifo_c);
}

int serverRun(const char *document_folder, unsigned int cache_size) {
    writeToTerminal("Server is initializing.\n");

    // Initialize cache and free queue
    initializeCache(cache_size);
    initializeFreeQueue();

    // run garbage collector
    if (garbageCollector() == 1) {
        writeToTerminal("[DEBUG]: Error: Garbage Collection Operation failed\n");
        return 1;
    }
 
    // Create the FIFO for communication with clients
    if (mkfifo(FIFO_PATH, 0666) == -1) {
        writeToTerminal("[DEBUG]: Error: Mkfifo creation failed.\n");
        return 1;
    }  

    int fifo_fd = open(FIFO_PATH, O_RDWR);
    if (fifo_fd == -1) {
        writeToTerminal("[DEBUG]: Error: Server communication channel cannot be opened for read and write.\n");
        return 1;
    }

    int exitflag = 0;
    //Loop until an operation with type CLOSE is received
    while (1) {
        operation op;
        while (read(fifo_fd, &op, sizeof(op)) > 0) {
            char operationType = getOperationType(&op);
            if (operationType == 'f') {
                processAndRespond(op, document_folder, NULL);
                exitflag = 1; // Set exit flag when "f" operation is received
                break;
            }
                
            if (operationType == 'c' || operationType == 's' || operationType == 'l') {
                metadata *m = NULL;
                if (operationType == 'c' || operationType == 'l')
                    m = searchCache(getIdentifierFromVector(op));
                pid_t pid = fork();
                if (pid == -1) {
                    writeToTerminal("[DEBUG]: Error: fork failed");
                    close(fifo_fd);
                    return 1;
                } else if (pid == 0) {
                    processAndRespond(op, document_folder, m);
                    _exit(1);
                }
           
            } else {
                processAndRespond(op, document_folder, NULL);
            }
        }  

        if (exitflag) 
            break; // Exit loop if "f" operation is processed
    }

    close(fifo_fd);
    unlink(FIFO_PATH);
    saveFreeQueue(); // Save the free queue data
    freeCache();     // Free cached data
    writeToTerminal("Server is closing\n");

    return 0;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        writeToTerminal("Usage: ./dcserver document_folder cache_size.\n");
        exit(EXIT_FAILURE);
    } 

    // Check if the given document folder exists
    DIR *dir = opendir(argv[1]);
    if (dir == NULL) {
        writeToTerminal("Error: The chosen directory is not valid.\n");
        exit(EXIT_FAILURE);
    }
    closedir(dir);

    // Ensure the folder path ends with '/'
    char document_folder[PATH_MAX];
    size_t len = strlen(argv[1]);
    if (argv[1][len - 1] == '/') {
        strncpy(document_folder, argv[1], PATH_MAX - 1);
        document_folder[PATH_MAX - 1] = '\0';
    } else {
        snprintf(document_folder, PATH_MAX, "%s/", argv[1]);
    }

    char *endptr;
    unsigned long cache_size_long = strtoul(argv[2], &endptr, 10); // endptr must point to \0 in the end; 10 is the base

    if (*endptr != '\0') {
        writeToTerminal("Error: cache_size is not a valid integer.\n");
        exit(EXIT_FAILURE);
    }

    if (cache_size_long > UINT_MAX) {
        writeToTerminal("Error: cache_size cannot surpass integer bounds.\n");
        exit(EXIT_FAILURE);
    }

    unsigned int cache_size = (unsigned int) cache_size_long;

    // Run the server
    if (serverRun(document_folder, cache_size) == 1) {
        writeToTerminal("Error: Server cannot open successfully.\n");
        exit(EXIT_FAILURE);
    }

    exit(EXIT_SUCCESS);
}