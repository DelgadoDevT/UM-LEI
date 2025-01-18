//
//  conversion.c
//  Directed Graph Representations and Traversal Algorithms
//
//  Created by Jorge Sousa Pinto on 2/12/15.
//  Copyright © 2015 Universidade do Minho. All rights reserved.
//

#include "mst.h"

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



// assume listas de adjacência ordenadas
// por ordem crescente do vértice destino
void graphLtoM(GraphM gm, GraphL gl, int n) {
    int i, j;
    struct edge *p;
    
    for (i = 0; i<n; i++)
        for (j = 0, p = gl[i]; j<n; j++) {
            if (p && p->dest == j) {
                gm[i][j] = p->weight;
                p = p -> next;
            }
            else
                gm[i][j] = NE;
        }
}



void graphMtoL(GraphL gl, GraphM gm, int n) {
    int i, j;
    struct edge *p, *new;
    
    for (i = 0; i<n; i++) {
        p = NULL;
        for (j = n-1; j>=0; j--)
            if (gm[i][j] != NE) {
                new = malloc(sizeof(struct edge));
                new -> dest = j;
                new -> weight = gm[i][j];
                new -> next = p;
                p = new;
            }
        gl[i] = p;
    }
}


// assume listas de adjacência ordenadas
// por ordem crescente do vértice destino
int inDegree(GraphL g, int j, int n) {
    int i, k = 0;
    struct edge *p;
    
    for (i=0; i<n; i++)
        for (p = g[i]; p && p->dest <= j ; p=p->next)
            if (p->dest == j) k++;
    
    return k;
}



int outDegree(GraphL g, int i) {
    int n = 0;
    struct edge *p;
    
    for (p = g[i]; p ; p=p->next) n++;
    
    return n;
}

