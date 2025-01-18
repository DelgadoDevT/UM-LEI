

#include "graphs.h"


int inDegree (GraphL g, int j, int n) {
    struct edge *e;
    int k, count = 0;
    
    for (k = 0; k < n; k++) {
        for (e = g[k]; e != NULL; e = e->next) {
            if (e->dest == j) count++;
        }
    }
    
    return count;
}



int outDegree (GraphL g, int j) {
    struct edge *e;
    int count = 0;
    
    for (e = g[j]; e != NULL; e = e->next) count++;
    
    return count;
}


int capacidadeL (GraphL g, int v, int n) {
    struct edge *e;
    int k, cin = 0, cout = 0;
    
    for (k = 0; k < n; k++) {
        for (e = g[k]; e != NULL; e = e->next) {
            if (e->dest == v) cin += e->weight;
            if (k == v) cout += e->weight;
        }
    }
    
    return cin - cout;
}

int maxCap(GraphL g, int n) {
    int cap[n], l;
    for (l = 0; l < n; l++) cap[l] = 0;
    
    struct edge *e;
    int k;
    for (k = 0; k < n; k++) {
        for (e = g[k]; e != NULL; e = e->next) {
            if (e->dest == k) cap[k] += e->weight;
            if (k == e->dest) cap[k] -= e->weight;
        }
    }
    
    int b, max = cap[0];
    for (b = 1; b < n; b++) {
        if (cap[b] > max) max = cap[b];
    }
    
    return max;
}




