/**
 * @file album_manager.h
 * @brief Header file for managing album records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This header file declares functions for managing album records, including
 * functionality to add, search, iterate, and validate albums. The implementation
 * hides the details of how the data is stored. The file also handles memory management
 * for album data.
 */

#ifndef ALBUM_MANAGER_H
#define ALBUM_MANAGER_H

/**
 * @brief Defines the maximum number of fields an album record can have.
 */
#define MAX_FIELDS 8

/**
 * @brief Retrieves the list of fields to be considered for album records.
 *
 * This function populates an integer array with flags indicating which fields
 * should be considered in the album data. The list corresponds to specific
 * fields in the album record (e.g., ID, title, artists, etc.).
 *
 * @param field_list An array of integers that will be populated with field flags.
 */
void get_album_list_fields(int *field_list);

/**
 * @brief Validates the album data fields.
 *
 * This function performs validation checks on the fields passed as arguments,
 * ensuring that the album data meets the required format or conditions.
 * (Currently a placeholder for actual validation logic.)
 *
 * @param fields An array of strings representing album data (e.g., ID, title, artist).
 * @return 0 if validation is successful, or an error code if validation fails.
 */
int validate_album(char **fields);

/**
 * @brief Initializes the album holder.
 *
 * This function sets up the necessary structures to store album records.
 * The implementation is hidden, ensuring the details of the storage mechanism are abstracted.
 * 
 * If the initialization fails, the program will terminate.
 */
void init_album_holder(void);

/**
 * @brief Frees all album data.
 *
 * This function cleans up the memory used to store the album records.
 * It ensures that no memory leaks occur when the album records are no longer needed.
 */
void free_album_data(void);

/**
 * @brief Adds an album record.
 *
 * This function stores an album record, using its ID to associate the record with its
 * corresponding data. The data passed as fields includes essential information like
 * the album's ID, title, artists, and producers.
 *
 * @param fields An array of strings representing album data (e.g., ID, title, artist).
 */
void add_album_to_holder(char **fields);

/**
 * @brief Searches for an album by its ID.
 *
 * This function checks if an album with the specified ID exists in the system.
 *
 * @param id The album ID to search for.
 * @return 0 if the album is found, 1 if the album does not exist.
 */
int album_search(char *id);

/**
 * @brief Initializes the album iteration process.
 *
 * This function prepares to iterate through all stored albums.
 * It sets up the internal iteration mechanism so albums can be processed one at a time.
 */
void iter_album_init(void);

/**
 * @brief Retrieves the next album's artist ID in the iteration.
 *
 * This function retrieves the artist ID for the next album in the iteration.
 * If there are more albums to iterate through, it returns 1.
 * If no more albums are available, it returns 0.
 *
 * @param artists_id A pointer to hold the artist ID of the current album.
 * @return 1 if there is another album to iterate through, 0 if no more albums remain.
 */
int get_album_next_and_verify_artist(char **artists_id);

#endif // ALBUM_MANAGER_H