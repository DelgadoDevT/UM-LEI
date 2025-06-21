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
 * Represents an exception specific to errors related to user operations or processing.
 * This exception serves as a general-purpose mechanism for handling user-related
 * error conditions within the application.
 */
public class UserException extends Exception {
    /**
     * Constructs a new UserException with the specified detail message.
     *
     * @param message the detail message providing more information about the exception
     */
    public UserException(String message) {
        super(message);
    }
}