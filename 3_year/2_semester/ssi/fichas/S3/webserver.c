#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

int main(int argc, char *argv[]) {
	if (argc != 2) {
		fprintf(stderr, "Usage: %s <port>\n", argv[0]);
		return 1;
	}

	int port = atoi(argv[1]);
	int sockfd = socket(AF_INET, SOCK_STREAM, 0);

	if (sockfd < 0) {
		perror("Error when creating socket");
		return 1;
	}
		
	struct sockaddr_in addr;
	memset(&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	addr.sin_addr.s_addr = INADDR_ANY;
	addr.sin_port = htons(port);

	if (bind(sockfd, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
		perror("Errr on bind");
		close(sockfd);
		return 1;
	}

	printf("Success: binded to port %d\n", port);
	close(sockfd);
	return 0;
}
