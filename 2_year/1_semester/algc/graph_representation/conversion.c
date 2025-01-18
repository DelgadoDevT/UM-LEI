

#include "graphs.h"

int readGraphM(GraphM g) {
    int i, j, n;

    printf("Número de vértices do grafo? (<=%d)\n", MAX);
    scanf("%d", &n);
    if (n > MAX) return 0; 

    printf("\nIntroduza matriz de adjacência do grafo:\n");
   
    for(i=0; i<n; i++)
       for(j=0; j<n; j++)
            scanf("%d", &g[i][j]);

    return n;
}

void printGraphM(GraphM g, int n) {
    int i, j;

    for(i=0; i<n; i++) {
        for(j=0; j<n; j++) 
            printf("%d ", g[i][j]);
        printf("\n");
    }
}

void printGraphL(GraphL g, int n) {
    int i;
    struct edge *p;

    for(i=0; i<n; i++) {
        printf("%d", i);
        for(p=g[i]; p; p=p->next) 
            printf(" --> (%d,%d)", p->dest, p->weight);
        printf("\n");
    }
}

void graphLtoM(GraphM gm, GraphL gl, int n) {
    int i, j;
    struct edge *node;
    
    for (i = 0; i < n; i++) {
        for (node = gl[i], j = 0; j < n && node; node = node->next, j++) {
            if (j == node->dest) gm[i][node->dest] = node->weight;
            else gm[i][j] = NE;
        }
    }
}



void graphMtoL(GraphL gl, GraphM gm, int n) {
    int i, j;
    struct edge *node, *list;
    
    for (i = 0; i < n; i++) {
        list = NULL;
        for (j = n - 1; j >= 0; j--) {
            if (gm[i][j] != NE) {
                node = malloc(sizeof(struct edge));
                node->dest = j;
                node->weight = gm[i][j];
                node->next = list;
                list = node;
            }
            gl[i] = list;
        }
    }
}


