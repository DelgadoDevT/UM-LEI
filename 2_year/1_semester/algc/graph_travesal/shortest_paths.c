
#include "graphs.h"




// determines shortest path from s to d
// stores vertices from s to d in array p
// returns length of the path/array 

int shortestPath (GraphL g, int s, int  d, int par[]) 
{
    struct edge *p;
    int color[MAX], q[MAX], parent[MAX], i, k, l, c = 0, first = 0, last = 0;
    
    for (i = 0; i < MAX; i++) {
        color[i] = WHITE;
        parent[i] = -1;
    }
    
    color[s] = GRAY;
    q[last++] = s;
    while (first < last) {
        k = q[first++];
        for (p = g[k]; p != NULL; p = p->next) {
            if (color[p->dest] == WHITE) {
                color[p->dest] = GRAY;
                parent[p->dest] = k;
                q[last++] = p->dest;
            }
        }
        color[k] = BLACK;
    }
    
    l = d;
    while (l != -1) {
        par[c] = l;
        l = parent[l];
        c++;
    }
    
    for (i = 0; i < c / 2; i++) {
        int temp = par[i];
        par[i] = par[c - i - 1];
        par[c - i - 1] = temp;
    }

    return c;
}


// determines distances from s to every node in the graph
// stores them in array dist

void dists (GraphL g, int s, int dist[]) 
{
    struct edge *p;
    int color[MAX], q[MAX], i, k, first = 0, last = 0;
    
    for (i = 0; i < MAX; i++) {
        color[i] = WHITE;
        dist[i] = -1; // Novo
    }
    
    dist[s] = 0; // Novo
    color[s] = GRAY;
    q[last++] = s;
    while (first < last) {
        k = q[first++];
        for (p = g[k]; p != NULL; p = p->next) {
            if (color[p->dest] == WHITE) {
                color[p->dest] = GRAY;
                dist[p->dest] = dist[k] + 1; // Novo
                q[last++] = p->dest;
            }
        }
        color[k] = BLACK;
    }
}


// determines path from s to the most distant vertex from it
// returns length of the path

int maisLonga (GraphL g, int s, int path[]) {
    struct edge *p;
    int q[MAX], parent[MAX], dist[MAX], first=0, last=0, i, u, v, c=0;
    char vis[MAX];
    
    for (u=0; u<MAX; u++) {
        dist[u] = -1;
        vis[u] = 0;
        parent[u] = -1;
    }
    
    vis[s] = 1;
    dist[s] = 0;
    q[last++] = s;
    
    while (first < last) {
        u = q[first++];
        for (p=g[u]; p;  p=p->next) {
            v = p->dest;
            if (!vis[v]) {
                vis[v] = 1;
                dist[v] = dist[u]+1;
                parent[v] = u;
                q[last++] = v;
            }
        }
    }
    
    c = dist[u]+1;
    i = c-1;
    while (i>=0)  {
        path[i] = u;
        u = parent[u];
        i--;
    }
    
    return c; 

}



