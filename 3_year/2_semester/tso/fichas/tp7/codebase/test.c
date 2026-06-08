#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>

#define BLOCK_SIZE 4096
#define NUM_WRITES 4

static void print_usage(const char *prog) {
    printf("Usage:\n");
    printf("  %s        # write without fsync\n", prog);
    printf("  %s flush  # write with fsync\n", prog);
}

int write_blocks(const char *path, int do_fsync) {
    char filename[128];
    time_t now = time(NULL);
    sprintf(filename, "%s/%s-%d.txt", path, "test",(int)now);

    printf("Writing %d bytes to %s...\n", BLOCK_SIZE*NUM_WRITES, filename);

    int fd = open(filename, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (fd < 0) {
        perror("open");
        return -1;
    }

    char buffer[BLOCK_SIZE];

    for (int i = 0; i < NUM_WRITES; i++) {
        memset(buffer, 'A' + i, BLOCK_SIZE);

        ssize_t written = write(fd, buffer, BLOCK_SIZE);
        if (written != BLOCK_SIZE) {
            perror("write");
            close(fd);
            return -1;
        }

        printf("Wrote block %d (%d bytes)\n", i, BLOCK_SIZE);
    }

    if (do_fsync) {
        fsync(fd);
        printf("Data fsynced\n");
    } else {
        printf("Data not fsynced\n");
    }

    printf("Doing some other work....\n");
    sleep(300);

    printf("Finished....");

    close(fd);

    return 0;
}

int main(int argc, char *argv[]) {
    const char *path = "/mnt/fs";
    int do_flush = 0;

    if (argc > 2) {
        fprintf(stderr, "Too many arguments.\n");
        print_usage(argv[0]);
        return 1;
    }

    if (argc == 2) {
        if (strcmp(argv[1], "flush") == 0) {
            do_flush = 1;
        } else if (strcmp(argv[1], "help") == 0) {
            print_usage(argv[0]);
            return 0;
        } else {
            fprintf(stderr, "Unknown mode: %s\n", argv[1]);
            print_usage(argv[0]);
            return 1;
        }
    }

    return write_blocks(path, do_flush);
}