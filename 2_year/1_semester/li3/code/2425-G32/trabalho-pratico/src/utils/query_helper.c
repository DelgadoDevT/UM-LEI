#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include "utils/query_helper.h"

int birth_date_to_age(const char *birth_date)
{
    int birth_year, birth_month, birth_day;
    int current_year = 2024, current_month = 9, current_day = 9;

    if (birth_date == NULL)
    {
        return -1;
    }

    if (sscanf(birth_date, "%d/%d/%d", &birth_year, &birth_month, &birth_day) != 3)
    {
        return -1;
    }

    int age = current_year - birth_year;

    if (current_month < birth_month || (current_month == birth_month && current_day < birth_day))
    {
        age--;
    }

    return age;
}

int count_digits(int number)
{
    if (number < 0)
    {
        number = -number;
    }

    if (number == 0)
    {
        return 1;
    }

    int count = 0;
    while (number > 0)
    {
        number /= 10;
        count++;
    }
    return count;
}

int convert_duration_to_seconds(const char *duration)
{
    int hours, minutes, seconds;

    sscanf(duration, "%2d:%2d:%2d", &hours, &minutes, &seconds);

    return (hours * 3600) + (minutes * 60) + seconds;
}

void convert_seconds_to_duration(int total_seconds, char *duration, size_t duration_size)
{
    int hours, minutes, seconds;

    hours = total_seconds / 3600;
    total_seconds %= 3600;
    minutes = total_seconds / 60;
    seconds = total_seconds % 60;

    snprintf(duration, duration_size, "%02d:%02d:%02d", hours, minutes, seconds);
}

int count_group_elements(char *string)
{
    char *str = strdup(string);
    int counter = 0;

    char *token = strtok(str, ",");
    while (token != NULL)
    {
        counter++;
        token = strtok(NULL, ",");
    }

    free(str);
    return counter;
}

int week_to_ind(char *date_str)
{
    int target_year, target_month, target_day;
    sscanf(date_str, "%d/%d/%d", &target_year, &target_month, &target_day);

    struct tm tm1 = { 0 };
    struct tm tm2 = { 0 };

    // Reference 2024/09/09
    tm1.tm_year = 2024 - 1900;
    tm1.tm_mon = 9 - 1;
    tm1.tm_mday = 9;
    tm1.tm_hour = tm1.tm_min = tm1.tm_sec = 0;
    tm1.tm_isdst = -1;

    tm2.tm_year = target_year - 1900;
    tm2.tm_mon = target_month - 1;
    tm2.tm_mday = target_day;
    tm2.tm_hour = tm2.tm_min = tm2.tm_sec = 0;
    tm2.tm_isdst = -1;

    time_t t1 = mktime(&tm1);
    time_t t2 = mktime(&tm2);

    double dt = difftime(t1, t2);
    int days = round(dt / 86400);

    if (days <= 1) return 0;
    else return ((days + 5) / 7);
}

char *add_one_day(char *date_str) 
{
    struct tm tm = {0};  
    char *result = malloc(11 * sizeof(char));

    sscanf(date_str, "%4d/%2d/%2d", &tm.tm_year, &tm.tm_mon, &tm.tm_mday);

    tm.tm_year -= 1900;
    tm.tm_mon -= 1; 

    time_t t = mktime(&tm);
    t += 86400;

    struct tm *new_tm = localtime(&t);
    strftime(result, 11, "%Y/%m/%d", new_tm);

    return result;
}

int is_same_year(char *year, char *timestamp)
{
    char target_year[5];
    sscanf(timestamp, "%4s", target_year);
    if (strcmp(year, target_year) == 0) return 1;
    else return 0;
}
