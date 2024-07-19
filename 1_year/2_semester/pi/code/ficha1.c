#include <stdio.h>
#include <math.h>

/* 

** 1 **

1 -
12 16

2 -
0 (random number)

3 -
A 65
B 66 2 50
b 98

4 -
100 200

** 2 **

1 -
3 5 

2 -
11 66

3 -
_#_#_#_#_#_#_#_#_#_#_#

4 -
0
10
110
1110
11110
111110
1111110
11111110
111111110
1111111110
11111111110
111111111110
1111111111110
11111111111110
111111111111110
1111111111111110

** 3 **

1 -

int main(int) {
    
    int s;
    scanf ("%d",&s);

    int f = s;
    int r = s;

    while (r>0) {
        if (s>0) {
            printf("#");
            s--;
        } else {
            printf("\n");
            r--;
            s = f;
        }
    }
    
    return 0;
}

2 -

int main(int) {
    
    int s;
    scanf ("%d",&s);

    int f = s;
    int r = s;
    int h = 1;

    while (r>0) {
        if (s>0) {
            if (h == 1) {
                printf("#");
                h--;
                s--;
            } else {
                printf("_");
                h++;
                s--;
            }
        } else {
            printf("\n");
            r--;
            s = f;
        }
    }
    
    return 0;
}

-- 3

-- a

int main(int) {

    int s;
    scanf ("%d",&s);

    int a = 0;
    int b = 1;

    while (b < s) {
        while (a < b) {
            printf("#");
            a++;
        }
        a = 0;
        b++;
        printf("\n");
    }

    a = 10;
    b = 5;

    while (a > s) {
        while (b < a) {
            printf("#");
            b++;
        }
        b = 5;
        a--;
        printf("\n");
    }

    return 0;
}

-- b

int main() {
    
    int s;
    scanf("%d", &s);

    int a = s - 1; 
    int f = 1;  
    int g, d;

    while (s > 0) {
        g = 0;
        d = 0;

        while (g < a) {
            printf(" ");
            g++;
        }

        while (d < f) {
            printf("#");
            d++;
        }

        s--;
        a--;
        f += 2;
        printf("\n");
    }

    return 0;
}

-- ** 4 **

*/

int dCircles(int r) {
    int p = 0;

    for (int i = -r; i <= r; i++) {
        for (int j = -r; j <= r; j++) {
            if (i*i + j*j <= r*r) {
                printf("#");
                p++;
            } else {
                printf(" ");
            }
        }
        printf("\n");
    }

    return p;
}

int main() {
    int r;
    scanf("%d", &r);

    int re = dCircles(r);

    printf("\n%d Hashes Printed \n", re);

    return 0;
}