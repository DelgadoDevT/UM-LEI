//
//  main.c
//
//  Created by Jorge Sousa Pinto on 22/5/15.
//  Copyright (c) 2015 Universidade do Minho. All rights reserved.
//

#include "mst.h"


int main(int argc, const char * argv[]) 
{
    GraphL g;
    int i, n, sum, ok1, ok2;
    int parent1[MAX], parent2[MAX];
    
    GraphM gm = {
        {0, 2, 0, 0, 0, 7, 3, 0, 0},
        {2, 0, 4, 0, 0, 0, 6, 0, 0},
        {0, 4, 0, 2, 0, 0, 0, 2, 0},
        {0, 0, 2, 0, 1, 0, 0, 8, 0},
        {0, 0, 0, 1, 0, 6, 0, 0, 2},
        {7, 0, 0, 0, 6, 0, 0, 0, 5},
        {3, 6, 0, 0, 0, 0, 0, 3, 1},
        {0, 0, 2, 8, 0, 0, 3, 0, 4},
        {0, 0, 0, 0, 2, 5, 1, 4, 0},
    };
    n = 9;
    
    graphMtoL(g, gm, n);
    
    ok1 = prim(g, n, parent1);
    if (ok1) {
        printf("Prim:");
        for (i=0, sum=0 ; i<n ; i++) {
            if (parent1[i]>=0) {
                printf("\n\t%d--%d", i, parent1[i]);
                sum += gm[i][parent1[i]];
            }
        }
        printf("\nTotal weight =  = %d\n\n", sum);
    }
    else printf("UNCONNECTED GRAPH, CANNOT BUILD MST!\n");
    
    ok2 = dijkstra(g, n, parent2);
    if (ok2) {
        printf("Dijkstra:");
        for (i=0, sum=0 ; i<n ; i++) {
            if (parent2[i]>=0) {
                printf("\n\t%d--%d", i, parent2[i]);
                sum += gm[i][parent2[i]];
            }
        }
        printf("\nTotal weight =  = %d\n\n", sum);
    }
    else printf("UNCONNECTED GRAPH, CANNOT BUILD Single-Source Shortest Path!\n");

}


