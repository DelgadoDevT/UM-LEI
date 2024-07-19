#include <stdio.h>
#include <math.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>

// 1
int perfeito(int x) {
    int plus = 0, i;

    for (i = 1; i < x / 2; i++) {
        if ((x % i) == 0) plus += i;
    }

    return (i == x) ? 1 : 0;
}

// 2
typedef struct {
    int x, y;
} Ponto;

int dist(Ponto p) {
    return sqrt(pow(p.x,2) + pow(p.y,2));
}

void ordena(Ponto pos[], int N) {
    for (int i = N - 1; i > 0; i--) {
        for (int k = 1; k < i; k++) {
            if (dist(pos[k]) < dist(pos[k - 1])) {
                Ponto temp = pos[k - 1];
                pos[k - 1] = pos[k];
                pos[k] = temp;
            }
        }
    }
}

// 3
typedef struct abin_nodo{
    int valor;
    struct abin_nodo *esq, *dir;
} *ABin;

int depth(ABin a, int x) {
    if (a == NULL) return -1;
    if (a->valor == x) return 1;

    int l = depth(a->esq, x);
    int r = depth(a->dir, x);

    if (l == -1 && r == -1) return -1;
    if (l == -1) return 1 + r;
    if (r == -1) return 1 + l;
    return (l < r) ? 1 + l : 1 + r;
}

// 4
int found(char ch, char s[], int len) {
    for (int i = 0; i < len; i++) {
        if (s[i] == ch) return 1;
    }

    return 0;
}

int wordle(char secreta[], char tentativa[]) {
    int len = strlen(secreta), hit = 0;

    for (int i = 0; i < len; i++) {
        if (secreta[i] == tentativa[i]) {
            tentativa[i] = toupper(tentativa[i]);
            hit++;
        } else if (!found(tentativa[i], secreta, len)) {
            tentativa[i] = '*';
        }
    }

    return hit;
}

// 5
typedef struct lint_nodo {
    int valor;
    struct lint_nodo *prox;
} *LInt;

LInt periodica(char s[]) {
    int len = strlen(s), i;

    LInt inf = malloc(sizeof(struct lint_nodo));
    if (inf == NULL) return NULL;
    LInt head = inf, start = NULL;
    inf->valor = s[i] - '0';
    inf->prox = NULL;

    for (i = 1; i < len && s[i] != '('; i++) {
        inf->prox = malloc(sizeof(struct lint_nodo));
        if (inf->prox == NULL) return NULL;
        inf = inf->prox;
        inf->valor = s[i] - '0';
        inf->prox = NULL;
    }

    if (s[i] == '(') {
        inf->prox = malloc(sizeof(struct lint_nodo));
        if (inf->prox == NULL) return NULL;
        inf = inf->prox;
        inf->valor = s[i] - '0';
        inf->prox = NULL;
        start = inf;
        i++;

        while (i < len && s[i] != ')') {
            inf->prox = malloc(sizeof(struct lint_nodo));
            if (inf->prox == NULL) return NULL;
            inf = inf->prox;
            inf->valor = s[i] - '0';
            inf->prox = NULL;
            i++;
        }

        inf->prox = start;
    }

    return inf;
}

// Main
int main() {
    return 0;
}
