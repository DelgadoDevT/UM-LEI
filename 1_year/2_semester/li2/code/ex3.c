#include <stdio.h>
#include <string.h>
#include <math.h>
#include <assert.h>

char check(char Exp[], int E, char Mant[], int M) {
    int aoe = 1;
    int aze = 1;
    int azm = 1;

    for (int i = 0; i < E; i++) {
        if (Exp[i] == '0') {
            aoe = 0;  
        } else {
            aze = 0;  
        }
    }

    for (int i = 0; i < M; i++) {
        if (Mant[i] == '1') {
            
            azm = 0;
        }
    }

    if (aoe && !azm) {
        return 'e';
    } else if (aoe && azm) {
        return 'i';
    } else if (aze && azm) {
        return '0';
    } else if (aze && !azm) {
        return 'd';
    } else {
        return 'n';
    }
}

double mantissaC(char Mant[], int M) {
    double f = 0.0;
    double pw = 0.5; 

    for (int i = 0; i < M; i++) {
        if (Mant[i] == '1') {
            f += pw;
        }
        pw *= 0.5;
    }

    return f;
}

double exc(int E) {
    return pow(2, (E - 1)) - 1;
}

double calcD(double s, double f, double e) {
    return pow(-1, s) * f * pow(2, 1 - e);
}

double decep(char Exp[], int E) {
    double v = 0;
    int h = E - 1;
    for (int i = 0; i < E; i++) {
        if (Exp[i] == '1') {
            v += pow(2,h);
            h--;
        } else {
            h--;
        }
    }
    return v;
}

double calcN(double s, double f, double v, double e) {
    return (pow(-1, s) * (1 + f) * pow(2, v - e));
}

int main() {
    int g;

    if (scanf("%d", &g) == 1) {
        char buf[BUFSIZ];
        assert(fgets(buf, BUFSIZ, stdin) != NULL);

        for (int i = 2; i <= g + 1; ++i) {
            if (i > 1) {
                assert(fgets(buf, BUFSIZ, stdin) != NULL);
            }

            assert(buf[strlen(buf) - 1] == '\n');
            buf[strlen(buf) - 1] = 0;
            int E, M;
            char bits[BUFSIZ] = {0};

            sscanf(buf, "%d %d %s", &E, &M, bits);

            char Sinal;
            char Exp[BUFSIZ] = {0};
            char Mant[BUFSIZ] = {0};

            Sinal = bits[0];
            strncpy(Exp, bits + 1, E);
            strncpy(Mant, bits + 1 + E, M);

            double res;

            switch (check(Exp, E, Mant, M)) {
                case '0':
                    if (Sinal == '1') {
                        printf("-0\n");
                    } else {
                        printf("0\n");
                    }
                    break;
                case 'd':
                    if (Sinal == '1') {
                        res = calcD(-1, mantissaC(Mant, M), exc(E));
                        printf("%lg\n", res);ail
                    } else {
                        res = calcD(0, mantissaC(Mant, M), exc(E));
                        printf("%lg\n", res);
                    }
                    break;
                case 'i':
                    if (Sinal == '1') {
                        printf("-Infinity\n");
                    } else {
                        printf("+Infinity\n");
                    }
                    break;
                case 'e':
                    printf("NaN\n");
                    break;
                case 'n':
                    if (Sinal == '1') {
                        res = calcN(-1, mantissaC(Mant, M), decep(Exp, E) ,exc(E));
                        printf("%lg\n", res);
                    } else {
                        res = calcN(0, mantissaC(Mant, M), decep(Exp, E) ,exc(E));
                        printf("%lg\n", res);
                    break;
                    }
            }
        }
    }

    return 0;
}