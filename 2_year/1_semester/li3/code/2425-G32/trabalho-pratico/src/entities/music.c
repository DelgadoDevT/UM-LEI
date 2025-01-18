#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "entities/music.h"

typedef struct music
{
    char *id;           /**< Unique identifier for the music. */
    char *id_artists;   /**< List of artist IDs associated with the music. */
    char *album_id;     /**< Album ID that the music belongs to. */
    char *duration;     /**< Duration of the music track. */
    char *genre;        /**< Genre of the music. */
} music;

music *create_music(char *id, char *id_artists, char *album_id, char *duration, char *genre)
{
    // Allocate memory for the new music
    music *new_music = malloc(sizeof(music));
    if (new_music == NULL)
    {
        perror("Cannot allocate memory for new music");
        return NULL;
    }

    // Allocate memory and copy id
    new_music->id = strdup(id);
    if (new_music->id == NULL)
    {
        perror("Cannot allocate memory for music id");
        free(new_music);
        return NULL;
    }

    // Copy of id_artists Single-Linked List
    new_music->id_artists = strdup(id_artists);
    if (new_music->id_artists == NULL)
    {
        perror("Cannot allocate memory for music artists id");
        free(new_music->id);
        free(new_music);
        return NULL;
    }

    new_music->album_id = strdup(album_id);
    if (new_music->album_id == NULL)
    {
        perror("Cannot allocate memory for music album id");
        free(new_music->id);
        free(new_music->id_artists);
        free(new_music);
        return NULL;
    }

    // Allocate memory and copy music duration
    new_music->duration = strdup(duration);
    if (new_music->duration == NULL)
    {
        perror("Cannot allocate memory for music duration");
        free(new_music->id);
        free(new_music->id_artists);
        free(new_music->album_id);
        free(new_music);
        return NULL;
    }

    // Allocate memory and copy music genre
    new_music->genre = strdup(genre);
    if (new_music->genre == NULL)
    {
        perror("Cannot allocate memory for music genre");
        free(new_music->id);
        free(new_music->id_artists);
        free(new_music->album_id);
        free(new_music->duration);
        free(new_music);
        return NULL;
    }

    return new_music;
}

void free_music(music *m)
{
    if (m == NULL) return;
    free(m->id);
    free(m->id_artists);
    free(m->album_id);
    free(m->duration);
    free(m->genre);
    free(m);
}

void free_music_wrapper(void *data)
{
    free_music((music *)data);
}

const char *get_music_id_artists(music *m)
{
    return m->id_artists;
}

const char *get_music_duration(music *m)
{
    return m->duration;
}

const char *get_music_genre(music *m)
{
    return m->genre;
}

const char *get_music_album_id(music *m)
{
    return m->album_id;
}