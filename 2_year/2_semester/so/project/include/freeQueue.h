/**
 * @file freeQueue.h
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

#ifndef FREEQUEUE_H
#define FREEQUEUE_H

/** 
 * @def QUEUE_FILE
 * @brief Path to the file where the free queue is stored persistently.
 */
#define QUEUE_FILE "bin/freeQueue.bin"

/**
 * @brief Initializes the free queue.
 * 
 * This function initializes the queue, either by creating a new empty queue or by 
 * reading an existing one from the disk if available.
 */
void initializeFreeQueue();

/**
 * @brief Inserts a value into the free queue.
 * 
 * @param value The value to be inserted into the queue.
 */
void insertToFreeQueue(unsigned int value);

/**
 * @brief Retrieves and removes a value from the free queue.
 * 
 * @return The value at the front of the queue, or -1 if the queue is empty or not initialized.
 */
int getFromFreeQueue();

/**
 * @brief Clears all values from the free queue.
 * 
 * This function removes all elements from the queue and frees associated memory.
 */
void clearFreeQueue();

/**
 * @brief Saves the free queue to disk.
 * 
 * This function writes the current state of the queue to the file for persistence.
 * 
 * @return 0 on success, or 1 on failure.
 */
int saveFreeQueue();

#endif // FREEQUEUE_H
