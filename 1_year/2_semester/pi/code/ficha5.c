#include <stdio.h>
#include <math.h>
#include <string.h>

typedef struct aluno {
    int numero;
    char nome[100];
    int miniT[6];
    float teste;
} Aluno;

// 1

int nota(Aluno a) {
    float m = 0;
    float n = a.teste;
    int h;

    for (int i = 0; i < 6; i++) {
        m += a.miniT[i];
    }

    if (m < 3) return 0;

    m = ((m * 20) / 12) * 0.2;
    n *= 0.80;

    h = round(m + n);

    if (h >= 10) return h; 
    else return 0;
}

// 2

void swap(Aluno a[], int i, int j);

void ordenaPorNum(Aluno a[], int n);

int procuraNum(int x, Aluno a[], int n) {
    ordenaPorNum(a, n);
    int l = 0, u = n-1, m;

    while (l <= u) {
        m = (l + u) / 2;
        if (a[m].numero == x) return m;
        if (a[m].numero < x) l = m + 1;
        else u = m - 1;
    }
    return -1;
}

// 3

void swap(Aluno a[], int i, int j) {
    Aluno temp = a[i];
    a[i] = a[j];
    a[j] = temp;
}

void ordenaPorNum(Aluno a[], int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (a[j].numero > a[j + 1].numero) {
                swap(a, j, j + 1);
            }
        }
    }
}

// 4

void swap2(int v[], int i, int j) {
    int tmp = v[i];
    v[i] = v[j];
    v[j] = tmp;
}

void criaIndPorNum(Aluno t[], int N, int ind[]) {
    for (int i = 0; i < N; i++) {
        ind[i] = i;
    }

    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N - i - 1; j++) {
            if (t[ind[j]].numero > t[ind[j + 1]].numero) {
                swap2(ind, j, j + 1);
            }
        }
    }
}

// 5

void imprimeAluno (Aluno *a){
    int i;
    printf ("%-5d %s (%d", a->numero, a->nome, a->miniT[0]);
    for(i=1; i<6; i++) printf (", %d", a->miniT[i]);
    printf (") %5.2f %d\n", a->teste, nota(*a));
}

void imprimeTurma(int ind[], Aluno t[], int N) {
    criaIndPorNum(t, N, ind); 

    for (int i = 0; i < N; i++) {
        imprimeAluno(&t[ind[i]]);
    }
}

// 6

int procuraNumInd(int num, int ind[], Aluno t[], int N) {
    int l = 0, u = N - 1, m;

    while (l <= u) {
        m = (l + u) / 2;
        if (t[ind[m]].numero == num)
            return m;
        if (t[ind[m]].numero < num)
            l = m + 1; 
        else
            u = m - 1;
    }
    return -1;
}

// 7

void criaIndPorNome(Aluno t[], int N, int ind[]) {
    for (int i = 0; i < N; i++) {
        ind[i] = i;
    }

    for (int i = 0; i < N; i++) {
        for (int j = 0; j < N - i - 1; j++) {
            if (strcmp(t[ind[j]].nome, t[ind[j + 1]].nome) > 0) {
                swap2(ind, j, j + 1);
            }
        }
    }
}

// Main

int main() {
    Aluno a1 = {123, "Roger Griffith", {2, 2, 2, 2, 2, 2}, 16};
    Aluno turma1[7] = {{4444, "André", {2,1,0,2,2,2}, 10.5}
                       ,{3333, "Paulo", {0,0,2,2,2,1},  8.7}
                       ,{8888, "Carla", {2,1,2,1,0,1}, 14.5}
                       ,{2222, "Joana", {2,0,2,1,0,2},  3.5}
                       ,{7777, "Maria", {2,2,2,2,2,1},  5.5}
                       ,{6666, "Bruna", {2,2,2,1,0,0}, 12.5}
                       ,{5555, "Diogo", {2,2,1,1,1,0},  8.5}
                       } ;
    Aluno turma2[7] = {{4444, "André", {2,1,0,2,2,2}, 10.5}
                       ,{3333, "Paulo", {0,0,2,2,2,1},  8.7}
                       ,{8888, "Carla", {2,1,2,1,0,1}, 14.5}
                       ,{2222, "Joana", {2,0,2,1,0,2},  3.5}
                       ,{7777, "Maria", {2,2,2,2,2,1},  5.5}
                       ,{6666, "Bruna", {2,2,2,1,0,0}, 12.5}
                       ,{5555, "Diogo", {2,2,1,1,1,0},  8.5}
                       } ;
    
    int ind[7];

    printf("Ficha 5 - Structs e Ordenações\n");

    printf("1- %d\n", nota(a1));

    printf("2 - %d\n", procuraNum(7777, turma1, 7));

    ordenaPorNum(turma1, 7);
    printf("5 - ");
    for (int i = 0; i < 7; i++) {
        printf("%d ", turma1[i].numero);
    }

    criaIndPorNum(turma2, 7, ind);
    printf("\n4 - ");
    for (int i = 0; i < 7; i++) {
        printf("%d ", ind[i]);
    }

    printf("\n5 -\n");
    imprimeTurma(ind, turma1, 7);

    printf("6 - %d\n", procuraNumInd(6666, ind, turma1, 7));

    criaIndPorNome(turma2, 7, ind);
    printf("7 - ");
    for (int h = 0; h < 7; h++) {
        printf("%d ", ind[h]);
    }
    printf("\n");

    return 0;
}