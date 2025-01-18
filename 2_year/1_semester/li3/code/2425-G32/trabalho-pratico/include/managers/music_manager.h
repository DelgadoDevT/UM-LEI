/**
 * @file music_manager.h
 * @brief Header file for managing music records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This module provides a set of functions for managing music records, including
 * adding, searching, iterating, and retrieving specific data related to music.
 * The internal data structure and storage are abstracted, and the module operates
 * using music records identified by unique IDs.
 */

#ifndef MUSIC_MANAGER_H
#define MUSIC_MANAGER_H

#include "entities/music.h"

/**
 * @brief Retrieves the list of fields to be considered for music records.
 *
 * This function populates an integer array with flags indicating which fields
 * should be considered for each music record (e.g., ID, title, artist ID, etc.).
 *
 * @param field_list An array of integers that will be populated with flags for each field.
 */
void get_musics_list_fields(int *field_list);

/**
 * @brief Validates the music data fields.
 *
 * This function validates the provided music data fields, ensuring that the
 * music ID, artist ID, album ID, and duration follow the required formats.
 *
 * @param fields An array of strings representing music data (e.g., music ID, title, etc.).
 * @return 1 if validation is successful, 0 if validation fails.
 */
int validate_music(char **fields);

/**
 * @brief Initializes the music data management system.
 *
 * This function prepares the system for storing and managing music records,
 * ensuring that the necessary structures are set up.
 * If initialization fails, the program will terminate.
 */
void init_music_holder(void);

/**
 * @brief Frees all music data.
 *
 * This function releases the memory used for storing music records.
 * It ensures that no memory is leaked when the data is no longer needed.
 */
void free_music_data(void);

/**
 * @brief Adds a music record to the management system.
 *
 * This function stores a new music record with the provided data. The data
 * includes fields like music ID, title, artist ID, album ID, duration, and genre.
 *
 * @param fields An array of strings representing the music data (e.g., music ID, title, etc.).
 */
void add_music_to_holder(char **fields);

/**
 * @brief Retrieves a music record by its ID.
 *
 * This function retrieves a specific music record from the data management
 * system using its unique ID.
 *
 * @param id The music record ID.
 * @return A pointer to the music record, or NULL if no record is found.
 */
music *music_by_id(char *id);

/**
 * @brief Searches for a music record by its ID.
 *
 * This function checks if a music record with the given ID exists in the system.
 *
 * @param id The music record ID.
 * @return 0 if the music record is found, 1 if it is not found.
 */
int music_search(char *id);

/**
 * @brief Retrieves the genre of a music record by its ID.
 *
 * This function retrieves the genre of a music record, given its ID.
 *
 * @param id The music record ID.
 * @return A string representing the genre of the music, or NULL if not found.
 */
char *get_music_genre_by_id(char *id);

/**
 * @brief Retrieves the artist ID associated with a music record.
 *
 * This function retrieves the artist ID linked to a specific music record, using its ID.
 *
 * @param id The music record ID.
 * @return A string representing the artist ID associated with the music record.
 */
char *get_artist_id_by_id(char *id);

/**
 * @brief Initializes the iterator for iterating over music records.
 *
 * This function sets up the iterator for sequentially accessing the music records.
 * Subsequent calls will allow retrieving data from each music record.
 */
void iter_music_init(void);

/**
 * @brief Retrieves the next music entry in the iteration.
 *
 * This function retrieves the next music entry during iteration, storing the
 * artist ID and music duration in the provided data array.
 *
 * @param data An array of strings where the retrieved music entry data (artist ID and duration) will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_music_entry(char **data);

/**
 * @brief Retrieves detailed music data for query 6.
 *
 * This function retrieves detailed data for a specific music record by its ID,
 * including the artist ID, album ID, and genre.
 *
 * @param id The music record ID.
 * @param data An array of strings where the retrieved music data (artist ID, album ID, genre) will be stored.
 */
void get_music_data_for_q6(char *id, char **data);

#endif // MUSIC_MANAGER_H