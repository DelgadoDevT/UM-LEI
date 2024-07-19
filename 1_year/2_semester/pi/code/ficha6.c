#include <stdio.h>
#include <stdlib.h>

#define Max 10

// Static queues 

struct staticQueue {
    int front;
    int length;
    int values[Max];
};

typedef struct staticQueue QUEUE;
typedef struct staticQueue *SQueue;

void SinitQueue(SQueue q) {
    q->front = 0;
    q->length = 0;
}

int SisEmptyQ(SQueue q) {
    if (q->length == 0) return 0;
    return 1;
}

int Senqueue(SQueue q, int x) {
    if (q->length == Max) return 1;

    q->values[(q->front + q->length) % Max] = x;
    q->length++;

    return 0;
}

int Sdequeue(SQueue q, int *x) {
    if (!SisEmptyQ(q)) return 1;

    *x = q->values[q->front];
    q->front = (q->front + 1) % Max;
    q->length--;

    return 0;
}

int Sfront(SQueue q, int *x) {
    if (q->length == 0) return 1;

    *x = q->values[q->front];

    return 0;
}

void ShowSQueue(SQueue q) {
    int i, p;
    printf("%d Items: ", q->length);
    for (i = 0, p = q->front; i < q->length; i++) {
        printf("%d ", q->values[p]);
        p = (p + 1) % Max;
    }
    putchar('\n');
}

// Queues with dynamic arrays

typedef struct dinQueue {
    int size;
    int front;
    int length;
    int *values;
} *DQueue;

int dupQueue(DQueue q) {
    q->size *= 2;
    int *valores = realloc(q->values, q->size * sizeof(int)); 

    if (valores == NULL) return 1;

    int i, k;
    for (k = q->size - 1, i = q->length - 1; i >= q->front; i--, k--) {
        valores[k] = q->values[i];
    }
    
    q->front = k;

    return 0;
}

void DinitQueue(DQueue q) {
    q->front = 0;
    q->length = 0;
    q->size = Max;
    q->values = malloc(q->size * sizeof(int));
}

int DisEmptyQ(DQueue q) {
    if (q->length == 0) return 0;
    return 1;
}

int Denqueue(DQueue q, int x) {
    if (q->length == q->size) {
        if (dupQueue(q) == 1) return 1;
    }

    q->values[(q->front + q->length) % q->size] = x;
    q->length++;

    return 0;
}

int Ddequeue(DQueue q, int *x) {
    if (!DisEmptyQ(q)) return 1;

    *x = q->values[q->front];
    q->length--;
    q->front = (q->front + 1) % q->size;

    return 0;
}

int Dfront(DQueue q, int *x) {
    if (!DisEmptyQ(q)) {
        return 1;
    }

    *x = q->values[q->front];

    return 0;
}

void ShowDQueue(DQueue q){
    int i, p;
    printf("%d Items: ", q->length);
    for (i = 0, p = q->front; i < q->length; i++) {
        printf("%d ", q->values[p]);
        p = (p + 1) % q->size;
    }
    putchar('\n');
}

int main() {
    int i;

    QUEUE q1;
    SQueue Q1 = &q1;
  
    printf("Testing Queues .... \n");
    SinitQueue(Q1);
    for (i = 0; i < 15; i++) {
        if (Senqueue(Q1, i) != 0) printf("ERROR enqueueing %d\n", i);
    }
    ShowSQueue(Q1);

    struct dinQueue r1;
    DQueue R1 = &r1;
    
    DinitQueue(R1);
    for (i = 0; i < 15; i++) {
        if (Denqueue(R1, i) != 0) printf("ERROR enqueueing %d\n", i);
    }
    ShowDQueue(R1);

    return 0;
}