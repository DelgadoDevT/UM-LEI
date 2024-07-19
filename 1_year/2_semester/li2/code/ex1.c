#include <stdio.h>
#include <locale.h>
#include <wchar.h>

int main()
{
    int dia, mes;

    /* Mudar para UTF8 */
    setlocale(LC_CTYPE, "C.UTF-8");

    if(scanf("%d%d", &dia, &mes) == 2) {
        switch (mes) {

          case 1:
            if(dia <= 19) {
                wchar_t c = 0x2651;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x2652;
                wprintf(L"%lc\n", c);
            }
            break;

          case 2:
            if(dia <= 18) {
                wchar_t c = 0x2652;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x2653;
                wprintf(L"%lc\n", c);
            }
            break;

          case 3:
            if(dia <= 20) {
                wchar_t c = 0x2653;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x2648;
                wprintf(L"%lc\n", c);                       
            }                                                       
            break;

          case 4:
            if(dia <= 20) {                         
                wchar_t c = 0x2648;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x2649;             
                wprintf(L"%lc\n", c);
            }
            break;

          case 5:
            if(dia <= 20) {
                wchar_t c = 0x2649;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x264a;
                wprintf(L"%lc\n", c);
            }
            break;

        case 6:
            if(dia <= 20) {
                wchar_t c = 0x264a;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x264b;
                wprintf(L"%lc\n", c);
            }
            break;

        case 7:
            if(dia <= 22) {
                wchar_t c = 0x264b;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x264c;
                wprintf(L"%lc\n", c);
            }
            break;

        case 8:
            if(dia <= 22) {
                wchar_t c = 0x264c;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x264d;
                wprintf(L"%lc\n", c);
            }
            break;

        case 9:
            if(dia <= 22) {
                wchar_t c = 0x264d;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x264e;
                wprintf(L"%lc\n", c);
            }
            break;

        case 10:
            if(dia <= 22) {
                wchar_t c = 0x264e;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x264f;
                wprintf(L"%lc\n", c);
            }
            break;

        case 11:
            if(dia <= 21) {
                wchar_t c = 0x264f;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x2650;
                wprintf(L"%lc\n", c);
            }
            break;

        case 12:
            if(dia <= 21) {
                wchar_t c = 0x2650;
                wprintf(L"%lc\n", c);
            } else {
                wchar_t c = 0x2651;
                wprintf(L"%lc\n", c);
            }
            break;

        }    
    }

    return 0;
}   