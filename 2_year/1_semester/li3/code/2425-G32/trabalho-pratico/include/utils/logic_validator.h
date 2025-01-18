/**
 * @file logic_validator.h
 * @brief Declarations of validation functions for entity relationships in the music and user datasets.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * This header defines functions to validate the integrity of relationships between artists, songs,
 * and users based on their IDs and constituent properties.
 */

#ifndef LOGIC_VALIDATOR_H
#define LOGIC_VALIDATOR_H

#include <glib.h>

/**
 * @brief Validates if the ID constituent type is correct for an artist.
 *
 * This function checks if the type of the artist's constituent (individual or group) 
 * matches the expected type for the given ID.
 *
 * @param id_constituents The artist's constituent ID.
 * @param type The expected type (individual or group).
 * @return 1 if the type is valid; 0 otherwise.
 */
int artist_id_constituent_type_validation(char *id_constituents, char *type);

/**
 * @brief Validates the existence of an album with the given ID.
 *
 * This function checks if an album exists by searching for the given album ID.
 *
 * @param album_id The ID of the album to validate.
 * @return 1 if the album exists; 0 otherwise.
 */
int album_id_music_validation(char *album_id);

/**
 * @brief Validates the existence of an artist linked to a given music ID.
 *
 * This function checks if any of the artist IDs associated with a music ID
 * exist in the system.
 *
 * @param id_artists The comma-separated artist IDs associated with the music.
 * @return 1 if at least one artist exists; 0 otherwise.
 */
int music_id_artist_validation(char *id_artists);

/**
 * @brief Validates the existence of a user linked to a given music ID.
 *
 * This function checks if any of the user IDs associated with a music ID
 * exist in the system.
 *
 * @param id_music The comma-separated music IDs associated with the user.
 * @return 1 if at least one music exists for the user; 0 otherwise.
 */
int music_id_user_validation(char *id_music);

#endif
