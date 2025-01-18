#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "entities/user.h"

typedef struct user
{
    char *id;               /**< Unique identifier (username) */
    char *email;            /**< User's email address */
    char *first_name;       /**< User's first name */
    char *last_name;        /**< User's last name */
    char *birth_date;       /**< User's birth date */
    char *country;          /**< User's country of residence */
    char *id_liked_songs;   /**< List of liked song IDs */
} user;

user *create_user(char *id, char *email, char *first_name, char *last_name, char *birth_date, char *country, char *id_liked_songs)
{
    user *new_user = malloc(sizeof(user));
    if (new_user == NULL)
    {
        perror("Cannot allocate memory for new user");
        return NULL;
    }

    new_user->id = strdup(id);
    if (new_user->id == NULL)
    {
        perror("Cannot allocate memory for user id");
        free(new_user);
        return NULL;
    }

    new_user->email = strdup(email);
    if (new_user->email == NULL)
    {
        perror("Cannot allocate memory for user email");
        free(new_user->id);
        free(new_user);
        return NULL;
    }

    new_user->first_name = strdup(first_name);
    if (new_user->first_name == NULL)
    {
        perror("Cannot allocate memory for user first_name");
        free(new_user->id);
        free(new_user->email);
        free(new_user);
        return NULL;
    }

    new_user->last_name = strdup(last_name);
    if (new_user->last_name == NULL)
    {
        perror("Cannot allocate memory for user last_name");
        free(new_user->id);
        free(new_user->email);
        free(new_user->first_name);
        free(new_user);
        return NULL;
    }

    new_user->birth_date = strdup(birth_date);
    if (new_user->birth_date == NULL)
    {
        perror("Cannot allocate memory for user birth_date");
        free(new_user->id);
        free(new_user->email);
        free(new_user->first_name);
        free(new_user->last_name);
        free(new_user);
        return NULL;
    }

    new_user->country = strdup(country);
    if (new_user->country == NULL)
    {
        perror("Cannot allocate memory for user country");
        free(new_user->id);
        free(new_user->email);
        free(new_user->first_name);
        free(new_user->last_name);
        free(new_user->birth_date);
        free(new_user);
        return NULL;
    }

    new_user->id_liked_songs = strdup(id_liked_songs);
    if (new_user->id_liked_songs == NULL)
    {
        perror("Cannot allocate memory for user id liked songs");
        free(new_user->id);
        free(new_user->email);
        free(new_user->first_name);
        free(new_user->last_name);
        free(new_user->birth_date);
        free(new_user->country);
        free(new_user);
        return NULL;
    }

    return new_user;
}

void free_user(user *u)
{
    if (u == NULL) return;
    free(u->id);
    free(u->email);
    free(u->first_name);
    free(u->last_name);
    free(u->birth_date);
    free(u->country);
    free(u->id_liked_songs);
    free(u);
}

void free_user_wrapper(void *data)
{
    free_user((user *)data);
}

const char *get_user_email(user *u)
{
    return u->email;
}

const char *get_user_first_name(user *u)
{
    return u->first_name;
}

const char *get_user_last_name(user *u)
{
    return u->last_name;
}

const char *get_user_birth_date(user *u)
{
    return u->birth_date;
}

const char *get_user_country(user *u)
{
    return u->country;
}

const char *get_user_id_liked_songs(user *u)
{
    return u->id_liked_songs;
}

const char *get_user_id(user *u)
{
    return u->id;
}