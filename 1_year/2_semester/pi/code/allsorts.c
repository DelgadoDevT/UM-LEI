#include <stdio.h>

// Insertion Sort
void insert(int x, int arr[], int n) {
    for (; n > 0 && x < arr[n - 1]; n--) {
        arr[n] = arr[n - 1];
    }
    arr[n] = x;
}

void isort(int arr[], int N) {
    for (int i = 0; i < N; i++) {
        int x = arr[i];
        insert(x, arr, i);
    }
}

// Selection Sort
void swap(int a[], int g, int h) {
    int k = a[g];
    a[g] = a[h];
    a[h] = k;
}

int max(int a[], int n) {
    int m = 0;
    for (int i = 1; i < n; i++)  
        if (a[i] > a[m]) m = i;
    return m;
}

void ssort(int a[], int n) {
    for (int i = n; i > 0; i--)
        swap(a, i - 1, max(a, i));
}

// Bubble Sort
void bubble(int a[], int n) {
    for (int i = 1; i < n; i++) {
        if (a[i - 1] > a [i]) 
            swap(a, i - 1, i);
    }
}

void bsort(int a[], int n) {
    for (int i = n; i > 0; i--) 
        bubble(a, i);
}

// Merge Sort
void merge(int a[], int na, int b[], int nb, int r[]) {
    int i = 0, j = 0, k = 0;

    while (i < na && j < nb) {
        if (a[i] <= b[j]) {
            r[k++] = a[i++];
        } else {
            r[k++] = b[j++];
        }
    }

    while (i < na) {
        r[k++] = a[i++];
    }

    while (j < nb) {
        r[k++] = b[j++];
    }
}

void copy(int aux[], int a[], int n) {
    for (int i = 0; i < n; i++) a[i] = aux[i];
}

void msort(int a[], int n) {
    if (n < 2) return;
    int m = n / 2;
    int aux[n];
    msort(a, m);
    msort(a + m, n - m);
    merge(a, m, a + m, n - m, aux);
    copy(aux, a, n);
}

// Quick Sort
int partition(int a[], int n, int x) {
    int i = -1;

    for (int j = 0; j < n; j++) {
        if (a[j] <= x) {
            i++;
            swap(a, i, j);
        }
    }

    return i + 1;
}

void qsort(int a[], int n) {
    if (n < 2) return;
    int p = partition(a, n - 1, a[n - 1]);
    swap(a, p, n - 1);
    qsort(a, p);
    qsort(a + p + 1, n - p - 1);
}

// Main for tests
int main(void) {
    int N = 10;
    int arr[10] = {3, 1, 5, 7, 8, 2, 9, 10, 4, 6};
    char input;
    int control = 1;

    scanf("%c", &input);

    switch(input) {
        case 'i' : printf("Insertion Sort: "); isort(arr, N); break; 
        case 's' : printf("Selection Sort: "); ssort(arr, N); break; 
        case 'b' : printf("Bubble Sort: "); bsort(arr, N); break;
        case 'm' : printf("Merge Sort: "); msort(arr, N); break;
        case 'q' : printf("Quick Sort: "); qsort(arr, N); break;
        default : printf("Failure!, sort algorithm not found\n"); control = 0; break;
    }

    if (control) {
        for (int h = 0; h < N; h++) printf("%d ", arr[h]);
        printf("\n");
    }

    return 0;
}