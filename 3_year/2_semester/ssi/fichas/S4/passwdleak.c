#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>

int main() {
    int fd = open("/etc/passwd", O_WRONLY | O_APPEND);
    if (fd < 0) {
	perror("open /etc/passwd");
	exit(1);
    }

    printf("Passwd FD leaked: %d\n", fd);
    setuid(getuid());
    execl("/bin/sh", "sh", NULL);
}
