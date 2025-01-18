#include <stdio.h>
#include <stdlib.h>
#include "output/query_output.h"
#include "utils/query_helper.h"

void write_query_empty(int command_number)
{
    if (command_number == -1) 
    {
        printf("This query has no data\n");
        return;
    }

    FILE *f;
    int digit_counter = count_digits(command_number);
    char *path = malloc(sizeof(char) * (digit_counter + 30));

    snprintf(path, digit_counter + 30, "resultados/command%d_output.txt", command_number);
    f = fopen(path, "w");
    if (f == NULL)
    {
        perror("Cannot create/open output file");
        free(path);
        return;
    }

    fprintf(f, "\n");

    free(path);
    fclose(f);
}

void write_query(int command_number, int elem_number, int format_controler, char **elems)
{
    // Write query on stdout(only on interative mode)
    if (command_number == -1)
    {
        for (int i = 0; i < elem_number; i++) 
        {
            if (i > 0) 
            {
                if (format_controler) printf("=%s", elems[i]);
                else printf(";%s", elems[i]);
            } 
            else 
            {
                printf("%s", elems[i]);
            }
        }

        printf("\n");
        return;
    }

    FILE *f;
    int digit_counter = count_digits(command_number);
    char *path = malloc(sizeof(char) * (digit_counter + 30)); // 30 is the size of the string letters offset

    snprintf(path, digit_counter + 30, "resultados/command%d_output.txt", command_number);

    f = fopen(path, "a");
    if (f == NULL)
    {
        perror("Cannot create output file");
        free(path);
        return;
    }

    // Write elements to the output file
    for (int i = 0; i < elem_number; i++) 
    {
        if (i > 0) 
        {
            if (format_controler) fprintf(f, "=%s", elems[i]);
            else fprintf(f, ";%s", elems[i]);
        } 
        else 
        {
            fprintf(f, "%s", elems[i]);
        }
    }
    fprintf(f, "\n");

    free(path);
    fclose(f);
}
