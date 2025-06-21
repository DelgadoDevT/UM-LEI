#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include "operation.h"

// Writes error messages directly to STDERR using low-level write()
// Note: Uses raw file descriptor (STDERR_FILENO) instead of stderr FILE* stream
void writeToTerminal(char *errorMessage) {
    ssize_t written = write(STDERR_FILENO, errorMessage, strlen(errorMessage));
    if (written == -1) {
        perror("[DEBUG]: Error writing to terminal");
        return;
    }
}

// Counts space-separated arguments in operation's argument string
// Note: Returns 0 for empty strings, uses heap allocation for safety
unsigned int countArguments(const operation op) {
    unsigned int count = 0;
    if (op.arguments[0] == '\0') return count;

    char *copy = malloc(strlen(op.arguments) + 1);
    if (copy == NULL) return count;
    strcpy(copy, op.arguments);

    char *token = strtok(copy, "\n");
    while (token != NULL) {
        count++;
        token = strtok(NULL, "\n");
    }

    free(copy);
    return count;
}

// Creates NULL-terminated char** array from newline-separated arguments
// Note: Caller must free both array and its elements, potential memory leaks on failure
char **vectorizeArguments(const operation op) {
    unsigned int argumentsCounter = countArguments(op);
    if (argumentsCounter == 0) return NULL;

    char **result = (char **)malloc(argumentsCounter * sizeof(char *));
    if (result == NULL) {
        writeToTerminal("[DEBUG]: Error: memory allocation for vector failed.\n");
        return NULL;
    }

    char *arguments = (char *)op.arguments;
    char *saveptr;
    char *token = strtok_r(arguments, "\n", &saveptr);
    unsigned int index = 0;

    while (token != NULL) {
        result[index] = strdup(token);
        if (result[index] == NULL) {
            writeToTerminal("[DEBUG]: Error: memory allocation for argument failed.\n");
            for (unsigned int j = 0; j < index; j++) {
                free(result[j]);
            }
            free(result);
            return NULL;
        }
        index++;
        token = strtok_r(NULL, "\n", &saveptr);
    }

    return result;
}

// Converts unsigned integer to heap-allocated string
// Note: Caller must free returned pointer, fixed buffer size may limit number range
char* uint_to_string(unsigned int number) {
    char *str = (char*) malloc(20 * sizeof(char));  
    if (str == NULL) {
        writeToTerminal("[DEBUG]: Error allocating memory for string conversion.\n");
        return NULL;
    }
    sprintf(str, "%u", number);
    return str;
}

// Comparison function for qsort() with unsigned integers
// Note: Not suitable for sorting negative values if used with signed integers
int compare_unsigned(const void *a, const void *b) {
    return (*(unsigned int *)a - *(unsigned int *)b);
}

unsigned int getIdentifierFromVector(const operation op) {
    char **vector = vectorizeArguments(op);
    unsigned int argumentsCounter = countArguments(op);
    unsigned int identifier = atoi(vector[0]);
    for (unsigned int i = 0; i < argumentsCounter; i++) 
        free(vector[i]);
    free(vector);
    return identifier;
}