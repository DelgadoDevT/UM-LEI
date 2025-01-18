#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "parsers/parse_query.h"
#include "utils/parse_helper.h"
#include "queries/query_one.h"
#include "queries/query_two.h"
#include "queries/query_three.h"
#include "queries/query_four.h"
#include "queries/query_five.h"
#include "queries/query_six.h"

#define QUERY_NUMBER 6

void parse_query(const char *query_path)
{
    FILE *file = fopen(query_path, "r");
    if (file == NULL)
    {
        return;
    }

    int command_number = 1; // Command number for tracking queries
    char *line = NULL;
    size_t len = 0;

    // Read and parse each query line
    int firsts[QUERY_NUMBER] = {1, 1, 1, 1, 1, 1};
    while (getline(&line, &len, file) != -1)
    {
        query_selector(line, command_number, firsts);
        command_number++;
    }

    if (firsts[0] == 0) free_query_one_data();
    if (firsts[1] == 0) free_query_two_data();
    if (firsts[2] == 0) free_query_three_data();
    if (firsts[3] == 0) free_query_four_data();
    if (firsts[4] == 0) free_query_five_data();
    if (firsts[5] == 0) free_query_six_data();
    
    free(line);
    fclose(file);
}

int query_format_selector(char *number_str)
{
    if (strlen(number_str) >= 2 && number_str[1] == 'S') return 1;
    else return 0;
}

void query_selector(char *line, int command_number, int *firsts)
{
    char *number_str, *elem1, *elem2, *elem3;

    number_str = strtok(line, " ");
    elem1 = strtok(NULL, " ");

    int number = number_str[0] - '0';
    if (number == 2) elem2 = strtok(NULL, "\"");
    else elem2 = strtok(NULL, " ");
    elem3 = strtok(NULL, " ");

    if (number_str == NULL)
    {
        perror("Invalid query format");
        return;
    }

    int query_format = query_format_selector(number_str);

    switch (number)
    {
    case 1:
    {
        handle_query_1(number, firsts, elem1, command_number, query_format);
        break;
    }

    case 2:
    {
        handle_query_2(number, firsts, elem1, elem2, command_number, query_format);
        break;
    }

    case 3:
    {
        handle_query_3(number, firsts, elem1, elem2, command_number, query_format);
        break;
    }

    case 4:
    {
        handle_query_4(number, firsts, elem1, elem2, command_number, query_format);
        break;
    }
    
    case 5:
    {
        handle_query_5(number, firsts, elem1, elem2, command_number, query_format, 0); // 0 to LI3 team recommender, 1 to our recommender
        break;
    }

    case 6:
    {
        handle_query_6(number, firsts, elem1, elem2, elem3, command_number, query_format);
        break;
    }

    default:
        printf("Invalid query\n");
    }
}

void handle_query_1(int number, int *firsts, char *elem1, int command_number, int query_format)
{
    if (firsts[number -1])
    {
        init_and_populate_artist_status_data();
        firsts[number - 1] = 0;
    }

    trim_newline(elem1);
    run_query_one(elem1, command_number, query_format);
}

void handle_query_2(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format)
{

    if (firsts[number - 1])
    {
        init_and_populate_query_two_data();
        firsts[number - 1] = 0;
    }

    if (elem2 == NULL)
    {
        trim_newline(elem1);
        run_query_two(elem1, NULL, command_number, query_format);
    }
    else
    {
        trim_newline(elem2);
        remove_quotes_start_and_end(elem2);
        run_query_two(elem1, elem2, command_number, query_format);
    }
}

void handle_query_3(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format)
{
    if (firsts[number - 1])
    {
        init_and_populate_query_three_data();
        firsts[number - 1] = 0;
    }

    trim_newline(elem2);
    run_query_three(elem1, elem2, command_number, query_format);
}

void handle_query_4(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format)
{
    if (firsts[number - 1])
    {
        init_and_populate_query_four_data();
        firsts[number - 1] = 0;
    }

    if (elem2 == NULL) 
    {
        run_query_four(NULL, NULL, command_number, query_format);
    }
    else
    {
        trim_newline(elem2);
        run_query_four(elem1, elem2, command_number, query_format);
    }
}

void handle_query_5(int number, int *firsts, char *elem1, char *elem2, int command_number, int query_format, int recommender_function)
{
    if (firsts[number - 1])
    {
        init_and_populate_query_five_data();
        firsts[number - 1] = 0;
    }

    trim_newline(elem2);
    run_query_five(elem1, elem2, command_number, query_format, recommender_function);
}

void handle_query_6(int number, int *firsts, char *elem1, char *elem2, char *elem3, int command_number, int query_format)
{
    if (firsts[number - 1])
    {
        init_and_populate_query_six_data();
        firsts[number - 1] = 0;
    }

    if (elem3 == NULL)
    {
        trim_newline(elem2);
        run_query_six(elem1, elem2, NULL, command_number, query_format);
    }
    else
    {
        trim_newline(elem3);
        run_query_six(elem1, elem2, elem3, command_number, query_format);
    }
}