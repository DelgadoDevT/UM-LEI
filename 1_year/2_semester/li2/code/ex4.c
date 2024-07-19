#include <stdio.h>
#include <math.h>
#include <assert.h>
#include <string.h>

char upper(char a) {
    char b;

    if (a >= 'a' && a <= 'z') {
        b = a - 32;
    } else {
        b = a;
    }
    return b;
}

int repeat(char ta[], char l) {
    int r = 0;

    for (int i = 0; ta[i] != '\0'; i++) {
        if ((upper (ta[i])) == l) r++;
    }

    return r;
}

double similar(char ta[]) {
    double Ei[26] = {43.31, 10.56, 23.13, 17.25, 56.88, 9.24, 12.59, 15.31, 38.45, 1.00, 5.61, 27.98, 15.36, 33.92, 36.51, 16.14, 1.00, 38.64, 29.23, 35.43, 18.51, 5.13, 6.57, 1.48, 9.06, 1.39};
    char alfa[26] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    double ms = 0;
    double Oi;

    for (int i = 0; i < 26; i++) {
        Oi = repeat(ta, alfa[i]);
        ms += (pow(Ei[i] - Oi, 2) / Ei[i]);
    }

    return ms;
}

int best(double sim[]) {
    double d = sim[0];
    int mv = 0;

    for (int i = 0; i < 26; i++) {
        if (sim[i] < d) {
            d = sim[i];
            mv = i; 
        }
    }

    return mv;
}

char caesar(char c, int j) {
    if (c >= 'A' && c <= 'Z') {
        return 'A' + (c - 'A' + j) % 26;
    } else if (c >= 'a' && c <= 'z') {
        return 'a' + (c - 'a' + j) % 26;
    } else {
        return c;
    }
}

char* jumping(char fr[], int d, char decoded[]) {

    for (int i = 0; fr[i] != '\0'; i++) {
        if ((fr[i] >= 'A' && fr[i] <= 'Z') || (fr[i] >= 'a' && fr[i] <= 'z')) {
            decoded[i] = caesar(fr[i], d);
        } else {
            decoded[i] = fr[i];
        }
    }

    decoded[strlen(decoded)] = '\0';

    return decoded;
}

int main() {
    char fr[10000];
    double sim[26];
    char ta[10000];
    char decoded[10000];
    int n; 

    if (scanf("%[^\n]", fr) == 1) {

        for (int j = 0; j < 26; j++) {

            for (int i = 0; fr[i] != '\0'; i++) {
                if ((fr[i] >= 'A' && fr[i] <= 'Z') || (fr[i] >= 'a' && fr[i] <= 'z')) {
                    ta[i] = caesar(fr[i], j);
                } else {
                    ta[i] = fr[i];
                }
            }

            ta[strlen(fr)] = '\0';
            sim[j] = similar(ta);
        }

        n = best(sim);
        jumping(fr, n, decoded);

        printf("%d %s\n", n, decoded);

    }
}