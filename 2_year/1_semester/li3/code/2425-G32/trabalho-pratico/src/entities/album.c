#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "entities/album.h"

typedef struct album
{
    char *id;          /**< Unique identifier for the album (null-terminated string). */
    char *artists_id;  /**< Null-terminated string of artist IDs associated with the album. */
} album;

album *create_album(char *id, char *artists_id) 
{
    // Allocate memory for the new album
    album *new_album = malloc(sizeof(album));
    if (new_album == NULL) 
    {
        perror("Cannot allocate memory for new album");
        return NULL;
    }

    // Allocate memory and copy id
    new_album->id = strdup(id);
    if (new_album->id == NULL) 
    {
        perror("Cannot allocate memory for album id");
        free(new_album);
        return NULL;
    }

    // Allocate memory and copy artists_id
    new_album->artists_id = strdup(artists_id);
    if (new_album->artists_id == NULL) 
    {
        perror("Cannot allocate memory for album artist id");
        free(new_album->id);
        free(new_album);
        return NULL;
    }

    return new_album;
}

void free_album(album *a)
{
    if (a == NULL) return;
    free(a->id);
    free(a->artists_id);
    free(a);
}

void free_album_wrapper(void *data)
{
    free_album((album *)data);
}

const char *get_album_artists_id(album *a)
{
    return a->artists_id;
}