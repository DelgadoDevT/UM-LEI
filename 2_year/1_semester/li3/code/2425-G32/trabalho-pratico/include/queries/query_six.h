/**
 * @file query_six.h
 * @brief Header file for Query 6 processing functions.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * This header defines functions for processing Query 6,
 * which involves analyzing user listening history to gather insights
 * such as the most frequent artists, genres, albums, and other related data.
 */

#ifndef QUERY_SIX_H
#define QUERY_SIX_H

/**
 * @brief Opaque structure representing an entry in the user's history.
 *
 * This structure holds details about a user's history, including
 * the user's ID and a list of history entry IDs associated with the user.
 */
typedef struct entry_on_history entry_on_history;

/**
 * @brief Opaque structure representing an entity count.
 *
 * This structure holds an entity ID and its associated count value.
 */
typedef struct entity_count entity_count;

/**
 * @brief Opaque structure representing an artist's data.
 *
 * This structure holds information about an artist, including
 * the total listening time and a list of their songs.
 */
typedef struct artist_pack artist_pack;

/**
 * @brief Frees the memory allocated for an entry_on_history record.
 *
 * Releases all memory associated with the entry_on_history record.
 * If the provided pointer is NULL, the function does nothing.
 *
 * @param data A pointer to the entry_on_history record to free.
 */
void free_entry_on_history_wrapper(void *data);

/**
 * @brief Frees the memory allocated for an entity_count record.
 *
 * Releases all memory associated with the entity_count record.
 * If the provided pointer is NULL, the function does nothing.
 *
 * @param data A pointer to the entity_count record to free.
 */
void free_entity_count_wrapper(void *data);

/**
 * @brief Frees the memory allocated for a times record.
 *
 * Releases all memory associated with the times record.
 * If the provided pointer is NULL, the function does nothing.
 *
 * @param data A pointer to the times record to free.
 */
void free_times_wrapper(void *data);

/**
 * @brief Frees the memory allocated for an artist_pack record.
 *
 * Releases all memory associated with the artist_pack record.
 * If the provided pointer is NULL, the function does nothing.
 *
 * @param data A pointer to the artist_pack record to free.
 */
void free_artist_pack_wrapper(void *data);

/**
 * @brief Creates a new entity_count record.
 *
 * Allocates memory for a new entity_count record, sets the provided
 * entity ID and count, and returns the newly created entity_count.
 *
 * @param id The ID of the entity.
 * @param count The count value associated with the entity.
 * @return A pointer to the newly created entity_count.
 */
entity_count *create_entity_count(char *id, int count);

/**
 * @brief Initializes and populates query six data.
 *
 * Initializes necessary data structures and populates them with data
 * for processing query six. It populates the user_entrys hash table
 * with user history entries.
 */
void init_and_populate_query_six_data(void);

/**
 * @brief Creates a new entry_on_history record.
 *
 * Allocates memory for a new entry_on_history record, sets the user
 * ID and history ID, and returns the newly created entry_on_history.
 *
 * @param id_user The ID of the user.
 * @param id_history The ID of the history entry.
 * @return A pointer to the newly created entry_on_history.
 */
entry_on_history *create_entry_on_history(char *id_user, char *id_history);

/**
 * @brief Frees all data associated with query six.
 *
 * Frees all memory allocated for the data structures used in query six,
 * including the user_entrys hash table.
 */
void free_query_six_data(void);

/**
 * @brief Retrieves the artist with the maximum listening time.
 *
 * Iterates through a list of artist_pack structures and returns the
 * artist ID with the maximum listening time.
 *
 * @param artists A pointer to a GSList of artist_pack structures.
 * @return The artist ID with the maximum listening time, or NULL if the list is empty.
 */
char *get_artist_with_max_listening_time(GSList *artists);

/**
 * @brief Retrieves the most frequent entity from a list.
 *
 * Iterates through a list of entity_count structures and returns the
 * entity ID with the highest count. If multiple entities have the
 * same count, the function resolves ties based on the specified order.
 *
 * @param list A pointer to a GSList of entity_count structures.
 * @param order If 0, returns the lexicographically smallest entity ID in case of a tie.
 *              If 1, returns the lexicographically largest entity ID in case of a tie.
 * @return The most frequent entity ID.
 */
char *get_most_frequent_entity(GSList *list, int order);

/**
 * @brief Fills the main data for printing query results.
 *
 * Populates the provided query_data array with data about albums, days,
 * hours, genres, and artists, as well as total listening time and the
 * total number of different music tracks.
 *
 * @param query_data An array of strings to store the query data.
 * @param albuns A pointer to a GSList of album entities.
 * @param days A pointer to a GSList of day entities.
 * @param hours A pointer to a GSList of hour entities.
 * @param genres A pointer to a GSList of genre entities.
 * @param artists_info A pointer to a GSList of artist_pack structures.
 * @param total_listening_time The total listening time in seconds.
 * @param total_different_musics The total number of different music tracks.
 */
void fill_print_data_main(char **query_data, GSList *albuns, GSList *days, GSList *hours, GSList *genres, GSList *artists_info, int total_listening_time, int total_different_musics);

/**
 * @brief Fills data for printing the second set of query results.
 *
 * Populates the provided data_artists array with data about artists and
 * their listening time and music counts.
 *
 * @param data_artists An array of strings to store artist data.
 * @param artists_info A pointer to a GSList of artist_pack structures.
 * @return 0 if the data was successfully filled, 1 if there was no data to fill.
 */
int fill_print_data_second(char **data_artists, GSList **artists_info);

/**
 * @brief Runs query six based on the given parameters.
 *
 * Processes the data related to a user and generates the results for query six.
 *
 * @param user_id The ID of the user to query.
 * @param year The year to filter data by (or NULL to include all years).
 * @param number_of_artists The number of artists to return in the second part of the query results.
 * @param command_number The command number for the query.
 * @param query_format Output format for the query result.
 */
void run_query_six(char *user_id, char *year, char *number_of_artists, int command_number, int query_format);

/**
 * @brief Finds an entity by its ID in a list.
 *
 * Searches through a list of entity_count structures and returns the
 * entity_count with the specified entity ID.
 *
 * @param list A pointer to a GSList of entity_count structures.
 * @param id The ID of the entity to search for.
 * @return A pointer to the entity_count with the specified ID, or NULL if not found.
 */
entity_count *findEntityById(GSList *list, char *id);

/**
 * @brief Finds an artist by its ID in a list.
 *
 * Searches through a list of artist_pack structures and returns the
 * artist_pack with the specified artist ID.
 *
 * @param artists A pointer to a GSList of artist_pack structures.
 * @param artist_id The ID of the artist to search for.
 * @return A pointer to the artist_pack with the specified artist ID, or NULL if not found.
 */
artist_pack* find_artist_by_id(GSList *artists, char *artist_id);

/**
 * @brief Fills query six data for a given user and year.
 *
 * Populates the data structures related to albums, days, hours, genres,
 * and artists for a specific user and year, and calculates the total
 * listening time and the total number of different music tracks.
 *
 * @param entry A pointer to the entry_on_history record for the user.
 * @param year The year to filter data by.
 * @param albuns A pointer to a GSList of album entities to populate.
 * @param days A pointer to a GSList of day entities to populate.
 * @param hours A pointer to a GSList of hour entities to populate.
 * @param genres A pointer to a GSList of genre entities to populate.
 * @param artists_info A pointer to a GSList of artist_pack structures to populate.
 * @param total_listening_time A pointer to the total listening time to update.
 * @param total_different_musics A pointer to the total number of different music tracks to update.
 * @return 1 if data was successfully populated, 0 otherwise.
 */
int fill_query_six_data(entry_on_history *entry, char *year, GSList **albuns, GSList **days, GSList **hours, GSList **genres, GSList **artists_info, int *total_listening_time, int *total_different_musics);

#endif // QUERY_SIX_H
