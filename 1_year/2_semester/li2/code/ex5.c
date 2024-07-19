#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>

int calcH1(int P, int x) {                        
    return (x % P);
}

int calcH2(int P, int x) {
    return (x / P) % P;
}

typedef struct cel {
    int valor;
    struct cel *prox;
} cel;

cel* inserir_elem(cel* L, int elem) {
    cel* C = (cel*)malloc(sizeof(cel));
    assert(C != NULL);
    C->valor = elem;
    C->prox = L;
    return C;
}

void freeList(cel* L) {
    while (L != NULL) {
        cel* prox = L->prox;
        free(L);
        L = prox;
    }
}

int Check(int a, int hash[], int P) {
    for (int i = 0; i < P; i++) {
        if (hash[i] == a) return 1;
    }
    return 0;
}

void LinProb(int p, int hash[], int P, int x, int l) {
    static int control = 0; 

    if (control) {
        return;
    }

    for (int i = 0; i < P; i++) {
        if (hash[p] == -1 || hash[p] == -2) {
            hash[p] = x;
        
            if (l == 0) {
                printf("%d -> %d\n", p, x);
                printf("OK\n");
            }

            return;
        }
        
        p = (p + 1) % P;
    }

    if (l == 0) {
        printf("GIVING UP!\n");
        control = 1; 
    } 
}

int check3(int x, int hash[], int has2[], int P) {
    for (int i = 0; i < P; i++) {
        if (hash[i] == x) return 1;
    }

    for (int h = 0; h < P; h++) {
        if (has2[h] == x) return 1;
    }

    return 0;
}

void mocho(int x, int t1[], int t2[], int P, int c);

void helper(int x, int t1[], int t2[], int P, int c) {
    if (c > P) {
        printf("GIVING UP!\n");
        return;
    }

    int h2 = calcH2(P, x);

    if (t2[h2] == -1) {
        t2[h2] = x;
        printf("1 %d -> %d\n", h2, x);
        printf("OK\n");
        return;
    } else {
        int te = t2[h2];
        t2[h2] = x;
        printf("1 %d -> %d\n", h2, x);

        mocho(te, t1, t2, P, c + 1);
    }
}

void mocho(int x, int t1[], int t2[], int P, int c) {
    if (c >= P) {
        printf("GIVING UP!\n");
        return;
    }

    int h1 = calcH1(P, x);

    if (t1[h1] == -1) {
        t1[h1] = x;
        printf("0 %d -> %d\n", h1, x);
        printf("OK\n");
        return;
    } else {
        int te = t1[h1];
        t1[h1] = x;
        printf("0 %d -> %d\n", h1, x);

        helper(te, t1, t2, P, c + 1);
    }
}

void keyDelete(char b, int hash[], int P, int x, int has2[], int l, cel** llist) {
    int index;

    switch (b) {
        case 'o':
            for (int i = P - 1; i >= 0; i--) {
                if (hash[i] == x) {
                    hash[i] = -2;
                    if (l == 0) printf("OK\n");
                    return;
                }
            }

            if (l == 0) printf("NO\n");
            break;

        case 'l':
            index = calcH1(P, x);
            cel* atual = llist[index];
            cel* anterior = NULL;

            while (atual != NULL) {
                if (atual->valor == x) {
                    if (anterior != NULL) {
                        anterior->prox = atual->prox;
                    } else {
                        llist[index] = atual->prox;
                    }
                    free(atual);
                    printf("OK\n");
                    return;
                }
                anterior = atual;
                atual = atual->prox;
            }
            printf("NO\n");
            break;

        case 'c':
            for (int i = 0; i < P; i++) {
                if (hash[i] == x) {
                    hash[i] = -1;
                    printf("OK\n");
                    return;

                } else if (has2[i] == x) {
                    has2[i] = -1;
                    printf("OK\n");
                    return;
                }
            }

            printf("NO\n");
            break;
    }
}

void keyInsert(char b, int hash[], int P, int x, int has2[], int l, cel** llist) {
    switch (b) {
        case 'o':
            if (Check(x, hash, P) == 1) {
                printf("%d EXISTS\n", x);
                keyDelete(b, hash, P, x, has2, 1, llist);
                keyInsert(b, hash, P, x, has2, 1, llist);
                return;
            } else {
                LinProb(calcH1(P, x), hash, P, x, l);
                return;
            }

        case 'l':
            llist[calcH1(P, x)] = inserir_elem(llist[calcH1(P, x)], x);
            printf("%d -> %d\n", calcH1(P, x), x);
            printf("OK\n");
            break;

        case 'c':
            if (check3(x, hash, has2, P) == 1) {
                printf("%d EXISTS\n", x);
                return;
            } else {
                mocho(x, hash, has2, P, 0);
                return;
            }
    }
}

void keyCheck(char b, int hash[], int P, int x, int has2[], cel** llist) {
    int index;

    switch (b) {
        case 'o':
            if (Check(x, hash, P) == 1) {
                keyDelete(b, hash, P, x, has2, 1, llist);
                keyInsert(b, hash, P, x, has2, 1, llist);
            }

            for (int i = 0; i < P; i++) {
                if (hash[i] == x) {
                    printf("%d\n", i);
                    return;
                }
            }
            printf("NO\n");
            break;

        case 'l':
            index = calcH1(P, x);
            cel* atual = llist[index];

            while (atual != NULL) {
                if (atual->valor == x) {
                    printf("%d\n", index);
                    return;
                }
                atual = atual->prox;
            }
            printf("NO\n");
            break;

        case 'c':
            for (int i = 0; i < P; i++) {
                if (hash[i] == x) {
                    printf("0 %d\n", i);
                    return;
                } else if (has2[i] == x) {
                    printf("1 %d\n", i);
                    return;
                }
            }
            printf("NO\n");
            break;
    }
}

void keyPrint(char b, int hash[], int P, int has2[], cel** llist) {
    switch (b) {
        case 'o':
            for (int i = 0; i < P; i++) {
                if (hash[i] == -2) printf("%d\tD\n", i);
                else if (hash[i] != -1) printf("%d\t%d\n", i, hash[i]);
            }
            break;

        case 'l':
            for (int i = 0; i < P; i++) {
                if (llist[i] != NULL) { //
                printf("%d", i);

                cel* atual = llist[i];
                while (atual != NULL) {
                    printf(" %d", atual->valor);
                    atual = atual->prox;
                }

                printf("\n");
                }
            }
    break;

        case 'c':
            for (int i = 0; i < P; i++) {
                if (hash[i] != -1) printf("0\t%d\t%d\n", i, hash[i]);
            }

            for (int h = 0; h < P; h++) {
                if (has2[h] != -1) printf("1\t%d\t%d\n", h, has2[h]);
            }

            break;
    }
}

void bridge(char b, int hash[], int P, int x, char h, int has2[], cel** llist) {
    switch (b) {
        case 'P': keyPrint(h, hash, P, has2, llist); break;
        case 'I': keyInsert(h, hash, P, x, has2, 0, llist); break;
        case 'D': keyDelete(h, hash, P, x, has2, 0, llist); break;
        case 'C': keyCheck(h, hash, P, x, has2, llist); break;
    }
}

int main() {
    int P;
    char in[10];
    char co[7];
    int x;
    char c;

    if (scanf("%d", &P) == 1) {
        int hash[P];
        int has2[P];
        cel** llist = (cel**)malloc(P * sizeof(cel*));
        assert(llist != NULL);
        for (int i = 0; i < P; i++) {
            hash[i] = -1;
            has2[i] = -1;
            llist[i] = NULL;
        }

        if (scanf("%s", co) == 1) {

            if (strcmp(co, "OPEN") == 0) {
                while (1) {
                    if (fgets(in, sizeof(in), stdin) == NULL) {
                        break;
                    }
                    if (sscanf(in, " %c %d", &c, &x) == 2) {
                        bridge(c, hash, P, x, 'o', has2, llist);
                    } else if (sscanf(in, " %c", &c) == 1) {
                        bridge('P', hash, P, 0, 'o', has2, llist);
                    } 
                }
            } else if (strcmp(co, "LINK") == 0) {
                while (1) {
                    if (fgets(in, sizeof(in), stdin) == NULL) {
                        break;
                    }
                    if (sscanf(in, " %c %d", &c, &x) == 2) {
                        bridge(c, hash, P, x, 'l', has2, llist);
                    } else if (sscanf(in, " %c", &c) == 1) {
                        bridge('P', hash, P, 0, 'l', has2, llist);
                    } 
                }
            } else if (strcmp(co, "CUCKOO") == 0) {
                while (1) {
                    if (fgets(in, sizeof(in), stdin) == NULL) {
                        break;
                    }
                    if (sscanf(in, " %c %d", &c, &x) == 2) {
                        bridge(c, hash, P, x, 'c', has2, llist);
                    } else if (sscanf(in, " %c", &c) == 1) {
                        bridge('P', hash, P, 0, 'c', has2, llist);
                    } 
                }
            }
        }
        for (int i = 0; i < P; i++) {
            freeList(llist[i]);
        }
        free(llist);
    }

    return 0;
}