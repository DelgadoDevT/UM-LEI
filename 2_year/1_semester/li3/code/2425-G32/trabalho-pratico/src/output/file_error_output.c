#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "output/file_error_output.h"
#include "utils/query_helper.h"

void path_select(char *path, int entitie)
{
    if (entitie == 0)
    {
        strcpy(path, "resultados/artists_errors.csv");
        return;
    }
    else if (entitie == 1)
    {
        strcpy(path, "resultados/albums_erros.csv");
    }
    else if (entitie == 2)
    {
        strcpy(path, "resultados/musics_errors.csv");
        return;
    }
    else if (entitie == 3)
    {
        strcpy(path, "resultados/users_errors.csv");
        return;
    }
    else if (entitie == 4)
    {
        strcpy(path, "resultados/history_errors.csv");
    }
}

void open_error_file(char *header, int entitie)
{
    FILE *error_csv;
    char *path = malloc(sizeof(char) * 48);
    if (path == NULL)
    {
        perror("Memory allocation for error_csv path failed");
        return;
    }

    path_select(path, entitie);

    error_csv = fopen(path, "w");
    if (error_csv == NULL)
    {
        perror("Failed to open error_csv for writing");
        free(path);
        return;
    }

    fprintf(error_csv, "%s\n", header);

    free(path);
    fclose(error_csv);
}

void write_error_line(char *line, int entitie)
{
    FILE *error_csv;
    char *path = malloc(sizeof(char) * 48);
    if (path == NULL)
    {
        perror("Memory allocation for error_csv path failed");
        return;
    }

    path_select(path, entitie);

    error_csv = fopen(path, "a");
    if (error_csv == NULL)
    {
        perror("Failed to open error_csv for appending");
        free(path);
        return;
    }

    fprintf(error_csv, "%s\n", line);

    free(path);
    fclose(error_csv);
}