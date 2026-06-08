#ifndef SYSTRACER_H
#define SYSTRACER_H

#ifndef S_IFMT // bit mask for the file type bit fields
#define S_IFMT  00170000
#endif

#ifndef S_IFREG // regular file
#define S_IFREG 0100000
#endif

// define layers types
#define L_SYSCALL 0
#define L_VFS 1
#define L_EXT4 2
#define L_FUSE 3
#define L_BIO 4

struct data_t {
   int entry; //1-entry, 2-exit;
   int pid;
   char command[16];
   long syscall_id;
   int type; //1-syscall, 2-vfs, 3-fs
   char filename[128];
   uint32_t hash;
   unsigned long ino;
   size_t size;
};

#endif
