//
//  mst.h
//
//  Created by Jorge Sousa Pinto on 22/5/15.
//  Copyright (c) 2015 Universidade do Minho. All rights reserved.
//

#ifndef C_Xcode_mst_h
#define C_Xcode_mst_h


#include <stdio.h>
#include <stdlib.h>

#define NE 0
#define MAX 20

typedef int WEIGHT;

struct edge {
    int dest;
    WEIGHT weight;
    struct edge *next;
};

typedef struct edge *GraphL[MAX];

typedef WEIGHT GraphM[MAX][MAX];


void graphMtoL(GraphL, GraphM, int);

int prim(GraphL g, int n, int parent[]);
int dijkstra(GraphL g, int n, int parent[]);
int mst_sol(GraphL g, int n, int parent[]);


#endif