#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "entities/history.h"

typedef struct history
{
    char *user_id;   /**< Unique identifier for the user. */
    char *music_id;  /**< Unique identifier for the music. */
    char *timestamp; /**< Timestamp of when the music was played. */
    char *duration;  /**< Duration for which the music was played. */
} history;

history *create_history(char *user_id, char *music_id, char *timestamp, char *duration) 
{
    // Allocate memory for the new history
    history *new_history = malloc(sizeof(history));
    if (new_history == NULL) 
    {
        perror("Cannot allocate memory for new history");
        return NULL;
    }

    // Allocate memory and copy user_id
    new_history->user_id = strdup(user_id);
    if (new_history->user_id == NULL) 
    {
        perror("Cannot allocate memory for history user id");
        free(new_history);
        return NULL;
    }

    // Allocate memory and copy music_id
    new_history->music_id = strdup(music_id);
    if (new_history->music_id == NULL) 
    {
        perror("Cannot allocate memory for history music id");
        free(new_history->user_id);
        free(new_history);
        return NULL;
    }

    // Allocate memory and copy timestamp
    new_history->timestamp = strdup(timestamp);
    if (new_history->timestamp == NULL) 
    {
        perror("Cannot allocate memory for history timestamp");
        free(new_history->user_id);
        free(new_history->music_id);
        free(new_history);
        return NULL;
    }

    // copy duration
    new_history->duration = strdup(duration);
    if (new_history->duration == NULL)
    {
        perror("Cannot allocate memory for history duration");
        free(new_history->user_id);
        free(new_history->music_id);
        free(new_history->timestamp);
        free(new_history);
        return NULL; 
    }

    return new_history;
}

void free_history(history *h)
{
    if (h == NULL) return;
    free(h->user_id);
    free(h->music_id);
    free(h->timestamp);
    free(h->duration);
    free(h);
}

void free_history_wrapper(void *data)
{
    free_history((history *)data);
}

const char *get_history_music_id(history *h)
{
    return h->music_id;
}

const char *get_history_timestamp(history *h)
{
    return h->timestamp;
}

const char *get_history_duration(history *h)
{
    return h->duration;
}

const char *get_history_user_id(history *h)
{
    return h->user_id;
}