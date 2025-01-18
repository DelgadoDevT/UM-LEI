#include <stdio.h>
#include <string.h>
#include "parsers/parser_sorter.h"

#define MAX_DATATYPES 5

int main(int argc, char *argv[])
{
    // Check if the correct number of arguments is provided
    if (argc != 3)
    {
        fprintf(stderr, "Usage: %s <path> <query_path>\n", argv[0]);
        return 1; // Incorrect usage
    }

    int parse_e;  // Variable to capture parse result error code
    size_t path_size = strlen(argv[1]) + 16;
    char musics_path[path_size], users_path[path_size], artists_path[path_size], albums_path[path_size], history_path[path_size];
    char *query_path = argv[2];

    // Construct paths to CSV files for music, users, and artists data
    snprintf(musics_path, sizeof(musics_path), "%s/musics.csv", argv[1]);
    snprintf(users_path, sizeof(users_path), "%s/users.csv", argv[1]);
    snprintf(artists_path, sizeof(artists_path), "%s/artists.csv", argv[1]);
    snprintf(albums_path, sizeof(albums_path), "%s/albums.csv", argv[1]);
    snprintf(history_path, sizeof(history_path), "%s/history.csv", argv[1]);

    const char *data_path[MAX_DATATYPES];
    data_path[0] = artists_path;
    data_path[1] = albums_path;
    data_path[2] = musics_path;
    data_path[3] = users_path;
    data_path[4] = history_path;

    // Parse data from specified CSV files
    parse_e = parse_data(data_path, (const char *)query_path, 0);
    if (parse_e == 1)
    {
        perror("Error on allocating memory for hash tables");
        return 1; // Parsing error
    }

    free_data();

    return 0; // Successful execution
}