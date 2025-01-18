


#include "graphs.h"


int readGraphM (GraphM g) {
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




void graphMtoL (GraphL gl, GraphM gm, int n) {
    int i, j;
    struct edge *head, *new;
    
    for (i = 0; i<n; i++) {
        head = NULL;
        for (j = n-1; j>=0; j--) 
            if (gm[i][j] != NE) {
                new = malloc(sizeof(struct edge));
                new -> dest = j;
                new -> weight = gm[i][j];
                new -> next = head;
                head = new;
            }
        gl[i] = head;
    }
}

