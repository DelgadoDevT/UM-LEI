#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "queries/query_two.h"
#include "output/file_error_output.h"
#include "output/query_output.h"
#include "utils/query_helper.h"
#include "managers/music_manager.h"
#include "managers/artist_manager.h"

static GHashTable *duration_hash;
static GSList *duration_linked;

typedef struct discography
{
    char *id;              /**< The ID of the artist. */
    int durations_sum;     /**< The total duration of the artist's music in seconds. */
} discography;

discography *create_discography(char *id, int durations_sum)
{
    discography *new_discography = malloc(sizeof(discography));
    if (new_discography == NULL)
    {
        perror("Cannot allocate memory for new_discography");
        return NULL;
    }

    new_discography->id = strdup(id);
    if (new_discography->id == NULL)
    {
        perror("Cannot allocate memory for id");
        free(new_discography);
        return NULL;
    }

    new_discography->durations_sum = durations_sum;

    return new_discography;
}

void free_discography(discography *d)
{
    if (d == NULL) return;
    free(d->id);
    free(d);
}

void free_discography_wrapper(void *data)
{
    free_discography((discography *)data);
}

int sort_by_duration(gconstpointer a, gconstpointer b)
{
    const discography *disc_a = (const discography *)a;
    const discography *disc_b = (const discography *)b;

    int disc_a_duration = ((discography *)disc_a)->durations_sum;
    int disc_b_duration = ((discography *)disc_b)->durations_sum;

    if (disc_a_duration > disc_b_duration)
    {
        return -1;
    }
    else if (disc_a_duration < disc_b_duration)
    {
        return 1;
    }
    else
    {
        return strcmp(((discography *)disc_a)->id, ((discography *)disc_b)->id);
    }
}

void init_and_populate_query_two_data(void)
{
    duration_hash = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_discography_wrapper);
    if (duration_hash == NULL)
    {
        exit(EXIT_FAILURE);
    }

    char *data[2];
    iter_music_init();
    while (get_next_music_entry(data) == 1)
    {
        int duration_sec = convert_duration_to_seconds(data[1]);
        char *token = strtok(data[0], ",");
        char *id;

        while (token != NULL)
        {
            id = token;
            discography *disc = g_hash_table_lookup(duration_hash, id);
            if (disc == NULL)
            {
                disc = create_discography(token, duration_sec);
                g_hash_table_insert(duration_hash, strdup(id), disc);
            }
            else
            {
                disc->durations_sum += duration_sec;
            }

            token = strtok(NULL, ",");
        }
        free(data[0]);
        free(data[1]);
    }

    init_and_populate_duration_linked();
    g_hash_table_destroy(duration_hash);
}

void init_and_populate_duration_linked(void)
{
    duration_linked = NULL;

    GHashTableIter iter;
    gpointer key, value;

    g_hash_table_iter_init(&iter, duration_hash);
    while (g_hash_table_iter_next(&iter, &key, &value))
    {
        discography *current_discography = value;
        char *id = strdup(current_discography->id);
        int duration_sum = current_discography->durations_sum;

        discography *disc = create_discography(id, duration_sum);
        duration_linked = g_slist_append(duration_linked, disc);
        free(id);
    }

    duration_linked = g_slist_sort(duration_linked, sort_by_duration);
}

void free_query_two_data(void)
{
    g_slist_free_full(duration_linked, free_discography_wrapper);
}

void run_query_two(char *top_N, char *country, int command_number, int format_controler)
{
    GSList *duration_linked_copy = NULL;
    GSList *current = duration_linked;
    int i_top_N = atoi(top_N);

    if (country == NULL)
    {
        for (int i = 0; i < i_top_N && current != NULL; i++, current = current->next)
        {
            duration_linked_copy = g_slist_append(duration_linked_copy, current->data);
        }

        prepare_query_two_print(duration_linked_copy, command_number, format_controler);
    }
    else
    {
        for (int i = 0; i < i_top_N && current != NULL; current = current->next)
        {
            if (is_same_country_artist(((discography *)(current->data))->id, country) == 0)
            {
                duration_linked_copy = g_slist_append(duration_linked_copy, current->data);
                i++;
            }
        }

        prepare_query_two_print(duration_linked_copy, command_number, format_controler);
    }

    g_slist_free(duration_linked_copy);
}

void prepare_query_two_print(GSList *duration_linked_copy, int command_number, int format_controler)
{
    char *formatted_duration = malloc(sizeof(char) * 9); // hh:mm:ss (8 + 1 for the null terminator)
    char *data[4];
    GSList *current = duration_linked_copy;
    int control = 0;

    while (current != NULL)
    {
        get_artist_resume_q2(((discography *)(current->data))->id, data);

        convert_seconds_to_duration((((discography *)(current->data))->durations_sum), formatted_duration, 9);
        data[2] = formatted_duration;

        write_query(command_number, 4, format_controler, data);
        control = 1;

        free(data[0]);
        free(data[1]);
        free(data[3]);
        current = current->next;
    }

    free(formatted_duration);

    if (control == 0) write_query_empty(command_number);
}