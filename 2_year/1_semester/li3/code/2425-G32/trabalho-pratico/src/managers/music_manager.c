#include <stdlib.h>
#include <glib.h>
#include <stdio.h>
#include "managers/music_manager.h"
#include "utils/syntax_validator.h"
#include "utils/logic_validator.h"
#include "entities/music.h"

#define MAX_FIELDS 8
static GHashTable *music_hash;
static GHashTableIter iter;
static gpointer key, value;

void get_musics_list_fields(int *field_list)
{
    int temp[MAX_FIELDS] = {0, 0, 1, 0, 0, 0, 0, 0};
    for (int i = 0; i < MAX_FIELDS; i++) field_list[i] = temp[i];
}

int validate_music(char **fields)
{
    return ((music_id_artist_validation(fields[2]) == 1) || (album_id_music_validation(fields[3]) == 1) || (duration_validation(fields[4]) == 1));
}

void init_music_holder(void)
{
    music_hash = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_music_wrapper);
    if (music_hash == NULL)
    {
        exit(EXIT_FAILURE);
    }
}

void free_music_data(void)
{
    g_hash_table_destroy(music_hash);
}

void add_music_to_holder(char **fields)
{
    music *mus = create_music(fields[0], fields[2], fields[3], fields[4], fields[5]); // Only save id, title, artist_id, album_id, duration, genre
    char *id_copy = strdup(fields[0]);
    g_hash_table_insert(music_hash, id_copy, mus);
}

music *music_by_id(char *id)
{
    music *m = g_hash_table_lookup(music_hash, id);
    return m;
}

int music_search(char *id)
{
    music *m = music_by_id(id);
    if (m == NULL)
    {
        return 1;
    }

    return 0;
}

char *get_music_genre_by_id(char *id)
{
    music *m = music_by_id(id);
    // Its safe to ignore the case that the music was not found because the previous function is called before

    return strdup(get_music_genre(m));
}

char *get_artist_id_by_id(char *id)
{
    music *m = music_by_id(id);

    return strdup(get_music_id_artists(m));
}

void iter_music_init(void)
{
    g_hash_table_iter_init(&iter, music_hash);
}

int get_next_music_entry(char **data)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        data[0] = strdup(get_music_id_artists(value));
        data[1] = strdup(get_music_duration(value));
        return 1;
    }
    else
    {
        return 0;
    }
}

void get_music_data_for_q6(char *id, char **data)
{
    music *m = music_by_id(id);
    // Its safe to ignore the case that the artist was not found because the previous function is called before
    
    data[0] = strdup(get_music_id_artists(m));
    data[1] = strdup(get_music_album_id(m));
    data[2] = strdup (get_music_genre(m));
}