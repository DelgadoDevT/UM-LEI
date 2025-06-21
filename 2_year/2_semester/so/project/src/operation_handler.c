/**
 * @file operation_handler.c
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

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>
#include "operation.h"
#include "operation_handler.h"
#include "metadata.h"
#include "utils.h"
#include "freeQueue.h"
#include "cache.h"

// Static variable to track number of delete operations performed
static unsigned int numberOfDeletes = 0;

operation handleOperation(operation op, const char *document_folder, metadata *m) {
    operation operation = createOperation(getOperationPid(&op));
    char **vector = vectorizeArguments(op);
    unsigned int argumentsCounter = countArguments(op);
    int errorCheck = 0;
    switch (getOperationType(&op)) {
        case 'a':
            errorCheck = indexNewMetaData(vector, &operation);
            break;

        case 'c':
            errorCheck = consultMetaData(vector, &operation, m);
            break;

        case 'd':
            errorCheck = deleteMetaData(vector, &operation);
            break;

        case 'l':
            errorCheck = documentLines(vector, &operation, document_folder, m);
            break;
    
        case 's':
            errorCheck = listDocKeyword(vector, &operation, document_folder);
            break;

        case 'f':
            errorCheck = serverCloseMessage(&operation);
            break;

        default:
            errorCheck = 1;
    }

    if (errorCheck) {
        writeToTerminal("[DEBUG]: Error: Some operation failed\n");
        setOperationPid(&op, 0);
    }

    for (unsigned int i = 0; i < argumentsCounter; i++) 
        free(vector[i]);
    free(vector);
    writeToTerminal("[DEBUG]: Success: Operation handled\n");
    return operation;
}

int indexNewMetaData(char **vector, operation *op) {
    unsigned int identifier;
    int file0 = open(IDENTIFIER_PATH, O_RDWR | O_CREAT, 0600);
    if (file0 == -1) {
        writeToTerminal("[DEBUG]: Error opening the identifier path\n");
        return 1;
    }

    ssize_t bytesReaden = read(file0, &identifier, sizeof(unsigned int));
    if (bytesReaden == -1) {
        writeToTerminal("[DEBUG]: Error reading the identifier path\n");
        close(file0);
        return 1;
    } else if (bytesReaden == 0) {
        identifier = 0;
    }

    if (lseek(file0, 0, SEEK_SET) == -1) {
        writeToTerminal("[DEBUG]: Error: lseek in indexNewMetaData Failed\n");
        close(file0);
        return 1;
    }

    unsigned int newIdentifier = identifier + 1;
    if (write(file0, &(newIdentifier), sizeof(unsigned int)) == -1) {
        writeToTerminal("[DEBUG]: Error adding new identifier to file\n");
        close(file0);
        return 1;
    }

    close(file0);

    // Create metadata structure and open metadata file for writing
    metadata *m = createMetadata(vector[0], vector[1], vector[2], vector[3], identifier, 1);
    int file = open(METADATA_PATH, O_WRONLY | O_CREAT , 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        return 1;
    }

    // Get the free position in the metadata file (available on freeQueue or in the EOF)
    int freePosition = getFromFreeQueue();
    if (freePosition != -1) {
        if (lseek(file, freePosition * sizeof(*m), SEEK_SET)) {
            writeToTerminal("[DEBUG]: Error: lseek in indexNewMetaData Failed\n");
            close(file);
            return 1;
        }

        if (write(file, m, sizeof(*m)) == -1) {
            writeToTerminal("[DEBUG]: Error: adding new index to files\n");
            close(file);
            return 1;
        }
        
    } else {
        if (lseek(file, 0, SEEK_END) == -1) {
            writeToTerminal("[DEBUG]: Error: lseek in indexNewMetaData Failed\n");
            close(file);
            return 1;
        }
        
        if (write(file, m, sizeof(*m)) == -1) {
            writeToTerminal("[DEBUG]: Error: adding new index to files\n");
            close(file);
            return 1;
        }
    }

    close(file);
    setOperationType(&(*op), 'r');

    char buffer[256];
    sprintf(buffer, "Document %d indexed", identifier);
    setOperationArguments(&(*op), buffer);

    freeMetadata(m);
    return 0;
}

int consultMetaData(char **vector, operation *op, metadata *m) {
    unsigned int identifier = atoi(vector[0]);

    setOperationType(&(*op), 'r');
    char buffer[499];
    if (m ==  NULL || !getValid(m)) 
        sprintf(buffer, "Document with %d index not found", identifier);
    else 
        sprintf(buffer, "Title: %s\nAuthors: %s\nYear: %s\nPath: %s", getTitle(m), getAuthor(m), getYear(m), getPath(m));  
    
    setOperationArguments(&(*op), buffer);
    return 0;
}

int deleteMetaData(char **vector, operation *op) {
    int file = open(METADATA_PATH, O_RDWR | O_CREAT, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        return 1;
    }
    unsigned int identifier = atoi(vector[0]);

    metadata buffer[CHUNK_SIZE];
    ssize_t bytesRead;
    off_t offset = 0;
    int check = 0;

    while ((bytesRead = read(file, buffer, sizeof(buffer))) > 0) { 
        int records = bytesRead / sizeof(metadata);  
        for (int i = 0; i < records; i++) {
            if (getIdentifier(&buffer[i]) == identifier && getValid(&buffer[i]) == 1) {
                setValid(&buffer[i], 0);
                
                off_t recordOffset = offset + i * sizeof(metadata);
                if (lseek(file, recordOffset, SEEK_SET) == -1) {
                    writeToTerminal("[DEBUG]: Error: lseek in deleteMetaData Failed\n");
                    close(file);
                    return 1;
                }
    
                insertToFreeQueue((unsigned int) recordOffset / sizeof(metadata));
                if (write(file, &buffer[i], sizeof(metadata)) == -1) {
                    writeToTerminal("[DEBUG]: Error: Can rewrite metadata entry as invalid\n");
                    close(file);
                    return 1;
                }

                check = 1;
                break;
            }
        }
        if (check) break;
        offset += bytesRead;
    }
    close(file);

    char bufferOut[256];
    setOperationType(op, 'r');
    
    if (check) {
        removeFromCache(identifier);
        sprintf(bufferOut, "Index entry %d deleted", identifier);
    } else {  
        sprintf(bufferOut, "Index entry %d does not exists", identifier);
    }

    setOperationArguments(op, bufferOut);

    numberOfDeletes += 1;
    if (numberOfDeletes >= GARBAGE_LIMIT) // Check if garbage collection is needed
        if (garbageCollector() == 1) {
            writeToTerminal("[DEBUG]: Error: Garbage Collection Operation failed\n");
            return 1;
        }

    return 0;
}

int documentLines(char **vector, operation *op, const char* directoryPath, metadata *m) {
    char* keyword = vector[1];
    int pipefd[2];
    char buffer[499];
    setOperationType(op, 'r');
    
    if (m == NULL || !getValid(m)) {
        close(pipefd[0]);
        close(pipefd[1]);
        setOperationArguments(op, "The requested file is not indexed");
        return 0;
    }
    char path[512];
    sprintf(path, "%s%s", directoryPath, getPath(m));

    int stdOutCopy = dup(STDOUT_FILENO);
    if (pipe(pipefd) == -1) {
        writeToTerminal("Error creating pipe\n");
        return 1;
    }

    int pid_child = fork();
    if (pid_child < 0) {
        perror("[DEBUG]: fork failed");
        return 1;
    }

    if (pid_child == 0) {
        // Child process: redirect output to the pipe and execute the `grep` command
        close(pipefd[0]);
        dup2(pipefd[1], STDOUT_FILENO);
        close(pipefd[1]);
        if (execlp("grep", "grep", "-c", keyword, path, NULL) == -1) {
            writeToTerminal("[DEBUG]: execlp failed");
            _exit(2);
        }
    } else {
        // Parent process: wait for child process to finish and read the output
        int status;
        close(pipefd[1]);
        wait(&status);
        
        dup2(stdOutCopy, STDOUT_FILENO);
        close(stdOutCopy);
        
        if (WIFEXITED(status)) {
            int code = WEXITSTATUS(status);
            if (code == 2) {
                writeToTerminal("[DEBUG]: Error on Child Process --> documentLines : execlp");
                close(pipefd[0]);
                return 1;
            }
        }
        
        ssize_t bytesRead = read(pipefd[0], buffer, sizeof(buffer) - 1);
        if (bytesRead > 0) {
            buffer[bytesRead - 1] = '\0';
            printf("%s", buffer);
            setOperationArguments(op, buffer);
        } else {
            writeToTerminal("[DEBUG]: Error reading from pipe");
            close(pipefd[0]);
            return 1;
        }
        close(pipefd[0]);
        return 0;
    }

    return 0;
}

int listDocKeyword(char **vector, operation *op, const char *document_folder) {
    char *keyword = vector[0];
    int max_procs = 1;
    if (vector[1] != NULL) {
        max_procs = atoi(vector[1]);
        if (max_procs <= 0) max_procs = 1;
    }

    int active_procs = 0;
    int pipefd[2];

    if (pipe(pipefd) == -1) {
        writeToTerminal("[DEBUG]: Error creating pipe\n");
        return 1;
    }

    int file = open(METADATA_PATH, O_RDONLY | O_CREAT, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error opening the metadata file\n");
        close(pipefd[0]);
        close(pipefd[1]);
        return 1;
    }

    metadata m;
    while (read(file, &m, sizeof(m)) > 0) {
        if (!getValid(&m)) continue;
        
        // Ensure the number of active processes doesn't exceed the limit
        while (active_procs >= max_procs) {
            wait(NULL);
            active_procs--;
        }

        pid_t pid = fork();
        if (pid == 0) {
            // Child process: search for the keyword in the document
            pid_t grandchild_pid = fork();
            if (grandchild_pid == 0) {
                // Grandchild process: execute grep command
                char newPath[512];
                sprintf(newPath, "%s%s", document_folder, getPath(&m));
                execlp("grep", "grep", "-q", keyword, newPath, NULL);
                _exit(2);
            } else if (grandchild_pid > 0) {
                int status;
                waitpid(grandchild_pid, &status, 0);

                if (WIFEXITED(status) && WEXITSTATUS(status) == 0) {
                    unsigned int id = getIdentifier(&m);
                    ssize_t bytes_written = write(pipefd[1], &id, sizeof(unsigned int));
                    if (bytes_written == -1) {
                        writeToTerminal("[DEBUG]: Error writing to pipe\n");
                        close(pipefd[1]);
                        _exit(1);
                    }
                }
                close(pipefd[1]);
                _exit(0);
            } else {
                writeToTerminal("[DEBUG]: Error forking grandchild process\n");
                close(pipefd[0]);
                close(pipefd[1]);
                return 1;
            }
        } else if (pid > 0) {
            active_procs++;
        } else {
            writeToTerminal("[DEBUG]: Error forking process\n");
            close(pipefd[0]);
            close(pipefd[1]);
            return 1;
        }
    }

    off_t endPos = lseek(file, 0, SEEK_END);
    if (endPos == -1) {
        writeToTerminal("[DEBUG]: Error: lseek failed\n");
        close(pipefd[0]);
        close(pipefd[1]);
        close(file);
        return 1;
    }

    close(file);

    while (active_procs > 0) {
        wait(NULL);
        active_procs--;
    }

    close(pipefd[1]);

    unsigned int buff[endPos / sizeof(metadata)];
    int len;
    int processed_docs = 0; // Count of valid documents
    while ((len = read(pipefd[0], buff, sizeof(buff))) > 0)
        for (int i = 0; i < len / sizeof(unsigned int); i++) 
            processed_docs++;
    close(pipefd[0]);

    if (len >= 0) {
        char response[499];
        if (processed_docs == 0) {
            snprintf(response, sizeof(response), "No documents found containing keyword '%s'", keyword);
        } else {
            qsort(buff, processed_docs, sizeof(unsigned int), compare_unsigned);
            snprintf(response, sizeof(response), "Documents containing keyword: [");
            for (size_t i = 0; i < processed_docs; i++) {
                char number_str[20];
                snprintf(number_str, sizeof(number_str), "%u", buff[i]);
                strncat(response, number_str, sizeof(response) - strlen(response) - 1);
                if (i < processed_docs - 1)
                    strncat(response, ", ", sizeof(response) - strlen(response) - 1);
            }
            strncat(response, "]", sizeof(response) - strlen(response) - 1);
        }

        setOperationType(op, 'r');
        setOperationArguments(op, response);
    } else {
        setOperationType(op, 'r');
        setOperationArguments(op, "Error reading search results");
        return 1;
    }

    return 0;
}

int serverCloseMessage(operation *op) {
    char buffer[256];
    setOperationType(op, 'r');
    sprintf(buffer, "Server is shuting down\n %s\n", getCacheHitRate());
    setOperationArguments(op, buffer);
    
    return 0;
}

int garbageCollector() {
    clearFreeQueue(); // Clear the free queue to get fresh information about free spots
    int metaR_fd = open(METADATA_PATH, O_RDONLY | O_CREAT, 0600);
    if (metaR_fd == -1) {
        writeToTerminal("[DEBUG]: Error opening metadata file for reading\n");
        return 1;
    }

    // Open a temporary file to store valid metadata entries
    int metaTRW_fd = open(TEMPORARY_METADATA_PATH, O_RDWR | O_CREAT | O_TRUNC, 0600);
    if (metaTRW_fd == -1) {
        writeToTerminal("[DEBUG]: Error opening temporary metadata file for writing\n");
        close(metaR_fd);
        return 1;
    }

    metadata buffer[CHUNK_SIZE];
    metadata validBuffer[CHUNK_SIZE];
    ssize_t readBytes;
    while ((readBytes = read(metaR_fd, buffer, sizeof(buffer))) > 0) {
        size_t num_entries = readBytes / sizeof(metadata); 
        int validEntrysNumber = 0;
        for (int i = 0; i < num_entries; i++) 
            if (getValid(&buffer[i]))
                validBuffer[validEntrysNumber++] = buffer[i];
            
        if (write(metaTRW_fd, validBuffer, validEntrysNumber * sizeof(metadata)) == -1) {
            writeToTerminal("[DEBUG]: Error writing valid metadata on temporary file\n");
            close(metaR_fd);
            close(metaTRW_fd);
            unlink(TEMPORARY_METADATA_PATH);
            return 1;
        }
    }

    close(metaR_fd);

    if (readBytes == -1) {
        writeToTerminal("[DEBUG]: Error reading metadata file\n");
        close(metaTRW_fd);
        unlink(TEMPORARY_METADATA_PATH);
        return 1;
    }

    if (lseek(metaTRW_fd, 0, SEEK_SET) == -1) {
        writeToTerminal("[DEBUG]: Error seeking to 0 current position on temporary file\n");
        close(metaTRW_fd);
        unlink(TEMPORARY_METADATA_PATH);
        return 1;
    }

    int metaW_fd = open(METADATA_PATH, O_WRONLY | O_CREAT | O_TRUNC, 0600);
    if (metaW_fd == -1) {
        writeToTerminal("[DEBUG]: Error opening metadata file for writing\n");
        close(metaTRW_fd);
        unlink(TEMPORARY_METADATA_PATH);
        return 1;
    }

    metadata endBuffer[CHUNK_SIZE];
    ssize_t readBytes2;
    while ((readBytes2 = read(metaTRW_fd, endBuffer, sizeof(endBuffer))) > 0) {
        if (write(metaW_fd, endBuffer, readBytes2) == -1) {
            writeToTerminal("[DEBUG]: Error writing valid metadata on real metadata file\n");
            close(metaW_fd);
            close(metaTRW_fd);
            unlink(TEMPORARY_METADATA_PATH);
            return 1;
        }
    }

    close(metaW_fd);
    close(metaTRW_fd);
    unlink(TEMPORARY_METADATA_PATH); // Delete the temporary file

    if (readBytes2 == -1) {
        writeToTerminal("[DEBUG]: Error on reading buffer to copy from temporary to real metadata file\n");
        return 1;
    }

    writeToTerminal("[DEBUG]: Garbage Collector completed. All invalid entrys eliminated\n");
    return 0;
}