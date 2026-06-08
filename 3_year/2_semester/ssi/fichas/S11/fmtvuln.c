#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void process_input(char *input) {
    unsigned long secret = 0xcafebabe;   /* a sensitive value resident on the stack */

    printf("[*] Address of secret on stack: %p\n", (void *)&secret);
    printf("[*] Processing input...\n");
    printf(input);             /* CWE-134: user input used as format string */
    printf("\n");
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <input>\n", argv[0]);
        return 1;
    }
    process_input(argv[1]);
    printf("[*] Normal programme termination.\n");
    return 0;
}