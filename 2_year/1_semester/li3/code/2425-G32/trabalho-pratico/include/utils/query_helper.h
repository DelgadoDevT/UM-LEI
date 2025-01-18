/**
 * @file query_helper.h
 * @brief Header file for query helper functions for data processing and conversion.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/MIT
 *
 * This header file includes declarations for functions that calculate age from a birth date,
 * count the digits in a number, convert a duration string to seconds,
 * and convert seconds back into a duration format.
 */

#ifndef QUERY_HELPER_H
#define QUERY_HELPER_H

#include <glib.h>

/**
 * @brief Converts a birth date string to age in years.
 *
 * This function calculates the age based on the given birth date string in
 * the format "YYYY-MM-DD" and the current date. Returns the computed age in years.
 *
 * @param birth_date A string representing the birth date in "YYYY-MM-DD" format.
 * @return The age in years.
 */
int birth_date_to_age(const char *birth_date);

/**
 * @brief Counts the number of digits in an integer.
 *
 * This function determines the number of digits in the provided integer,
 * handling both positive and negative values correctly.
 *
 * @param number The integer whose digits are to be counted.
 * @return The number of digits in the integer.
 */
int count_digits(int number);

/**
 * @brief Converts a duration string to total seconds.
 *
 * This function takes a string in the format "HH:MM:SS" and converts it
 * to an integer representing the total number of seconds.
 *
 * @param duration A string in the format "HH:MM:SS" to convert.
 * @return The total number of seconds represented by the duration.
 */
int convert_duration_to_seconds(const char *duration);

/**
 * @brief Converts a total number of seconds to a duration string.
 *
 * This function takes an integer representing total seconds and converts it
 * to a string in the format "HH:MM:SS". The duration string is written to
 * the provided buffer.
 *
 * @param total_seconds The total number of seconds to convert.
 * @param duration A buffer where the resulting "HH:MM:SS" string will be stored.
 * @param duration_size The size of the duration buffer.
 */
void convert_seconds_to_duration(int total_seconds, char *duration, size_t duration_size);

/**
 * @brief Counts the number of elements in a comma-separated string.
 *
 * This function counts how many elements are present in a comma-separated
 * string by splitting it at each comma.
 *
 * @param string A string containing comma-separated values.
 * @return The number of elements in the string.
 */
int count_group_elements(char *string);

/**
 * @brief Converts a date string to its corresponding week number.
 *
 * This function takes a date string in "YYYY/MM/DD" format and calculates
 * the week number relative to a fixed reference date (2024/09/09).
 *
 * @param date_str A string containing the date in "YYYY/MM/DD" format.
 * @return The week number of the given date relative to the reference date.
 */
int week_to_ind(char *date_str);

/**
 * @brief Adds one day to a given date string.
 *
 * This function takes a date string in "YYYY/MM/DD" format, adds one day to
 * it, and returns the new date string in the same format.
 *
 * @param date_str A string containing the date in "YYYY/MM/DD" format.
 * @return A newly allocated string with the date incremented by one day.
 */
char *add_one_day(char *date_str);

/**
 * @brief Checks if a timestamp is in the same year as a given year.
 *
 * This function compares a given year string with the year part of a timestamp.
 *
 * @param year A string representing the year to compare.
 * @param timestamp A string containing the timestamp in "YYYY-MM-DD" format.
 * @return 1 if the year matches, 0 otherwise.
 */
int is_same_year(char *year, char *timestamp);

#endif // QUERY_HELPER_H
