#include <stdio.h>
#include <assert.h>

// 1

float multInt1(int n, float m) {
    float r = 0; 

    for (int i = 1; i <= n; i++) {
        r += m;  
    }

    return r;
}

// 2

float multInt2 (int n, float m) {
    float r = 0;
    int c = 0;

    while (n > 0) {
        if ((n % 2) != 0) {
            r = r + m;
            c = c + 2;
        } else {
            c = c + 1;
        }
        m = m * 2;
        n = n / 2;
    }

    return r;
}

// 3

int mdc1(int a, int b) {
    int me = (a < b) ? a : b;

    while (me > 0) {
        if (a % me == 0 && b % me == 0) {
            break;
        }
        me--;
    }

    return me;
}

// 4

int mdc2(int a, int b) {
    while (a != 0 && b != 0) {
        if (a > b) {
            a -= b;
        } else if (a < b) {
            b -= a;
        } else {
            return a;
        }
    }

    if (a == 0) {
        return b;
    }

    return a;
}


// 5

int mdc3(int a, int b) {
    while (a != 0 && b != 0) {
        if (a > b) {
            a %= b;
        } else if (a < b) {
            b %= a;
        } else {
            return a;
        }
    }

    if (a == 0) {
        return b;
    }

    return a;
}

// 6 

// a

int fib1(int n) {
    if (n < 2) {
        return 1;
    }
    return fib1(n-1) + fib1(n-2);
}

// b

int fib2(int n) {
    int a, b;
    a = b = 1;

    for (int i = 2; i <= n; i++) {
        b = b + a;
        a = b - a;
    }

    return b;
}

int main() {
    int n = 5;
    float m = 4.0;

    float r = multInt1(n, m);
    assert(r == 20);

    n = 27;
    m = 12;

    float r2 = multInt2(n, m);
    assert(r2 == 324);

    int r3 = mdc1(8, 12);
    assert(r3 == 4);

    assert(mdc1(50, 55) == 5);

    assert(mdc2(8, 16) == 8);
    assert(mdc2(8, 12) == 4);
    assert(mdc2(50, 55) == 5);
    assert(mdc2(21, 7) == 7);

    assert(fib1(6) == 13);
    assert(fib1(7) == 21);
    assert(fib1(8) == 34);
    assert(fib1(9) == 55);
    assert(fib1(10) == 89);
    assert(fib1(11) == 144);

    assert(fib2(6) == 13);
    assert(fib2(7) == 21);
    assert(fib2(8) == 34);
    assert(fib2(9) == 55);
    assert(fib2(10) == 89);
    assert(fib2(11) == 144);

    return 0;
}