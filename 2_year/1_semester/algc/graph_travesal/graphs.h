 

#include <stdio.h>
#include <stdlib.h>

#define NE 0

#define MAX 100

typedef int WEIGHT;

struct edge {
  int dest;
  WEIGHT weight;
  struct edge *next;
};

typedef struct edge *GraphL[MAX];

typedef WEIGHT GraphM[MAX][MAX];


int readGraphM(GraphM);
void graphMtoL(GraphL, GraphM, int);


#define WHITE 0
#define GRAY 1
#define BLACK 2


void df_visit(GraphL g, int s); 
void dfs(GraphL g, int n); 
void bf_visit(GraphL g, int s); 
void bfs(GraphL g, int n); 


void dists (GraphL g, int s, int dist[]);
int shortestPath (GraphL g, int s, int  d, int p[]);
int maisLonga (GraphL g, int s, int path[]);
void componentes(GraphL g, int n, int comp[]);

int topSort_Tarjan (GraphL, int, int []);
int topSort_Kahn (GraphL, int, int []);


void dists_sol (GraphL g, int s, int dist[]);
int shortestPath_sol (GraphL g, int s, int  d, int p[]);
int maisLonga_sol (GraphL g, int s, int path[]);
void componentes_sol(GraphL g, int n, int comp[]);

