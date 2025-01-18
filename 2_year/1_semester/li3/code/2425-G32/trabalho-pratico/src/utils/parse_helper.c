#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include "utils/parse_helper.h"

#define MAX_FIELDS 8

void remove_quotes_start_and_end(char *string)
{
    if (string[0] == '"')
    {
        memmove(string, string + 1, strlen(string));  // Remove the first quote
    }
    if (string[strlen(string) - 1] == '"')
    {
        string[strlen(string) - 1] = '\0';  // Remove the last quote
    }
}

void trim_newline(char *string)
{
    size_t len = strlen(string);
    if (len > 0 && string[len - 1] == '\n')
    {
        string[len - 1] = '\0';
    }
}

void remove_brackets_and_single_quotes(const char *input, char *output)
{
    int j = 0;

    for (int i = 0; input[i] != '\0'; i++)
    {
        if (input[i] != '[' && input[i] != ']' && input[i] != '\'' && input[i] != ' ')
        {
            output[j++] = input[i];
        }
    }
    output[j] = '\0';
}

void parse_list(char *id_constituents_pre, char *id_constituents)
{
    if (strcmp(id_constituents_pre, "[]") == 0)
    {
        strcpy(id_constituents, "EMPTY");
        return;
    }
    else
    {
        remove_brackets_and_single_quotes(id_constituents_pre, id_constituents);
    }
}

char* convert_to_original_list(char* simple_list) 
{
    int len = strlen(simple_list) * 2 + 2;
    int i = 0;
    char *result = malloc(sizeof(char) * len);
    if (result == NULL) return NULL;

    result[i++] = '[';

    char *token = strtok(simple_list, ",");
    int first = 1;
    while (token != NULL)
    {
        if (!first) 
        {
            result[i++] = ',';
            result[i++] = ' ';
        }
        first = 0;

        result[i++] = '\''; 
        strcpy(result + i, token); 
        i += strlen(token);  
        result[i++] = '\'';

        token = strtok(NULL, ",");
    }

    result[i++] = ']';
    result[i] = '\0';

    return result;
}

char *reassemble_original_line(char **fields, int num_fields, void (*get_list_fields_function)(int *))
{
    int field_list[MAX_FIELDS];
    get_list_fields_function(field_list);

    size_t line_size = 1;
    for (int i = 0; i < num_fields; i++) 
    {
        if (field_list[i]) line_size += strlen(fields[i]) * 2 + 2;
        else line_size += strlen(fields[i]) + 3;
    }
    
    char *original_line = malloc(line_size);
    if (original_line == NULL) return NULL;

    snprintf(original_line, line_size, "\"%s\"", fields[0]);
    for (int i = 1; i < num_fields; i++) 
    {
        strcat(original_line, ";\"");
        if (field_list[i]) 
        {
            char *converted = convert_to_original_list(fields[i]);
            strcat(original_line, converted);
            free(converted);
        }
        else strcat(original_line, fields[i]);
        strcat(original_line, "\"");
    }

    return original_line;
} 