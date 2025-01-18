#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include "queries/query_four.h"
#include "utils/query_helper.h"
#include "managers/history_manager.h"
#include "managers/music_manager.h"
#include "managers/artist_manager.h"
#include "output/query_output.h"

#define MAX_WEEKS 500

static GSList *weeks_pos[MAX_WEEKS] = {NULL};

typedef struct artist_lt
{
    char *id_artist;  /**< Unique identifier for the artist (string). */
    int duration;     /**< Duration associated with the artist (in seconds or another relevant unit). */
    int top10;        /**< Number of times the artist appeared in the top 10. */
} artist_lt;



void init_and_populate_query_four_data(void)
{
    GHashTable *weeks_pre[MAX_WEEKS] = {NULL};
    init_hash_tables_with_artist_lt(weeks_pre);

    for (int i = 0; i < MAX_WEEKS; i++)
    {
        GHashTable *table = weeks_pre[i];
        if (table != NULL)
        {
            GList *list = g_hash_table_get_values(table);
            list = g_list_sort(list, compare_artist_lt);
            int counter = 0;
            GSList *new = NULL;
            for (GList *iter = list; iter != NULL && counter < 10; iter = iter->next, counter++)
            {
                artist_lt *artist = (artist_lt *)iter->data;
                artist_lt *new_artist = create_artist_lt(artist->id_artist, artist->duration, 0);
                new = g_slist_append(new, new_artist);
            }

            g_list_free(list);
            g_hash_table_destroy(table);

            weeks_pos[i] = new;
        }
    }
}

void init_hash_tables_with_artist_lt(GHashTable *weeks_pre[]) 
{
    char *data[3];
    iter_history_init();
    while (get_next_history_top_data(data) == 1) 
    {
        char *artists_id, *saveptr, *token;
        artists_id = get_artist_id_by_id(data[0]);

        token = strtok_r(artists_id, ",", &saveptr);
        while (token != NULL) 
        {
            process_artist_lt(weeks_pre, token, data);
            token = strtok_r(NULL, ",", &saveptr);
        }

        free(artists_id);
        for (int i = 0; i < 3; i++) if (data[i]) free(data[i]);
    }
}

void process_artist_lt(GHashTable *weeks_pre[], char *artist_id, char **data)
{
    char *timestamp = data[1];
    char *duration = data[2];

    char *listening_moment = strchr(timestamp, ' ');
    listening_moment++;

    int listening_moment_time = convert_duration_to_seconds(listening_moment);
    int duration_time = convert_duration_to_seconds(duration);
    int ind1 = week_to_ind(timestamp);

    if (listening_moment_time + duration_time > 86400) // Seconds in a day 86400 = 24 * 60 * 60
    {
        char *next_day = add_one_day(timestamp);
        int ind2 = week_to_ind(next_day);

        if (ind1 != ind2) 
        {
            int first_time = 86400 - listening_moment_time;
            int second_time = duration_time - first_time;
            add_to_weeks_pre_hash(weeks_pre, artist_id, first_time, ind1);
            add_to_weeks_pre_hash(weeks_pre, artist_id, second_time, ind2);
        }
        else
        {
            add_to_weeks_pre_hash(weeks_pre, artist_id, duration_time, ind1);
        }

        free(next_day);
    }
    else
    {
        add_to_weeks_pre_hash(weeks_pre, artist_id, duration_time, ind1);
    }
}

void add_to_weeks_pre_hash(GHashTable *weeks_pre[], char *artist_id, int duration, int index)
{
    GHashTable *table = weeks_pre[index];
    if (table == NULL) 
    {
        table = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_artist_lt_wrapper);
        weeks_pre[index] = table;
    }

    artist_lt *check = g_hash_table_lookup(table, artist_id);
    if (check == NULL) 
    {
        artist_lt *new = create_artist_lt(artist_id, duration, 0);
        g_hash_table_insert(table, strdup(artist_id), new);
    }
    else 
    {
        check->duration += duration;
    } 
}

void free_artist_lt_wrapper(void *data)
{
    artist_lt *lt = (artist_lt *)data;
    if (lt == NULL) return;
    free(lt->id_artist);
    free(lt);
}

int compare_artist_lt(gconstpointer a, gconstpointer b)
{
    const artist_lt *artist_a = (const artist_lt *)a;
    const artist_lt *artist_b = (const artist_lt *)b;

    if (artist_a->duration > artist_b->duration)
    {
        return -1;
    }
    else if (artist_a->duration < artist_b->duration)
    {
        return 1;
    }

    return strcmp(artist_a->id_artist, artist_b->id_artist);
}

artist_lt *create_artist_lt(char *id, int duration, int top10)
{
    artist_lt *new = malloc(sizeof(artist_lt));
    if (new == NULL)
    {
        perror("Cannot allocate memory for new artist_lt");
        return NULL;
    }

    new->id_artist = strdup(id);
    if (new->id_artist == NULL)
    {
        perror("Cannot allocate memory for id_artist");
        free(new);
        return NULL;
    }

    new->duration = duration;
    new->top10 = top10;
    return new;
}

void free_query_four_data(void)
{
    for (int i = 0; i < MAX_WEEKS; i++)
    {
        g_slist_free_full(weeks_pos[i], free_artist_lt_wrapper);
    }
}

int compare_artist_by_id(gconstpointer a, gconstpointer b)
{
    const artist_lt *artist_a = (const artist_lt *)a;
    const char *id = (const char *)b;
    return strcmp(artist_a->id_artist, id);
}


void run_query_four(char *start, char *end, int command_number, int query_format)
{
    GSList *join = NULL;
    int beginning = MAX_WEEKS - 1, finish = 0;

    if (start != NULL && end != NULL)
    {
        beginning = week_to_ind(start);
        finish = week_to_ind(end);
    }

    while (beginning >= finish)
    {
        if (weeks_pos[beginning] != NULL)
        {
            for (GSList *iter = weeks_pos[beginning]; iter != NULL; iter = iter->next)
            {
                artist_lt *artist = (artist_lt *)iter->data;
                GSList *node = g_slist_find_custom(join, artist->id_artist, compare_artist_by_id);

                if (node == NULL)
                {
                    artist_lt *new = create_artist_lt(artist->id_artist, artist->duration, 1);
                    join = g_slist_prepend(join, new);
                }
                else
                {
                    artist_lt *current = (artist_lt *)node->data;
                    current->top10 += 1;
                }
            }
        }

        beginning--;
    }

    if (join == NULL)
    {
        write_query_empty(command_number);
        return;
    }

    artist_lt *prefix = NULL;
    for (GSList *iter = join; iter != NULL; iter = iter->next)
    {
        artist_lt *artist = (artist_lt *)iter->data;
        if (prefix == NULL || artist->top10 > prefix->top10 || (artist->top10 == prefix->top10 && strcmp(artist->id_artist, prefix->id_artist) < 0))
        {
            prefix = artist;
        }
    }

    char *data[3];
    get_artist_resume_q4A(prefix->id_artist, data);
    data[0] = strdup(prefix->id_artist);
    data[2] = malloc(11);
    snprintf(data[2], 11, "%d", prefix->top10);

    write_query(command_number, 3, query_format, data);

    for (int i = 0; i < 3; i++) if (data[i]) free(data[i]);
    g_slist_free_full(join, free_artist_lt_wrapper);
}