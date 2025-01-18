/**
 * @file parse_query.h
 * @brief Header file for parsing and handling database queries.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header defines the functions required for parsing and processing queries
 * from a query file. It contains function prototypes for selecting and 
 * handling queries, as well as various helpers for executing different 
 * types of database queries. The functionality supports handling six different
 * query types, processing elements, and controlling the flow of query operations.
 */

#ifndef PARSE_QUERY_H
#define PARSE_QUERY_H

/**
 * @brief Parses a query file and processes each query line.
 * 
 * This function reads the query file line by line, identifies the type of 
 * query in each line, and calls the appropriate query handler function.
 * It also handles the initialization of necessary data for the queries 
 * and ensures proper data cleanup after processing.
 * 
 * @param query_path Path to the query file to be parsed.
 */
void parse_query(const char *query_path);

/**
 * @brief Selects and routes the query to the appropriate handler.
 * 
 * Based on the query type (identified by the query number), this function
 * selects and calls the relevant query handler function. It processes the
 * query line and ensures that query data is handled correctly.
 * 
 * @param line The query line to be processed.
 * @param command_number The command number for tracking the current query.
 * @param firsts An array to track whether the first call to a specific query type has occurred.
 */
void query_selector(char *line, int command_number, int *firsts);

/**
 * @brief Determines the format of the query based on the number string.
 * 
 * This function checks if the query number indicates a specific format (e.g., 'S').
 * 
 * @param number_str The string representing the query number.
 * @return 1 if the query format is 'S', 0 otherwise.
 */
int query_format_selector(char *number_str);

/**
 * @brief Handles the execution of query 1.
 * 
 * This function is called when a query of type 1 is identified. It handles
 * the data processing for the query, including trimming, validating, and
 * passing data to the relevant database operation.
 * 
 * @param number The query number (should be 1 for this function).
 * @param firsts An array tracking the first call of the query.
 * @param elem1 The first element in the query to be processed.
 * @param command_number The command number for the query.
 * @param query_format The format of the query.
 */
void handle_query_1(int number, int *firsts, char *elem1, int command_number, int query_format);

/**
 * @brief Handles the execution of query 2.
 * 
 * This function is called when a query of type 2 is identified. It processes
 * the elements in the query, performing necessary operations and passing 
 * them to the relevant function for execution.
 * 
 * @param number The query number (should be 2 for this function).
 * @param firsts An array tracking the first call of the query.
 * @param elem1 The first element in the query to be processed.
 * @param elem2 The second element in the query to be processed.
 * @param command_number The command number for the query.
 * @param query_format The format of the query.
 */
void handle_query_2(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format);

/**
 * @brief Handles the execution of query 3.
 * 
 * This function is called when a query of type 3 is identified. It processes
 * the elements for query 3 and invokes the necessary functions to handle
 * the data.
 * 
 * @param number The query number (should be 3 for this function).
 * @param firsts An array tracking the first call of the query.
 * @param elem1 The first element in the query to be processed.
 * @param elem2 The second element in the query to be processed.
 * @param command_number The command number for the query.
 * @param query_format The format of the query.
 */
void handle_query_3(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format);

/**
 * @brief Handles the execution of query 4.
 * 
 * This function is called when a query of type 4 is identified. It processes
 * the query elements and executes the appropriate database operations.
 * 
 * @param number The query number (should be 4 for this function).
 * @param firsts An array tracking the first call of the query.
 * @param elem1 The first element in the query to be processed.
 * @param elem2 The second element in the query to be processed.
 * @param command_number The command number for the query.
 * @param query_format The format of the query.
 */
void handle_query_4(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format);

/**
 * @brief Handles the execution of query 5.
 * 
 * This function is called when a query of type 5 is identified. It processes
 * the elements for query 5, with options for different types of recommenders.
 * 
 * @param number The query number (should be 5 for this function).
 * @param firsts An array tracking the first call of the query.
 * @param elem1 The first element in the query to be processed.
 * @param elem2 The second element in the query to be processed.
 * @param command_number The command number for the query.
 * @param query_format The format of the query.
 * @param recommender_function An integer indicating the recommender function (0 or 1).
 */
void handle_query_5(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format, int recommender_function);

/**
 * @brief Handles the execution of query 6.
 * 
 * This function is called when a query of type 6 is identified. It processes
 * the elements in the query and executes the necessary operations for query 6.
 * 
 * @param number The query number (should be 6 for this function).
 * @param firsts An array tracking the first call of the query.
 * @param elem1 The first element in the query to be processed.
 * @param elem2 The second element in the query to be processed.
 * @param elem3 The third element in the query to be processed.
 * @param command_number The command number for the query.
 * @param query_format The format of the query.
 */
void handle_query_6(int number, int *firsts, char *elem1, char *elem2, char *elem3, int command_number, int query_format);

#endif // PARSE_QUERY_H