#ifndef SYSTRACER_H
#define SYSTRACER_H

#ifndef S_IFMT
#define S_IFMT  00170000
#endif

#ifndef S_IFREG
#define S_IFREG 0100000
#endif

#define MAX_OPS 6
#define MAX_PIDS 100

// Define a struct to be used as key for the counter map, which includes the pid, command and the syscall id (op)
struct counter_key {
    char command[16];
    int pid;
    int op;
};

#endif
