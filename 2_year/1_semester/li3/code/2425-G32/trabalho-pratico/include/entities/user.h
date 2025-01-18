/**
 * @file user.h
 * @brief Header file for user-related functions and data structures.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header file declares functions and structures for creating,
 * managing, and freeing user records, as well as accessing specific
 * fields within a user record.
 */

#ifndef USER_H
#define USER_H

typedef struct user user;  ///< Forward declaration of the user structure

/**
 * @brief Creates a new user record.
 *
 * This function allocates memory for a new user record and initializes
 * it with the provided information.
 *
 * @param id The unique identifier (username) for the user.
 * @param email The email address associated with the user.
 * @param first_name The user's first name.
 * @param last_name The user's last name.
 * @param birth_date The user's birth date.
 * @param country The user's country of residence.
 * @param id_liked_songs A string of IDs representing songs liked by the user.
 * @return A pointer to the newly created user record, or NULL if memory allocation fails.
 */
user *create_user(char *id, char *email, char *first_name, char *last_name, char *birth_date, char *country, char *id_liked_songs);

/**
 * @brief Frees the memory allocated for a user record.
 *
 * This function deallocates all memory associated with a user record,
 * including its attributes. If the provided pointer is NULL, the function does nothing.
 *
 * @param u A pointer to the user record to be freed.
 */
void free_user(user *u);

/**
 * @brief Wrapper function to free a user record.
 *
 * This function is intended to be used as a wrapper for freeing user
 * records in contexts such as GSList or GHashTable operations.
 *
 * @param data A pointer to the user record to be freed.
 */
void free_user_wrapper(void *data);

/**
 * @brief Retrieves the email address of the user.
 *
 * This function returns the email address associated with the specified user.
 *
 * @param u A pointer to the user record.
 * @return A pointer to the user's email address.
 */
const char *get_user_email(user *u);

/**
 * @brief Retrieves the first name of the user.
 *
 * This function returns the first name associated with the specified user.
 *
 * @param u A pointer to the user record.
 * @return A pointer to the user's first name.
 */
const char *get_user_first_name(user *u);

/**
 * @brief Retrieves the last name of the user.
 *
 * This function returns the last name associated with the specified user.
 *
 * @param u A pointer to the user record.
 * @return A pointer to the user's last name.
 */
const char *get_user_last_name(user *u);

/**
 * @brief Retrieves the birth date of the user.
 *
 * This function returns the birth date associated with the specified user.
 *
 * @param u A pointer to the user record.
 * @return A pointer to the user's birth date.
 */
const char *get_user_birth_date(user *u);

/**
 * @brief Retrieves the country of residence of the user.
 *
 * This function returns the country associated with the specified user.
 *
 * @param u A pointer to the user record.
 * @return A pointer to the user's country of residence.
 */
const char *get_user_country(user *u);

/**
 * @brief Retrieves the string of liked song IDs for the user.
 *
 * This function returns a string of song IDs that the user has marked as liked.
 *
 * @param u A pointer to the user record.
 * @return A string containing the user's liked song IDs.
 */
const char *get_user_id_liked_songs(user *u);

/**
 * @brief Retrieves the unique identifier of the user.
 *
 * This function returns the ID (username) associated with the user.
 *
 * @param u A pointer to the user record.
 * @return A pointer to the user's ID.
 */
const char *get_user_id(user *u);

#endif // USER_H