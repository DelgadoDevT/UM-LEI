/**
 * @file query_five.h
 * @brief Header file for Query 5 processing functions.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * This header defines functions for processing Query 5,
 * which involves recommending users with similar listening preferences
 * based on their favorite music genres.
 */

#ifndef QUERY_FIVE_H
#define QUERY_FIVE_H

/**
 * @typedef recom
 * @brief Opaque data type representing a user's music preference profile.
 *
 * This structure encapsulates the internal data used to track a user's
 * favorite genres and their corresponding metrics.
 * The details of its members are intentionally hidden to encapsulate its usage.
 */
typedef struct recom recom;

/**
 * @brief Frees memory allocated for a recom structure.
 *
 * @param data Pointer to the recom structure to be freed.
 */
void free_recom_wrapper(void *data);

/**
 * @brief Converts a genre name to its corresponding index.
 *
 * Maps a genre string to a predefined index in the genre list.
 *
 * @param genre The name of the genre.
 * @return The index of the genre in the predefined list, or -1 if not found.
 */
int genre_to_ind(char *genre);

/**
 * @brief Initializes and populates data for Query 5.
 *
 * Processes user and listening history data to create matrices and structures
 * that represent user preferences for music genres.
 */
void init_and_populate_query_five_data(void);

/**
 * @brief Processes user and genre data into a hash table.
 *
 * Populates a hash table with user information and their favorite genres
 * based on listening history data.
 *
 * @param process Pointer to the hash table for storing processed data.
 */
void process_data_to_hash(GHashTable *process);

/**
 * @brief Creates a new recom structure for a user.
 *
 * Initializes a user's preference profile, setting metrics for all genres
 * to zero, and optionally setting an initial genre.
 *
 * @param user_id The unique ID of the user.
 * @param genre Optional initial genre to be added to the user's preferences.
 * @return Pointer to the newly created recom structure.
 */
recom *create_new_recom(char *user_id, char *genre);

/**
 * @brief Frees all memory allocated for Query 5 data structures.
 *
 * Releases resources allocated for user preference matrices and related data.
 */
void free_query_five_data(void);

/**
 * @brief Executes Query 5 and generates the result.
 *
 * Recommends users with similar listening preferences to the specified user.
 * The number of recommendations and the recommender function to use
 * are configurable.
 *
 * @param user_id_recom ID of the user requesting recommendations.
 * @param number_of_users Number of users to recommend.
 * @param command_number Unique identifier for the query.
 * @param query_format Output format for the query result.
 * @param recommender_function Selector for the recommender function to use.
 */
void run_query_five(char *user_id_recom, char *number_of_users, int command_number, int query_format, int recommender_function);

#endif // QUERY_FIVE_H
