#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>
#include <string.h>

#define FILENAME "testfile.txt"
#define BLOCK_SIZE 4096
#define ITERATIONS 10
#define ALIGNMENT 4096

void generate_random_text(char *buf, size_t len)
{
    const char charset[] =
        "abcdefghijklmnopqrstuvwxyz"
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        "0123456789";

    for (size_t i = 0; i < len - 1; i++) {
        int key = rand() % (sizeof(charset) - 1);
        buf[i] = charset[key];
    }
    buf[len - 1] = '\n';
}

int write_test(int use_odirect) {
    if (use_odirect) printf("Initiating write test with O_DIRECT\n");
    else printf("Initiating write test without O_DIRECT\n");

    char *buf;
    if (use_odirect) {
        if (posix_memalign((void **)&buf, ALIGNMENT, BLOCK_SIZE) != 0) exit(1);
    } else {
        buf = malloc(BLOCK_SIZE);
    }

    int flags = O_WRONLY | O_CREAT | O_TRUNC;
    if (use_odirect) flags |= O_DIRECT;

    int fd = open(FILENAME, flags, 0644);
    if (fd < 0) {
        perror("open (write)");
        free(buf);
        return 1;
    }

    for (int i = 0; i < ITERATIONS; i++) {
        generate_random_text(buf, BLOCK_SIZE);

        ssize_t written = write(fd, buf, BLOCK_SIZE);
        if (written < 0) {
            perror("write");
            close(fd);
            free(buf);
            return 1;
        }

        printf("Write %ld\n", written);
    }

    if (close(fd) < 0) {
        perror("close (write)");
        free(buf);
        return 1;
    }

    free(buf);
    return 0;
}

int read_test(int use_odirect) {
    if (use_odirect) printf("Initiating read test with O_DIRECT\n");
    else printf("Initiating read test without O_DIRECT\n");

    char *buf;
    if (use_odirect) {
        if (posix_memalign((void **)&buf, ALIGNMENT, BLOCK_SIZE) != 0) exit(1);
    } else {
        buf = malloc(BLOCK_SIZE);
    }

    int flags = O_RDONLY;
    if (use_odirect) flags |= O_DIRECT;

    int fd = open(FILENAME, flags);
    if (fd < 0) {
        perror("open (read)");
        free(buf);
        return 1;
    }

    for (int i = 0; i < ITERATIONS; i++) {
        ssize_t bytes_read = read(fd, buf, BLOCK_SIZE);
        if (bytes_read < 0) {
            perror("read");
            close(fd);
            free(buf);
            return 1;
        }

        if (bytes_read == 0)
            break;  // EOF

        printf("Read  %ld\n", bytes_read);
    }

    if (close(fd) < 0) {
        perror("close (read)");
        free(buf);
        return 1;
    }

    free(buf);
    return 0;
}

int main(int argc, char *argv[])
{
    if (argc < 2) {
        printf("Usage: %s [rw|r|w|rws|rs|ws]\n", argv[0]);
        return 1;
    }

    printf("PID: %d\n", getpid());
    printf("Press Enter to continue...\n");
    getchar();

    if (strcmp(argv[1], "rw") == 0) {
        write_test(0);
        read_test(0);
    }
    else if (strcmp(argv[1], "rwo") == 0) {
        write_test(1);
        read_test(1);
    }

}
