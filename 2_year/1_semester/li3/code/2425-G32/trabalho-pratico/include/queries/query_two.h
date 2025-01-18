/**
 * @file query_two.h
 * @brief Header file for managing query two functionality.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This header file declares functions and structures for handling query two,
 * which involves retrieving the top N artists based on the total duration of
 * their music, optionally filtered by country.
 * 
 * The query collects and sorts music durations for artists and can output
 * the results in various formats.
 */

#ifndef QUERY_TWO_H
#define QUERY_TWO_H

#include <glib.h>

typedef struct discography discography; ///< Forward declaration of the discography structure

/**
 * @brief Compares two discographies based on the total duration of music.
 *
 * This comparison function is used to sort the discographies in descending
 * order of their total durations. If two discographies have the same duration,
 * the comparison is done lexicographically based on the artist ID.
 *
 * @param a A pointer to the first discography.
 * @param b A pointer to the second discography.
 * @return A negative integer if `a` should come before `b`, a positive integer if `b` should come before `a`, and 0 if they are equal.
 */
int sort_by_duration(gconstpointer a, gconstpointer b);

/**
 * @brief Initializes and populates the query two data structures.
 *
 * This function creates and populates a hash table (`duration_hash`) with data
 * for each artist's ID and the total duration of their music. It iterates over
 * all music entries and accumulates the total duration for each artist.
 */
void init_and_populate_query_two_data(void);

/**
 * @brief Initializes and populates the linked list for duration data.
 *
 * After the data is collected in the hash table, this function populates a
 * linked list (`duration_linked`) with the discography data, sorted by total
 * music duration. The list is then ready for further processing and output.
 */
void init_and_populate_duration_linked(void);

/**
 * @brief Frees the memory allocated for the query two data structures.
 *
 * This function frees the resources used by the linked list of `discography`
 * structures that was populated during the query two operation.
 */
void free_query_two_data(void);

/**
 * @brief Runs query two and retrieves the top N artists.
 *
 * This function processes the query for the top N artists, either globally or
 * filtered by a specified country. It retrieves the discographies, sorts them
 * by total duration, and prepares the data for output.
 *
 * @param top_N The number of top artists to retrieve.
 * @param country The country to filter artists by (can be NULL for no filtering).
 * @param command_number The query command number, used for generating output.
 * @param format_controler The format controler, which determines the output format.
 */
void run_query_two(char *top_N, char *country, int command_number, int format_controler);

/**
 * @brief Prepares the output for query two.
 *
 * This function prepares and outputs the results of query two, which involves
 * displaying the artist's ID, name, and total music duration. It formats the
 * output based on the provided `command_number` and `format_controler`.
 *
 * @param duration_linked_copy A linked list containing the sorted discography data.
 * @param command_number The query command number, used for generating output.
 * @param format_controler The format controler, which determines the output format.
 */
void prepare_query_two_print(GSList *duration_linked_copy, int command_number, int format_controler);

#endif // QUERY_TWO_H