#include <stdio.h>
#include <stdlib.h>
#include <time.h>

typedef struct nodo {
    int valor;
    struct nodo *esq, *dir;
} * ABin;

ABin newABin (int r, ABin e, ABin d) {
   ABin a = malloc (sizeof(struct nodo));
   if (a!=NULL) {
      a->valor = r; a->esq = e; a->dir = d;
   }
   return a;
}

void dumpABin(ABin a, int N) {
    if (a != NULL) {
        printf("%d ", a->valor);
        dumpABin(a->esq, N);
        dumpABin(a->dir, N);
    }
}

void freeABin(ABin a) {
    if (a != NULL) {
        freeABin(a->esq);
        freeABin(a->dir);
        free(a);
    }
}

ABin RandArvFromArray (int v[], int N) {
   ABin a = NULL;
    int m;
    if (N > 0){
    	m = rand() % N;
    	a = newABin (v[m], RandArvFromArray (v,m), RandArvFromArray (v+m+1,N-m-1));
    }
    return a;	
}

int altura (ABin a){
    if (a == NULL) return 0;

    int l = altura(a->esq);
    int r = altura(a->dir);
    
    return 1 + ((l > r) ? l : r);
}

int nFolhas (ABin a){
    if (a == NULL) return 0;

    int l = nFolhas(a->esq);
    int r = nFolhas(a->dir);
    
    return 1 + l + r;
}

ABin maisEsquerda(ABin a) {
    if (a == NULL) return NULL;
    while (a->esq != NULL) {
        a = a->esq;
    }
    return a; 
}

void imprimeNivel (ABin a, int l) {
    if (a == NULL) return;
    if (l == 0) {
        printf("%d ", a->valor);
    } else {
        imprimeNivel(a->esq, l - 1);
        imprimeNivel(a->dir, l - 1);
    }
}

int procuraE(ABin a, int x) {
    if (a == NULL) return 0;
    if (a->valor == x) return 1;
    return procuraE(a->esq, x) || procuraE(a->dir, x);
}

struct nodo *procura (ABin a, int x) {
    if (a == NULL) return NULL;

    else while (a != NULL && a->valor != x) {
        if (a->valor > x) a = a->esq;
        else a = a->dir;
    }

    return a;
}

int nivel (ABin a, int x) {
    int lvl = 0;
    if (a == NULL) return -1;

    else while (a != NULL && a->valor != x) {
        if (a->valor > x) a = a->esq;
        else a = a->dir;
        lvl++;
    }

    return lvl;
}

void imprimeAte(ABin a, int x) {
    if (a == NULL) return;
    if (x <= a->valor) imprimeAte(a->esq, x);
    else {
        imprimeAte(a->esq, x);
        printf("%d ", a->valor);
        imprimeAte(a->dir, x);
    }
}

int main (){
    int v1[15] = { 1, 3, 5, 7, 9,11,13,15,17,19,21,23,25,27,29},
        v2[15] = {21, 3,15,27, 9,11,23, 5,17,29, 1,13,25, 7,19},
        N=15;
    ABin a1, a2,r;
    
    srand(time(NULL));
    
    printf ("_______________ Testes _______________\n\n");
    // N = rand() % 16;
    a1 = RandArvFromArray (v2, N);
    printf ("Primeira árvore de teste (%d elementos)\n", N);
    dumpABin (a1, N);
    
    printf ("altura = %d\n", altura (a1));
    printf ("numero de folhas: %d\n", nFolhas (a1));
    printf ("Nodo mais à esquerda: ");
    r = maisEsquerda (a1);
    if (r==NULL) printf ("(NULL)\n"); else printf ("%d\n", r->valor);
    printf ("Elementos no nivel 3_______\n");
    imprimeNivel (a1, 3);
    printf ("\n___________________________\n");

    printf ("procura de 2: %d\n", procuraE (a1, 2));
    printf ("procura de 9: %d\n", procuraE (a1, 9));
    
    freeABin (a1);
    
    // N = rand() % 16;
    a2 = RandArvFromArray (v1, N);
    printf ("\nSegunda árvore de teste (%d elementos)\n", N);
    dumpABin (a2, N);
    
    printf ("procura de 9: ");
    r = procura (a2, 9);
    if (r==NULL) printf ("(NULL)\n"); else printf ("%d\n", r->valor);   
    printf ("procura de 2: ");
    r = procura (a2, 2);
    if (r==NULL) printf ("(NULL)\n"); else printf ("%d\n", r->valor);   
    printf ("nível do elemento 2: %d\n", nivel (a2, 2));
    printf ("nível do elemento 9: %d\n", nivel (a2, 9));
    imprimeAte (a2, 20);

    freeABin (a1);

    printf ("\n\n___________ Fim dos testes ___________\n\n");
    return 0;
}