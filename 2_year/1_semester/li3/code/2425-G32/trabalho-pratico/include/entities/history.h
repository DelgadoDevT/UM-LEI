/**
 * @file history.h
 * @brief Header file for managing history records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This module provides functions for creating, freeing, and accessing
 * history records, which track user interaction with music, including
 * music ID, timestamp, and duration of play.
 */

#ifndef HISTORY_H
#define HISTORY_H

typedef struct history history; ///< Forward declaration of the history structure

/**
 * @brief Creates a new history record.
 *
 * Allocates memory for a history record and initialises its fields with
 * the provided data, including user ID, music ID, timestamp, and duration.
 *
 * @param user_id The unique ID of the user.
 * @param music_id The unique ID of the music.
 * @param timestamp The timestamp of when the music was played.
 * @param duration The duration the music was played.
 * @return A pointer to the created history record, or NULL if memory allocation fails.
 */
history *create_history(char *user_id, char *music_id, char *timestamp, char *duration);

/**
 * @brief Frees memory allocated for a history record.
 *
 * This function releases all memory associated with a history record,
 * including its attributes such as user ID, music ID, timestamp, and duration.
 *
 * @param h A pointer to the history record to free.
 */
void free_history(history *h);

/**
 * @brief Wrapper function to free history data.
 *
 * This function acts as a generic interface for freeing history data when
 * used in collection-based structures (e.g., linked lists, arrays).
 *
 * @param data A pointer to the history record to free. This pointer must be castable to (history *).
 */
void free_history_wrapper(void *data);

/**
 * @brief Retrieves the music ID from the history record.
 *
 * This function returns the music ID associated with the specified history record.
 *
 * @param h A pointer to the history record.
 * @return The music ID as a string.
 */
const char *get_history_music_id(history *h);

/**
 * @brief Retrieves the timestamp from the history record.
 *
 * This function returns the timestamp of when the music was played for the specified history record.
 *
 * @param h A pointer to the history record.
 * @return The timestamp as a string.
 */
const char *get_history_timestamp(history *h);

/**
 * @brief Retrieves the duration from the history record.
 *
 * This function returns the duration for which the music was played for the specified history record.
 *
 * @param h A pointer to the history record.
 * @return The duration as a string.
 */
const char *get_history_duration(history *h);

/**
 * @brief Retrieves the user ID from the history record.
 *
 * This function returns the user ID associated with the specified history record.
 *
 * @param h A pointer to the history record.
 * @return The user ID as a string.
 */
const char *get_history_user_id(history *h);

#endif // HISTORY_H