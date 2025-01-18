/**
 * @file artist_manager.h
 * @brief Header file for managing artist records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This module provides a set of functions to manage artist records, including
 * operations to add, search, validate, and retrieve information about artists.
 * The data storage and management details are abstracted to hide internal
 * implementation specifics.
 */

#ifndef ARTIST_MANAGER_H
#define ARTIST_MANAGER_H

#include "entities/artist.h"

/**
 * @brief Retrieves the list of fields to be considered for artist records.
 *
 * This function populates an integer array with flags indicating which fields
 * should be considered in the artist data. Each field corresponds to specific
 * artist data elements (e.g., ID, name, country, etc.).
 *
 * @param field_list An array of integers that will be populated with field flags.
 */
void get_artists_list_fields(int *field_list);

/**
 * @brief Validates the artist data fields.
 *
 * This function validates the artist data provided in the fields array,
 * ensuring the data adheres to the necessary format and conditions.
 *
 * @param fields An array of strings representing artist data (e.g., ID, name, country).
 * @return 1 if validation is successful, 0 if validation fails.
 */
int validate_artist(char **fields);

/**
 * @brief Initializes the artist data management system.
 *
 * This function prepares the system to store artist records. It sets up the
 * necessary structures and ensures that the management system is ready for use.
 * If the initialization fails, the program will terminate.
 */
void init_artist_holder(void);

/**
 * @brief Frees all artist data.
 *
 * This function releases the memory used to store the artist records.
 * It ensures that no memory leaks occur when the artist records are no longer needed.
 */
void free_artist_data(void);

/**
 * @brief Adds an artist record to the management system.
 *
 * This function stores an artist record, using its ID to associate the record
 * with its corresponding data. The data passed as fields includes essential
 * information like the artist's ID, name, country, and other attributes.
 *
 * @param fields An array of strings representing artist data (e.g., ID, name, country).
 */
void add_artist_to_holder(char **fields);

/**
 * @brief Retrieves an artist record by its ID.
 *
 * This function looks up an artist by their ID and returns the corresponding
 * artist data if it exists.
 *
 * @param id The artist's ID to search for.
 * @return A pointer to the artist record, or NULL if the artist is not found.
 */
artist *artist_by_id(char *id);

/**
 * @brief Searches for an artist by its ID.
 *
 * This function checks if an artist with the specified ID exists in the system.
 *
 * @param id The artist ID to search for.
 * @return 0 if the artist is found, 1 if the artist does not exist.
 */
int artist_search(char *id);

/**
 * @brief Retrieves a detailed artist summary for a specific query.
 *
 * This function retrieves a summary of an artist's details for query 1A, including
 * the artist's recipe per stream, ID constituents, and type.
 *
 * @param id The artist's ID to look up.
 * @param data A pointer to an array that will hold the retrieved data.
 */
void get_artist_resume_q1A(char *id, char **data);

/**
 * @brief Retrieves a detailed artist summary for a specific query.
 *
 * This function retrieves a summary of an artist's details for query 1B, including
 * the artist's name, type, and country of origin.
 *
 * @param id The artist's ID to look up.
 * @param data A pointer to an array that will hold the retrieved data.
 */
void get_artist_resume_q1B(char *id, char **data);

/**
 * @brief Checks if an artist is from a specified country.
 *
 * This function compares the country of a given artist with the provided
 * country string.
 *
 * @param id The artist's ID.
 * @param country The country to compare.
 * @return 0 if the artist is from the specified country, non-zero otherwise.
 */
int is_same_country_artist(char *id, char *country);

/**
 * @brief Retrieves a detailed artist summary for query 2.
 *
 * This function retrieves a summary of an artist's details for query 2, which includes
 * the artist's name, type, and country.
 *
 * @param id The artist's ID to look up.
 * @param data A pointer to an array that will hold the retrieved data.
 */
void get_artist_resume_q2(char *id, char **data);

/**
 * @brief Retrieves a partial artist summary for query 4A.
 *
 * This function retrieves a partial summary of an artist's details for query 4A,
 * including the artist's type.
 *
 * @param id The artist's ID to look up.
 * @param data A pointer to an array that will hold the retrieved data.
 */
void get_artist_resume_q4A(char *id, char **data);

#endif // ARTIST_MANAGER_H
