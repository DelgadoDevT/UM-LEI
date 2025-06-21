/** 
 * @file utils.h
 * @brief Provides utility functions for terminal I/O and data manipulation.
 *
 * Contains helper functions for error handling, string manipulation,
 * and data conversion operations.
 *
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef UTILS_H
 #define UTILS_H
 
 #include "operation.h"
 
 /**
  * @brief Writes a message to standard error output
  * 
  * @param errorMessage Null-terminated string to display
  * @note Uses low-level POSIX write() system call
  */
 void writeToTerminal(char *errorMessage);
 
 /**
  * @brief Counts space-delimited arguments in operation's argument string
  * 
  * @param op Operation containing arguments to count
  * @return Number of arguments found (0 if empty or error)
  * @warning Modifies internal copy of arguments (thread-unsafe)
  */
 unsigned int countArguments(const operation op);
 
 /**
  * @brief Splits operation arguments into individual strings
  * 
  * @param op Operation containing arguments to vectorize
  * @return NULL-terminated array of strings (must be freed) or NULL on error
  * @note Caller must free both array and individual elements
  * @warning Uses newline delimiter instead of space
  */
 char **vectorizeArguments(const operation op);
 
 /**
  * @brief Converts unsigned integer to string representation
  * 
  * @param number Value to convert
  * @return Dynamically allocated string (must be freed) or NULL on failure
  * @warning Returns NULL if memory allocation fails
  */
 char* uint_to_string(unsigned int number);
 
 /**
  * @brief Comparison function for unsigned integers (qsort compatible)
  * 
  * @param a Pointer to first unsigned int
  * @param b Pointer to second unsigned int
  * @return Negative if a < b, positive if a > b, zero if equal
  * @note Produces ascending order sorting
  */
 int compare_unsigned(const void *a, const void *b);

 /**
 * @brief Extracts the identifier from the argument vector of an operation.
 *
 * This function converts the first element of the argument vector of a given
 * operation into an unsigned integer, which is assumed to represent the identifier.
 * It handles memory cleanup after extracting the identifier.
 *
 * @param op The operation containing the arguments.
 * @return The extracted identifier as an unsigned integer.
 */
unsigned int getIdentifierFromVector(const operation op);
 
 #endif // UTILS_H