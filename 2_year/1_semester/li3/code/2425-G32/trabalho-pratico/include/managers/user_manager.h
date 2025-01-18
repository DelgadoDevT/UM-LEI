/**
 * @file user_manager.h
 * @brief Header file for managing user records.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This module provides a set of functions for managing user records, including
 * adding, searching, iterating, and retrieving specific data related to users.
 * The internal data structure and storage are abstracted, and the module operates
 * using user records identified by unique IDs.
 */

#ifndef USER_MANAGER_H
#define USER_MANAGER_H

#include "entities/user.h"

/**
 * @brief Retrieves the list of fields to be considered for user records.
 *
 * This function populates an integer array with flags indicating which fields
 * should be considered for each user record (e.g., ID, email, first name, etc.).
 *
 * @param field_list An array of integers that will be populated with flags for each field.
 */
void get_users_list_fields(int *field_list);

/**
 * @brief Validates the user data fields.
 *
 * This function validates the provided user data fields, ensuring that the
 * email, subscription type, birth date, and other fields follow the required formats.
 *
 * @param fields An array of strings representing user data (e.g., user ID, email, etc.).
 * @return 1 if validation is successful, 0 if validation fails.
 */
int validate_user(char **fields);

/**
 * @brief Initializes the user data management system.
 *
 * This function prepares the system for storing and managing user records,
 * ensuring that the necessary structures are set up.
 * If initialization fails, the program will terminate.
 */
void init_user_holder(void);

/**
 * @brief Frees all user data.
 *
 * This function releases the memory used for storing user records.
 * It ensures that no memory is leaked when the data is no longer needed.
 */
void free_user_data(void);

/**
 * @brief Adds a user record to the management system.
 *
 * This function stores a new user record with the provided data. The data
 * includes fields like user ID, email, first name, last name, birth date, country, etc.
 *
 * @param fields An array of strings representing the user data (e.g., user ID, email, etc.).
 */
void add_user_to_holder(char **fields);

/**
 * @brief Searches for a user record by its ID.
 *
 * This function checks if a user record with the given ID exists in the system.
 *
 * @param id The user record ID.
 * @return 0 if the user record is found, 1 if it is not found.
 */
int user_search(char *id);

/**
 * @brief Retrieves a user's resume data.
 *
 * This function retrieves detailed information for a specific user by its ID,
 * including email, first name, last name, birth date, and country.
 *
 * @param id The user record ID.
 * @param data An array of strings where the retrieved user data (email, name, country, etc.) will be stored.
 */
void get_user_resume(char *id, char **data);

/**
 * @brief Initializes the iterator for iterating over user records.
 *
 * This function sets up the iterator for sequentially accessing the user records.
 * Subsequent calls will allow retrieving data from each user record.
 */
void iter_user_init(void);

/**
 * @brief Retrieves the next user entry in the iteration.
 *
 * This function retrieves the next user entry during iteration, storing the
 * user ID and birth date in the provided data array.
 *
 * @param data An array of strings where the retrieved user entry data (user ID, birth date) will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_user_entry(char **data);

/**
 * @brief Retrieves the next user ID in the iteration.
 *
 * This function retrieves the next user ID during iteration.
 *
 * @param data An array of strings where the retrieved user ID will be stored.
 * @return 1 if there is more data to iterate over, 0 if the end of records is reached.
 */
int get_next_user_ids(char **data);

#endif // USER_MANAGER_H