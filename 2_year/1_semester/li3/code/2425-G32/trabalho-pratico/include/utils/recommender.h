/**
 * @file recommender.h
 * @brief Header file for recommending users based on music classification similarity.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 *
 * This header file declares the necessary functions for implementing
 * a user recommender system based on cosine similarity and distance measures.
 */

#ifndef RECOMMENDER_H
#define RECOMMENDER_H

/**
 * @brief Defines a type for a structure used internally to represent user distances.
 */
typedef struct distPair distPair;

/**
 * @brief Comparator function for sorting distances.
 *
 * Compares two distance values (wrapped in an internal structure)
 * for sorting in ascending order.
 *
 * @param a Pointer to the first element to compare.
 * @param b Pointer to the second element to compare.
 * @return -1 if the first distance is smaller, 1 if it is larger, or 0 if they are equal.
 */
int compareDistance(const void *a, const void *b);

/**
 * @brief Finds the index of a user in a list of user IDs.
 *
 * Searches for the index of a specific user ID in an array of user IDs.
 *
 * @param user_id The user ID to search for.
 * @param ids_users Array of user IDs.
 * @param user_num The total number of users.
 * @return The index of the user ID in the array, or -1 if not found.
 */
int find_index(char *user_id, char **ids_users, int user_num);

/**
 * @brief Calculates the cosine similarity between two vectors.
 *
 * Computes the cosine similarity between two integer vectors representing
 * user preferences in different music genres.
 *
 * @param v1 The first vector.
 * @param v2 The second vector.
 * @param genre_num The number of elements (genres) in each vector.
 * @return A double representing the cosine similarity (range: 0 to 1).
 */
double calc_cosine_similarity(int *v1, int *v2, int genre_num);

/**
 * @brief Recommends users based on music classification similarity.
 *
 * Generates a list of recommended users for a target user by calculating
 * the cosine similarity between their music classification vectors and those of
 * other users. The results are sorted by similarity (closest users first).
 *
 * @param idTargetUser The ID of the target user for whom recommendations are generated.
 * @param matrixClassificationMusic A 2D array of integers representing users' classifications for each genre.
 * @param idsUsers Array of user IDs.
 * @param usersNum The total number of users.
 * @param genresNum The number of music genres.
 * @param numRecommendations The number of users to recommend.
 * @return A dynamically allocated array of recommended user IDs (strings). The caller must free this memory.
 */
char **recommendsUsers(char *idTargetUser, int **matrixClassificationMusic, char **idsUsers, int usersNum, int genresNum, int numRecommendations);

#endif // RECOMMENDER_H
