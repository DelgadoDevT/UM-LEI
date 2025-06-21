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
 * Represents an exception that is specific to errors encountered during playback history
 * operations or processes. This exception serves as a general-purpose mechanism for
 * handling playback history-related error conditions within the application.
 */
public class PlaybackHistoryException extends Exception {
    /**
     * Constructs a new PlaybackHistoryException with the specified detail message.
     *
     * @param message the detail message providing more information about the exception
     */
    public PlaybackHistoryException(String message) {
        super(message);
    }
}