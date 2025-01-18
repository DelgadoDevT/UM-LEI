/**
 * @file query_four.h
 * @brief Header file for Query 4 processing functions.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * This header defines functions for processing Query 4,
 * which involves determining the most popular artists based on listening
 * duration and their frequency in weekly top 10 rankings within a specified 
 * time range.
 */

#ifndef QUERY_FOUR_H
#define QUERY_FOUR_H

#include <glib.h>

/**
 * @typedef artist_lt
 * @brief Opaque data type representing an artist's aggregated data for Query 4.
 *
 * This structure holds the internal data used to track an artist's performance 
 * metrics.
 * The details of its members are intentionally hidden to encapsulate its usage.
 */
typedef struct artist_lt artist_lt;

/**
 * @brief Frees memory allocated for an artist_lt structure.
 *
 * @param data Pointer to the artist_lt structure to be freed.
 */
void free_artist_lt_wrapper(void *data);

/**
 * @brief Compares two artist_lt structures by duration and ID.
 *
 * Artists are first compared by total listening duration (in descending order).
 * If durations are equal, they are compared lexicographically by their ID.
 *
 * @param a Pointer to the first artist_lt structure.
 * @param b Pointer to the second artist_lt structure.
 * @return Negative value if a < b, 0 if a == b, positive value if a > b.
 */
int compare_artist_lt(gconstpointer a, gconstpointer b);

/**
 * @brief Initializes and populates weekly data for Query 4.
 *
 * This function processes historical listening data, calculates the total
 * duration for each artist per week, and stores the top 10 artists for each week.
 */
void init_and_populate_query_four_data(void);

/**
 * @brief Initializes hash tables to store artist_lt data for each week.
 *
 * @param weeks_pre Array of hash tables, one for each week, where the data will be stored.
 */
void init_hash_tables_with_artist_lt(GHashTable *weeks_pre[]);

/**
 * @brief Processes an artist's data for inclusion in weekly tables.
 *
 * This function updates the weekly data for a specific artist based on
 * listening duration and timestamps.
 *
 * @param weeks_pre Array of hash tables storing weekly artist data.
 * @param artist_id The unique ID of the artist being processed.
 * @param data Array containing timestamp and duration of the listening event.
 */
void process_artist_lt(GHashTable *weeks_pre[], char *artist_id, char **data);

/**
 * @brief Adds an artist's data to a specific weekly hash table.
 *
 * If the artist already exists in the hash table, their duration is updated.
 * Otherwise, a new entry is created for the artist.
 *
 * @param weeks_pre Array of hash tables storing weekly artist data.
 * @param artist_id The unique ID of the artist being added.
 * @param duration Listening duration (in seconds) to be added.
 * @param index Index of the hash table corresponding to the week.
 */
void add_to_weeks_pre_hash(GHashTable *weeks_pre[], char *artist_id, int duration, int index);

/**
 * @brief Creates a new artist_lt structure.
 *
 * @param id The unique ID of the artist.
 * @param duration Total listening duration (in seconds) for the artist.
 * @param top10 Number of times the artist appeared in the top 10.
 * @return Pointer to the newly created artist_lt structure.
 */
artist_lt *create_artist_lt(char *id, int duration, int top10);

/**
 * @brief Frees memory allocated for Query 4 data.
 *
 * This function releases resources allocated for the weekly top 10 artist lists.
 */
void free_query_four_data(void);

/**
 * @brief Compares an artist_lt structure with a given artist ID.
 *
 * Used to find an artist in a list by comparing their ID.
 *
 * @param a Pointer to the artist_lt structure.
 * @param b Pointer to the artist ID string.
 * @return Negative value if IDs do not match, 0 if they match.
 */
int compare_artist_by_id(gconstpointer a, gconstpointer b);

/**
 * @brief Executes Query 4 and generates the result.
 *
 * Determines the artist who appeared most frequently in the top 10 weekly rankings
 * within the specified time range. Outputs the result to the appropriate format.
 *
 * @param start Start date of the range (format: YYYY-MM-DD).
 * @param end End date of the range (format: YYYY-MM-DD).
 * @param command_number Unique identifier for the query.
 * @param query_format Output format for the query result.
 */
void run_query_four(char *start, char *end, int command_number, int query_format);

#endif // QUERY_FOUR_H