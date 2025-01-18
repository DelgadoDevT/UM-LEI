/**
 * @file music.h
 * @brief Header file for managing music records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header file declares the structure and functions for creating,
 * managing, and retrieving information from music records. It includes
 * functions to create a new music record, free allocated memory, and
 * access various attributes of a music record.
 */

#ifndef MUSIC_H
#define MUSIC_H

typedef struct music music;  ///< Forward declaration of the music structure

/**
 * @brief Creates a new music record.
 *
 * This function allocates memory for a new music record and sets its fields
 * based on the provided parameters.
 *
 * @param id The unique identifier for the music.
 * @param id_artists The list of artist IDs associated with the music.
 * @param album_id The album ID that the music belongs to.
 * @param duration The duration of the music track.
 * @param genre The genre of the music.
 * @return A pointer to the newly created music record, or NULL if memory allocation fails.
 */
music *create_music(char *id, char *id_artists, char *album_id, char *duration, char *genre);

/**
 * @brief Frees the memory allocated for a music record.
 *
 * This function deallocates the memory associated with the music record,
 * including its attributes. If the provided music pointer is NULL, the
 * function does nothing.
 *
 * @param m A pointer to the music record to be freed.
 */
void free_music(music *m);

/**
 * @brief Wrapper function to free a music record.
 *
 * This function is intended to be used as a wrapper for freeing music
 * records in contexts such as GSList or GHashTable operations.
 *
 * @param data A pointer to the music record to be freed.
 */
void free_music_wrapper(void *data);

/**
 * @brief Retrieves the list of artist IDs associated with a music record.
 *
 * This function returns the list of artist IDs for the given music record.
 *
 * @param m A pointer to the music record.
 * @return A pointer to a string containing artist IDs.
 */
const char *get_music_id_artists(music *m);

/**
 * @brief Retrieves the duration of the music record.
 *
 * This function returns the duration of the specified music record.
 *
 * @param m A pointer to the music record.
 * @return A pointer to a string representing the duration of the music.
 */
const char *get_music_duration(music *m);

/**
 * @brief Retrieves the genre of the music record.
 *
 * This function returns the genre of the specified music record.
 *
 * @param m A pointer to the music record.
 * @return A pointer to a string representing the genre of the music.
 */
const char *get_music_genre(music *m);

/**
 * @brief Retrieves the album ID associated with the music record.
 *
 * This function returns the album ID for the given music record.
 *
 * @param m A pointer to the music record.
 * @return A pointer to a string representing the album ID.
 */
const char *get_music_album_id(music *m);

#endif // MUSIC_H