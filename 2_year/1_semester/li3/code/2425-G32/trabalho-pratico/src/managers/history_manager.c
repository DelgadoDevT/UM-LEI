#include <stdlib.h>
#include <glib.h>
#include "managers/history_manager.h"
#include "utils/syntax_validator.h"
#include "utils/query_helper.h"
#include "entities/history.h"

#define MAX_FIELDS 8
static GHashTable *history_hash;
static GHashTableIter iter;
static gpointer key, value;

void get_history_list_fields(int *field_list)
{
    int temp[MAX_FIELDS] = {0, 0, 0, 0, 0, 0, 0, 0};
    for (int i = 0; i < MAX_FIELDS; i++) field_list[i] = temp[i];
}

int validate_history(char **fields)
{
    return ((duration_validation(fields[4]) == 1) || plataform_validation(fields[5]) == 1);
}

void init_history_holder(void)
{
    history_hash = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_history_wrapper);
    if (history_hash == NULL)
    {
        exit(EXIT_FAILURE);
    }
}

void free_history_data(void)
{
    g_hash_table_destroy(history_hash);
}

void add_history_to_holder(char **fields)
{
    history *his = create_history(fields[1], fields[2], fields[3], fields[4]); // Save some fields (user_id, music_id, timestamp, duration)
    char *id_copy = strdup(fields[0]);
    g_hash_table_insert(history_hash, id_copy, his);
}

void iter_history_init(void)
{
    g_hash_table_iter_init(&iter, history_hash);
}

int get_next_history_music_id(char **music_id)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        *music_id = strdup(get_history_music_id(value));
        return 1;
    }
    else
    {
        return 0;
    }
}

int get_next_history_top_data(char **data)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        data[0] = strdup(get_history_music_id(value));
        data[1] = strdup(get_history_timestamp(value));
        data[2] = strdup(get_history_duration(value));
        return 1;
    }
    else
    {
        return 0;
    }
}

int get_next_history_ids(char **data)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        data[0] = strdup(get_history_user_id(value));
        data[1] = strdup(get_history_music_id(value));
        return 1;
    }
    else
    {
        return 0;
    }
}

int get_next_history_q6_ids(char **data)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        data[0] = strdup(get_history_user_id(value));
        data[1] = strdup(key);
        return 1;
    }
    else
    {
        return 0;
    }
}

void get_history_data_for_q6(char *id, char **data)
{
    history *h = g_hash_table_lookup(history_hash, id);
    if (h == NULL) return;

    data[0] = strdup(get_history_music_id(h));
    data[1] = strdup(get_history_timestamp(h));
    data[2] = strdup(get_history_duration(h));
}