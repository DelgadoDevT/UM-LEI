#include <stdlib.h>
#include <glib.h>
#include "managers/artist_manager.h"
#include "utils/logic_validator.h"
#include "entities/artist.h"

#define MAX_FIELDS 8
static GHashTable *artist_hash;

void get_artists_list_fields(int *field_list)
{
    int temp[MAX_FIELDS] = {0, 0, 0, 0, 1, 0, 0, 0};
    for (int i = 0; i < MAX_FIELDS; i++) field_list[i] = temp[i];
}

int validate_artist(char **fields)
{
    return (artist_id_constituent_type_validation(fields[4], fields[6]) == 1);
}

void init_artist_holder(void)
{
    artist_hash = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_artist_wrapper);
    if (artist_hash == NULL)
    {
        exit(EXIT_FAILURE);
    }
}

void free_artist_data(void)
{
    g_hash_table_destroy(artist_hash);
}

void add_artist_to_holder(char **fields)
{
    artist *art = create_artist(fields[0], fields[1], fields[3], fields[4], fields[5], fields[6]); // Only save id, name, recipe_per_stream, id_list, country and type
    char *id_copy = strdup(fields[0]);
    g_hash_table_insert(artist_hash, id_copy, art);
}

artist *artist_by_id(char *id)
{
    artist *a = g_hash_table_lookup(artist_hash, id);
    return a;
}

int artist_search(char *id)
{
    artist *a = artist_by_id(id);
    if (a == NULL)
    {
        return 1;
    }

    return 0;
}

void get_artist_resume_q1A(char *id, char **data)
{
    artist *a = artist_by_id(id);

    data[0] = strdup(get_artist_recipe_per_stream(a));
    data[1] = strdup(get_artist_id_constituents(a));
    data[2] = strdup(get_artist_type(a));
}

void get_artist_resume_q1B(char *id, char **data)
{
    artist *a = artist_by_id(id);

    data[0] = strdup(get_artist_name(a));
    data[1] = strdup(get_artist_type(a));
    data[2] = strdup(get_artist_country(a));
}

int is_same_country_artist(char *id, char *country)
{
    artist *a = artist_by_id(id);
    return strcmp(get_artist_country(a), country);
}

void get_artist_resume_q2(char *id, char **data)
{
    artist *a = artist_by_id(id);
    // Its safe to ignore the case that the artist was not found because the previous function is called before

    data[0] = strdup(get_artist_name(a));
    data[1] = strdup(get_artist_type(a));
    data[3] = strdup(get_artist_country(a));
}

void get_artist_resume_q4A(char *id, char **data)
{
    artist *a = artist_by_id(id);

    data[1] = strdup(get_artist_type(a));
}