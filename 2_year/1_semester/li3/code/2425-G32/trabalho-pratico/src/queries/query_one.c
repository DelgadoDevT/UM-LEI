#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include "managers/user_manager.h"
#include "managers/music_manager.h"
#include "managers/album_manager.h"
#include "managers/artist_manager.h"
#include "managers/history_manager.h"
#include "queries/query_one.h"
#include "output/query_output.h"
#include "utils/query_helper.h"

static GHashTable *artist_status;

typedef struct a_status
{
    double recipe;   /**< The artist's total recipe value. */
    int album_num;   /**< The number of albums the artist has. */
} a_status;

void init_and_populate_artist_status_data(void)
{
    artist_status = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_artist_status_wrapper);
    calc_num_albums_individual();
    calc_total_recipe();
}

void calc_num_albums_individual(void)
{
    char *artists_id = NULL, *token;

    iter_album_init();
    while (get_album_next_and_verify_artist(&artists_id) == 1)
    {
        token = strtok(artists_id, ",");
        while (token != NULL)
        {
            a_status *ar = g_hash_table_lookup(artist_status, token);
            if (ar == NULL)
            {
                a_status *new = new_artist_status(0, 1);
                g_hash_table_insert(artist_status, strdup(token), new);
            }
            else
            {
                ar->album_num++;
            }

            token = strtok(NULL, ",");
        }
        free(artists_id);
    }
}

void calc_total_recipe(void)
{
    char *music_id = NULL, *token;

    iter_history_init();
    while (get_next_history_music_id(&music_id) == 1)
    {
        char *artists_id, *saveptr;
        artists_id = get_artist_id_by_id(music_id);

        token = strtok_r(artists_id, ",", &saveptr);
        while (token != NULL)
        {
            char *data[3];
            get_artist_resume_q1A(token, data);
            double recipe = strtod(data[0], NULL);

            a_status *ar = g_hash_table_lookup(artist_status, token);
            if (ar == NULL)
            {
                a_status *new = new_artist_status(recipe, 0);
                g_hash_table_insert(artist_status, strdup(token), new);
            }
            else ar->recipe += recipe;

            if (strcmp(data[2], "group") == 0)
            {
                int group_elems = count_group_elements(data[1]);
                char *list = strdup(data[1]), *token2, *saveptr2;
                token2 = strtok_r(list, ",", &saveptr2);
                while (token2 != NULL)
                {
                    a_status *ar2 = g_hash_table_lookup(artist_status, token2);
                    double single_recipe = (double)recipe / group_elems;
                    if (ar2 == NULL)
                    {
                        a_status *new = new_artist_status(single_recipe, 0);
                        g_hash_table_insert(artist_status, strdup(token2), new);
                    }
                    else
                    {
                        ar2->recipe += single_recipe;
                    }

                    token2 = strtok_r(NULL, ",", &saveptr2);
                }
                free(list);
            }

            token = strtok_r(NULL, ",", &saveptr);
            for (int i = 0; i < 3; i++) free(data[i]);
        }

        free(artists_id);
        free(music_id);
    }
}


a_status *new_artist_status(double recipe, int album_num)
{
    a_status *new = malloc(sizeof(a_status));
    new->recipe = recipe;
    new->album_num = album_num;
    return new;
}

void free_query_one_data(void)
{
    g_hash_table_destroy(artist_status);
}

void free_artist_status_wrapper(void *data)
{
    a_status *status = (a_status *)data;
    free(status);
}

void run_query_one(char *id, int command_number, int format_controler)
{
    if (user_search(id) == 0) user_handle(id, command_number, format_controler);
    else if (artist_search(id) == 0) artist_handle(id, command_number, format_controler);
    else write_query_empty(command_number);
}

void user_handle(char *id, int command_number, int format_controler)
{
    int age;
    char *data[5];
    get_user_resume(id, data);

    age = birth_date_to_age(data[3]);
    free(data[3]);
    data[3] = malloc(11); // Int size plus \0
    snprintf(data[3], 11, "%d", age);

    write_query(command_number, 5, format_controler, data);
    for (int i = 0; i < 5; i++) free(data[i]);
}

void artist_handle(char *id, int command_number, int format_controler)
{
    char *data[5];
    get_artist_resume_q1B(id, data);

    a_status *ar = g_hash_table_lookup(artist_status, id);
    data[3] = malloc(11);
    data[4] = malloc(11);
    if (ar != NULL)
    {
        snprintf(data[3], 11, "%d", ar->album_num);
        snprintf(data[4], 11, "%.2f", ar->recipe);
    }
    else
    {
        snprintf(data[3], 11, "%d", 0);
        snprintf(data[4], 11, "%.2f", 0.00);
    }

    write_query(command_number, 5, format_controler, data);
    for (int i = 0; i < 5; i++) free(data[i]);
}