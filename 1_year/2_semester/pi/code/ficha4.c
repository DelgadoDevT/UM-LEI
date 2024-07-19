#include <stdio.h>
#include <string.h>

// Funções sobre strings

// 1

int contaVogais(char *g) {
    int v = 0;

    while (*g != '\0') {
        if (*g == 'a' || *g == 'A' || *g == 'E' || *g == 'e' || *g == 'i' || *g == 'I' || *g == 'o' || *g == 'O' || *g == 'u' || *g == 'U') {
            v++;
        }
        g++;
    }

    return v;
}

// 2

void retiraChar(char *s, int in) {
    while (*(s + in) != '\0') {
        *(s + in) = *(s + in + 1);
        in++;
    }
}

void removeDuplicatas(char *s) {
    int i = 0;
    while (*(s + i) != '\0') {
        if (*(s + i) == *(s + i + 1)) {
            retiraChar(s, i + 1);
        } else {
            i++;
        }
    }
}

int retiraVogaisRep(char *s) {
    int v = 0;
    int i = 0;
    int c = 0;

    while (*(s + i) != '\0') {
        if ((*(s + i) == 'a' || *(s + i) == 'A' || *(s + i) == 'E' || *(s + i) == 'e' || *(s + i) == 'i' || *(s + i) == 'I' || *(s + i) == 'o' || *(s + i) == 'O' || *(s + i) == 'u' || *(s + i) == 'U') && c == 0) {
            c = 1;
        } else if ((*(s + i) == 'a' || *(s + i) == 'A' || *(s + i) == 'E' || *(s + i) == 'e' || *(s + i) == 'i' || *(s + i) == 'I' || *(s + i) == 'o' || *(s + i) == 'O' || *(s + i) == 'u' || *(s + i) == 'U') && c == 1) {
            retiraChar(s, i);
            c = 0;
            v++;
        } else {
            c = 0;
        }

        i++;
    }

    removeDuplicatas(s);

    return v;
}

// 3

void reDuplica(char s[], int i) {
    int l = strlen(s);

    for (int h = l; h > i; h--) {
        s[h] = s[h - 1];
    }

    s[i + 1] = s[i];
    s[l + 1] = '\0';
}

int duplicaVogais(char s[]) {
    int v = 0;

    for (int i = 0; s[i] != '\0'; i++) {
        if (s[i] == 'a' || s[i] == 'A' || s[i] == 'E' || s[i] == 'e' || s[i] == 'i' || s[i] == 'I' || s[i] == 'o' || s[i] == 'O' || s[i] == 'u' || s[i] == 'U') {
            reDuplica(s, i);
            v++;
            i++;
        }
    }

    return v;
}

// Arrays ordenados

// 1

int ordenado(int v[], int N) {
    for (int i = 0; i < N - 1; i++) {
        if (v[i] > v[i + 1]) return 0;
    }

    return 1;
}

// 2

void merge(int a[], int na, int b[], int nb, int r[]) {
    int i = 0, j = 0, k = 0;

    while (i < na && j < nb) {
        if (a[i] <= b[j]) {
            r[k++] = a[i++];
        } else {
            r[k++] = b[j++];
        }
    }

    while (i < na) {
        r[k++] = a[i++];
    }

    while (j < nb) {
        r[k++] = b[j++];
    }
}

// 3

int partition(int v[], int N, int x) {
    int a = 0, b = N - 1;

    for (int i = 0; i < N; i++) {
        if (v[i] <= x) {
            int tm = v[a];
            v[a] = v[i];
            v[i] = tm;
            a++;
        }
    }

    return a;
}

// Main function

int main() {
    #define lg 10

    char g[1000] = {"How many vogals does this sentence have?\0"};
    char s[1000] = {"Estaa e umaa string coom duuuplicadoos\0"};
    char j[1000] = {"Esta e uma string com duplicados\0"};
    int a[lg] = {1,2,3,4,5,6,7,8,9,10};
    int b[lg] = {3,4,5,6,7,8,9,10,11,12};
    int c[20];
    int v1;
    int v2;
    
    printf("Funções sobre strings:\n");

    v1 = contaVogais(g);
    printf("1- %d vowels\n", v1);

    v2 = retiraVogaisRep(s);
    printf("2- %s\n", s);

    v2 = duplicaVogais(j);
    printf("3- %d\n", v2);

    printf("\n");
    printf("Arrays ordenados:\n");

    printf("1- %d\n", ordenado(a, lg));

    merge(a, lg, b, lg, c);
    printf("2- ");
    for (int i = 0; i < 20; i++) {
        printf("%d", c[i]);
    }
    printf("\n");

    printf("3- %d\n", partition(b, lg, 5));

    return 0;
}