/**
 * @file history_manager.h
 * @brief Header file for managing history records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This module provides a set of functions to manage history records, including
 * adding, searching, iterating, and retrieving data associated with the history of
 * user actions related to music. The internal data storage mechanisms are abstracted
 * to hide implementation details.
 */

#ifndef HISTORY_MANAGER_H
#define HISTORY_MANAGER_H

#include "entities/history.h"

/**
 * @brief Retrieves the list of fields to be considered for history records.
 *
 * This function populates an integer array with flags indicating which fields
 * should be considered in the history data. Each field corresponds to specific
 * history data elements (e.g., user ID, music ID, timestamp, etc.).
 *
 * @param field_list An array of integers that will be populated with field flags.
 */
void get_history_list_fields(int *field_list);

/**
 * @brief Validates the history data fields.
 *
 * This function validates the history data provided in the fields array,
 * ensuring that the duration and platform values adhere to the necessary format
 * and conditions.
 *
 * @param fields An array of strings representing history data (e.g., user ID, music ID, etc.).
 * @return 1 if validation is successful, 0 if validation fails.
 */
int validate_history(char **fields);

/**
 * @brief Initializes the history data management system.
 *
 * This function prepares the system to store history records. It sets up the
 * necessary structures and ensures that the management system is ready for use.
 * If the initialization fails, the program will terminate.
 */
void init_history_holder(void);

/**
 * @brief Frees all history data.
 *
 * This function releases the memory used to store the history records.
 * It ensures that no memory leaks occur when the history records are no longer needed.
 */
void free_history_data(void);

/**
 * @brief Adds a history record to the management system.
 *
 * This function stores a history record, using its ID to associate the record
 * with its corresponding data. The data passed as fields includes user ID, music ID,
 * timestamp, and duration.
 *
 * @param fields An array of strings representing history data (e.g., user ID, music ID, etc.).
 */
void add_history_to_holder(char **fields);

/**
 * @brief Initializes the iterator for iterating over history records.
 *
 * This function prepares the iterator for traversing the stored history records
 * and allows subsequent calls to retrieve data sequentially.
 */
void iter_history_init(void);

/**
 * @brief Retrieves the next music ID in the history records.
 *
 * This function retrieves the music ID from the next history record during iteration.
 *
 * @param music_id A pointer to a string where the music ID will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_history_music_id(char **music_id);

/**
 * @brief Retrieves the next set of top data from the history records.
 *
 * This function retrieves a set of top data (music ID, timestamp, and duration)
 * from the next history record during iteration.
 *
 * @param data An array of strings where the retrieved data will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_history_top_data(char **data);

/**
 * @brief Retrieves the next set of IDs from the history records.
 *
 * This function retrieves the user ID and music ID from the next history record
 * during iteration.
 *
 * @param data An array of strings where the retrieved IDs will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_history_ids(char **data);

/**
 * @brief Retrieves the next set of IDs for query 6 from the history records.
 *
 * This function retrieves the user ID and record ID (key) from the next history
 * record during iteration for query 6.
 *
 * @param data An array of strings where the retrieved IDs will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_history_q6_ids(char **data);

/**
 * @brief Retrieves detailed history data for query 6.
 *
 * This function retrieves detailed data (music ID, timestamp, and duration)
 * for a specific history record by its ID.
 *
 * @param id The history record ID to look up.
 * @param data An array of strings where the retrieved data will be stored.
 */
void get_history_data_for_q6(char *id, char **data);

#endif // HISTORY_MANAGER_H