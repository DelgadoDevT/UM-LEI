#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include "utils/syntax_validator.h"

int artist_type_validation(char *type) 
{
    if (type == NULL) return 1;
    char *p = type;

    while (*p) 
    {
        *p = tolower((unsigned char)*p);
        p++;
    }

    if (strcmp(type, "individual") == 0 || strcmp(type, "group") == 0) return 0;
    else return 1; 
}

int plataform_validation(char *platform)
{
    if (platform == NULL) return 1;
    char *p = platform;

    while (*p) 
    {
        *p = tolower((unsigned char)*p);
        p++;
    }

    if (strcmp(platform, "mobile") == 0 || strcmp(platform, "desktop") == 0) return 0;
    else return 1;  
}

int duration_validation(const char *duration)
{
    int len = strlen(duration);

    if (len != 8) return 1;
    if (duration[2] != ':' || duration[5] != ':') return 1;

    for(int i = 0; i < len; i++)
    {
        if(i == 2 || i == 5) continue;
        if(!isdigit(duration[i])) return 1;
    }

    char hours[3] = {duration[0], duration[1], '\0'};
    char minutes[3] = {duration[3], duration[4], '\0'};
    char seconds[3] = {duration[6], duration[7], '\0'};

    int h = atoi(hours);
    int m = atoi(minutes);
    int s = atoi(seconds);

    if(h < 0 || h > 99) return 1;
    if(m < 0 || m > 59) return 1;
    if(s < 0 || s > 59) return 1;
    return 0;
}

int date_validation(const char *date)
{
    int len = strlen(date);

    if (len != 10) return 1;
    if (date[4] != '/' || date[7] != '/') return 1;

    char year[5] = {date[0], date[1], date[2], date[3], '\0'};
    char month[3] = {date[5], date[6], '\0'};
    char day[3] = {date[8], date[9], '\0'};

    for (int i = 0; i < 4; i++)
    {
        if (!isdigit(year[i])) return 1;
    }
    for (int i = 0; i < 2; i++)
    {
        if (!isdigit(month[i]) || !isdigit(day[i])) return 1;
    }

    int y = atoi(year);
    int m = atoi(month);
    int d = atoi(day);

    if (y < 0 || y > 2024) return 1;
    if (m < 1 || m > 12) return 1;
    if (d < 1 || d > 31) return 1;

    if (y == 2024)
    {
        if (m > 9 || (m == 9 && d > 9)) return 1;
    }

    return 0;
}

int email_validation(char *email)
{
    char *email_copy = strdup(email);
    if (email_copy == NULL)
    {
        perror("Cannot allocate memory for email copy");
        return 1;
    }

    char *username = strtok(email_copy, "@");
    char *domain = strtok(NULL, "");

    int valid = 1;

    if (username == NULL || strlen(username) < 1 || is_valid_username(username) == 1)
    {
        valid = 0;
    }

    if (domain != NULL && domain[0] != '.') // if the first element is '.' check, to avoid problems with strtok
    {
        char *domain_l = strtok(domain, ".");
        char *domain_r = strtok(NULL, ".");

        if (domain_l == NULL || strlen(domain_l) < 1 || !all_lowercase(domain_l))
        {
            valid = 0;
        }

        if (domain_r == NULL || strlen(domain_r) < 2 || strlen(domain_r) > 3 || !all_lowercase(domain_r))
        {
            valid = 0;
        }

        if (strtok(NULL, ".") != NULL)
        {
            valid = 0;
        }
    }
    else
    {
        valid = 0;
    }

    free(email_copy);

    return valid ? 0 : 1;
}

int subscription_type_validation(char *subscription_type)
{
    if ((strcmp(subscription_type, "normal") == 0) || (strcmp(subscription_type, "premium") == 0))
    {
        return 0;
    }

    return 1;
}

int list_validation(char *list)
{
    if (list == NULL || strlen(list) < 2) return 1;
    if (list[0] == '[' && list[strlen(list) - 1] == ']') return 0;
    return 1;
}

int all_lowercase(const char *str)
{
    while (*str)
    {
        if (!islower(*str)) return 0;
        str++;
    }

    return 1;
}

int is_valid_username(char *input)
{
    for (int i = 0; input[i] != '\0'; i++)
    {
        if (!islower(input[i]) && !isdigit(input[i]))
        {
            return 1;
        }
    }
    return 0;
}

