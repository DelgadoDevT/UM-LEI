#include <stdio.h>
#include "operation.h"
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "utils.h"

char* argumentsParser(int argc, char* argv[]) {
    if (argc <= 2) return NULL;

    int total_length = 0;
    for (int i = 2; i < argc; ++i) {
        total_length += strlen(argv[i]) + 1;  // +1 for newline or null terminator
    }

    if (total_length <= 0) return NULL;
    char* result = malloc(total_length * sizeof(char));
    if (result == NULL) {
        writeToTerminal("[DEBUG] malloc failed --> argumentsParser dclient");
        _exit(1);
    }

    result[0] = '\0';  // Initialize the string

    for (int i = 2; i < argc; ++i) {
        strcat(result, argv[i]);
        if (i < argc - 1) {
            strcat(result, "\n");  // Add newline between arguments
        }
    }

    return result;
}

void makeRequest(char* argv[], int argc)
{
    operation request = createOperation(getpid());

    // Set the operation type based on the command-line flags (-a, -c, etc.)
    setOperationType(&request, getopt(argc, argv, "acdlsf"));

    // If there are additional arguments, set them
    if (argv[2] != NULL) 
        setOperationArguments(&request, argumentsParser(argc, argv));

    // Open the server FIFO for writing
    pid_t requestPipe = open("tmp/serverChannel", O_WRONLY);
    if (write(requestPipe, &request, sizeof(operation)) == -1)
        writeToTerminal("[DEBUG] Can't write request to server\n");   
    close(requestPipe);
}

void getResponse(char *clientChannel)
{
    operation response;

    // Open the client FIFO for reading
    pid_t response_pid = open(clientChannel, O_RDONLY);
    if (response_pid == -1) {
        writeToTerminal("[DEBUG] Error: Could not open response channel ---> getResponse -> dclient\n");
        _exit(1);
    }

    while (1)
    {
        ssize_t bytesRead;

        // Continuously read responses from the server
        while ((bytesRead = read(response_pid, &response, sizeof(operation))) > 0)
        {
            const char *message = getOperationArguments(&response);
            writeToTerminal((char *)message);
        }

        // If read returns 0, the server has closed the FIFO
        if (bytesRead == 0) {
            break; // Exit the outer while loop
        }

        // If an error occurred during read
        if (bytesRead == -1) {
            perror("read");
            break;
        }
    }

    close(response_pid);
    writeToTerminal("\n");
}

void runClient(int argc, char* argv[])
{
    writeToTerminal("Client ready\n");

    char clientChannel[256];

    // Generate a unique client FIFO name based on the PID
    sprintf(clientChannel, "%s%d", "tmp/clientChannel", getpid());

    // Create the client FIFO
    if (mkfifo(clientChannel, 0666) == -1)
    {
        writeToTerminal("[DEBUG] Error: Mkfifo failed ---> runClient -> dclient\n");
        _exit(1);
    }

    makeRequest(argv, argc);
    getResponse(clientChannel);

    // Remove the client FIFO after communication
    unlink(clientChannel);
    writeToTerminal("Client finished.\n");
}

int main(int argc, char* argv[]) {
    runClient(argc, argv);
    return 0;
}
