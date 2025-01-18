#include <stdlib.h>
#include <string.h>
#include <glib.h>
#include "managers/album_manager.h"
#include "entities/album.h"

#define MAX_FIELDS 8
static GHashTable *album_hash;
static GHashTableIter iter;
static gpointer key, value;

void get_album_list_fields(int *field_list)
{
    int temp[MAX_FIELDS] = {0, 0, 1, 0, 1, 0, 0, 0};
    for (int i = 0; i < MAX_FIELDS; i++) field_list[i] = temp[i];
}

int validate_album(char **fields)
{
    (void)fields;
    return 0;
}

void init_album_holder(void)
{
    album_hash = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_album_wrapper);
    if (album_hash == NULL)
    {
        exit(EXIT_FAILURE);
    }
}

void free_album_data(void)
{
    g_hash_table_destroy(album_hash);
}

void add_album_to_holder(char **fields)
{
    album *alb = create_album(fields[0], fields[2]); // Only save id, title, id_artists and producers
    char *id_copy = strdup(fields[0]);
    g_hash_table_insert(album_hash, id_copy, alb);
}

int album_search(char *id)
{
    album *a = g_hash_table_lookup(album_hash, id);
    if (a == NULL)
    {
        return 1;
    }

    return 0;
}

void iter_album_init(void)
{
    g_hash_table_iter_init(&iter, album_hash);
}

int get_album_next_and_verify_artist(char **artists_id)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        *artists_id = strdup(get_album_artists_id(value));
        return 1;
    }
    else
    {
        return 0;
    }
}