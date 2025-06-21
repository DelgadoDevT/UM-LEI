/** 
 * @file operation.h
 * @brief Defines structures and functions for handling client operations.
 *
 * This module manages operation structures that encapsulate client requests,
 * including their arguments, process IDs, and operation types.
 *
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef OPERATION_H
 #define OPERATION_H
 
 /**
  * @struct operation
  * @brief Represents a client operation with arguments and metadata
  */
 typedef struct operation {
     char arguments[499];       /**< Buffer for operation arguments (null-terminated) */
     unsigned int occupied;     /**< Number of bytes used in arguments buffer */
     char type;                 /**< Operation type code (e.g., 'C'=CONSULT, 'A'=ADD) */
     int pid;                   /**< Client process identifier */
 } operation;
 
 /**
  * @brief Creates a new operation structure with default values
  * 
  * @param pid Client process identifier
  * @return New initialized operation structure
  */
 operation createOperation(int pid);
 
 /**
  * @brief Sets operation arguments with safe string copying
  * 
  * @param op Operation to modify
  * @param args Null-terminated string containing arguments
  * @note Truncates arguments to fit in 498 character buffer + null terminator
  */
 void setOperationArguments(operation *op, const char *args);
 
 /**
  * @brief Sets operation type code
  * 
  * @param op Operation to modify
  * @param type Single character representing operation type
  */
 void setOperationType(operation *op, char type);
 
 /**
  * @brief Updates client process identifier
  * 
  * @param op Operation to modify
  * @param pid New process ID
  */
 void setOperationPid(operation *op, int pid);
 
 /**
  * @brief Retrieves operation arguments
  * 
  * @param op Operation to query
  * @return Pointer to arguments buffer, NULL for invalid input
  */
 const char* getOperationArguments(const operation *op);
 
 /**
  * @brief Gets occupied space in arguments buffer
  * 
  * @param op Operation to query
  * @return Number of bytes used in arguments buffer (0-498)
  */
 unsigned int getOperationOccupied(const operation *op);
 
 /**
  * @brief Retrieves operation type code
  * 
  * @param op Operation to query
  * @return Type character code, 0 for invalid input
  */
 char getOperationType(const operation *op);
 
 /**
  * @brief Gets client process identifier
  * 
  * @param op Operation to query
  * @return Process ID, -1 for invalid input
  */
 int getOperationPid(const operation *op);
 
 #endif // OPERATION_H