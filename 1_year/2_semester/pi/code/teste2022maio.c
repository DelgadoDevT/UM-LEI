#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// 1
void swap(int a[], int f, int ind) {
    int h = a[f];
    a[f] = a[ind];
    a[ind] = h;
}

int nesimo(int a[], int N, int i) {
    for (int j = 0; j < N - 1; j++) {
        for (int k = j + 1; k < N; k++) {
            if (a[j] > a[k]) {
                swap(a, j, k);
            }
        }
    }
    return a[i - 1];
}

// 2
typedef struct LInt_nodo {
    int valor;
    struct LInt_nodo *prox;
} *LInt;

LInt removeMaiores(LInt l, int x) {
    if (l == NULL || l->valor >= x) {
        return l;
    }

    LInt prev = l;
    LInt current = l->prox;

    while (current != NULL && current->valor < x) {
        prev = current;
        current = current->prox;
    }

    if (current != NULL) {
        prev->prox = NULL;
    }

    return l;
}

// 3
typedef struct ABin_nodo {
    int valor;
    struct ABin_nodo *esq, *dir;
} *ABin;

int procura(ABin a, int x) {
    while (a != NULL && a->valor != x) {
        if (x > a->valor) a = a->dir;
        if (x < a->valor) a = a->esq;
    }

    return (a->valor == x) ? 0 : 1;
}

LInt newLInt(int x) {
    LInt new = malloc(sizeof(struct LInt_nodo));
    new->valor = x;
    new->prox = NULL;
    return new;
}

LInt caminho(ABin a, int x) {
    if (a == NULL || procura(a, x)) return NULL;

    LInt path = newLInt(a->valor);
    LInt aux = path;

    while (a->valor != x) {
        if (x > a->valor) a = a->dir;
        else a = a->esq;
        aux->prox = newLInt(a->valor);
        aux = aux->prox;
    }

    return path;
}

// 4
void inc(char s[]) {
    int l = strlen(s), i;

    for (i = l - 1; i >= 0 && s[i] == '9'; s[i--] = '0');

    if (i == -1) {s[0] = '1'; s[l] = '0'; s[l+1] = '\0';}
    else s[i]++;
}

// 5
int sacosR(int p[], int N, int C, int lSize, int ind) {
    if (ind >= N) {
        if (lSize < C && lSize != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    if (p[ind] > C) {
        return -1;
    }

    if (p[ind] > lSize) {
        return 1 + sacosR(p, N, C, C - p[ind], ind + 1);
    } else {
        return sacosR(p, N, C, lSize - p[ind], ind + 1);
    }
}

int sacos(int p[], int N, int C) {
    return sacosR(p, N, C, C, 0);
}

// Main
ABin newABin(int x) {
    ABin new = malloc(sizeof(struct ABin_nodo));
    new->valor = x;
    new->esq = NULL;
    new->dir = NULL;
    return new;
}

int main() {
    // Início dos testes

    // Teste para nesimo
    printf("Teste para nesimo: ");
    int arr1[] = {3, 1, 5, 6, 2, 7, 9, 10, 8, 4};
    int n = sizeof(arr1) / sizeof(arr1[0]);
    int i = 4;
    printf("%d\n", nesimo(arr1, n, i));

    // Teste para removeMaiores
    printf("Teste para removeMaiores: ");
    LInt l = newLInt(1);
    l->prox = newLInt(3);
    l->prox->prox = newLInt(5);
    l->prox->prox->prox = newLInt(7);
    l->prox->prox->prox->prox = newLInt(9);
    int x = 6;
    LInt nova_lista = removeMaiores(l, x);
    while (nova_lista != NULL) {
        printf("%d ", nova_lista->valor);
        nova_lista = nova_lista->prox;
    }
    printf("\n");

    // Teste para caminho
    printf("Teste para caminho: ");
    ABin arvore = malloc(sizeof(struct ABin_nodo));
    arvore->valor = 5;
    arvore->esq = malloc(sizeof(struct ABin_nodo));
    arvore->esq->valor = 3;
    arvore->esq->esq = newABin(2);
    arvore->esq->dir = newABin(4);
    arvore->dir = malloc(sizeof(struct ABin_nodo));
    arvore->dir->valor = 8;
    arvore->dir->esq = newABin(7);
    arvore->dir->dir = newABin(10);
    int elemento = 4;
    LInt cam = caminho(arvore, elemento);
    while (cam != NULL) {
        printf("%d ", cam->valor);
        cam = cam->prox;
    }
    printf("\n");

    // Teste para inc
    printf("Teste para inc: ");
    char s1[] = "123";
    char s2[] = "199";
    char s3[] = "999";
    inc(s1);
    inc(s2);
    inc(s3);
    printf("%s\n", s1);
    printf("%s\n", s2);
    printf("%s\n", s3);

    // Teste para sacos
    printf("Teste para sacos: ");
    int pesos1[] = {3, 6, 2, 1, 5, 7, 2, 4, 1};
    int C1 = 10;
    int pesos2[] = {3, 3, 3, 3, 5, 5, 11};
    int C2 = 11;
    printf("%d\n", sacos(pesos1, n, C1));
    printf("%d\n", sacos(pesos2, sizeof(pesos2) / sizeof(pesos2[0]), C2));

    // Fim dos testes
    return 0;
}