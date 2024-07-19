#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stdlib.h>

// 50 Questões (Memória Dinâmica)

// Listas Ligadas
typedef struct lligada {
    int valor;
    struct lligada *prox;
} *LInt;

// 1
int length(LInt l) {
    int len = 0;

    if (l == NULL) return len;
    while (l != NULL) {
        len++;
        l = l->prox;
    }
    
    return len;
}

// 2
void freeL(LInt l) {
    if (l == NULL) return;
    while (l != NULL) {
        LInt aux = l;
        l = l->prox;
        free(aux);
    }
}

// 3
void imprimeL(LInt l) {
    if (l == NULL) return; 
    while (l != NULL) {
        printf("%d ", l->valor);
        l = l->prox;
    }
}

// 4
LInt reverseL(LInt l) {
    if (l == NULL || l->prox == NULL) 
        return l;

    LInt next = l->prox;

    while (next != NULL) {
        LInt aux = next->prox;
        next->prox = l; 
        l = next;       
        next = aux;     
    }

    return l;
}

// 5
LInt newLInt(int x) {
    LInt new = malloc(sizeof(struct lligada));
    new->valor = x;
    new->prox = NULL;

    return new;
}

void insertOrd(LInt *l, int x) {
    LInt novo = newLInt(x);
    if (novo == NULL) return;

    if (*l == NULL || x < (*l)->valor) {
        novo->prox = *l;
        *l = novo;
        return;
    }

    LInt atual = *l;
    while (atual->prox != NULL && x > atual->prox->valor) {
        atual = atual->prox;
    }

    novo->prox = atual->prox;
    atual->prox = novo;
}

// 6
int removeOneOrd(LInt *l, int x) {
    if (*l == NULL) return 1;

    LInt atual = *l;
    LInt prev = NULL;

    while (atual != NULL && atual->valor != x) {
        prev = atual;
        atual = atual->prox;
    }

    if (atual == NULL) return 1;

    if (prev == NULL) *l = atual->prox;
    else prev->prox = atual->prox;

    free(atual);
    return 0;
}

// 7
void merge(LInt *r, LInt a, LInt b) {
    *r = NULL;

    while (a != NULL && b != NULL) {
        if (a->valor < b->valor) {
            if (*r == NULL) *r = a;
            else (*r)->prox = a;
            a = a->prox;
        } else {
            if (*r == NULL) *r = b;
            else (*r)->prox = b;
            b = b->prox;
        }
        *r = (*r)->prox;
    }

    if (a != NULL) *r = a;
    else if (b != NULL) *r = b;
}

// 8
void splitQS(LInt l, int x, LInt *mx, LInt *Mx) {
    if (l == NULL) return;
    
    *mx = NULL;
    *Mx = NULL;

    while (l != NULL) {
        if (l->valor < x) {
            if (*mx == NULL) {
                *mx = l;
            } else {
                (*mx)->prox = l;
                *mx = (*mx)->prox;
            }
        } else {
            if (*Mx == NULL) {
                *Mx = l;
            } else {
                (*Mx)->prox = l;
                *Mx = (*Mx)->prox;
            }
        }
        l = l->prox;
    }
    
    if (*mx != NULL) (*mx)->prox = NULL;
    if (*Mx != NULL) (*Mx)->prox = NULL;
}

// 9
LInt parteAmeio(LInt *l) {
    if (*l == NULL) return NULL;

    int len = length(*l), half = len / 2, i;
    LInt y = *l;

    for (i = 0; i < half - 1; i++) *l = (*l)->prox;

    LInt temp = (*l)->prox;
    (*l)->prox = NULL;
    *l = temp;

    return y;
}

// 10
int removeAll(LInt *l, int x) {
    if (*l == NULL) return 0;

    int counter = 0;
    if ((*l)->valor == x) {
        LInt temp = (*l);
        *l = (*l)->prox;
        free(temp);
        counter++;
    }

    while ((*l)->prox != NULL) {
        LInt next = (*l)->prox;
        if (next->valor == x) {
            LInt temp = next;
            (*l)->prox = next->prox;
            free(temp);
            counter++;
        } else (*l) = (*l)->prox;
    }

    return counter;
}

// 11
int removeDups(LInt *l) {
    if (*l == NULL) return 0;
    int counter = 0;

    while ((*l)->prox != NULL) {
        LInt aux = *l;
        int x = aux->valor;
        counter += removeAll(&aux->prox, x);
        *l = (*l)->prox;
    }

    return counter;
}

// 12
int removeMaiorL(LInt *l) {
    if (*l == NULL) return 0;
    int nu = (*l)->valor, ind = 0;
    LInt run = (*l), aux = (*l), next = (*l);

    for (int i = 0; run->prox != NULL; i++) { 
        if (run->valor < run->prox->valor) {
            nu = run->prox->valor;
            ind = i;
        }
        run = run->prox; 
    }

    for (int i = 0; next != NULL && i < ind; i++) { 
        aux = next;
        next = next->prox;
    }

    if (aux == NULL) *l = next->prox;
    else aux->prox = next->prox;

    free(next);

    return nu;
}

// 13
void init (LInt *l) {
    if (*l == NULL) return;

    if ((*l)->prox == NULL) {
        free(*l);
        return;
    }

    LInt ant = (*l), cur = (*l)->prox;
    while (cur->prox != NULL) {
        ant = cur;
        cur = cur->prox;
    }

    free(cur); 
    ant->prox = NULL;
}

// 14
void appendL(LInt *l, int x) {
    LInt new = malloc(sizeof(struct lligada));
    if (new == NULL) return;
    new->prox = NULL;
    new->valor = x;

    if (*l == NULL) {
        *l = new; 
        return;
    }

    LInt aux = (*l);
    while (aux->prox != NULL) aux = aux->prox;
    aux->prox = new;
}

// 15
void concatL(LInt *a, LInt b) {
    if (*a == NULL) { 
        *a = b; 
        return;
    }

    LInt aux = (*a);
    while (aux->prox != NULL) aux = aux->prox;
    aux->prox = b;
}

// 16
LInt cloneL(LInt l) {
    if (l == NULL) return NULL;

    LInt new = malloc(sizeof(struct lligada));
    if (new == NULL) return NULL;
    new->prox = NULL;
    new->valor = l->valor;

    LInt aux = new;
    l = l->prox;
    while (l != NULL) {
        aux->prox = malloc(sizeof(struct lligada));
        if (aux->prox == NULL) return NULL;
        aux = aux->prox;
        aux->valor = l->valor;
        aux->prox = NULL;
        l = l->prox;
    }

    return new;
}

// 17
LInt cloneRev(LInt l) {
    if (l == NULL) return NULL;
    LInt reverse = NULL;

    while (l != NULL) {
        LInt new = malloc(sizeof(struct lligada));
        if (new == NULL) return NULL;
        new->valor = l->valor;  
        new->prox = reverse;
        reverse = new;
        l = l->prox;
    }

    return reverse;
}

// 18
int maximo(LInt l) {
    if (l == NULL) return -1;
    int max = l->valor;

    l = l->prox;
    while (l != NULL) {
        if (max < l->valor) max = l->valor;
        l = l->prox;
    }

    return max;
}

// 19
int take(int n, LInt *l) {
    int len = length(*l);
    if (len <= n) return 0;

    for (int i = 0; (*l) != NULL && i < n; i++, *l = (*l)->prox);
    while (l != NULL) {
        LInt temp = *l;
        *l = (*l)->prox;
        free(temp);
        len--;
    }

    return len;
}

// 20
int drop(int n, LInt *l) {
    int len = length(*l);
    if (len <= n) return 0;

    for (int i = 0; (*l) != NULL && i < n; i++) {
        LInt temp = *l;
        (*l) = (*l)->prox;
        free(temp);
        len--;
    }

    return len;
}

// 21
LInt nforward(LInt l, int N) {
    if (l == NULL) return NULL;

    while (l != NULL) {
        l = l->prox;
    }

    return l;
}

// 22
int listToArray(LInt l, int v[], int N) {
    if (l == NULL) return 0;
    int ind = 0;

    while (l != NULL) {
        v[ind] = l->valor;
        l = l->prox;
        ind++;
    }

    return ind;
}

// 23
LInt arrayToList(int v[], int N) {
    LInt new = malloc(sizeof(struct lligada));
    if (new == NULL) return NULL;
    new->valor = v[0];
    new->prox = NULL;

    LInt aux = new;
    for (int i = 1; i < N; i++) {
        LInt next = malloc(sizeof(struct lligada));
        next->valor = v[i];
        next->prox = NULL;
        aux->prox = next;
        aux = aux->prox;
    }

    return new;
}

// 24
LInt somasAcL(LInt l) {
    if (l == NULL) return NULL;

    int sumL = l->valor;
    LInt slist = malloc(sizeof(struct lligada));
    if (slist == NULL) return NULL;
    LInt aux = slist;
    aux->valor = l->valor;
    aux->prox = NULL;

    l = l->prox;
    while (l != NULL) {
        sumL += l->valor;
        LInt next = malloc(sizeof(struct lligada));
        if (next == NULL) return NULL;
        next->valor = sumL;
        next->prox = NULL;
        aux->prox = next;
        l = l->prox;
    }

    return slist;
}

// 25
void remreps(LInt l) {
    if (l == NULL) return;

    while (l->prox != NULL) {
        if (l->prox->valor == l->valor) {
            LInt temp = l->prox;
            l->prox = temp->prox;
            free(temp);
        }
        else l = l->prox;
    }
}

// 26
LInt rotateL(LInt l) {
    if (l == NULL || l->prox == NULL) return l;

    LInt fst = l, head = l->prox;
    while (l->prox != NULL) l = l->prox;
    fst->prox = NULL;
    l->prox = fst;

    return head;
}

// 27
LInt parte(LInt l) {
    if (l == NULL || l->prox == NULL) return NULL;

    int even = 1;
    LInt x = NULL;
    LInt odd = l;
    while (odd != NULL && odd->prox != NULL) {
       if (even) {
            if (x == NULL) {
                x = malloc(sizeof(struct lligada));
                if (x == NULL) return NULL; 
                x->valor = odd->valor;
                x->prox = NULL;
            } else { 
                LInt temp = x;
                while (temp->prox != NULL) temp = temp->prox;
                temp->prox = malloc(sizeof(struct lligada)); 
                if (temp->prox == NULL) return NULL; 
                temp->prox->valor = odd->valor;
                temp->prox->prox = NULL;
            }
            odd->prox = odd->prox->prox;
        } else odd = odd->prox; 
        
        even = !even;
    }

    return x;
}

// Árvores Binárias
typedef struct nodo {
    int valor;
    struct nodo *esq, *dir;
} *ABin;

// 28
int altura(ABin a) {
    if (a == NULL) return 0;

    int i = altura(a->esq);
    int e = altura(a->dir);

    return 1 + ((i > e) ? i : e);
}

// 29
ABin cloneAB(ABin a) {
    if (a == NULL) return NULL;

    ABin b = malloc(sizeof(struct nodo));
    if (b == NULL) return NULL;

    b->valor = a->valor;
    b->esq = cloneAB(a->esq);
    b->dir = cloneAB(a->dir);

    return b;
}

// 30
void mirror(ABin *a) {
    if (*a == NULL) return;

    ABin temp = (*a)->esq;
    (*a)->esq = (*a)->dir;
    (*a)->dir = temp;
    mirror(&((*a)->esq));
    mirror(&((*a)->dir));
}

// 31
void inorder(ABin a, LInt *l) {
    if (a == NULL) return;

    inorder(a->esq, l);
    LInt new = malloc(sizeof(struct lligada));
    if (new == NULL) return;
    new->valor = a->valor;
    new->prox = (*l)->prox;
    (*l)->prox = new;
    inorder(a->dir, l);
}

// 32
void preorder(ABin a, LInt *l) {
    if (a == NULL) return;

    LInt new = malloc(sizeof(struct lligada));
    if (new == NULL) return;
    new->valor = a->valor;
    new->prox = (*l)->prox;
    (*l)->prox = new;
    preorder(a->esq, l);
    preorder(a->dir, l);
}

// 33
void postorder(ABin a, LInt *l) {
    if (a == NULL) return;

    preorder(a->esq, l);
    preorder(a->dir, l);
    LInt new = malloc(sizeof(struct lligada));
    if (new == NULL) return;
    new->valor = a->valor;
    new->prox = (*l)->prox;
    (*l)->prox = new;
}

// 34
int depth(ABin a, int x) {
    if (a == NULL) return -1;
    if (a->valor == x) return 1;
    
    int i = depth(a->esq, x);
    int k = depth(a->dir, x);

    if (i == -1 && k == -1) return -1;
    if (i == -1) return 1 + k;
    if (k == -1) return 1 + i;
    return (i > k) ? 1 + k : 1 + i;
}

// 35
int freeAB(ABin a) {
    if (a == NULL) return 0;

    int h = freeAB(a->esq);
    int k = freeAB(a->dir);
    free(a);

    return 1 + h + k;
}

// 36
int pruneAB(ABin *a, int l) {
    if (*a == NULL) return 0;
    if (l == 0) {
        free(*a);
        *a = NULL;
        return 1;
    }

    int h = pruneAB(&(*a)->esq, l - 1);
    int k = pruneAB(&(*a)->dir, l - 1);
    free(*a);

    return 1 + h + k; 
}

// 37
int iguaisAB(ABin a, ABin b) {
    if (a == NULL && b == NULL) return 1;
    if ((a == NULL && b != NULL) || (a != NULL && b == NULL)) return 0;

    int h = iguaisAB(a->esq, b->esq); 
    int k = iguaisAB(a->dir, b->dir);

    return ((a->valor == b->valor) && h && k);
}

// 38
LInt mergeL(LInt a, LInt b) {
    if (a == NULL) return b;

    LInt temp = a;
    while (temp->prox != NULL) temp = temp->prox;
    temp->prox = b;

    return a;
}

LInt nivelL(ABin a, int n) {
    if (a == NULL || n <= 0) return NULL;

    if (n == 1) {
        LInt node = malloc(sizeof(struct lligada));
        node->valor = a->valor;
        node->prox = NULL;
        return node;
    }
    else return mergeL(nivelL(a->esq, n - 1), nivelL(a->dir, n - 1));
}

// 39
int nivelV(ABin a, int n, int v[]) {
    if (a == NULL || n <= 0) return 0;

    if (n == 1) {
        v[0] = a->valor;
        return 1;
    } else {
        int h = nivelV(a->esq, n - 1, v);
        int k = nivelV(a->dir, n - 1, v + h);
        return h + k;
    }
}

// 40
int dumpAbin(ABin a, int v[], int N) {
    if (a == NULL || N <= 0) return 0;
    int m = N / 2;

    int h = dumpAbin(a->esq, v, N - 1);
    a->valor = v[m];
    int k =dumpAbin(a->dir, v + m, N - h);
    
    return 1 + h + k;
}

// 41
ABin somasAcA(ABin a) {
    if (a == NULL) return NULL;

    ABin new = malloc(sizeof(struct nodo));
    if (new == NULL) return NULL;

    new->valor = a->valor;
    new->esq = somasAcA(a->esq);
    new->dir = somasAcA(a->dir);

    if (new->esq != NULL)
        new->valor += new->esq->valor;

    if (new->dir != NULL)
        new->valor += new->dir->valor;

    return new;
}

// 42
int contaFolhas(ABin a) {
    if (a == NULL) return 0;
    if (a->esq == NULL && a->dir == NULL) return 1;

    int h = contaFolhas(a->esq);
    int g = contaFolhas(a->dir);

    return h + g;
}

// 43
ABin cloneMirror(ABin a) {
    if (a == NULL) return NULL;

    ABin new = malloc(sizeof(struct nodo));
    if (new == NULL) return NULL;

    new->valor = a->valor;
    new->esq = cloneMirror(a->dir);
    new->dir = cloneMirror(a->esq);

    return new;
}

// 44
int addOrd(ABin *a, int x) {
    if (*a == NULL) {
        ABin new = malloc(sizeof(struct nodo));
        if (new == NULL) return -1; 
        new->valor = x;
        new->esq = NULL;
        new->dir = NULL;
        *a = new;
        return 0;
    }

    ABin cur = *a;
    ABin anterior = NULL;

    while (cur != NULL) {
        if (cur->valor < x) {
            anterior = cur;
            cur = cur->dir;
        } else if (cur->valor > x) {
            anterior = cur;
            cur = cur->esq;
        } else {
            return 1; 
        }
    }

    ABin new = malloc(sizeof(struct nodo));
    if (new == NULL) return -1; 
    new->valor = x;
    new->esq = NULL;
    new->dir = NULL;

    if (anterior != NULL) {
        if (anterior->valor < x) {
            anterior->dir = new;
        } else {
            anterior->esq = new;
        }
    }

    return 0;
}

// 45
int lookupAB (ABin a, int x) {
    while (a != NULL) {
        if (a->valor == x) return 1;
        else if (a->valor > x) a = a->esq;
        else if (a->valor < x) a = a->dir;
    }

    return 0;
}

// 46
int depthOrd(ABin a, int x) {
    if (!lookupAB(a, x)) return -1;
    int lvl = 1;

    while (a != NULL) {
        if (a->valor == x) return lvl;
        else if (a->valor > x) {
            a = a->esq;
            lvl++;
        } else if (a->valor < x) {
            a = a->dir;
            lvl++;
        }
    }

    return lvl;
}

// 47
int maiorAB(ABin a) {
    if (a == NULL) return -1;
    while (a->dir != NULL) a = a->dir;
    return a->valor;
}

// 48
void removeMaiorA(ABin *a) {
    if (*a == NULL) return;

    if ((*a)->dir == NULL) {
        ABin temp = *a;
        *a = (*a)->esq;
        free(temp);
    } else {
        while ((*a)->dir->dir != NULL) a = &(*a)->dir;
        ABin temp = (*a)->dir;
        (*a)->dir = NULL;
        free(temp);
    }
}

// 49
int quantosMaiores(ABin a, int x) {
    if (a == NULL) return 0;

    if (a->valor <= x) return quantosMaiores(a->dir, x);
    else return 1 + quantosMaiores(a->esq, x) + quantosMaiores(a->dir, x);
}

// 50
void listToBTree(LInt l, ABin *a) {
    if (l == NULL) {
        *a = NULL;
        return;
    }

    LInt f = l, s = l, ant = NULL;
    while (f != NULL && f->prox != NULL) {
        ant = s;
        s = s->prox;
        f = f->prox->prox;
    }

    if (ant != NULL) ant->prox = NULL;

    *a = malloc(sizeof(struct nodo));
    (*a)->valor = s->valor;
    listToBTree(l, &((*a)->esq)); 
    listToBTree(s->prox, &((*a)->dir));
}

// 51
int deProcura(ABin a) {
    if (a == NULL) return 1;

    if (a->esq != NULL && (a->esq->valor >= a->valor || !deProcura(a->esq)))
        return 0;

    if (a->dir != NULL && (a->dir->valor <= a->valor || !deProcura(a->dir)))
        return 0;

    return 1;
}

// Main
int main(void) {
    return 0;
}