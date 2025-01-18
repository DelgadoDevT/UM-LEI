#include <stdlib.h>
#include "minheap.h"

void initHeap (Heap *h, int size) 
{
    h->size = size;
    h->used = 0;
    h->values = malloc(sizeof(size));
}

void bubbleUp (Elem h[], int i) 
{
    while (h[PARENT(i)] > h[i])
    {
        int temp = h[PARENT(i)];
        h[PARENT(i)] = h[i];
        h[i] = temp;
    }
}

int insertHeap(Heap *h, Elem x) 
{
    if (h->used == h->size)
    {
        h->values = realloc(h->values, h->size * 2 * sizeof(Elem));
        h->size *= 2;
    }
    
    h->values[h->used] = x;
    h->used++;
    
    bubbleUp(h->values, h->used - 1);
    return 1;
}

void bubbleDown(Elem h[], int N) 
{
    int i = 0;
    while (i < N)
    {
        int left = LEFT(i);
        int right = RIGHT(i);
        int smallest = i;
        
        if (left < N && h[left] < h[smallest]) smallest = left;
        if (right < N && h[right] < h[smallest]) smallest = right;
        
        if (smallest != i)
        {
            Elem temp = h[i];
            h[i] = h[smallest];
            h[smallest] = temp;
            i = smallest;
        }
        else
        {
            break;
        }
    }
}

int extractMin(Heap *h, Elem *x) 
{
    if (h->used > 0)
    {
        *x = h->values[0];
        h->values[0] = h->values[h->used - 1];   
        h->used--;
        bubbleDown(h->values, h->used);
        return 1;
    }
    return 0;
}

int minHeapOK(Heap h) 
{ 
    int i;
    for (i = 0; i < h.used; i++)
    {
        if (LEFT(i) < h.used && h.values[i] > h.values[LEFT(i)]) return 0;
        if (RIGHT(i) < h.used && h.values[i] > h.values[RIGHT(i)]) return 0;
    }
    return 1;
}