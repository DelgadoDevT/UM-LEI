/**
 * @file parse_helper.h
 * @brief Header file for helper functions used in parsing and string manipulation.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header file declares utility functions that assist with parsing operations,
 * such as removing specific characters, trimming newlines, and validating strings.
 */

#ifndef PARSE_HELPER_H
#define PARSE_HELPER_H

/**
 * @brief Removes quotes from the beginning and end of a string.
 *
 * This function checks for double quotes at the start and end of the provided string
 * and removes them if present.
 *
 * @param string The string to remove quotes from.
 */
void remove_quotes_start_and_end(char *string);

/**
 * @brief Removes the newline character from the end of a string.
 *
 * This function checks if the last character in the string is a newline (`\n`)
 * and removes it by replacing it with a null terminator.
 *
 * @param string The string to trim the newline from.
 */
void trim_newline(char *string);

/**
 * @brief Removes brackets, single quotes, and spaces from a string.
 *
 * This function iterates through the input string and copies only characters
 * that are not brackets (`[` and `]`), single quotes (`'`), or spaces into
 * the output buffer.
 *
 * @param input The input string to clean.
 * @param output The output buffer to store the cleaned string.
 */
void remove_brackets_and_single_quotes(const char *input, char *output);

/**
 * @brief Parses a list-like string and converts it to a specific format.
 *
 * This function checks if the provided string represents an empty list ("[]") and
 * converts it to a special "EMPTY" string or processes the list and removes
 * brackets and quotes.
 *
 * @param id_constituents_pre The input string representing the list.
 * @param id_constituents The output buffer for the formatted string.
 */
void parse_list(char *id_constituents_pre, char *id_constituents);

/**
 * @brief Converts a simple list to its original string representation.
 *
 * This function takes a simple list string and converts it back to its original
 * form by adding brackets, single quotes, and commas between elements.
 *
 * @param simple_list The input simple list string.
 * @return The reconstructed original list string.
 */
char *convert_to_original_list(char *simple_list);

/**
 * @brief Reassembles a line of fields into a single original line format.
 *
 * This function takes an array of fields, the number of fields, and a function
 * that determines how to process list fields. It then reconstructs the original
 * line with proper formatting.
 *
 * @param fields An array of field strings to reassemble.
 * @param num_fields The number of fields in the array.
 * @param get_list_fields_function A function that provides the list fields.
 * @return A new string representing the reassembled line.
 */
char *reassemble_original_line(char **fields, int num_fields, void (*get_list_fields_function)(int *));

#endif // PARSE_HELPER_H
