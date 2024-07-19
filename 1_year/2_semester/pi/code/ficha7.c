#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct celula {
    char *palavra;
    int ocorr;
    struct celula *prox;
} *Palavras;

void libertaLista (Palavras l){
    Palavras save;

    while (l != NULL) {
        save = l;
        l = l->prox;
        free(save->palavra);
        free(save);
    }
}

int quantasP (Palavras l){
    int counter = 0;

    while (l != NULL) {
        counter++;
        l = l->prox;
    }

    return counter;
}

void listaPal (Palavras l){
    while (l != NULL) {
        printf("%s (%d)\n", l->palavra, l->ocorr);
        l = l->prox;
    }
}

char *ultima(Palavras l) {
    if (l == NULL) {
        return NULL;
    }

    while (l->prox != NULL) {
        l = l->prox;
    }

    return l->palavra;
}

Palavras acrescentaInicio (Palavras l, char *p){
    Palavras nod = malloc(sizeof(struct celula));
    nod->palavra = strdup(p);
    nod->ocorr = 1;
    nod->prox = l;
    
    return nod;
}

Palavras acrescentaFim (Palavras l, char *p){
    Palavras start = l;

    Palavras nod = malloc(sizeof(struct celula));
    nod->palavra = strdup(p);
    nod->ocorr = 1;
    nod->prox = NULL;

    if (l == NULL) {
        return nod;
    } else {
        while (l != NULL) l = l->prox;
        l->prox = nod;
    }

    return start;
}

int verifica(Palavras l, char *p) {
    while (l != NULL) {
        if (strcmp(l->palavra, p) == 0) { 
            return 0;
        } else {
            l = l->prox;
        } 
    }

    return 1;
}

Palavras acrescenta (Palavras l, char *p){
    Palavras start = l;

    if (verifica(l, p) == 1) {
        return acrescentaInicio(l, p);
    }

    while (l != NULL && strcmp(l->palavra, p) != 0) l = l->prox;
    l->ocorr++;

    return start;
}

struct celula *maisFreq (Palavras l){
    if (l == NULL) return NULL;
    
    Palavras freq = l;
    int num = l->ocorr;

    while (l != NULL) {
        if (l->ocorr > num) {
            freq = l;
            num = l->ocorr;
        }

        l = l->prox;
    }

    return freq;
}

int main () {
    Palavras dic = NULL;

    char *canto1 [44] = {"as", "armas", "e", "os", "baroes", "assinalados",
                          "que", "da", "ocidental", "praia", "lusitana", 
                          "por", "mares", "nunca", "de", "antes", "navegados",
                          "passaram", "ainda", "alem", "da", "taprobana",
                          "em", "perigos", "e", "guerras", "esforcados",
                          "mais", "do", "que", "prometia", "a", "forca", "humana",
                          "e", "entre", "gente", "remota", "edificaram", 
                          "novo", "reino", "que", "tanto", "sublimaram"};

    printf ("\n_____________ Testes _____________\n\n");

    int i; struct celula *p;
    for (i=0;i<44;i++)
        dic = acrescentaInicio (dic, canto1[i]);

    printf ("Foram inseridas %d palavras\n", quantasP (dic));
    printf ("palavras existentes:\n");
    listaPal (dic);
    printf ("última palavra inserida: %s\n", ultima (dic));

    libertaLista (dic);

    dic = NULL;

    srand(42);
    
    for (i=0; i<1000; i++)
        dic = acrescenta (dic, canto1 [rand() % 44]);
    
    printf ("Foram inseridas %d palavras\n", quantasP (dic));
    printf ("palavras existentes:\n");
    listaPal (dic);
    printf ("última palavra inserida: %s\n", ultima (dic));
    
    p = maisFreq (dic);
    printf ("Palavra mais frequente: %s (%d)\n", p->palavra, p->ocorr);
    
    printf ("\n_________ Fim dos testes _________\n\n");

    return 0;
}