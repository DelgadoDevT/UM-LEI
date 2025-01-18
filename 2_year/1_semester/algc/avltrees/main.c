//
//  main.c
//  AVL Trees
//
//  Created by Jorge Sousa Pinto on 24/3/17.
//

#include <stdio.h>
#include "avl.h"


int main() {
    
    Tree t = NULL;
    int cresceu,i;
    

    for (i=1023;i>0;i--)
        t=insertTree(t,i,&cresceu);
    
    for (i=1024;i<2048;i++)
        t=insertTree(t,i,&cresceu);

    // in the following the function should be replaced by treeHeight
    printf("Altura: %d\n",treeHeight(t));
    
    // in the following the function should be replaced by isAVL_opt
    printf("%d\n",isAVL_opt(t));
    
    return 0;
}

