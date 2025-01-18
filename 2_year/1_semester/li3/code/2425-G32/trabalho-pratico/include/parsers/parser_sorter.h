/**
 * @file parser_sorter.h
 * @brief Header module for parser sorter functions.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This module provides functions for parsing and populating data structures
 * from CSV files, managing relationships between entities (artists, albums, music, users, history),
 * and executing queries.
 */

#ifndef PARSER_SORTER_H
#define PARSER_SORTER_H

/**
 * @brief Parses CSV files and populates data structures for a specific entity type.
 *
 * This function reads a CSV file corresponding to an entity type (artist, album, music, etc.),
 * validates the data, and adds it to the appropriate holder.
 *
 * @param data_path Path to the CSV file.
 * @param number_of_fields Number of fields expected in each record.
 * @param entitie_number Identifier for the entity type (e.g., 0 for artist, 1 for album).
 * @param validation_function Pointer to the validation function for the entity.
 * @param add_data_to_holder_function Pointer to the function that adds valid data to the holder.
 * @param get_list_fields_function Pointer to the function that retrieves the fields to be listed.
 */
void parse_and_populate_data(const char *data_path, int number_of_fields, int entitie_number, 
                             int (*validation_function)(char **), 
                             void (*add_data_to_holder_function)(char **), 
                             void (*get_list_fields_function)(int *));

/**
 * @brief Parses data files and query file for a specified type.
 *
 * This function initializes data holders for different entities, processes CSV files,
 * and optionally parses a query file based on the provided type.
 *
 * @param data_path Array of paths to the data files for each entity.
 * @param query_path Path to the query file.
 * @param type Query type (0 for parsing and processing queries, other values for specific actions).
 * @return 0 on successful execution.
 */
int parse_data(const char **data_path, const char *query_path, int type);

/**
 * @brief Frees allocated memory for all data structures.
 *
 * This function releases memory allocated for all entity holders, including artists,
 * albums, music, users, and history.
 */
void free_data(void);

#endif // PARSER_SORTER_H
