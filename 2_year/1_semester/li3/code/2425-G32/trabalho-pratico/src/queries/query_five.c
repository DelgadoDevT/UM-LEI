#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <glib.h>
#include "queries/query_five.h"
#include "utils/recomendador.h"
#include "managers/music_manager.h"
#include "managers/history_manager.h"
#include "managers/user_manager.h"
#include "output/query_output.h"
#include "utils/recommender.h"

#define MAX_GENRE 10

static int **matrix;
static char **users;
static int users_num;
static char *genres[MAX_GENRE] = {"Metal", "Classical", "Pop", "Electronic", "Rock", "Jazz", "Blues", "Reggae", "Hip Hop", "Country"};

typedef struct recom 
{
    char *user_id;           /**< Unique identifier for the user (string). */
    int fav_genre[MAX_GENRE]; /**< Array indicating the user's favorite genres, with a maximum size defined by MAX_GENRE. */
} recom;


void free_recom_wrapper(void *data)
{
    recom *rc = (recom *)data;
    free(rc->user_id);
    free(rc); 
}

int genre_to_ind(char *genre)
{
    for (int i = 0; i < MAX_GENRE; i++)
    {
        if (strcmp(genre, genres[i]) == 0) return i;
    }
    return -1;
}

void init_and_populate_query_five_data(void)
{
    GHashTable *process = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_recom_wrapper);
    process_data_to_hash(process);
    users_num = g_hash_table_size(process);

    users = malloc(users_num * sizeof(char *));
    matrix = malloc(users_num * sizeof(int *));
    
    GList *list = g_hash_table_get_values(process);
    int pos = 0;
    for (GList *iter = list; iter != NULL; iter = iter->next)
    {
        recom *rec = (recom *)iter->data;
        users[pos] = strdup(rec->user_id);
        matrix[pos] = malloc(MAX_GENRE * sizeof(int));
        for (int j = 0; j < MAX_GENRE; j++)
        {
            matrix[pos][j] = rec->fav_genre[j];
        }
        pos++;
    }

    g_list_free(list);
    g_hash_table_destroy(process);
}

void process_data_to_hash(GHashTable *process)
{
    char *id[1];
    iter_user_init();
    while (get_next_user_ids(id) == 1)
    {
        recom *temp = g_hash_table_lookup(process, id[0]);
        if (temp == NULL)
        {
            recom *new_entry = create_new_recom(id[0], NULL);
            g_hash_table_insert(process, strdup(id[0]), new_entry);
        }

        free(id[0]);
    }

    char *data[2];
    iter_history_init();
    while (get_next_history_ids(data) == 1)
    {
        char *user_id = data[0];
        char *genre = get_music_genre_by_id(data[1]);
        recom *temp = g_hash_table_lookup(process, user_id);
        if (temp != NULL)
        {
            int ind = genre_to_ind(genre);
            temp->fav_genre[ind] += 1;
        }

        free(genre);
        for (int i = 0; i < 2; i++) if (data[i]) free(data[i]);
    }
}

recom *create_new_recom(char *user_id, char *genre)
{
    recom *new = malloc(sizeof(recom));
    new->user_id = strdup(user_id);
    for (int i = 0; i < MAX_GENRE; i++) new->fav_genre[i] = 0;

    if (genre != NULL) 
    {
        int ind = genre_to_ind(genre);
        new->fav_genre[ind] = 1;
    }

    return new;
}

void free_query_five_data(void)
{
    for (int i = 0; i < users_num; i++)
    {
        free(users[i]);
        free(matrix[i]);
    }

    free(users);
    free(matrix);
}

void run_query_five(char *user_id_recom, char *number_of_users, int command_number, int query_format, int recommender_function)
{
    int exists = user_search(user_id_recom);
    int number_of_users_recommended = atoi(number_of_users);
    if (exists == 1 || number_of_users_recommended == 0) 
    {
        write_query_empty(command_number);
        return;
    }

    char **nearest_users = NULL;
    if (recommender_function == 0)
    {
        nearest_users = recomendaUtilizadores(user_id_recom, matrix, users, genres, users_num, MAX_GENRE, number_of_users_recommended);
        // Use LI3 team reccommender function
    }
    else
    {
        nearest_users = recommendsUsers(user_id_recom, matrix, users, users_num, MAX_GENRE, number_of_users_recommended);
        // Use our recommender function
    }

    if (nearest_users == NULL)
    {
        write_query_empty(command_number);
        return;
    }

    for (int i = 0; i < number_of_users_recommended; i++)
    {
        if (nearest_users[i] != NULL)
        {
            char *elems[1];
            elems[0] = nearest_users[i];
            write_query(command_number, 1, query_format, elems);
        }
    }

    if (recommender_function == 1) 
    {
        for (int k = 0; k < number_of_users_recommended; k++) free(nearest_users[k]);
    }
    free(nearest_users);
}