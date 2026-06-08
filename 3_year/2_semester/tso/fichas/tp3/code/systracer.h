#ifndef SYSTRACER_H
#define SYSTRACER_H

struct data_t {
   int pid;
   char command[16];
   long syscall_id;
   int layer;
};

#define MYOP_READ        1001
#define MYOP_WRITE       1002
#define MYOP_OPENAT      1003 
#define MYOP_FILE_READ   1004
#define MYOP_FILE_WRITE  1005
#define MYOP_BLOCK_READ  1006
#define MYOP_BLOCK_WRITE 1007

#endif
