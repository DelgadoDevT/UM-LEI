#include <stdio.h>
#include <stdlib.h>
#include "managers/artist_manager.h"
#include "managers/music_manager.h"
#include "managers/user_manager.h"
#include "managers/album_manager.h"
#include "managers/history_manager.h"
#include "parsers/parse_query.h"
#include "parsers/generic_parser.h"
#include "parsers/parser_sorter.h"

int parse_data(const char **data_path, const char *query_path, int type)
{
    init_artist_holder();
    init_album_holder();
    init_music_holder();
    init_user_holder();
    init_history_holder();

    parse_and_populate_data(data_path[0], 7, 0, validate_artist, add_artist_to_holder, get_artists_list_fields);
    parse_and_populate_data(data_path[1], 5, 1, validate_album, add_album_to_holder, get_album_list_fields);
    parse_and_populate_data(data_path[2], 8, 2, validate_music, add_music_to_holder, get_musics_list_fields);
    parse_and_populate_data(data_path[3], 8, 3, validate_user, add_user_to_holder, get_users_list_fields);
    parse_and_populate_data(data_path[4], 6, 4, validate_history, add_history_to_holder, get_history_list_fields);

    if (type == 0) parse_query(query_path);

    return 0;
}

void free_data(void)
{
    free_artist_data();
    free_album_data();
    free_music_data();
    free_user_data();
    free_history_data();
}

void parse_and_populate_data(const char *data_path, int number_of_fields, int entitie_number, int (*validation_function)(char **), void (*add_data_to_holder_function)(char **), void (*get_list_fields_function)(int *))
{
    FILE *file = fopen(data_path, "r");
    if (file == NULL)
    {            
        return;
    }

    char *line = NULL, *header = NULL;
    size_t len = 0;

    int safe = getline(&header, &len, file);
    if (safe == 0)
    {
        return;
    }

    int invalid_control = 0;
    while (getline(&line, &len, file) != -1)
    {
        generic_parse(line, header, &invalid_control, entitie_number, number_of_fields ,validation_function, add_data_to_holder_function, get_list_fields_function);
    }

    free(header);
    free(line);
    fclose(file);
}