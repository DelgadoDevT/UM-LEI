#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

// 50 Questões (Memória Automática)

// 1
int mElem(){
    int n, m = 0;

    scanf("%d", &n);

    while (n != 0) {
        if (n >= m)
            m = n;
        
        scanf("%d", &n);
    }

    return m;
}

// 2
float medSeq(){
    float n, m = 0.0, c = 0.0, r = 0.0;

    scanf("%f", &n);

    while (n != 0) {
        m += n;
        c++;
        scanf("%f", &n);
    }

    r = m / c;

    return r;
}

// 3
int twoMax() {
    int n, m = 0, s = 0;
    
    scanf("%d", &n);

    while (n != 0) {
        if (n >= m) {
            s = m;
            m = n;
        } else if (n > s && n < m) {
            s = n;
        }

        scanf("%d", &n);
    }

    return s;
}

// 4
int bitsUM(unsigned int n) {
    int c = 0;

    while (n > 0) {
        if (n % 2 == 1) c++;
        n = n / 2;
    }

    return c;
}

// 5
int trailingZ(unsigned int n) {
    int c = 0;

    while (n > 0) {
        if (n % 2 != 0) break;
        if (n % 2 == 0) c++;
        n = n / 2;
    }

    return c;
}

// 6
int qDig(unsigned int n) {
    int count = 0;
    if (n == 0) return 1;

    while (n != 0) {
        n /= 10;
        count++;
    }

    return count;
}

// 7
char *strcatQ(char s1[], char s2[]) {
    int size1 = strlen(s1), size2 = strlen(s2);

    for (int i = 0; i < size2; i++, size1++) s1[size1] = s2[i];
    s1[size1] = '\0'; 

    return s1;
}

// 8
char *strcpyQ(char *dest, char source[]) {
    int size = strlen(source), i;

    for (i = 0; i < size; i++) dest[i] = source[i];
    dest[i] = '\0'; 

    return dest;
}

// 9
int strcmpQ(char s1[], char s2[]) {
    int comp = 0, size1 = strlen(s1), size2 = strlen(s2);
    int tsize = (size1 < size2) ? size1 : size2;

    for (int i = 0; i < tsize; i++) {
        if (s1[i] > s2[i]) {
            comp = 1;
            break;
        } else if (s1[i] < s2[i]) {
            comp = -1;
            break;
        }
    }

    if (comp == 0) {
        if (size1 < size2) comp = -1;
        else if (size1 > size2) comp = 1;
    }

    return comp;
}

// 10
char *strstrQ(char s1[], char s2[]) {
    int size1 = strlen(s1), size2 = strlen(s2), ind = 0;
    
    for (int i = 0; i < size1; i++) {
        if (ind == size2) return &s1[i - size2 + 1];
        if (s1[i] == s2[ind]) ind++;
        else ind = 0;
    }

    return NULL;
}

// 11
void strrev(char s[]) {
    if (s == NULL) return;
    int end = strlen(s) - 1, start = 0;
    char c;

    while (start < end) {
        c = s[start];
        s[start] = s[end];
        s[end] = c;
        start++;
        end--;
    };
}

// 12
int isvowel(int ch) {
  int c = toupper(ch);

  return (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U');
}

void strnoV(char s[]) {
    int i, j;

    for (i = 0, j = 0; s[i] != '\0'; i++) {
        if (!isvowel(s[i])) s[j++] = s[i];
    }

    s[j] = '\0';
}

// 13
void truncW(char t[], int n) {
    int counter = 0, i, j;

    for (i = 0, j = 0; t[i] != '\0'; i++) {
        if (!isspace(t[i]) && (counter < n)) {
            t[j++] = t[i];
            counter++;
        } else if (isspace(t[i])) {
            t[j++] = t[i];
            counter = 0;
            while (isspace(t[i+1])) i++;
        }
    }

    t[j] = '\0';
}

// 14
char charMaisfreq(char s[]) {
    int count[256] = {0}; 
    
    for (int i = 0; s[i] != '\0'; i++) {
        count[s[i]]++;
    }
    
    char mFreq = '\0';
    int maxFreq = 0;
    
    for (int i = 0; i < 256; i++) {
        if (count[i] > maxFreq) {
            maxFreq = count[i];
            mFreq = (char)i;
        }
    }
    
    return mFreq;
}

// 15
int iguaisConsecutivos(char s[]) {
    int con = 1, max;

    for (int i = 1; s[i] != '\0'; i++) {
        if (s[i - 1] ==  s[i]) con++;
        else {
            if (con > max) max = con;
            con = 1;
        }
    }

    return max;
}

// 16
int diffConsecutivos(char s[]) {
    int last[256] = {-1};
    int max = 0; 
    int start = 0;

    for (int end = 0; s[end]; end++) {
        if (last[s[end]] >= start) start = last[s[end]] + 1;
        last[s[end]] = end;
        max = (end - start + 1) > max ? (end - start + 1) : max;
    }

    return max;
}

// 17
int maiorPrefixo (char s1 [], char s2 []) {
    int i;

    for(i = 0; s1[i] == s2[i] && s1[i] != '\0'; i++);

    return i;
}

// 18
int maiorSufixo(char s1[], char s2[]) {
    int size1 = strlen(s1) - 1, size2 = strlen(s2) - 1, counter = 0;
    
    while (s1[size1] == s2[size2]) {
        size1--;
        size2--;
        counter++;
    }

    return counter;
}

// 19
int sufPref(char s1[], char s2[]) {
    int size1 = strlen(s1);
    int size2 = strlen(s2);
    int mSuf = 0;
    
    for (int i = 0; i < size1; i++) {
        int suf = 0;
        int j = 0;
        
        while (s1[i + j] == s2[j] && s2[j] != '\0') {
            suf++;
            j++;
        }
        
        if (suf > mSuf) {
            mSuf = suf;
        }
    }
    
    return mSuf;
}

// 20
int contaPal(char s[]) {
    int counter = 0;

    for (int i = 0; s[i] != '\0'; i++) if (!isspace(s[i])) counter++;

    return counter;
}

// 21
int contaVogais(char s[]) {
    int counter = 0;
    char c;

    for (int i = 0; s[i] != '\0'; i++) {
        c = toupper(s[i]);
        if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') counter++;
    }

    return counter;
}

// 22
int contida(char a[], char b[]) {
    int size = strlen(a), counter = 0;

    for (int i = 0; a[i] != '\0'; i++) {
        for (int h = 0; b[h] != '\0'; h++) {
            if (a[i] == b[h]) {
                counter++;
                break;
            }
        }
    }

    return (size == counter) ? 1 : 0;
}

// 23
int palindorome(char s[]) {
    int m = 0, k = strlen(s) - 1, control = 0;

    while (m < k) {
        if (s[m] == s[k]) {
            control = 1;
        } else {
            control = 0;
            break;
        }
        m++;
        k--;
    }

    return control;
}

// 24
void allLeft(char x[], int ind) {
    while (ind < strlen(x)) {
        x[ind] = x[ind + 1];
        ind++;
    }
}

int remRep(char x[]) {
    int i = 1;

    while (x[i] != '\0') {
        if (x[i - 1] == x[i]) allLeft(x, i);
        else i++;
    }

    return strlen(x);
}

// 25
int limpaEspacos(char x[]) {
    int i = 1;

    while (x[i] != '\0') {
        if ((x[i - 1] == x[i]) && isspace(x[i])) allLeft(x, i);
        else i++;
    }

    return strlen(x);
}

// 26
void insere(int v[], int N, int x) {
    int ind = N - 1;

    while (ind >= 0 && v[ind] > x) {
        v[ind + 1] = v[ind];
        ind--;
    }

    v[ind + 1] = x;
}

// 27
void merge(int r[], int a[], int b[], int na, int nb) {
    int p1 = 0, p2 = 0, pos = 0;

    while (p1 < na && p2 < nb) {
        if (a[p1] <= b[p2]) {
            r[pos] = a[p1];
            p1++;
        } else {
            r[pos] = b[p2];
            p2++;
        }
        pos++;
    }

    while (p1 < na) {
        r[pos] = a[p1];
        p1++;
        pos++;
    }

    while (p2 < nb) {
        r[pos] = b[p2];
        p2++;
        pos++;
    }
}

// 28
int crescente(int a[], int i, int j) {
    int control = 0;

    while (i < j) {
        if (a[i] = a[j] - 1) {
            control = 1; 
            i++;
        } else {
            control = 0;
            break;
        }
    }

    return control;
}

// 29
void allLeftN(int x[], int ind, int N) {
    while (ind < N) {
        x[ind] = x[ind + 1];
        ind++;
    }
}

int retiraNeg(int v[], int N) {
    int counter = N;

    for (int i = 0; i < counter; i++) {
        if (v[i] < 0) {
            allLeftN(v, i, N);
            counter--;
            i--;
        }
    }

    return counter;
}

// 30
int menosFreq(int v[], int N) {
    int mfreq = v[0], fatu = 1, mfreqg = N;

    for (int i = 1; i < N; i++) {
        if (v[i] == v[i - 1]) fatu++;
        else {
            if (fatu < mfreqg) {
                mfreqg = fatu;
                mfreq = v[i - 1];
            }
            fatu = 1;
        }
    }

    if (fatu < mfreqg) mfreq = v[N - 1];

    return mfreq;
}

// 31
int maisFreq(int v[], int N) {
    int mfreq = v[0], fatu = 1, mfreqg = 1;

    for (int i = 1; i < N; i++) {
        if (v[i] == v[i - 1]) fatu++;
        else {
            if (fatu > mfreqg) {
                mfreqg = fatu;
                mfreq = v[i - 1];
            }
            fatu = 1;
        }
    }

    if (fatu > mfreqg) mfreq = v[N - 1];

    return mfreq;
}

// 32
int maxCresc(int v[], int N) {
    int lmax = 1, gmax = 1;

    for (int i = 1; i < N; i++) {
        if (v[i - 1] < v[i]) lmax++;
        else {
            if (lmax > gmax) gmax = lmax;
            lmax = 1;
        }
    }

    if (lmax > gmax) gmax = lmax;

    return gmax;
}

// 33
int elimRep(int v[], int n) {
    int rep, len = n;
    
    for (int i = 0; i < len; i++) {
        rep = v[i];
        for (int k = i + 1; k < n; k++) {
            if (v[k] == rep) {
                allLeftN(v, k, n);
                len--;
                k--;
            }
        }
    }

    return len;
}

// 34
int elimRepOrd(int v[], int n) {
    return elimRep(v, n);
}

// 35
int eqOrd(int a[], int na, int b[], int nb) {
    int eq = 0;
    int i = 0, j = 0;

    while (i < na && j < nb) {
        if (a[i] == b[j]) {
            eq++;
            i++;
            j++;
        } else if (a[i] < b[j]) {
            i++;
        } else {
            j++;
        }
    }

    return eq;
}

// 36
int comuns(int a[], int na, int b[], int nb) {
    int eq = 0;

    for (int i = 0; i < na; i++) {
        for (int k = 0; k < nb; k++) {
            if (a[i] == b[k]) {
                eq++;
                break;
            }
        }
    }

    return eq;
}

// 37
int minInd(int v[], int n) {
    int ind = 0, elem = v[0];

    for (int i = 1; i < n; i++) {
        if (elem > v[i]) {
            elem = v[i];
            ind = i;
        }
    }

    return ind;
}

// 38
void somasAc(int v[], int Ac[], int N) {
    int sum = 0;

    for (int i = 0; i < N; i++) {
        sum += v[i];
        Ac[i] = sum;
    }
}

// 39
int triSup(int N, float m[N][N]) {
    int cube = 1;

    for (int i = 0; i < N; i++) {
        for (int k = 0; k < i; k++) {
            if(m[i][k]) cube = 0;
        }
    }

    return cube;
}

// 40
void transposta(int N, float m[N][N]) {
    float aux;

    for (int i = 0; i < N; i++) {
        for (int k = 0; k < i; k++) {
            aux = m[i][k];
            m[i][k] = m[k][i];
            m[k][i] = aux;
        }
    }
}

// 41
void addTo(int N, int M, int a[N][M], int b[N][M]) {
    for (int i = 0; i < N; i++) {
        for (int k = 0; k < M; k++) {
            a[i][k] += b[i][k];
        }
    }
}

// 42
int unionSet(int N, int v1[N], int v2[N], int r[N]) {
    for (int i = 0; i < N; i++) {
        if (v1[i] || v2[i]) r[i] = 1;
        else r[i] = 0;
    }

    return 1;
}

// 43
int intersectSet(int N, int v1[N], int v2[N], int r[N]) {
    for (int i = 0; i < N; i++) {
        if (v1[i] && v2[i]) r[i] = 1;
        else r[i] = 0;
    }

    return 1;   
}

// 44
int intersectMSet(int N, int v1[N], int v2[N], int r[N]) {
    for (int i = 0; i < N; i++) {
        r[i] = (v1[i] < v2[i]) ? v1[i] : v2[i];
    }
    return 1;
}

// 45
int unionMSet(int N, int v1[N], int v2[N], int r[N]) {
    for (int i = 0; i < N; i++) {
        r[i] = v1[i] + v2[i];
    }
    return 1;
}

// 46
int cardinalMSet(int N, int v[N]) {
    int counter = 0;

    for (int i = 0; i < N; i++) counter += v[i];

    return counter;
}

// 47
typedef enum movimento {Norte, Oeste, Sul, Este} Movimento;

typedef struct posicao {
    int x, y;
} Posicao;

Posicao posFinal(Posicao inicial, Movimento mov[], int N) {
    for (int i = 0; i < N; i++) {
        switch (mov[i]) {
            case Norte:
                inicial.y++;
                break;
            case Oeste:
                inicial.x--;
                break;
            case Sul:
                inicial.y--;
                break;
            case Este:
                inicial.x++;
                break;
        }
    }

    return inicial;
}

// 48
int caminho(Posicao inicial, Posicao final, Movimento mov[], int N) {
    int ind = 0;

    while (inicial.x != final.x) {
        if (inicial.x > final.x) {
            inicial.x--;
            mov[ind++] = Oeste;
        } else {
            inicial.x++;
            mov[ind++] = Este;
        }
        if (ind >= N) return -1;
    }

    while (inicial.y != final.y) {
        if (inicial.y > final.y) {
            inicial.y--;
            mov[ind++] = Sul;
        } else {
            inicial.y++;
            mov[ind++] = Norte;
        }
        if (ind >= N) return -1;
    }

    return ind;
}

// 49
int maisCentral(Posicao pos[], int N) {
    int dist[N];

    for (int i = 0; i < N; i++) dist[i] = sqrt(pow(pos[i].x, 2) + pow(pos[i].y, 2));

    int menor = dist[0]; 
    for (int h = 1; h < N; h++) if (menor > dist[h]) menor = dist[h];

    return menor;
}

// 50
int vizinhos(Posicao p, Posicao pos[], int N) {
    int neig = 0;
    
    for (int i = 0; i < N; i++) if (sqrt(pow(pos[i].x, 2) + pow(pos[i].y, 2)) == 1) neig++;

    return neig;
}

// Main
int main() {
    return 0;
}