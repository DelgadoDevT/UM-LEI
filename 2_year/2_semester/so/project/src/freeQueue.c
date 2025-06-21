/**
 * @file freeQueue.c
 * @brief Provides functions to manage a free queue, which persists to disk.
 *
 * This module provides operations for initializing the free queue, adding and removing 
 * items, saving the queue to disk, and clearing the queue. The free queue is used to 
 * manage reusable values (e.g., memory blocks) that are stored persistently between sessions.
 *
 * @author João Delgado, Simão Mendes, Tiago Brito
 * @date 2025
 * 
 * @license MIT License
 * @copyright Copyright (c) 2025 João Delgado, Simão Mendes, Tiago Brito
 */

#include <stdio.h>
#include <stdlib.h>
#include <glib.h>
#include <unistd.h>
#include <fcntl.h>
#include <string.h>
#include "utils.h"
#include "freeQueue.h"

// Static queue to hold positions of invalid elements in metadata
static GQueue *queue = NULL;

void initializeFreeQueue() {
    queue = g_queue_new();

    int file = open(QUEUE_FILE, O_RDONLY);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Queue file not found, initializing an empty queue.\n");
        return; 
    }

    // Read the queue from disk
    unsigned int value;
    ssize_t bytesRead;
    while ((bytesRead = read(file, &value, sizeof(value))) > 0) {
        if (bytesRead == sizeof(value)) {
            g_queue_push_tail(queue, GINT_TO_POINTER(value)); // Add value to the queue
        }
    }

    if (bytesRead == -1)
        writeToTerminal("[DEBUG]: Error reading from the queue file.\n");

    close(file);
}

void insertToFreeQueue(unsigned int value) {
    if (!queue) {
        writeToTerminal("[DEBUG]: Error: Queue is not initialized.\n");
        return;
    }

    g_queue_push_tail(queue, GINT_TO_POINTER(value)); // Push value to the tail of the queue
}


int getFromFreeQueue() {
    if (!queue || g_queue_is_empty(queue)) {
        writeToTerminal("[DEBUG]: Attempt to get from an empty queue or uninitialized queue.\n");
        return -1;  // Return -1 if the queue is empty or not initialized
    }

    unsigned int value = GPOINTER_TO_INT(g_queue_pop_head(queue)); 
    return value;
}

void clearFreeQueue() {
    if (!queue) {
        writeToTerminal("[DEBUG]: Error: Queue is not initialized.\n");
        return;
    }
    g_queue_clear(queue); // Clear all values from the queue
    writeToTerminal("[DEBUG]: FreeQueue has been cleared.\n");
}

int saveFreeQueue() {
    if (!queue) {
        writeToTerminal("[DEBUG]: Error: Queue is not initialized.\n");
        return 1;
    }

    int file = open(QUEUE_FILE, O_WRONLY | O_CREAT | O_TRUNC, 0600);
    if (file == -1) {
        writeToTerminal("[DEBUG]: Error: Unable to open the queue file for writing.\n");
        return 1;
    }

    for (GList *iter = queue->head; iter != NULL; iter = iter->next) {
        unsigned int value = GPOINTER_TO_INT(iter->data);
        if (write(file, &value, sizeof(value)) == -1) {
            writeToTerminal("[DEBUG]: Error: Failed to write to the queue file.\n");
            close(file);
            return 1;
        }
    }

    close(file);
    return 0;
}
