/**
 * @file query_output.h
 * @brief Header file for managing query output files and printing query results to stdout.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This header defines functions for managing the output of query results, either by writing
 * them to specific output files or printing them to standard output. The query results are
 * stored in files named according to the query command number, allowing users to review 
 * the results of specific queries.
 * 
 * The system supports customizable formatting of query results, with options for using 
 * different separators between elements (e.g., semicolons or equals signs).
 * 
 * The output files are stored in the "resultados" directory, with filenames in the format:
 * `command<command_number>_output.txt`.
 */

#ifndef QUERY_OUTPUT_H
#define QUERY_OUTPUT_H

/**
 * @brief Writes an empty result for a query command.
 *
 * This function writes an empty result (just a newline) to the output file corresponding
 * to the given query command number. If the `command_number` is -1, the function simply
 * prints a message to `stdout` indicating that the query has no data.
 *
 * @param command_number The number of the query command. If -1, a message is printed to `stdout`.
 */
void write_query_empty(int command_number);

/**
 * @brief Writes query results to the output file or `stdout`.
 *
 * This function writes the elements of a query to an output file corresponding to the given
 * query command number. If the `command_number` is -1, the elements are printed to `stdout` 
 * instead of being written to a file. The format of the output can be controlled with the 
 * `format_controler` parameter.
 *
 * The elements are separated by either semicolons (`;`) or equals signs (`=`), depending on
 * the value of `format_controler`.
 *
 * @param command_number The number of the query command. If -1, results are printed to `stdout`.
 * @param elem_number The number of elements to write or print.
 * @param format_controler A flag that determines the output format:
 *                         - If `1`, elements are separated by equals signs (`=`).
 *                         - If `0`, elements are separated by semicolons (`;`).
 * @param elems A list of elements (strings) to write or print.
 */
void write_query(int command_number, int elem_number, int format_controler, char **elems);

#endif // QUERY_OUTPUT_H