#include "avl.h"

// requires:
// (t != NULL) && (t->left != NULL)
//
Tree rotateRight(Tree t)
{
    Tree aux = t->left;
    t->left = aux->right;
    aux->right = t;
    t = aux;
    return t;
}

// requires:
// (t != NULL) && (t->right != NULL)
//
Tree rotateLeft(Tree t)
{
    Tree aux = t->right;
    t->right = aux->left;
    aux->left = t;
    t = aux;
    return t;
}


Tree balanceRight(Tree t)
{
    if (t->right->bf==RH) {
        // Rotacao simples a esquerda
        t = rotateLeft(t);
        t->bf = EH;
        t->left->bf = EH;
    }
    else {
        //Dupla rotacao
        t->right = rotateRight(t->right);
        t=rotateLeft(t);
        switch (t->bf) {
            case EH:
                t->left->bf = EH;
                t->right->bf = EH;
                break;
            case LH:
                t->left->bf = EH;
                t->right->bf = RH;
                break;
            case RH:
                t->left->bf = LH;
                t->right->bf = EH;
        }
        t->bf = EH;
    }
    return t;
}

Tree balanceLeft(Tree t)
{
    if (t->left->bf==LH){
        // Rotação simples à direita
        t = rotateRight(t);
        t->bf=EH;
        t->right->bf=EH;
    }
    else {
        //Dupla rotacao
        t->left = rotateLeft(t->left);
        t=rotateRight(t);
        switch (t->bf) {
            case EH:
                t->left->bf = EH;
                t->right->bf = EH;
                break;
            case LH:
                t->left->bf = EH;
                t->right->bf = RH;
                break;
            case RH:
                t->left->bf = LH;
                t->right->bf = EH;
        }
        t->bf = EH;
    }
    return t;
}




Tree insertTree(Tree t, TreeEntry e, int *cresceu)
{
    if (t==NULL){
        t = (Tree)malloc(sizeof(struct treenode));
        t->entry = e;
        t->right = t->left = NULL;
        t->bf = EH;
        *cresceu = 1;
    }
    else if (e>t->entry) {
        t->right=insertTree(t->right, e, cresceu);
        if (*cresceu) {
            switch (t->bf) {
                case LH:
                    t->bf = EH;
                    *cresceu = 0;
                    break;
                case EH:
                    t->bf = RH;
                    *cresceu = 1;
                    break;
                case RH:
                    t=balanceRight(t);
                    *cresceu = 0;
            }
        }
    }
    else {
        t->left=insertTree(t->left,e,cresceu);
        if (*cresceu) {
            switch (t->bf) {
                case LH:
                    t = balanceLeft(t);
                    *cresceu = 0;
                    break;
                case EH:
                    t->bf = LH;
                    *cresceu = 1;
                    break;
                case RH:
                    t->bf = EH;
                    *cresceu = 0;
            } 
        }
    }
    
    return t;
}
