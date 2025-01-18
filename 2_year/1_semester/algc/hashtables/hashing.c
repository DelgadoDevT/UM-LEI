#include <stdio.h>
#include <stdlib.h>
#include <string.h>


#include "hashing.h"


int hash(char key[], int size) 
{
    int i=0, sum=0;
    while(key[i] != '\0') sum += key[i++];
    return (sum % size);
}


void initHT(HT *h, int size) 
{
    h->tbl = calloc(size, sizeof(struct pair));
    if (h->tbl == NULL) return;
    h->used = 0;
    h->size = size;
    
    int i;
    for (i = 0; i < size; i++) strcpy(h->tbl[i].key, EMPTY);
}


int freeHT(HT *h, int k) 
{
    if ((strcmp(h->tbl[k].key,EMPTY) == 0) || (strcmp(h->tbl[k].key, DELETED) == 0)) return 1;
    return 0;
}



int writeHT (HT *h, char key[], int value) 
{
    // float alpha = h->used / h->size;
    // if (alpha > 0.8) extendHT(h);
    
    int hashed = hash(key, h->size);
    while (!freeHT(h, hashed)) hashed = (hashed + 1) % (h->size);
    
    strcpy(h->tbl[hashed].key, key);
    h->tbl[hashed].value = value;
    
    return hashed;
}

int readHT (HT *h, char key[], int* value) 
{
    int hashed = hash(key, h->size);
    int original_hashed = hashed;
    
    while (strcmp(h->tbl[hashed].key, EMPTY) != 0)
    {
        if (strcmp(h->tbl[hashed].key, key) == 0) 
        {
            *value = h->tbl[hashed].value;
            return hashed;
        }
        hashed = (hashed + 1) % (h->size);
        if (hashed == original_hashed) break;
    }
    
    return -1;
}

int deleteHT (HT *h, char key[]) 
{
    int value;
    int ind = readHT(h, key, &value);
    
    if (ind == -1)
    {
        return -1;
    }
    else
    {
        strcpy(h->tbl[ind].key, DELETED);
        h->used--;
        return ind;
    }
}




