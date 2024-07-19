#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

// 1
int isFib(int x) {
    int a = 0, b = 1, c;
    while (c <= x) {
        if (c == x) return 1; 
        c = a + b;
        a = b;
        b = c;
    }
    return 0; 
}

// 2
typedef struct {
    float teste, minis;
} Aluno;

int moda(Aluno turma[], int N) {
    int final[N];
    for (int i = 0; i < N; i++) {
        final[i] = round(turma[i].teste * 0.8 + turma[i].minis * 0.2);
    }

    for (int k = N; k > 0; k--) {
        for (int j = 1; j < k; j++) {
            if (final[j] < final[j - 1]) {
                int temp = final[j - 1];
                final[j - 1] = final[j];
                final[j] = temp;
            }
        }
    }

    int mfreq = final[0];
    int fcounter = 1, flocal = 1;
    for (int o = 1; o < N; o++) {
        if (final[o] == final[o - 1]) {
            flocal++;
        } else {
            if (fcounter < flocal) {
                fcounter = flocal;
                mfreq = final[o - 1];
            }
            flocal = 1;
        }
    }

    if (mfreq <= 9) return 0; 
    else return 1;
}

// 3
typedef struct lint_nodo {  
    int valor;  
    struct lint_nodo *prox;  
} *LInt;

int length(LInt l) {
    if (l == NULL) return 0;

    int len = 0;
    while (l != NULL) {
        l = l->prox;
        len++;
    }

    return len;
}

int take(int n, LInt *l) {
    if (*l == NULL || n == 0) return 0;
    if (length(*l) <= n) return 0;

    LInt current = *l;
    for (int i = 1; i < n && current->prox != NULL; i++) {
        current = current->prox;
    }

    int counter = 0;
    while (*l != NULL) {
        LInt temp = *l;
        *l = (*l)->prox;
        free(temp);
        counter++;
    }

    return counter;
}

// 4
int verifica(char frase[], int k) {
    int counter = 0;

    for (int i = 0; frase[i] != '\0'; i++) {
        if (isspace(frase[i])) {
            if (counter < k) return 0;
            else counter = 0;
        } else counter++;
    }

    if (counter < k) return 0;

    return 1;
}

// 5
typedef struct abin_nodo {  
    int valor;  
    struct abin_nodo *esq, *dir;  
} *ABin;

ABin reconstroiAux(char s[], int *pos) {
    if (s[*pos] == '\0' || s[*pos] == '*') {
        (*pos)++;
        return NULL;
    }

    ABin new = malloc(sizeof(struct abin_nodo));
    if (new == NULL) return NULL;

    new->valor = atoi(&s[*pos]);
    (*pos)++;
    new->esq = reconstroiAux(s, pos);
    new->dir = reconstroiAux(s, pos);

    return new;
}

ABin reconstroi(char s[]) {
    int pos = 0;
    return reconstroiAux(s, &pos);
}

// Main
int main() {
    return 0;
}