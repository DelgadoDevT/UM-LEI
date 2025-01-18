
#include <stdlib.h>
#include <stdio.h>
#include "mst.h"

#define INTREE 0
#define FRINGE 1
#define UNSEEN 2
#define INF 9999

int prim(GraphL g, int n, int parent[])
{
    struct edge *p;
    int fringe[n], status[n], i, x, edgeCount, wxy, k, local;
    
    for (i = 0; i < n; i++) {
        fringe[i] = INF;
        status[i] = UNSEEN;
        parent[i] = -1;
    }
    
    x = 0;
    status[x] = INTREE;
    fringe[x] = 0;
    
    for (edgeCount = 0; edgeCount < n - 1; edgeCount++) {
        for (p = g[x]; p != NULL; p = p->next) {
            wxy = p->weight;
            if (status[p->dest] == UNSEEN) {
                status[p->dest] = FRINGE;
                parent[p->dest] = x;
                fringe[p->dest] = wxy;
            }
            else if (status[p->dest] == FRINGE && wxy < fringe[p->dest]) {
                parent[p->dest] = x;
                fringe[p->dest] = wxy;
            }
        }
        
        local = INF;
        for (k = 0; k < n; k++) {
            if (status[k] != INTREE && fringe[k] < local) {
                local = fringe[k];
                x = k;
            }
        }
        
        if (local == INF) return 0;
        
        fringe[x] = -1;
        status[x] = INTREE;
    }
    
    return 1;
}


int dijkstra(GraphL g, int n, int parent[])
{
    struct edge *p;
    int fringe[n], status[n], dist[n], i, x, edgeCount, wxy, k, local;
    
    for (i = 0; i < n; i++) {
        fringe[i] = INF;
        status[i] = UNSEEN;
        parent[i] = -1;
        dist[i] = INF;
    }
    
    x = 0;
    status[x] = INTREE;
    fringe[x] = 0;
    dist[x] = 0;
    
    for (edgeCount = 0; edgeCount < n - 1; edgeCount++) {
        for (p = g[x]; p != NULL; p = p->next) {
            wxy = p->weight;
            if (status[p->dest] == UNSEEN) {
                status[p->dest] = FRINGE;
                parent[p->dest] = x;
                fringe[p->dest] = wxy;
                dist[p->dest] = dist[x] + wxy;
            }
            else if (status[p->dest] == FRINGE && wxy < fringe[p->dest]) {
                parent[p->dest] = x;
                fringe[p->dest] = wxy;
                dist[p->dest] = dist[x] + wxy;
            }
        }
        
        local = INF;
        for (k = 0; k < n; k++) {
            if (status[k] != INTREE && dist[k] < local) {
                local = dist[k];
                x = k;
            }
        }
        
        if (fringe[x] == INF) return 0;
        
        fringe[x] = -1;
        status[x] = INTREE;
    }
    
    return 1;   
}


    



