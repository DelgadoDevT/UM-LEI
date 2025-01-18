/**
 * @file artist.h
 * @brief Header file for managing artist records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This module provides functions for creating, freeing, and accessing
 * artist records, including their attributes and associated constituent IDs.
 */

#ifndef ARTIST_H
#define ARTIST_H

typedef struct artist artist; ///< Forward declaration of the artist structure

/**
 * @brief Creates a new artist record.
 *
 * Allocates memory for an artist and initialises its fields with the provided data.
 *
 * @param id A unique identifier for the artist.
 * @param name The name of the artist.
 * @param recipe_per_stream The artist's earnings per stream.
 * @param id_constituents A string representing the artist's constituents or collaborators.
 * @param country The country of origin of the artist.
 * @param type The type or genre of the artist.
 * @return A pointer to the created artist record, or NULL if memory allocation fails.
 */
artist *create_artist(char *id, char *name, char *recipe_per_stream, char *id_constituents, char *country, char *type);

/**
 * @brief Frees memory allocated for an artist record.
 *
 * This function releases all memory associated with the artist, including
 * its attributes and constituent IDs.
 *
 * @param a A pointer to the artist record to free.
 */
void free_artist(artist *a);

/**
 * @brief Wrapper function to free artist data.
 *
 * This function acts as a generic interface for freeing artist data when
 * used in collection-based structures (e.g., linked lists, arrays).
 *
 * @param data A pointer to the artist record to free. This pointer must be castable to (artist *).
 */
void free_artist_wrapper(void *data);

/**
 * @brief Retrieves the ID of the artist.
 *
 * This function returns the unique identifier of the specified artist.
 *
 * @param a A pointer to the artist record.
 * @return The artist's ID as a string.
 */
const char *get_artist_id(artist *a);

/**
 * @brief Retrieves the name of the artist.
 *
 * This function returns the name of the specified artist.
 *
 * @param a A pointer to the artist record.
 * @return The artist's name as a string.
 */
const char *get_artist_name(artist *a);

/**
 * @brief Retrieves the type of the artist.
 *
 * This function returns the type of the specified artist (e.g., genre).
 *
 * @param a A pointer to the artist record.
 * @return The artist's type as a string.
 */
const char *get_artist_type(artist *a);

/**
 * @brief Retrieves the country of the artist.
 *
 * This function returns the country of origin of the specified artist.
 *
 * @param a A pointer to the artist record.
 * @return The artist's country as a string.
 */
const char *get_artist_country(artist *a);

/**
 * @brief Retrieves the constituents of the artist.
 *
 * This function returns a string of the artist's constituent IDs.
 *
 * @param a A pointer to the artist record.
 * @return A string of constituent IDs.
 */
const char *get_artist_id_constituents(artist *a);

/**
 * @brief Retrieves the artist's recipe per stream.
 *
 * This function returns the artist's earnings per stream as a string.
 *
 * @param a A pointer to the artist record.
 * @return The artist's recipe per stream as a string.
 */
const char *get_artist_recipe_per_stream(artist *a);

#endif // ARTIST_H