#include <stdio.h>
#include <math.h>

/*
1 
a - 
1 1 4
2 2 6
3 3 8
4 4 10
5 5 12

b -
13
*/ 

// 2

void swapM (int *x, int *y) {
    int h = *y;
    *y = *x;
    *x = h;
}

// 3

void swap (int v[], int i, int j) {
    int h = v[i];
    v[i] = v[j];
    v[j] = h;
}

// 4

int soma (int v[], int N) {
    int s = 0;

    for (int i = 0; i < N; i++) {
        s += v[i];
    }

    return s;
}

// 5

void inverteArray1(int v[], int N) {
    for (int i = 0; i < N / 2; i++) {
        swapM(&v[i], &v[N - i - 1]);
    }
}

void inverteArray2(int v[], int N) {
    for (int i = 0; i < N / 2; i++) {
        swap(v, i, N - i - 1);
    }
}

// 6

int maximum(int v[], int N, int *m) {
    int h = 0;

    for (int i = 0; i < N; i++) {
        if (v[i] > h) h = v[i]; 
    }

    *m = h;
}

// 7

void quadrados(int q[], int N) {
    for (int i = 0; i < N; i++) {
        q[i] = pow(q[i], 2);
    }
}

// 8

void pascal(int v[], int N) {
    if (N == 1) {
        v[0] = 1;
        printf("1\n");
    } else {
        int a[N - 1];
        pascal(a, N - 1);

        v[0] = 1;
        v[N - 1] = 1;

        int i;
        for (i = 1; i < N - 1; i++) {
            v[i] = a[i] + a[i - 1];
        }

        int j;
        for (j = 0; j < N; j++) {
            printf("%d ", v[j]);
        }
        printf("\n");
    }
}

int main() {
    int x = 3;
    int y = 5;

    swapM (&x, &y);
    printf ("%d %d\n", x, y);

    int v[5] = {1,2,3,4,5};
    swap (v, 2, 3);
    for (int i = 0; i < 5; i++) {
        printf ("%d",v[i]);
    }
    printf("\n");

    int g[5] = {2,4,6,8,10};
    int n = soma (g, 5);
    printf ("%d\n",n);

    int k[5] = {45,23,423,5,7};
    inverteArray1(k, 5);
    for (int i = 0; i < 5; i++) {
        printf ("%d", k[i]);
    }

    printf("\n");

    int b[8] = {1,2,3,4,5,6,7,8};
    inverteArray2(b, 8);
    for (int i = 0; i < 8; i++) {
        printf ("%d", b[i]);
    }

    printf("\n");

    int c[5] = {3,2,5,9,1};
    int m;
    maximum(c, 5, &m);
    printf("%d\n",m);

    int d[4] = {2,4,6,8};
    quadrados(d, 4);
    for (int i = 0; i < 4; i++) {
        printf ("%d", d[i]);
    }

    printf("\n");

    int p[1] = {1};
    pascal(p, 6);

    return 0;
}