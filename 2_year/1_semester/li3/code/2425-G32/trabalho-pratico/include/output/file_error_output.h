/**
 * @file file_error_output.h
 * @brief Header file for managing error output files for different entities.
 *
 * © 2025 João Delgado, Simão Mendes, Pedro Pereira. All rights reserved.
 *
 * Licensed under the MIT License. You may obtain a copy of the License at
 * https://opensource.org/licenses/MIT
 * 
 * This header defines functions for selecting paths for error output files,
 * opening error files with headers, and writing error lines for various
 * entities (e.g., artists, albums, musics, users, history).
 * 
 * The system uses specific CSV files to record errors for each entity type,
 * allowing users to track and resolve issues with data processing.
 */

#ifndef FILE_ERROR_OUTPUT_H
#define FILE_ERROR_OUTPUT_H

/**
 * @brief Selects the path for the error file based on the entity type.
 *
 * This function determines the file path for the error CSV file depending on
 * the provided entity type. Different entity types correspond to different
 * error files. The path is written to the `path` parameter.
 *
 * @param path A pointer to a character array where the selected file path will be stored.
 * @param entitie An integer representing the entity type for the error file:
 *                - 0: Artists
 *                - 1: Albums
 *                - 2: Musics
 *                - 3: Users
 *                - 4: History
 */
void path_select(char *path, int entitie);

/**
 * @brief Opens an error output file and writes the header.
 *
 * This function opens (or creates) an error output file for the specified
 * entity type and writes the given header to the file. The file is created
 * if it does not exist and will be overwritten if it already does.
 *
 * @param header A string containing the header to write to the error file.
 * @param entitie An integer representing the entity type for the error file:
 *                - 0: Artists
 *                - 1: Albums
 *                - 2: Musics
 *                - 3: Users
 *                - 4: History
 */
void open_error_file(char *header, int entitie);

/**
 * @brief Appends an error line to the corresponding error output file.
 *
 * This function appends a new error line to the error file for the specified
 * entity type. The file is opened in append mode, ensuring that existing
 * data is not overwritten.
 *
 * @param line A string containing the error line to append.
 * @param entitie An integer representing the entity type for the error file:
 *                - 0: Artists
 *                - 1: Albums
 *                - 2: Musics
 *                - 3: Users
 *                - 4: History
 */
void write_error_line(char *line, int entitie);

#endif // FILE_ERROR_OUTPUT_H