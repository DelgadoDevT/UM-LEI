
#include "graphs.h"

int color[MAX];



void df_visit(GraphL g, int s) {
    struct edge *p;
    
    color[s] = GRAY;
    printf("%d GRAY\n", s);
    
    for (p=g[s]; p;  p=p->next) {
        if (color[p->dest] == WHITE)
            df_visit (g, p->dest);
    }
 //   
    color[s] = BLACK;
    printf("%d BLACK\n", s);
}


void dfs(GraphL g, int n) {
    int u;
    
    for (u=0; u<n; u++)
        color[u] = WHITE;
    
    for (u=0; u<n; u++)
        if (color[u] == WHITE) df_visit (g, u);

}



