#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parsers/parser_sorter.h"
#include "parsers/parse_query.h"
#include "queries/query_one.h"
#include "queries/query_two.h"
#include "queries/query_three.h"
#include "queries/query_four.h"
#include "queries/query_five.h"
#include "queries/query_six.h"

#define CYAN "\e[0;94m"
#define WHITE "\e[0;37m"
#define BOLD_WHITE "\e[1;37m"
#define RESET "\e[0m"  

#define MAX_DATATYPES 5
#define QUERY_NUMBER 6

int firsts[QUERY_NUMBER] = {1, 1, 1, 1, 1, 1};

char *get_user_input() 
{
    char *buffer = NULL; 
    size_t buffer_size = 0;

    ssize_t length = getline(&buffer, &buffer_size, stdin);

    if (length == -1) 
    {
        free(buffer);
        return NULL;
    }

    if (buffer[length - 1] == '\n') buffer[length - 1] = '\0';
    return buffer;
}

void free_query_data(void)
{
    if (firsts[0] == 0) free_query_one_data();
    if (firsts[1] == 0) free_query_two_data();
    if (firsts[2] == 0) free_query_three_data();
    if (firsts[3] == 0) free_query_four_data();
    if (firsts[4] == 0) free_query_five_data();
    if (firsts[5] == 0) free_query_six_data();
}

int handle_queries_interative(char *query_input)
{
    int query_number = query_input[0] - '0';
    int query_format = query_format_selector(query_input);

    switch (query_number)
    {
        case 1:
            printf(WHITE "Insert the artist/user id:\n" RESET);
            printf(CYAN  "Q1: " RESET);
            char *id = get_user_input();
            if (strcmp(id, "quit") == 0) return 1;
            handle_query_1(query_number, firsts, id, -1, query_format);
            printf(WHITE "-----------------------------------\n" RESET);
            free(id);
            break;

        case 2:
            printf(WHITE "Insert the number of artists in the output:\n" RESET);
            printf(CYAN  "Q2: " RESET);
            char *nartist = get_user_input();
            if (strcmp(nartist, "quit") == 0) return 1;
            printf(WHITE "Enter the artists' country (Optional). To ignore press enter:\n" RESET);
            printf(CYAN  "Q2: " RESET);
            char *country = get_user_input();
            if (country != NULL && strcmp(country, "quit") == 0) return 1;
            if (country != NULL && country[0] == '\0')
            {
                free(country);
                country = NULL;
            }
            handle_query_2(query_number, firsts, nartist, country, -1, query_format);
            printf(WHITE "-----------------------------------\n" RESET);
            free(nartist);
            if (country) free(country);
            break;

        case 3:
            printf(WHITE "Insert the minimum age range:\n" RESET);
            printf(CYAN  "Q3: " RESET);
            char *min_age = get_user_input();
            if (strcmp(min_age, "quit") == 0) return 1;
            printf(WHITE "Insert the maximum age range:\n" RESET);
            printf(CYAN  "Q3: " RESET);
            char *max_age = get_user_input();
            if (strcmp(max_age, "quit") == 0) return 1;
            handle_query_3(query_number, firsts, min_age, max_age, -1, query_format);
            printf(WHITE "-----------------------------------\n" RESET);
            free(min_age);;
            free(max_age);
            break;

        case 4:
            printf(WHITE "Insert the first date (Optional). To ignore press enter:\n" RESET);
            printf(CYAN  "Q4: " RESET);
            char *first_date = get_user_input();
            if (strcmp(first_date, "quit") == 0) return 1;
            if (first_date != NULL && first_date[0] == '\0')
            {
                free(first_date);
                handle_query_4(query_number, firsts, NULL, NULL, -1, query_format);
            }
            else
            {
                printf(WHITE "Insert the last date:\n" RESET);
                printf(CYAN  "Q4: " RESET);
                char *last_date = get_user_input();
                if (strcmp(last_date, "quit") == 0) return 1;
                handle_query_4(query_number, firsts, first_date, last_date, -1, query_format);
                free(first_date);
                free(last_date);
            }
            printf(WHITE "-----------------------------------\n" RESET);
            break;

        case 5:
            printf(WHITE "Enter the user id to generate the list of recommendations:\n" RESET);
            printf(CYAN  "Q5: " RESET);
            char *user_id = get_user_input();
            if (strcmp(user_id, "quit") == 0) return 1;
            printf(WHITE "Enter the number of recommendations you would like to see:\n" RESET);
            printf(CYAN  "Q5: " RESET);
            char *user_num = get_user_input();
            if (strcmp(user_num, "quit") == 0) return 1;
            handle_query_5(query_number, firsts, user_id, user_num, -1, query_format, 1); // We are using our recommender function here
            printf(WHITE "-----------------------------------\n" RESET);
            free(user_id);
            free(user_num);
            break;

        case 6:
            printf(WHITE "Enter the user id:\n" RESET);
            printf(CYAN  "Q6: " RESET);
            char *user_ids = get_user_input();
            if (strcmp(user_ids, "quit") == 0) return 1;
            printf(WHITE "Enter the year of the statistics:\n" RESET);
            printf(CYAN  "Q6: " RESET);
            char *year = get_user_input();
            if (strcmp(year, "quit") == 0) return 1;
            printf(WHITE "Enter the number of favorite artists (Optional). To ignore press enter:\n" RESET);
            printf(CYAN  "Q6: " RESET);
            char *num_artists = get_user_input();
            if (num_artists != NULL && strcmp(num_artists, "quit") == 0) return 1;
            if (num_artists != NULL && num_artists[0] == '\0')
            {
                free(num_artists);
                num_artists = NULL;
            }
            handle_query_6(query_number, firsts, user_ids, year, num_artists, -1, query_format);
            printf(WHITE "-----------------------------------\n" RESET);
            free(user_ids);
            free(year);
            if (num_artists) free(num_artists);
            break;
    }
    
    return 0;
}

int is_valid_dataset(const char **datapath)
{
    for (int i = 0; i < MAX_DATATYPES; i++)
    {
        FILE *fp = fopen(datapath[i], "r");
        if (fp == NULL) 
        {
            printf(WHITE "Error: Unable to open file " BOLD_WHITE "%s\n" RESET, datapath[i]);
            return 1;
        }
        fclose(fp);
    }

    return 0;
}

int main(int argc, char **argv)
{
    if (argc != 1 || argv[0] == NULL)
    {
        printf(BOLD_WHITE "Usage: " WHITE "./programa-interativo\n" RESET);
        exit(EXIT_FAILURE);
    }

    printf(BOLD_WHITE "Welcome " WHITE "to the interative " BOLD_WHITE "LI3 " WHITE "program!\n" RESET);
    printf(WHITE "Remember you can always " BOLD_WHITE "quit " WHITE "the program by typing \"" BOLD_WHITE "quit" WHITE"\"\n" RESET);
    while (1)
    {
        printf(WHITE "Input the dataset path (Write " BOLD_WHITE "default " WHITE "to use the current folder): \n" RESET);
        printf(CYAN  "Path: " RESET);
        
        char *dataset_path = get_user_input();

        if (strcmp(dataset_path, "quit") == 0)
        {
            free(dataset_path);
            printf(WHITE "Memory freed\n" RESET);
            printf(BOLD_WHITE "Thank you " WHITE "for using the " BOLD_WHITE "LI3 " WHITE "program. " BOLD_WHITE "Goodbye!" RESET "\n");
            exit(EXIT_SUCCESS);
        }

        if (strcmp(dataset_path, "default") == 0) 
        {
            free(dataset_path);  
            dataset_path = strdup(".");  
        }

        size_t path_size = strlen(dataset_path) + 16;
        char musics_path[path_size], users_path[path_size], artists_path[path_size], albums_path[path_size], history_path[path_size];
        snprintf(musics_path, sizeof(musics_path), "%s/musics.csv", dataset_path);
        snprintf(users_path, sizeof(users_path), "%s/users.csv", dataset_path);
        snprintf(artists_path, sizeof(artists_path), "%s/artists.csv", dataset_path);
        snprintf(albums_path, sizeof(albums_path), "%s/albums.csv", dataset_path);
        snprintf(history_path, sizeof(history_path), "%s/history.csv", dataset_path);

        const char *data_path[MAX_DATATYPES];
        data_path[0] = artists_path;
        data_path[1] = albums_path;
        data_path[2] = musics_path;
        data_path[3] = users_path;
        data_path[4] = history_path;

        if (is_valid_dataset(data_path) == 0) 
        {
            parse_data(data_path, NULL, 1);
            printf(WHITE "Dataset loaded\n" RESET);

            while(1)
            {
                printf(WHITE "Which query do you want to execute?\n" RESET);
                printf(CYAN  "Query: " RESET);
                char *input = get_user_input();
                
                if (((strlen(input) == 2 && input[1] == 'S') || strlen(input) == 1) && (input[0] >= '0' && input[0] <= '6'))
                {
                    int check = handle_queries_interative(input);
                    if (check) 
                    {
                        free(input);
                        free_query_data();
                        free_data();
                        printf(WHITE "Memory freed\n" RESET);
                        printf(BOLD_WHITE "Thank you " WHITE "for using the " BOLD_WHITE "LI3 " WHITE "program. " BOLD_WHITE "Goodbye!" RESET "\n");
                        exit(EXIT_SUCCESS);
                    }
                }
                else if (strcmp(input, "quit") == 0)
                {
                    free(input);
                    free_query_data();
                    free_data();
                    printf(WHITE "Memory freed\n" RESET);
                    printf(BOLD_WHITE "Thank you " WHITE "for using the " BOLD_WHITE "LI3 " WHITE "program. " BOLD_WHITE "Goodbye!" RESET "\n");
                    exit(EXIT_SUCCESS);
                }
                else 
                {
                    printf(BOLD_WHITE "Invalid " WHITE "input. Please insert a " BOLD_WHITE "valid " WHITE "query\n" RESET);
                    clearerr(stdin);
                }

                free(input);  
            }
        }
        else
        {
            printf(BOLD_WHITE "Invalid " WHITE "dataset. Please try again\n" RESET);
            free(dataset_path);
            dataset_path = NULL;
        }
    }

    printf(WHITE "Memory freed\n" RESET);
    printf(BOLD_WHITE "Thank you " WHITE "for using the " BOLD_WHITE "LI3 " WHITE "program. " BOLD_WHITE "Goodbye!" RESET "\n");
    exit(EXIT_SUCCESS);
}

