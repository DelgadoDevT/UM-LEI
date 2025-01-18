/**
 * @file syntax_validator.h
 * @brief Header file for validating various input formats.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header file declares functions for validating input formats such as
 * duration, date, email addresses, subscription types, and other string
 * validations. These functions ensure the inputs conform to specific criteria.
 */

#ifndef SYNTAX_VALIDATOR_H
#define SYNTAX_VALIDATOR_H

#include <glib.h>

/**
 * @brief Validates the type of an artist.
 *
 * Checks whether the given artist type is valid. Valid types are:
 * - "individual"
 * - "group"
 *
 * @param type A string representing the artist type.
 * @return 0 if the type is valid, 1 otherwise.
 */
int artist_type_validation(char *type);

/**
 * @brief Validates the platform type.
 *
 * Checks whether the given platform type is valid. Valid types are:
 * - "mobile"
 * - "desktop"
 *
 * @param platform A string representing the platform type.
 * @return 0 if the platform type is valid, 1 otherwise.
 */
int plataform_validation(char *platform);

/**
 * @brief Validates a duration string in "HH:MM:SS" format.
 *
 * Ensures the duration string follows the format and constraints:
 * - Hours: 0 to 99
 * - Minutes: 0 to 59
 * - Seconds: 0 to 59
 *
 * @param duration A string representing the duration.
 * @return 0 if the duration is valid, 1 otherwise.
 */
int duration_validation(const char *duration);

/**
 * @brief Validates a date string in "YYYY/MM/DD" format.
 *
 * Ensures the date string is correctly formatted and within the valid range:
 * - Year: 0 to 2024
 * - Month: 1 to 12
 * - Day: 1 to 31
 * - Checks for valid dates in the year 2024.
 *
 * @param date A string representing the date.
 * @return 0 if the date is valid, 1 otherwise.
 */
int date_validation(const char *date);

/**
 * @brief Validates an email address format.
 *
 * Checks the provided email for correctness:
 * - Username must not be empty.
 * - Domain must be valid, with proper structure and characters.
 * - Only lowercase letters are allowed in the username and domain.
 *
 * @param email A string representing the email address.
 * @return 0 if the email is valid, 1 otherwise.
 */
int email_validation(char *email);

/**
 * @brief Validates the subscription type.
 *
 * Checks whether the subscription type is valid. Valid types are:
 * - "normal"
 * - "premium"
 *
 * @param subscription_type A string representing the subscription type.
 * @return 0 if the subscription type is valid, 1 otherwise.
 */
int subscription_type_validation(char *subscription_type);

/**
 * @brief Validates a list format string.
 *
 * Ensures the string begins with '[' and ends with ']', with a minimum length of 2.
 *
 * @param list A string representing the list format.
 * @return 0 if the list is valid, 1 otherwise.
 */
int list_validation(char *list);

/**
 * @brief Checks if a string contains only lowercase letters.
 *
 * Iterates through the string and verifies if every character is a lowercase letter.
 *
 * @param str The string to validate.
 * @return 1 if all characters are lowercase, 0 otherwise.
 */
int all_lowercase(const char *str);

/**
 * @brief Validates if a username contains only lowercase letters and digits.
 *
 * Ensures the given username consists exclusively of lowercase letters and digits.
 *
 * @param input The username string to validate.
 * @return 0 if the username is valid, 1 otherwise.
 */
int is_valid_username(char *input);

#endif // SYNTAX_VALIDATOR_H
