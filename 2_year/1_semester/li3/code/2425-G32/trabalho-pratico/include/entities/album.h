/**
 * @file album.h
 * @brief Header file for album-related functions and data structures.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * This header file declares functions and structures for creating,
 * managing, and freeing album records, as well as accessing specific
 * fields within an album record.
 */

#ifndef ALBUM_H
#define ALBUM_H

typedef struct album album; ///< Forward declaration of the album structure

/**
 * @brief Creates a new album record.
 *
 * Allocates memory for an album and initialises its fields with the given data.
 *
 * @param id A unique identifier for the album. This must be a null-terminated string.
 * @param artists_id A string of IDs representing the artists of the album. This must be a null-terminated string.
 * @return A pointer to the created album record, or NULL if memory allocation fails.
 */
album *create_album(char *id, char *artists_id);

/**
 * @brief Frees the memory allocated for an album record.
 *
 * Releases all memory associated with the album record, including its individual fields.
 * If the provided pointer is NULL, the function does nothing.
 *
 * @param a A pointer to the album record to free.
 */
void free_album(album *a);

/**
 * @brief Wrapper function to free album data.
 *
 * This function acts as a generic interface for freeing album data when
 * used in collection-based structures (e.g., linked lists, arrays).
 *
 * @param data A pointer to the album record to free. This pointer must be castable to (album *).
 */
void free_album_wrapper(void *data);

/**
 * @brief Retrieves the artist IDs associated with the album.
 *
 * Returns a string containing the IDs of the artists for the specified album. The returned pointer
 * must not be freed by the caller and remains valid as long as the album object exists.
 *
 * @param a A pointer to the album record.
 * @return A pointer to a null-terminated string containing the artist IDs.
 */
const char *get_album_artists_id(album *a);

#endif // ALBUM_H