/**
 * @file query_three.h
 * @brief Header file for managing query three functionality.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This header file declares functions and structures for handling query three,
 * which involves analyzing the popularity of music genres based on user likes,
 * grouped by age.
 * 
 * The query processes user data to calculate the total number of likes for each
 * genre in a specified age range and outputs the most popular genres.
 */

#ifndef QUERY_THREE_H
#define QUERY_THREE_H

#include <glib.h>

typedef struct musicPopularity musicPopularity; ///< Forward declaration of the musicPopularity structure

/**
 * @brief Compares two music genres based on their popularity (number of likes).
 *
 * This comparison function is used to sort the music genres in descending
 * order of popularity (likes). If two genres have the same number of likes,
 * the comparison is done lexicographically based on the genre name.
 *
 * @param a A pointer to the first music genre.
 * @param b A pointer to the second music genre.
 * @return A negative integer if `a` should come before `b`, a positive integer if `b` should come before `a`, and 0 if they are equal.
 */
int compare_genre_popularity(gconstpointer a, gconstpointer b);

/**
 * @brief Compares two music genres lexicographically.
 *
 * This comparison function is used to sort the genres by their name.
 *
 * @param a A pointer to the first music genre.
 * @param b A pointer to the second music genre name.
 * @return A negative integer if `a` should come before `b`, a positive integer if `a` should come after `b`, and 0 if they are equal.
 */
int compare_genre(gconstpointer a, gconstpointer b);

/**
 * @brief Creates a new musicPopularity object.
 *
 * This function creates and initializes a `musicPopularity` object with the given genre
 * and the number of likes.
 *
 * @param genre The name of the genre.
 * @param likes The number of likes for the genre.
 * @return A pointer to the newly created `musicPopularity` object.
 */
musicPopularity *create_musicPopularity(char *genre, int likes);

/**
 * @brief Frees the memory allocated for a musicPopularity object.
 *
 * This function deallocates the memory used by a `musicPopularity` object, including
 * its genre string.
 *
 * @param p A pointer to the `musicPopularity` object to free.
 */
void free_musicPopularity(musicPopularity *p);

/**
 * @brief Wrapper function to free memory for musicPopularity objects in a GSList.
 *
 * This function is used to properly free memory of `musicPopularity` objects stored in a GSList.
 *
 * @param data A pointer to the `musicPopularity` object to free.
 */
void free_musicPopularity_wrapper(void *data);

/**
 * @brief Frees the memory allocated for all query three data structures.
 *
 * This function frees the resources used by the linked lists storing music genre popularity,
 * which was populated during query three.
 */
void free_query_three_data(void);

/**
 * @brief Initializes and populates the data for query three.
 *
 * This function processes user data to collect the popularity information for each music genre.
 * It tracks the number of likes each genre receives and stores this data, grouped by user age.
 */
void init_and_populate_query_three_data(void);

/**
 * @brief Runs query three and retrieves the popularity of music genres in a specified age range.
 *
 * This function processes the query for music genres, calculates the total number of likes received
 * by each genre in the specified age range, and prepares the results for output.
 *
 * @param min_age_s The minimum age of the users to include in the query.
 * @param max_age_s The maximum age of the users to include in the query.
 * @param command_number The query command number, used for generating output.
 * @param format_controler The format controler, which determines the output format.
 */
void run_query_three(char *min_age_s, char *max_age_s, int command_number, int format_controler);

/**
 * @brief Prepares the output for query three.
 *
 * This function formats the results of query three, which include the genre names and their
 * corresponding like counts, and outputs the data based on the given `command_number` and `format_controler`.
 *
 * @param music_popularity_single_linked A linked list of genres sorted by popularity.
 * @param command_number The query command number, used for generating output.
 * @param format_controler The format controler, which determines the output format.
 */
void prepare_query_three_print(GSList *music_popularity_single_linked, int command_number, int format_controler);

#endif // QUERY_THREE_H