#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include "operation.h"
#include "utils.h"

// Initializes an operation struct with safe defaults
// Note: Returns by value - stack allocated, consider size for large operations
operation createOperation(int pid) {
    operation op;
    op.occupied = 0;
    op.type = 0;
    op.pid = pid;
    memset(op.arguments, 0, sizeof(op.arguments));
    return op;
}

// Sets operation arguments with buffer overflow protection
// Note: occupied becomes strlen() which might differ from original args length
void setOperationArguments(operation *op, const char *args) {
    if (op == NULL) return;  // Null guard
    
    // Safe copy with length restriction. 
    strncpy(op->arguments, args, sizeof(op->arguments) - 1);
    op->occupied = strlen(op->arguments);
}

// Sets operation type identifier
// Type meaning should be defined in operation.h (not shown here)
void setOperationType(operation *op, char type) {
    if (op == NULL) return;  // Null safety
    op->type = type;
}

// Associates process ID with operation
// PID should match actual system process for tracking purposes
void setOperationPid(operation *op, int pid) {
    if (op == NULL) return;
    op->pid = pid;
}

// Returns read-only pointer to arguments buffer
const char* getOperationArguments(const operation *op) {
    if (op == NULL) return NULL;  // Error case handling
    return op->arguments;
}

// Returns actual data length in arguments buffer
unsigned int getOperationOccupied(const operation *op) {
    if (op == NULL) return 0;
    return op->occupied;
}

// Retrieves operation type identifier
// Returns 0 (invalid) if op is NULL
char getOperationType(const operation *op) {
    if (op == NULL) return 0;
    return op->type;
}

// Gets associated process ID
// Returns -1 for invalid op
int getOperationPid(const operation *op) {
    if (op == NULL) return -1;
    return op->pid;
}