#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "entities/artist.h"

typedef struct artist
{
    char *id;                 /**< Unique identifier for the artist. */
    char *name;               /**< Name of the artist. */
    char *recipe_per_stream;  /**< Artist's earnings per stream. */
    char *id_constituents;    /**< Constituent IDs of the artist. */
    char *country;            /**< Artist's country of origin. */
    char *type;               /**< Type or genre of the artist. */
} artist;

artist *create_artist(char *id, char *name, char *recipe_per_stream, char *id_constituents, char *country, char *type)
{
    // Allocate memory for the new artist
    artist *new_artist = malloc(sizeof(artist));
    if (new_artist == NULL)
    {
        perror("Cannot allocate memory for new artist");
        return NULL;
    }

    // Allocate memory and copy id
    new_artist->id = strdup(id);
    if (new_artist->id == NULL)
    {
        perror("Cannot allocate memory for artist id");
        free(new_artist);
        return NULL;
    }

    // Allocate memory and copy name
    new_artist->name = strdup(name);
    if (new_artist->name == NULL)
    {
        perror("Cannot allocate memory for artist name");
        free(new_artist->id);
        free(new_artist);
        return NULL;
    }

    new_artist->recipe_per_stream = strdup(recipe_per_stream);
    if (new_artist->recipe_per_stream == NULL)
    {
        perror("Cannot allocate memory for artist reciper per stream");
        free(new_artist->id);
        free(new_artist->name);
        free(new_artist);
        return NULL;
    }

    // Copy of id_constituents Single-Linked List
    new_artist->id_constituents = strdup(id_constituents);
    if (new_artist->id_constituents == NULL)
    {
        perror("Cannot allocate memory for artist id constituents");
        free(new_artist->id);
        free(new_artist->name);
        free(new_artist->recipe_per_stream);
        free(new_artist);
        return NULL;
    }

    // Allocate memory and copy country
    new_artist->country = strdup(country);
    if (new_artist->country == NULL)
    {
        perror("Cannot allocate memory for artist country");
        free(new_artist->id);
        free(new_artist->name);
        free(new_artist->recipe_per_stream);
        free(new_artist->id_constituents);
        free(new_artist);
        return NULL;
    }

    // Allocate memory and copy artist type
    new_artist->type = strdup(type);
    if (new_artist->type == NULL)
    {
        perror("Cannot allocate memory for artist type");
        free(new_artist->id);
        free(new_artist->name);
        free(new_artist->recipe_per_stream);
        free(new_artist->id_constituents);
        free(new_artist->country);
        free(new_artist);
        return NULL;
    }

    return new_artist;
}

void free_artist(artist *a)
{
    if (a == NULL) return;
    free(a->id);
    free(a->name);
    free(a->recipe_per_stream);
    free(a->id_constituents);
    free(a->country);
    free(a->type);
    free(a);
}


void free_artist_wrapper(void *data)
{
    free_artist((artist *)data);
}

const char *get_artist_id(artist *a)
{
    return a->id;
}

const char *get_artist_name(artist *a)
{
    return a->name;
}

const char *get_artist_type(artist *a)
{
    return a->type;
}

const char *get_artist_country(artist *a)
{
    return a->country;
}

const char *get_artist_id_constituents(artist *a)
{
    return a->id_constituents;
}

const char *get_artist_recipe_per_stream(artist *a)
{
    return a->recipe_per_stream;
}