#include <stdlib.h>
#include <glib.h>
#include "managers/user_manager.h"
#include "utils/syntax_validator.h"
#include "utils/logic_validator.h"
#include "entities/user.h"

#define MAX_FIELDS 8
static GHashTable *user_hash;
static GHashTableIter iter;
static gpointer key, value;

void get_users_list_fields(int *field_list)
{
    int temp[MAX_FIELDS] = {0, 0, 0, 0, 0, 0, 0, 1};
    for (int i = 0; i < MAX_FIELDS; i++) field_list[i] = temp[i];
}

int validate_user(char **fields)
{
    return ((subscription_type_validation(fields[6]) == 1) || (date_validation(fields[4]) == 1) || (music_id_user_validation(fields[7]) == 1) || (email_validation(fields[1]) == 1));
}

void init_user_holder(void)
{
    user_hash = g_hash_table_new_full(g_str_hash, g_str_equal, free, free_user_wrapper);
    if (user_hash == NULL)
    {
        exit(EXIT_FAILURE);
    }
}

void free_user_data(void)
{
    g_hash_table_destroy(user_hash);
}

void add_user_to_holder(char **fields)
{
    user *us = create_user(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[7]); // Save all fields
    char *id_copy = strdup(fields[0]);
    g_hash_table_insert(user_hash, id_copy, us);
}

int user_search(char *id)
{
    user *u = g_hash_table_lookup(user_hash, id);
    if (u == NULL)
    {
        return 1;
    }

    return 0;
}

void get_user_resume(char *id, char **data)
{
    user *u = g_hash_table_lookup(user_hash, id);
    // Its safe to ignore the case that the user is not found because the previous function is called before

    data[0] = strdup(get_user_email(u));
    data[1] = strdup(get_user_first_name(u));
    data[2] = strdup(get_user_last_name(u));
    data[3] = strdup(get_user_birth_date(u));
    data[4] = strdup(get_user_country(u));
}

void iter_user_init(void)
{
    g_hash_table_iter_init(&iter, user_hash);
}

int get_next_user_entry(char **data)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        data[0] = strdup(get_user_id_liked_songs(value));
        data[1] = strdup(get_user_birth_date(value));
        return 1;
    }
    else
    {
        return 0;
    }
}

int get_next_user_ids(char **data)
{
    if (g_hash_table_iter_next(&iter, &key, &value))
    {
        data[0] = strdup(get_user_id(value));
        return 1;
    }
    else
    {
        return 0;
    }  
}