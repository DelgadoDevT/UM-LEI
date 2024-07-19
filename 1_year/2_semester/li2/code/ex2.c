#include <stdio.h>
#include <wchar.h>
#include <locale.h>

void rnum(unsigned long long int n) {
    const wchar_t *yijing[] = {
        L"\u4DC0", L"\u4DC1", L"\u4DC2", L"\u4DC3", L"\u4DC4", L"\u4DC5", L"\u4DC6", L"\u4DC7",
        L"\u4DC8", L"\u4DC9", L"\u4DCA", L"\u4DCB", L"\u4DCC", L"\u4DCD", L"\u4DCE", L"\u4DCF",
        L"\u4DD0", L"\u4DD1", L"\u4DD2", L"\u4DD3", L"\u4DD4", L"\u4DD5", L"\u4DD6", L"\u4DD7",
        L"\u4DD8", L"\u4DD9", L"\u4DDA", L"\u4DDB", L"\u4DDC", L"\u4DDD", L"\u4DDE", L"\u4DDF",
        L"\u4DE0", L"\u4DE1", L"\u4DE2", L"\u4DE3", L"\u4DE4", L"\u4DE5", L"\u4DE6", L"\u4DE7",
        L"\u4DE8", L"\u4DE9", L"\u4DEA", L"\u4DEB", L"\u4DEC", L"\u4DED", L"\u4DEE", L"\u4DEF",
        L"\u4DF0", L"\u4DF1", L"\u4DF2", L"\u4DF3", L"\u4DF4", L"\u4DF5", L"\u4DF6", L"\u4DF7",
        L"\u4DF8", L"\u4DF9", L"\u4DFA", L"\u4DFB", L"\u4DFC", L"\u4DFD", L"\u4DFE", L"\u4DFF"
    };
    
    if (n > 0) {
        unsigned long long int r = n % 64;
        n = n / 64;
        rnum(n);
        
        if (n == 0) wprintf(L"%ls", yijing[r]);
        else wprintf(L" %ls", yijing[r]);
    }
}

int main() {
    unsigned long long int n;

    setlocale(LC_CTYPE, "C.UTF-8");

    if (scanf("%llu", &n) == 1) {     
        if (n == 0) {
        wprintf(L"\u4DC0\n"); }   
        else { 
        rnum(n);
        wprintf(L"\n");
        }
    }

    return 0;
}