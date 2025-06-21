/**
 * @file dclient.h
 * @brief Declares functions for handling the client-side of a distributed metadata system.
 *
 * This module provides the interface for parsing client arguments, sending
 * requests to the server, receiving responses, and running the client process
 * in a distributed system using named pipes (FIFOs).
 *
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

 #ifndef DCLIENT_H
 #define DCLIENT_H
 
 /**
  * @brief Sends a request to the server using a named pipe.
  *
  * Parses the command-line arguments into a valid operation and sends it to
  * the server channel.
  *
  * @param argv Array of command-line arguments.
  * @param argc Number of arguments.
  */
 void makeRequest(char* argv[], int argc);
 
 /**
  * @brief Waits and retrieves the response from the server.
  *
  * Opens the client-specific channel and reads the response until the server
  * closes the pipe.
  *
  * @param clientChannel Path to the named pipe for the client.
  */
 void getResponse(char *clientChannel);
 
 /**
  * @brief Main client routine.
  *
  * Creates a client FIFO, sends a request, reads the server's response,
  * and performs cleanup operations.
  *
  * @param argc Number of arguments.
  * @param argv Array of command-line arguments.
  */
 void runClient(int argc, char* argv[]);
 
 /**
  * @brief Parses command-line arguments into a single string.
  *
  * Concatenates arguments into a newline-separated string starting from index 2.
  *
  * @param argc Number of arguments.
  * @param argv Array of command-line arguments.
  * @return Concatenated string or NULL if invalid.
  */
 char* argumentsParser(int argc, char* argv[]);
 
 #endif // DCLIENT_H
 