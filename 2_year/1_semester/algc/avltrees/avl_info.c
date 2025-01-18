#include "avl.h"

int nonAVL_treeHeight(Tree t) {
    int l, r;
    
    if (t==NULL)
        return 0;
    
    l = nonAVL_treeHeight(t->left);
    r = nonAVL_treeHeight(t->right);
    if (l>r)
        return l+1;
    else return r+1;
    
}

int treeHeight(Tree t) {
    int l, r;
    if (t == NULL) return 0;
    
    l = treeHeight(t->left);
    r = treeHeight(t->right);
    
    return (l > r) ? l + 1 : r + 1;
}

int isAVL (Tree t) {
    int l, r;
    if (t == NULL) return 1;
    
    l = nonAVL_treeHeight (t->left);
    r = nonAVL_treeHeight (t->right);
    
    return (abs (l-r) <= 1 &&
            isAVL(t->left) &&
            isAVL(t->right));
}



int isAVL_aux (Tree t, int *p) 
{
    int l, r;
    if (*p == 0) return 0;
    if (t == NULL) return 1;
    
    l = treeHeight(t->left);
    r = treeHeight(t->right);
    if (abs(l - r) > 1) *p = 0;  
    
    return (isAVL_aux(t->left, p) && isAVL_aux(t->right, p));
}


int isAVL_opt (Tree a) {
    int p = 1;
    return (isAVL_aux (a, &p));
}


