#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <glib.h>
#include <time.h>
#include <sys/resource.h>
#include "parsers/parser_sorter.h"
#include "parsers/parse_query.h"
#include "parsers/generic_parser.h"
#include "managers/artist_manager.h"
#include "managers/music_manager.h"
#include "managers/user_manager.h"
#include "managers/album_manager.h"
#include "managers/history_manager.h"
#include "queries/query_one.h"
#include "queries/query_two.h"
#include "queries/query_three.h"
#include "queries/query_four.h"
#include "queries/query_five.h"
#include "queries/query_six.h"

#define QUERY_NUMBER 6
#define MAX_DATATYPES 5

void handle_error(const char *message)
{
    perror(message);
    exit(EXIT_FAILURE);
}

int parse_query_test(const char *query_path, GSList **linked_arr)
{
    FILE *file = fopen(query_path, "r");
    if (file == NULL)
    {
        handle_error("Cannot open input file\n");
    }

    int command_number = 1; // Command number for tracking queries
    char *line = NULL;
    size_t len = 0;

    // Read and parse each query line
    int firsts[QUERY_NUMBER] = {1, 1, 1, 1, 1, 1};
    while (getline(&line, &len, file) != -1)
    {
        clock_t start, end;
        int query_type = atoi(line);
        start = clock();

        query_selector(line, command_number, firsts);
        command_number++;

        end = clock();
        double query_time_used = ((double)(end - start)) / CLOCKS_PER_SEC;

        double *query_time_ptr = malloc(sizeof(double));
        if (!query_time_ptr)
        {
            free(line);
            fclose(file);
            handle_error("Memory allocation failed for query time");
        }
        *query_time_ptr = query_time_used;
        linked_arr[query_type - 1] = g_slist_append(linked_arr[query_type - 1], query_time_ptr);
    }

    if (firsts[0] == 0) free_query_one_data();
    if (firsts[1] == 0) free_query_two_data();
    if (firsts[2] == 0) free_query_three_data();
    if (firsts[3] == 0) free_query_four_data();
    if (firsts[4] == 0) free_query_five_data();
    if (firsts[5] == 0) free_query_six_data();

    free(line);
    fclose(file);
    return command_number;
}

int parse_data_test(const char **data_path, const char *query_path, GSList **linked_arr)
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

    int command_number = parse_query_test(query_path, linked_arr);

    free_data();

    return command_number;
}

void compare_files_and_write_diferences(FILE *output, FILE *real_output, FILE *expected_output, int *total_querys_type, int *incorrect_querys_type, int query_type, int command_number)
{
    char *real_line = NULL;
    char *expected_line = NULL;
    size_t real_len = 0, expected_len = 0;
    int line_num = 1;
    int fail = 0;

    while (1)
    {
        ssize_t real_ret = getline(&real_line, &real_len, real_output);
        ssize_t expected_ret = getline(&expected_line, &expected_len, expected_output);

        if (real_ret == -1 || expected_ret == -1)
        {
            if (real_ret != expected_ret)
            {
                fprintf(output, "Q%d - Incongruence detected on query number %d, line %d\n",query_type, command_number, line_num);
                fail = 1;
                incorrect_querys_type[query_type - 1]++;
            }
            break;
        }

        if (strcmp(real_line, expected_line) != 0)
        {
            fprintf(output, "Q%d - Incongruence detected on query number %d, line %d\n", query_type, command_number, line_num);
            incorrect_querys_type[query_type - 1]++;
            fail = 1;
            break;
        }
        line_num++;
    }

    total_querys_type[query_type - 1]++;
    if (!fail) fprintf(output, "Q%d - Query %d run as expected\n", query_type, command_number);

    if (real_line) free(real_line);
    if (expected_line) free(expected_line);
}

void write_output_file(double cpu_time_used, GSList **linked_arr, const char *expected_output_folder, struct rusage r_usage, int command_number, const char *query_path)
{
    FILE *output = fopen("resultados/test.txt", "w");
    if (output == NULL) handle_error("Cannot open output file");

    int total_querys_type[QUERY_NUMBER] = {0};
    int incorrect_querys_type[QUERY_NUMBER] = {0};

    FILE *query_file = fopen(query_path, "r");
    if (query_path == NULL) handle_error("Cannot open query input file\n");
    char *line_query = NULL;
    size_t len_query = 0;

    for (int i = 1; i < command_number; i++)
    {
        int safe = getline(&line_query, &len_query, query_file);
        if (!safe) return;
        int query_type = atoi(line_query);

        char real_path[64];
        snprintf(real_path, sizeof(real_path), "resultados/command%d_output.txt", i);
        FILE *real_output = fopen(real_path, "r");
        if (real_output == NULL) handle_error("Cannot open produced output");

        char expected_path[strlen(expected_output_folder) + 64];
        snprintf(expected_path, sizeof(expected_path), "%s/command%d_output.txt", expected_output_folder, i);
        FILE *expected_output = fopen(expected_path, "r");
        if (expected_output == NULL) handle_error("Cannot open respective expected output");

        compare_files_and_write_diferences(output, real_output, expected_output, total_querys_type, incorrect_querys_type, query_type, i);

        fclose(real_output);
        fclose(expected_output);
    }

    if (line_query != NULL) free(line_query);

    fprintf(output, "\n");
    for (int j = 0; j < QUERY_NUMBER; j++)
    {
        if (linked_arr[j] != NULL)
        {
            GSList *current_list = linked_arr[j];
            double sum = 0.0;
            int count = 0;

            for (GSList *node = current_list; node != NULL; node = node->next)
            {
                double query_time = *(double *)node->data;
                sum += query_time;
                count++;
            }

            double average_time = (count > 0) ? (sum / count) : 0.0;
            fprintf(output, "Average time for query type %d: %.5f seconds\n", j + 1, average_time);
        }
    }

    fprintf(output, "\n");
    int all_querys_correct = 1;
    for (int k = 0; k < QUERY_NUMBER; k++)
    {
        fprintf(output, "Query type %d: %d/%d occurrences correct\n", k + 1, total_querys_type[k] - incorrect_querys_type[k], total_querys_type[k]);
        if (incorrect_querys_type[k] != 0) all_querys_correct = 0;
    }

    fprintf(output, "\nOverall Program performance:\n\n");
    if (all_querys_correct)
    {
        fprintf(output, "All queries were executed successfully and without discrepancies.\n");
    }
    else
    {
        fprintf(output, "The execution of some queries failed, please check the discrepancies above.\n");
    }

    fprintf(output, "Total CPU time used: %.2f Seconds\n", cpu_time_used);
    fprintf(output, "Total Memory used: %.2f MegaBytes\n", r_usage.ru_maxrss / 1024.0);

    fclose(output);
}

int main(int argc, char *argv[])
{
    if (argc != 4)
    {
        fprintf(stderr, "Usage: %s <dataset_path> <query_path> <expected_output_folder>\n", argv[0]);
        return EXIT_FAILURE;
    }

    struct rusage r_usage;
    GSList **linked_arr = calloc(QUERY_NUMBER, sizeof(GSList *));
    if (linked_arr == NULL) handle_error("Memory allocation failed for linked array");

    size_t path_size = strlen(argv[1]) + 16;
    char musics_path[path_size], users_path[path_size], artists_path[path_size], albums_path[path_size], history_path[path_size];
    char *query_path = argv[2];
    char *expected_output_folder = argv[3];

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

    clock_t start = clock();

    int parse_e = parse_data_test(data_path, query_path, linked_arr);
    if (parse_e == 1)
    {
        free(linked_arr);
        return EXIT_FAILURE;
    }

    double cpu_time_used = ((double)(clock() - start)) / CLOCKS_PER_SEC;
    getrusage(RUSAGE_SELF, &r_usage);

    write_output_file(cpu_time_used, linked_arr, expected_output_folder, r_usage, parse_e, query_path);

    for (int i = 0; i < QUERY_NUMBER; i++)
    {
        g_slist_free_full(linked_arr[i], free);
    }
    free(linked_arr);

    return EXIT_SUCCESS;
}