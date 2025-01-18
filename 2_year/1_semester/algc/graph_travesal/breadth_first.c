
#include "graphs.h"

int color[MAX];


// 1 -> 2 -> 3
// 1 -> 3

void bf_visit(GraphL g, int s) {
    struct edge *p;
    int q[MAX], first=0, last=0, u, v;
    
    color[s] = GRAY;
    printf("%d GRAY\n", s);
    q[last++] = s;
    
    while (first < last) {
        u = q[first++];
        for (p=g[u]; p;  p=p->next) {
            v = p->dest;
            if (color[v] == WHITE) {
                color[v] = GRAY;
                printf("%d GRAY\n", v);
                q[last++] = v;
            }
        }
        color[u] = BLACK;
        printf("%d BLACK\n", u);
    }
}


void bfs(GraphL g, int n) {
    int u;
    
    for (u=0; u<n; u++)
        color[u] = WHITE;
    
    for (u=0; u<n; u++)
        if (color[u] == WHITE) bf_visit (g, u);
    
}


