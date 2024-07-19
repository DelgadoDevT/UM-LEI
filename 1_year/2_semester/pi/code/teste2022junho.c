#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// 1
int pesquisa(int a[], int N, int x) {
    int l = 0, m = N - 1, k;

    while (l <= m) {
        k = (l + m) / 2;
        if (x < a[k]) m = k - 1;
        else if (x > a[k]) l = k + 1;
        else if (x = a[k]) return k;
    }

    return -1;
}

// 2
typedef struct LInt_nodo {  
    int valor;  
    struct LInt_nodo *prox;  
} *LInt;

void roda(LInt *l) {
    if (*l == NULL || (*l)->prox == NULL) return;

    LInt aux = *l, prev = NULL;
    while (aux->prox != NULL) {
        prev = aux;
        aux = aux->prox;
    }

    prev->prox = NULL;
    aux->prox = *l;
    *l = aux;
}

// 3
typedef struct ABin_nodo {
    int valor;
    struct ABin_nodo *esq, *dir;
} *ABin;

int apagaAux(ABin *a, int *counter, int n) {
    if (*a == NULL) return 0;

    int aesq = apagaAux(&((*a)->esq), counter, n);
    int adir = apagaAux(&((*a)->dir), counter, n);

    if (*counter >= n) return 0;

    free(*a);
    *a = NULL;
    (*counter)++;
    
    return 1 + aesq + adir;
}

int apaga(ABin a, int n) {
    int counter = 0;
    int del = apagaAux(&a, &counter, n);
    return del;
}

// 4
void checksum (char s[]) {
    char luhn, sum = 0, even = 1, size = strlen(s);
    int digit;
    
    for (int i = size - 1; i >= 0; i--) {
        if (even) {
            digit = 2 * (s[i] - '0');
            digit = (digit > 9) ? digit - 9 : digit;
            sum += digit; 
            even = 0;
        } else {
            digit = s[i] - '0';
            sum += digit; 
            even = 1;
        }
    }    

    luhn = (10 - (sum % 10)) + '0';
    s[size] = luhn;
    s[size + 1] = '\0';
}

// 5
int escolheR(int N, int valor[], int peso[], int C, int quant[], int ind) {
    if (ind >= N) return 0;
    int max = 0;

    for (int i = 0; i * peso[ind] <= C; i++) {
        int atual = i * valor[ind] + escolheR(N, valor, peso, C - i * peso[ind], quant, ind + 1);
        if (atual > max) {
            max = atual;
            quant[ind] = i;
        }
    }

    return max;
}

int escolhe(int N, int valor[], int peso[], int C, int quant[]) {
    return escolheR(N, valor, peso, C, quant, 0);
}

// Main
int main() {
    // 1. Teste da função pesquisa
    int arr[] = {2, 4, 6, 8, 10};
    int N = sizeof(arr) / sizeof(arr[0]);
    int x = 6;
    int pos = pesquisa(arr, N, x);
    if (pos != -1) {
        printf("O valor %d está na posição %d.\n", x, pos);
    } else {
        printf("O valor %d não foi encontrado.\n", x);
    }

    // 2. Teste da função roda
    LInt lista = malloc(sizeof(struct LInt_nodo));
    lista->valor = 1;
    lista->prox = malloc(sizeof(struct LInt_nodo));
    lista->prox->valor = 2;
    lista->prox->prox = malloc(sizeof(struct LInt_nodo));
    lista->prox->prox->valor = 3;
    lista->prox->prox->prox = NULL;
    printf("Lista original: %d -> %d -> %d\n", lista->valor, lista->prox->valor, lista->prox->prox->valor);
    roda(&lista);
    printf("Lista após roda: %d -> %d -> %d\n", lista->valor, lista->prox->valor, lista->prox->prox->valor);

    // 3. Teste da função apaga
    ABin arvore = malloc(sizeof(struct ABin_nodo));
    arvore->valor = 1;
    arvore->esq = malloc(sizeof(struct ABin_nodo));
    arvore->esq->valor = 2;
    arvore->esq->esq = NULL;
    arvore->esq->dir = NULL;
    arvore->dir = malloc(sizeof(struct ABin_nodo));
    arvore->dir->valor = 3;
    arvore->dir->esq = NULL;
    arvore->dir->dir = NULL;
    int n = 1; // número de nós a serem apagados
    int nodes_apagados = apaga(arvore, n);
    printf("Número de nós apagados: %d\n", nodes_apagados);

    // 4. Teste da função checksum
    char str[] = {'9', '8', '7', '1', '\0'};
    checksum(str);
    printf("String após checksum: %s\n", str);

    // 5. Teste da função escolhe
    int valor[] = {20, 150, 30};
    int peso[] = {2, 10, 3};
    int C = 14;
    int quant[3]; // array para armazenar as quantidades escolhidas
    int valorTotal = escolhe(3, valor, peso, C, quant);
    printf("Valor total: %d\n", valorTotal);
    printf("Quantidades escolhidas: [%d, %d, %d]\n", quant[0], quant[1], quant[2]);

    return 0;
}
