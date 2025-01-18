//
//  main.c
//  Directed Graph Representations and Traversal Algorithms
//
//  Created by Jorge Sousa Pinto on 2/12/15.
//

#include <stdio.h>

/* Main function of the C program. */

#include <stdio.h>
#include <stdlib.h>

#include "graphs.h"

int main()
{

    GraphM gm1 = {
    {0, 1, 0, 0},
    {1, 0, 0, 0},
    {0, 0, 0, 1},
    {0, 0, 1, 0}
    } ;
    
    /*
    GraphM gm2 = {
    {0, 1, 0, 0},
    {0, 0, 1, 0},
    {0, 1, 0, 0},
    {0, 0, 0, 0}
    } ;
    */
    
    GraphM gm2 = {
    {0, 1, 1, 0},
    {0, 0, 1, 0},
    {0, 0, 0, 0},
    {0, 1, 0, 0}
    } ;
    
    GraphM gm3 = {
    {0, 1, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 1, 0, 0, 0, 1, 0, 0},
    {0, 0, 0, 0, 0, 0, 0, 1, 0},
    {0, 0, 1, 0, 1, 0, 0, 1, 0},
    {0, 0, 0, 0, 0, 0, 0, 0, 0},
    {1, 0, 0, 0, 1, 0, 0, 0, 1},
    {1, 0, 0, 0, 0, 0, 0, 0, 0},
    {0, 0, 0, 0, 0, 0, 1, 0, 1},
    {0, 0, 0, 0, 1, 0, 1, 0, 0},
    }; 
    
    GraphL gl1, gl2, gl3;
    int n1 = 4, n2 = 4, n3 = 9;
    
    int i, c, d[MAX], path[MAX], comp[MAX];
    int cycle, tsort[MAX];
    
    graphMtoL(gl1, gm1, n1); 
    graphMtoL(gl2, gm2, n2); 
    graphMtoL(gl3, gm3, n3); 
     

    dists (gl3, 3, d);
    printf("\nDistances :\n");
    for (i=0; i<n3; i++) 
        printf("to %d: %d\n", i, d[i]);
    
    printf("\nShortest path :\n");
    c = shortestPath(gl3, 3, 0, path);
    for (i=0; i<c; i++) 
        printf("%d\t", path[i]);
    printf("\n");
    
    printf("\nMore distant :\n");
    c = maisLonga(gl3, 3, path);
    for (i=0; i<c; i++) 
        printf("%d\t", path[i]);
    printf("\n");
    
    
    
    
    printf("\nComponents :\n");
    componentes(gl1, n1, comp);
    for (i=0; i<n1; i++) 
        printf("(%d,%d)\t", i, comp[i]);
    printf("\n\n");

    return EXIT_SUCCESS;
}
