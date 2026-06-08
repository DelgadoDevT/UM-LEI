#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

int main() {
    int dfd;
    char *argv[2];

    dfd = open("/root", O_RDONLY);
    if (dfd == -1) {
	perror("open /root");
	exit(1);
    }
    printf("Directory FD is %d\n", dfd);

    if (mkdir("/root/backupssi", 0700) == -1) {
	perror("mkdir /root/backupssi");
    }
    
    if (setuid(getuid()) == -1) {
	perror("setuid");
	exit(1);
    }
    
    argv[0] = "/bin/sh";
    argv[1] = NULL;
    execve(argv[0], argv, NULL);

    perror("execve");
    return 0;
}
