#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "utils/recommender.h"

typedef struct distPair
{
    char *user_id;  /**< User ID. */
    double dist;    /**< Users related distance. */
} distPair;


int compareDistance(const void *a, const void *b)
{
    const distPair *parA = (const distPair *)a;
    const distPair *parB = (const distPair *)b;

    if (parA->dist < parB->dist) return -1;
    if (parA->dist > parB->dist) return 1;
    return 0;
}

int find_index(char *user_id, char **ids_users, int user_num)
{
    for (int i = 0; i < user_num; i++)
    {
        if (strcmp(ids_users[i], user_id) == 0)
        {
            return i;
        }
    }

    return -1;
}

double calc_cosine_similarity(int *v1, int *v2, int genre_num)
{
    double product_sum = 0, norm_v1 = 0, norm_v2 = 0;

    for (int i = 0; i < genre_num; i++)
    {
        product_sum += v1[i] * v2[i];
        norm_v1 += v1[i] * v1[i];
        norm_v2 += v2[i] * v2[i];
    }

    if (norm_v1 == 0 || norm_v2 == 0) return 0;
    return product_sum / (sqrt(norm_v1) * sqrt(norm_v2));
}

char **recommendsUsers(char *idTargetUser, int **matrixClassificationMusic, char **idsUsers, int usersNum, int genresNum, int numRecommendations)
{
    if (!matrixClassificationMusic || !idsUsers || !idTargetUser || numRecommendations == 0) return NULL;

    int id_target = find_index(idTargetUser, idsUsers, usersNum);
    if (id_target == -1) return NULL;

    distPair *distances = malloc((usersNum - 1) * sizeof(distPair));
    int count = 0;

    for (int i = 0; i < usersNum; i++) 
    {
        if (i == id_target) continue;

        double cosine_similarity = calc_cosine_similarity(matrixClassificationMusic[id_target], matrixClassificationMusic[i], genresNum);
        double distance = 1 - cosine_similarity;

        distances[count].user_id = malloc(strlen(idsUsers[i]) + 1);
        strcpy(distances[count].user_id, idsUsers[i]);
        distances[count].dist = distance;
        count++;
    }

    qsort(distances, count, sizeof(distPair), compareDistance);

    if (numRecommendations > count) numRecommendations = count;

    char **recommendedUsers = malloc(numRecommendations * sizeof(char *));
    for (int j = 0; j < numRecommendations; j++) 
    {
        recommendedUsers[j] = malloc(strlen(distances[j].user_id) + 1);
        strcpy(recommendedUsers[j], distances[j].user_id);
    }

    for (int i = 0; i < count; i++) free(distances[i].user_id);
    free(distances);

    return recommendedUsers;
}  