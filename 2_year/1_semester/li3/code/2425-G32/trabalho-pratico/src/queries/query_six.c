#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <glib.h>
#include "queries/query_six.h"
#include "output/query_output.h"
#include "managers/history_manager.h"
#include "managers/artist_manager.h"
#include "managers/music_manager.h"
#include "managers/album_manager.h"
#include "managers/user_manager.h"
#include "utils/parse_helper.h"
#include "utils/query_helper.h"

static GHashTable *user_entrys;

typedef struct entry_on_history
{
    char *user_id;          ///< The unique identifier for the user.
    GSList *history_ids;    ///< A list of history IDs associated with the user.
} entry_on_history;

typedef struct entity_count
{
    char *entity_id; ///< The unique identifier for the entity (e.g., album, genre).
    int count;       ///< The accumulated count or value for the entity (e.g., listening time).
} entity_count;

typedef struct artist_pack
{
    int listening_time;   ///< The total listening time (in seconds) for this artist.
    char *artist_id;      ///< The unique identifier for the artist.
    GSList *musics;       ///< A list of `entity_count` objects representing individual music entries associated with the artist.
} artist_pack;

void free_entry_on_history_wrapper(void *data)
{
    entry_on_history *en = (entry_on_history *)data;
    free(en->user_id);
    g_slist_free_full(en->history_ids, free);
    free(en);
}

void free_entity_count_wrapper(void *data)
{
    entity_count *ec = (entity_count *)data;
    free(ec->entity_id);
    free(ec);
}

void free_artist_pack_wrapper(void *data)
{
    artist_pack *ap = (artist_pack *)data;
    free(ap->artist_id);
    g_slist_free_full(ap->musics, free_entity_count_wrapper);
    free(ap);
}

entity_count *create_entity_count(char *id, int count)
{
    entity_count *new = malloc(sizeof(entity_count));
    new->entity_id = strdup(id);
    new->count = count;
    return new;
}

void init_and_populate_query_six_data(void)
{
    char *data[2];
    user_entrys = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_entry_on_history_wrapper);

    iter_history_init();
    while (get_next_history_q6_ids(data) == 1)
    {
        char *id_user = data[0], *id_history = data[1];

        entry_on_history *en = g_hash_table_lookup(user_entrys, id_user);
        if (en == NULL)
        {
            entry_on_history *new = create_entry_on_history(id_user, id_history);
            g_hash_table_insert(user_entrys, strdup(id_user), new);
        }
        else
        {
            en->history_ids = g_slist_prepend(en->history_ids, strdup(id_history));
        }

        for (int i = 0; i < 2; i++) if (data[i]) free(data[i]);
    }
}

entry_on_history *create_entry_on_history(char *id_user, char *id_history)
{
    entry_on_history *en = malloc(sizeof(entry_on_history));
    en->user_id = strdup(id_user);
    
    GSList *list = NULL;
    list = g_slist_prepend(list, strdup(id_history));
    en->history_ids = list;

    return en;
}

void free_query_six_data(void)
{
    g_hash_table_destroy(user_entrys);
}

char* get_artist_with_max_listening_time(GSList *artists)
{
    if (artists == NULL || g_slist_length(artists) == 0) return NULL;

    artist_pack *max_artist = (artist_pack *)artists->data;
    for (GSList *iter = artists->next; iter != NULL; iter = iter->next) 
    {
        artist_pack *current_artist = (artist_pack *)iter->data;
        if (current_artist->listening_time > max_artist->listening_time || (current_artist->listening_time == max_artist->listening_time && strcmp(current_artist->artist_id, max_artist->artist_id) < 0)) 
        {
            max_artist = current_artist;
        }
    }

    return max_artist->artist_id;
}

char* get_most_frequent_entity(GSList *list, int order)
{
    if (list == NULL || g_slist_length(list) == 0) return NULL;

    int max_count = 0;
    char *most_frequent = NULL;

    for (GSList *iter = list; iter != NULL; iter = iter->next) 
    {
        entity_count *entry = (entity_count *)iter->data;
        char *key = entry->entity_id;
        int count = entry->count;

        if (count > max_count || (count == max_count && (order == 0 ? strcmp(key, most_frequent) < 0 : strcmp(key, most_frequent) > 0))) 
        {
            max_count = count;
            most_frequent = key;
        }
    }

    return most_frequent;
}

void fill_print_data_main(char **query_data, GSList *albuns, GSList *days, GSList *hours, GSList *genres, GSList *artists_info, int total_listening_time, int total_different_musics)
{
    query_data[0] = malloc(11);
    convert_seconds_to_duration(total_listening_time, query_data[0], 11);
    query_data[1] = malloc(11);
    snprintf(query_data[1], 11, "%d", total_different_musics);
    query_data[2] = strdup(get_artist_with_max_listening_time(artists_info));

    query_data[3] = strdup(get_most_frequent_entity(days, 1));
    query_data[6] = strdup(get_most_frequent_entity(hours, 0));
    query_data[4] = strdup(get_most_frequent_entity(genres, 0));
    query_data[5] = strdup(get_most_frequent_entity(albuns, 0));
}

int fill_print_data_second(char **data_artists, GSList **artists_info)
{
    if (*artists_info == NULL) return 1;

    artist_pack *cond = (artist_pack *)(*artists_info)->data;
    if (cond == NULL || g_slist_length(cond->musics) == 0) return 1;

    data_artists[0] = strdup((get_artist_with_max_listening_time(*artists_info)));
    
    for (GSList *iter = *artists_info; iter != NULL; iter = iter->next)
    {
        artist_pack *entry = (artist_pack *)iter->data;
        if (strcmp(entry->artist_id, data_artists[0]) == 0) 
        {
            data_artists[1] = malloc(11);
            data_artists[2] = malloc(11);
            snprintf(data_artists[1], 11, "%d", g_slist_length(entry->musics));
            convert_seconds_to_duration(entry->listening_time, data_artists[2], 11);

            free_artist_pack_wrapper(entry);
            *artists_info = g_slist_remove_link(*artists_info, iter);  
            g_slist_free_1(iter);
            break;
        }
    }

    return 0;
}

void run_query_six(char *user_id, char *year, char *number_of_artists, int command_number, int query_format)
{
    entry_on_history *entry = g_hash_table_lookup(user_entrys, user_id);
    if (user_search(user_id) == 1 || entry == NULL) 
    {
        write_query_empty(command_number);
        return;
    }

    GSList *albuns = NULL, *days = NULL, *hours = NULL, *genres = NULL, *artists_info = NULL;
    int total_listening_time = 0, total_different_musics = 0;
    int check = fill_query_six_data(entry, year, &albuns, &days, &hours, &genres, &artists_info, &total_listening_time, &total_different_musics);
    if (check == 0)
    {
        write_query_empty(command_number);
        return;
    }

    char *query_data[7];
    fill_print_data_main(query_data, albuns, days, hours, genres, artists_info, total_listening_time, total_different_musics);
    write_query(command_number, 7, query_format, query_data);

    if (number_of_artists != NULL) 
    {
        int number_of_artists_num = atoi(number_of_artists);
        for (int j = 0; j < number_of_artists_num; j++)
        {
            char *data_artists[3];
            int check2 = fill_print_data_second(data_artists, &artists_info);
            if (check2 == 0) write_query(command_number, 3, query_format, data_artists);
            if (check2 == 1) 
            {
                for (int l = 0; l < 3; l++) if (data_artists[l]) free(data_artists[l]);
                break;
            }
        }
    }

    for (int p = 0; p < 7; p++) if (query_data[p]) free(query_data[p]);

    g_slist_free_full(albuns, free_entity_count_wrapper);
    g_slist_free_full(days, free_entity_count_wrapper);
    g_slist_free_full(hours, free_entity_count_wrapper);
    g_slist_free_full(genres, free_entity_count_wrapper);
    g_slist_free_full(artists_info, free_artist_pack_wrapper);
}

entity_count *findEntityById(GSList *list, char *id) 
{
    for (GSList *iter = list; iter != NULL; iter = iter->next) 
    {
        entity_count *current = (entity_count *)iter->data;
        if (strcmp(current->entity_id, id) == 0) return current;
    }
    return NULL;
}

artist_pack* find_artist_by_id(GSList *artists, char *artist_id) 
{
    for (GSList *iter = artists; iter != NULL; iter = iter->next) 
    {
        artist_pack *artist = (artist_pack *)iter->data;
        if (strcmp(artist->artist_id, artist_id) == 0) return artist;
    }
    return NULL;
}

int fill_query_six_data(entry_on_history *entry, char *year, GSList **albuns, GSList **days, GSList **hours, GSList **genres, GSList **artists_info, int *total_listening_time, int *total_different_musics) 
{
    int check = 0;

    for (GSList *iter = entry->history_ids; iter != NULL; iter = iter->next) 
    {
        char *history_id = (char *)iter->data;
        char *history_data[3];
        get_history_data_for_q6(history_id, history_data);
        
        if (is_same_year(year, history_data[1])) 
        {
            char *music_id = history_data[0];
            char *music_data[3];
            get_music_data_for_q6(music_id, music_data);
            int duration_in_seconds = convert_duration_to_seconds(history_data[2]);
            (*total_listening_time) += duration_in_seconds;
            (*total_different_musics) += 1;
            
            char *album_id = music_data[1];
            entity_count *album_entity_count = findEntityById(*albuns, album_id);
            if (!album_entity_count)
            {
                entity_count *album_entity = create_entity_count(album_id, duration_in_seconds);
                *albuns = g_slist_prepend(*albuns, album_entity);
            }
            else album_entity_count->count += duration_in_seconds;

            char *music_genre = music_data[2];
            entity_count *genre_entity_count = findEntityById(*genres, music_genre);
            if (!genre_entity_count) 
            {
                entity_count *genre_entity = create_entity_count(music_genre, duration_in_seconds);
                *genres = g_slist_prepend(*genres, genre_entity);
            }
            else genre_entity_count->count += duration_in_seconds;

            char *timestamp = history_data[1];
            char *date_part = strtok(timestamp, " ");
            char *hour_part = strtok(NULL, " ");
            char *hour = strtok(hour_part, ":");

            entity_count *day_entity_count = findEntityById(*days, date_part);
            if (!day_entity_count)
            {
                entity_count *day_entity = create_entity_count(date_part, 1);
                *days = g_slist_prepend(*days, day_entity);
            }
            else day_entity_count->count += 1;

            entity_count *hour_entity_count = findEntityById(*hours, hour);
            if (!hour_entity_count)
            {
                entity_count *hour_entity = create_entity_count(hour, duration_in_seconds);
                *hours = g_slist_prepend(*hours, hour_entity);
            }
            else hour_entity_count->count += duration_in_seconds;

            char *token;
            char *save_ptr;
            token = strtok_r(music_data[0], ",", &save_ptr);
            while (token != NULL) 
            {
                artist_pack *artist = find_artist_by_id(*artists_info, token);
                if (!artist) 
                {
                    artist = malloc(sizeof(artist_pack));
                    artist->artist_id = strdup(token);
                    artist->listening_time = duration_in_seconds;
                    GSList *musics_list = NULL;
                    entity_count *music_entity = create_entity_count(token, 1);
                    musics_list = g_slist_prepend(musics_list, music_entity);
                    artist->musics = musics_list;
                    *artists_info = g_slist_prepend(*artists_info, artist);
                } 
                else 
                {
                    entity_count *music = findEntityById(artist->musics, token);
                    if (!music) 
                    {
                        entity_count *music_entity = create_entity_count(token, 1);
                        artist->musics = g_slist_prepend(artist->musics, music_entity);
                    }
                    else music->count++;
                }
                token = strtok_r(NULL, ",", &save_ptr);
            }

            check = 1;
            for (int i = 0; i < 3; i++) if (music_data[i]) free(music_data[i]);
        }

        for (int i = 0; i < 3; i++) if (history_data[i]) free(history_data[i]);
    }

    return check;
}

