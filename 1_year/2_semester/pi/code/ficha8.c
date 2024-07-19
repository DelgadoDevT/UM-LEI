#include <stdio.h>
#include <stdlib.h>

typedef struct slist {
    int valor;
    struct slist * prox;
} * LInt;

typedef LInt Stack;

typedef struct {
    LInt inicio,fim;
} Queue;

typedef LInt QueueC;

void initStack (Stack *s){
    *s = NULL;
}

int SisEmpty (Stack s){
    return (s == NULL);
}

int push(Stack *s, int x) {
    LInt c = malloc(sizeof(struct slist));

    if (c == NULL) return 1;
    c->valor = x;
    c->prox = *s;
    *s = c;

    return 0;
}

int pop(Stack *s, int *x) {
    if (SisEmpty(*s)) {
        return 1;
    }
    LInt aux = *s;
    (*x) = aux->valor;
    (*s) = aux->prox;
    free(aux);
    return 0;
}

int top (Stack s, int *x){
    if (SisEmpty) return 1;
    *x = s->valor;
    return 0;
}

void initQueue (Queue *q){
    q->inicio = NULL;
    q->fim = NULL;
}

int QisEmpty (Queue q){
    return (q.inicio == NULL);
}

int enqueue (Queue *q, int x){
    LInt aux = malloc(sizeof(struct slist));
    if (aux == NULL) return 1;
    aux->valor = x;
    aux->prox = NULL;

    if (q->inicio == NULL) q->inicio = aux;
    else q->fim->prox = aux; 
    q->fim = aux;
    
    return 0;
}

int dequeue (Queue *q, int *x){
    if (q->inicio == NULL) return 1;
    *x = q->inicio->valor;
    LInt aux = q->inicio->prox;
    free(q->inicio);
    q->inicio = aux;
    if (q->inicio == NULL) q->fim = NULL;

    return 0;
}

int frontQ (Queue q, int *x){
    if (q.inicio == NULL) return 1;
    *x = (q.inicio)->valor;
    return 0;
}

void initQueueC (QueueC *q) {
    *q = NULL;
}

int QisEmptyC (QueueC q){
    return (q == NULL);
}

int frontC (QueueC q, int *x){
    if (q == NULL) return 1;
    *x = q->prox->valor;

    return 0;
}

int main (){
  int i, a, b;
  Stack s; Queue q;
  printf ("_______________ Testes _______________\n\n");
  printf ("Stack:\n");
  initStack (&s);
  push (&s,1);
  push (&s,1);
  for (i=0; i<10; i++){
    pop (&s,&a); top (s,&b); push (&s,b); push (&s,a+b);
  }
  while (! SisEmpty (s)) {
    pop (&s,&a);
    printf ("%d ", a);
  }

  printf ("\nQueue:\n");
  initQueue (&q);
  enqueue (&q,1);
  enqueue (&q,1);
  for (i=0; i<10; i++){
    dequeue (&q,&a); frontQ (q,&b); enqueue (&q, a); enqueue (&q,a+b);
  }
  while (! QisEmpty (q)) {
    dequeue (&q,&a);
    printf ("%d ", a);
  }

    printf ("\n\n___________ Fim dos Testes ___________\n\n");
  return 0;
}
