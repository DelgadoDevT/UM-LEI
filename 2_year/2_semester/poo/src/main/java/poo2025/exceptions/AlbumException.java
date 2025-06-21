/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.exceptions;

/**
 * Represents an exception specific to errors related to album management or processing.
 * This exception serves as a general-purpose exception within the application's context
 * to handle album-related error conditions.
 */
public class AlbumException extends Exception {
    /**
     * Constructs a new AlbumException with the specified detail message.
     *
     * @param message the detail message, providing more information about the exception
     */
    public AlbumException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new AlbumException with the specified detail message and cause.
     *
     * @param message the detail message, which provides more information about the exception
     * @param cause the cause of the exception, which can be another throwable that led to this exception
     */
    public AlbumException(String message, Throwable cause) {
        super(message, cause);
    }
}