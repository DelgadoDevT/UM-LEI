/**
 * @file query_one.h
 * @brief Header file for handling Query One operations (user and artist data).
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header file declares functions for querying user and artist information, 
 * including album counts and total recipe values for artists. The implementation
 * of these functions abstracts the retrieval and handling of data, and memory
 * management for these operations is also handled within the functions.
 */

#ifndef QUERY_ONE_H
#define QUERY_ONE_H

typedef struct a_status a_status; ///< Forward declaration of the a_status structure

/**
 * @brief Initializes and populates the artist status data.
 *
 * This function sets up the `artist_status` hash table and populates it with data 
 * by calculating the number of albums each artist has and the total recipe value
 * for each artist. The data is gathered through specific functions that interact 
 * with the album and history data.
 */
void init_and_populate_artist_status_data(void);

/**
 * @brief Calculates the number of albums for each artist.
 *
 * This function iterates over all album data, splitting the artist IDs for each
 * album and counting the albums for each artist. This data is stored in the 
 * `artist_status` hash table where each key represents an artist's ID and the value
 * is an `a_status` structure containing the count of albums.
 */
void calc_num_albums_individual(void);

/**
 * @brief Calculates the total recipe for each artist.
 *
 * This function calculates the total recipe for each artist by iterating through 
 * the history data and aggregating recipe values from various music records. 
 * It updates the `artist_status` hash table with the total recipe for each artist.
 */
void calc_total_recipe(void);

/**
 * @brief Creates a new artist status structure.
 *
 * This function allocates and initializes a new `a_status` structure with the
 * specified recipe value and album count. The `a_status` structure holds the 
 * data for each artist, including their total recipe and the number of albums
 * they have.
 * 
 * @param recipe The artist's total recipe value.
 * @param album_num The number of albums the artist has.
 * @return A pointer to the newly created `a_status` structure.
 */
a_status *new_artist_status(double recipe, int album_num);

/**
 * @brief Frees all resources used by the query one data structures.
 *
 * This function frees the memory used by the `artist_status` hash table and
 * its associated data, ensuring that there are no memory leaks related to
 * the query one operations.
 */
void free_query_one_data(void);

/**
 * @brief Frees an individual artist status structure.
 *
 * This function is a callback for freeing each `a_status` structure stored 
 * in the `artist_status` hash table. It is used to release memory allocated
 * for the data in the hash table when it is no longer needed.
 * 
 * @param data A pointer to the artist status data to be freed.
 */
void free_artist_status_wrapper(void *data);

/**
 * @brief Runs Query One for user or artist based on the given ID.
 *
 * This function first checks whether the provided ID corresponds to a user or 
 * an artist. Depending on the result, it delegates the handling to the appropriate 
 * function for processing the data. If neither a user nor an artist is found, 
 * it calls a function to handle the empty query case.
 *
 * @param id The user or artist ID.
 * @param command_number The query command number.
 * @param format_controler The format controler, which determines the output format.
 */
void run_query_one(char *id, int command_number, int format_controler);

/**
 * @brief Handles user data for Query One.
 *
 * This function fetches the user's data based on their ID, calculates their age
 * based on their birth date, and formats the data for output. The data is then 
 * written to the output based on the specified query command number and format.
 *
 * @param id The user ID.
 * @param command_number The query command number.
 * @param format_controler The format controler, which determines the output format.
 */
void user_handle(char *id, int command_number, int format_controler);

/**
 * @brief Handles artist data for Query One.
 *
 * This function fetches the artist's data, including the number of albums and 
 * their total recipe value. It then formats the data for output. If the artist 
 * does not have any recorded albums or recipe, the function will return zeros 
 * for these values.
 *
 * @param id The artist ID.
 * @param command_number The query command number.
 * @param format_controler The format controler, which determines the output format.
 */
void artist_handle(char *id, int command_number, int format_controler);

#endif // QUERY_ONE_H