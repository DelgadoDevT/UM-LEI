    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include "managers/artist_manager.h"
    #include "managers/music_manager.h"
    #include "managers/user_manager.h"
    #include "managers/album_manager.h"
    #include "managers/history_manager.h"
    #include "utils/parse_helper.h"
    #include "output/file_error_output.h"
    #include "parsers/generic_parser.h"
    #include "utils/syntax_validator.h"

    #define MAX_FIELDS 8

    void generic_parse(char *line, char *header, int *invalid_control, int entitie_number, int number_of_fields, int (*validation_function)(char **), void (*add_data_to_holder_function)(char **), void (*get_list_fields_function)(int *))
    {
        int check, counter = 0, control_list;
        int free_fields[MAX_FIELDS] = {0};
        char *fields[MAX_FIELDS], *token;

        token = strtok(line, ";");
        while (token != NULL && counter < MAX_FIELDS)
        {
            fields[counter++] = token;
            token = strtok(NULL, ";");
        }

        for (int h = 0; h < counter; h++)
        {
            trim_newline(fields[h]);
            remove_quotes_start_and_end(fields[h]);
        }

        trim_newline(header);

        control_list = parse_lists(fields, free_fields, get_list_fields_function);
        check = validate_and_handle_errors(fields, header, invalid_control, entitie_number, number_of_fields, control_list, validation_function, get_list_fields_function);
        if (!check) add_data_to_holder_function(fields);

        for (int k = 0; k < MAX_FIELDS; k++) if (free_fields[k]) free(fields[k]);
    }

    int parse_lists(char **fields, int *free_fields, void (*get_list_fields_function)(int *))
    {
        int field_list[MAX_FIELDS], control_list = 0;
        get_list_fields_function(field_list);

        for (int i = 0; i < MAX_FIELDS; i++)
        {
            if (field_list[i])
            {
                control_list = list_validation(fields[i]);

                char *new_list = malloc(strlen(fields[i]) + 6);
                if (new_list == NULL)
                {
                    exit(EXIT_FAILURE);
                }

                parse_list(fields[i], new_list);
                fields[i] = new_list;
                free_fields[i] = 1;
            }
        }
        
        return control_list;
    }

    int validate_and_handle_errors(char **fields, char *header, int *invalid_control, int entitie_number, int number_of_fields, int control_list, int (*validation_function)(char **), void (*get_list_fields_function)(int *))
    {
        int check = 0;

        if (control_list || validation_function(fields))
        {
            if (*invalid_control == 0)
            {
                open_error_file(header, entitie_number);
                *invalid_control = 1;
            }

            char *original_line = reassemble_original_line(fields, number_of_fields, get_list_fields_function);
            write_error_line(original_line, entitie_number);
            free(original_line);
            check = 1;
        }

        return check;
    }