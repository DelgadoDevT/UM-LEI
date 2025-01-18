#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "queries/query_three.h"
#include "output/file_error_output.h"
#include "output/query_output.h"
#include "utils/query_helper.h"
#include "managers/music_manager.h"
#include "managers/user_manager.h"

#define MAX_AGE 150
static GSList *music_popularity_arr[MAX_AGE];

typedef struct musicPopularity
{
    char *genre; ///< The genre of the music
    int likes;   ///< The number of likes for this genre
} musicPopularity;

int compare_genre_popularity(gconstpointer a, gconstpointer b)
{
    musicPopularity *mpopa = (musicPopularity*)a;
    musicPopularity *mpopb = (musicPopularity*)b;

    int mpopa_likes = mpopa->likes;
    int mpopb_likes = mpopb->likes;

    if (mpopa_likes > mpopb_likes)
    {
        return -1;
    }
    else if (mpopa_likes < mpopb_likes)
    {
        return 1;
    }
    else
    {
        return strcmp(mpopa->genre, mpopb->genre);
    }
}

int compare_genre(gconstpointer a, gconstpointer b)
{
    musicPopularity *mpopa = (musicPopularity*)a;
    const char *genre1 = mpopa->genre;
    const char *genre2 = (const char *)b;

    return strcmp(genre1, genre2);
}

musicPopularity *create_musicPopularity(char *genre, int likes)
{
    musicPopularity *new_musicPopularity = malloc(sizeof(musicPopularity));
    if (new_musicPopularity == NULL)
    {
        perror("Cannot allocate memory for new_musicPopularity");
        return NULL;
    }

    new_musicPopularity->genre = strdup(genre);
    if (new_musicPopularity->genre == NULL)
    {
        perror("Cannot allocate memory for genre");
        free(new_musicPopularity);
        return NULL;
    }

    new_musicPopularity->likes = likes;

    return new_musicPopularity;
}

void free_musicPopularity(musicPopularity *p)
{
    if (p == NULL) return;
    free(p->genre);
    free(p);
}

void free_musicPopularity_wrapper(void *data)
{
    free_musicPopularity((musicPopularity *)data);
}

void free_query_three_data(void)
{
    for (int i = 0; i < MAX_AGE; i++)
    {
        g_slist_free_full(music_popularity_arr[i], free_musicPopularity_wrapper);
    }
}

void init_and_populate_query_three_data(void)
{
    for (int i = 0; i < MAX_AGE; i++)
    {
        music_popularity_arr[i] = NULL;
    }

    char *data[2];
    iter_user_init();
    while (get_next_user_entry(data) == 1)
    {
        int user_age = birth_date_to_age(data[1]);
        GSList *current = music_popularity_arr[user_age];
        char *token = strtok(data[0], ",");
        char *id;

        while (token != NULL)
        {
            id = token;
            char *genre = get_music_genre_by_id(id);

            GSList *node =  g_slist_find_custom(current, (gpointer)genre, compare_genre);
            if (node != NULL)
            {
                ((musicPopularity *)(node->data))->likes++;
                free(genre);
            }
            else
            {
                musicPopularity *mpop = create_musicPopularity(genre, 1);
                current = g_slist_append(current, mpop);
                free(genre);
            }

            token = strtok(NULL, ",");
        }

        music_popularity_arr[user_age] = current;

        free(data[0]);
        free(data[1]);
    }
}

void run_query_three(char *min_age_s, char *max_age_s, int command_number, int format_controler)
{
    int min_age = atoi(min_age_s);
    int max_age = atoi(max_age_s);

    GSList *music_popularity_single_linked = NULL;
    for (int i = min_age; i <= max_age; i++)
    {
        for (GSList *current = music_popularity_arr[i]; current != NULL; current = current->next)
        {
            char *genre = (((musicPopularity *)current->data))->genre;
            int likes = (((musicPopularity *)current->data))->likes;

            GSList *node = g_slist_find_custom(music_popularity_single_linked, (gpointer)genre, compare_genre);
            if (node == NULL)
            {
                char *genre_cp = strdup(genre);
                musicPopularity *mpop = create_musicPopularity(genre_cp, likes);
                music_popularity_single_linked = g_slist_append(music_popularity_single_linked, mpop);
                free(genre_cp);
            }
            else
            {
                ((musicPopularity *)(node->data))->likes += likes;
            }
        }
    }

    music_popularity_single_linked = g_slist_sort(music_popularity_single_linked, compare_genre_popularity);
    prepare_query_three_print(music_popularity_single_linked, command_number, format_controler);

    g_slist_free_full(music_popularity_single_linked, free_musicPopularity_wrapper);
}

void prepare_query_three_print(GSList *music_popularity_single_linked, int command_number, int format_controler)
{
    char *data[2];
    int control = 0;

    for (GSList *current = music_popularity_single_linked; current != NULL; current = current->next)
    {
        data[0] = ((musicPopularity *)(current->data))->genre;

        int likes = ((musicPopularity *)(current->data))->likes; 
        char likes_str[count_digits(likes) + 1];
        snprintf(likes_str, sizeof(likes_str), "%d", likes);
        data[1] = likes_str;

        write_query(command_number, 2, format_controler, data);
        control = 1;
    }

    if (control == 0) write_query_empty(command_number);
}