/**
 * @file generic_parser.h
 * @brief Header file for the generic parsing and validation of entity data.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This header defines functions for parsing, validating, and handling errors for various
 * entities such as artists, albums, musics, users, and history. The system allows for flexible
 * parsing of data from input lines, ensuring that fields are properly validated and formatted.
 */

#ifndef GENERIC_PARSER_H
#define GENERIC_PARSER_H

/**
 * @brief Parses and validates an input line for a specific entity.
 *
 * This function takes a line of input data, splits it into fields, trims and removes unnecessary
 * characters, validates the fields, and either stores valid data or logs errors based on the
 * validation results. It handles different entity types by calling the appropriate validation
 * and data handling functions.
 *
 * @param line A string containing the input data line.
 * @param header A string containing the header for the current data, used for error reporting.
 * @param invalid_control A pointer to an integer that tracks whether any invalid entries have been encountered. If invalid data is found, this flag will be set.
 * @param entitie_number An integer representing the entity type (e.g., artist, album, etc.).
 * @param number_of_fields The total number of fields expected in the input line.
 * @param validation_function A function pointer to the entity-specific validation function.
 * @param add_data_to_holder_function A function pointer to the function that handles adding validated data to the data holder.
 * @param get_list_fields_function A function pointer to the function that retrieves the list of fields for the entity type.
 */
void generic_parse(char *line, char *header, int *invalid_control, int entitie_number, int number_of_fields, int (*validation_function)(char **), void (*add_data_to_holder_function)(char **), void (*get_list_fields_function)(int *));

/**
 * @brief Parses and validates list fields in the input data.
 *
 * This function processes fields that contain lists, validating and parsing each list.
 * It uses a list-specific validation function and modifies the fields accordingly.
 *
 * @param fields A list of strings containing the parsed fields from the input data.
 * @param free_fields An array that tracks which fields have dynamically allocated memory that should be freed.
 * @param get_list_fields_function A function pointer to retrieve the list of fields specific to the entity.
 * 
 * @return An integer that indicates if list parsing was successful or encountered issues.
 */
int parse_lists(char **fields, int *free_fields, void (*get_list_fields_function)(int *));

/**
 * @brief Validates the fields and handles errors during parsing.
 *
 * This function checks whether the parsed fields pass validation. If any errors are detected,
 * it logs the error and marks the invalid control flag. The error is written to a specific error file
 * for the entity type.
 *
 * @param fields A list of strings containing the parsed fields.
 * @param header A string containing the header for the data.
 * @param invalid_control A pointer to an integer that tracks if any invalid entries have been found.
 * @param entitie_number The entity number to identify the type of data (e.g., artist, album).
 * @param number_of_fields The total number of fields expected in the data.
 * @param control_list An integer that tracks the list validation status.
 * @param validation_function A function pointer to validate the fields.
 * @param get_list_fields_function A function pointer to retrieve the list fields for the entity.
 *
 * @return An integer indicating whether validation was successful (0) or failed (1).
 */
int validate_and_handle_errors(char **fields, char *header, int *invalid_control, int entitie_number, int number_of_fields, int control_list, int (*validation_function)(char **), void (*get_list_fields_function)(int *));

#endif // GENERIC_PARSER_H