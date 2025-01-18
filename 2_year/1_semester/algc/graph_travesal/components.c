
#include "graphs.h"

// Componentes de um grafo não-orientado

void df_visit_comp(GraphL g, int i, int c, int comp[]) {
    struct edge *p;
    comp[i] = c;
    
    for (p=g[i]; p != NULL; p = p->next) {
        if (comp[p->dest] == 0) {
            df_visit_comp(g, p->dest, c, comp);
        }
    }
}

void componentes(GraphL g, int n, int comp[]) {
    int i, c = 1;
    for (i = 0; i < n; i++) comp[i] = 0;
    for (i = 0; i < n; i++) {
        if (comp[i] == 0) df_visit_comp(g, i, c++, comp);
    }
}



//    0 -- 1    2 -- 3 -- 4     3 -- 5 -- 6
//    A    A    V    V    V     V    V    V 
