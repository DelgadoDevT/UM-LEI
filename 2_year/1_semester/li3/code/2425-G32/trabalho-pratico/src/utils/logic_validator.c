#include <string.h>
#include "utils/logic_validator.h"
#include "managers/artist_manager.h"
#include "managers/music_manager.h"
#include "managers/album_manager.h"

int artist_id_constituent_type_validation(char *id_constituents, char *type)
{
    if ((strcmp(type, "individual") == 0) && (strcmp(id_constituents, "EMPTY") != 0))
    {
        return 1;
    }

    return 0;
}

int album_id_music_validation(char *album_id)
{
    if (album_search(album_id)) return 1;
    else return 0;
}

int music_id_artist_validation(char *id_artists)
{
    char *cpstr = strdup(id_artists);
    char *token = strtok(cpstr, ",");

    while (token != NULL)
    {
        if (artist_search(token))
        {
            free(cpstr);
            return 1;
        }

        token = strtok(NULL, ",");
    }

    free(cpstr);
    return 0;
}

int music_id_user_validation(char *id_music)
{
    char *cpstr = strdup(id_music);
    char *token = strtok(cpstr, ",");

    while (token != NULL)
    {
        if (music_search(token))
        {
            free(cpstr);
            return 1;
        }

        token = strtok(NULL, ",");
    }

    free(cpstr);
    return 0;
}
