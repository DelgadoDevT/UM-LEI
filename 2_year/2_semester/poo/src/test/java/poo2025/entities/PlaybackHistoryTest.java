/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for the {@link PlaybackHistory} class, specifically focusing on the time validation functionality.
 * This class contains unit tests that verify the correct behavior of the {@code hasBeenPlayedInLastXHours} method
 * under different scenarios including recent, old, boundary, and future playback times.
 *
 * @see PlaybackHistory
 */
public class PlaybackHistoryTest {

    /**
     * Tests for the hasBeenPlayedInLastXHours method in the PlaybackHistory class.
     * <p>
     * This method determines whether a specific playback record
     * occurred within the given number of hours from the current time.
     */

    /**
     * Tests the hasBeenPlayedInLastXHours method with a recent playback.
     * The test creates a playback history with a timestamp 2 hours in the past
     * and verifies that it's correctly identified as having been played
     * within the last 5 hours.
     */
    @Test
    public void testHasBeenPlayedInLastXHours_WithRecentPlayback() {
        // Arrange: Create a playback history for a recent playback
        LocalDateTime recentPlayback = LocalDateTime.now().minusHours(2);
        PlaybackHistory playbackHistory = new PlaybackHistory(1, 10, recentPlayback);

        // Act: Check if it has been played in the last 5 hours
        boolean playedRecently = playbackHistory.hasBeenPlayedInLastXHours(5);

        // Assert: Expect the playback to be considered recent
        assertTrue(playedRecently);
    }

    /**
     * Tests the hasBeenPlayedInLastXHours method with an old playback.
     * The test creates a playback history with a timestamp 10 hours in the past
     * and verifies that it's correctly identified as not having been played
     * within the last 5 hours.
     */
    @Test
    public void testHasBeenPlayedInLastXHours_WithOldPlayback() {
        // Arrange: Create a playback history for an old playback
        LocalDateTime oldPlayback = LocalDateTime.now().minusHours(10);
        PlaybackHistory playbackHistory = new PlaybackHistory(1, 20, oldPlayback);

        // Act: Check if it has been played in the last 5 hours
        boolean playedRecently = playbackHistory.hasBeenPlayedInLastXHours(5);

        // Assert: Expect the playback to be too old
        assertFalse(playedRecently);
    }

    /**
     * Tests the hasBeenPlayedInLastXHours method with a boundary case playback.
     * The test creates a playback history with a timestamp exactly 5 hours in the past
     * and verifies that it's correctly identified as not having been played
     * within the last 5 hours (exclusive boundary).
     */
    @Test
    public void testHasBeenPlayedInLastXHours_WithExactBoundaryPlayback() {
        // Arrange: Create a playback history for the exact boundary hour
        LocalDateTime boundaryPlayback = LocalDateTime.now().minusHours(5);
        PlaybackHistory playbackHistory = new PlaybackHistory(2, 30, boundaryPlayback);

        // Act: Check if it has been played in the last 5 hours
        boolean playedRecently = playbackHistory.hasBeenPlayedInLastXHours(5);

        // Assert: Expect the playback to not be included as recent
        assertFalse(playedRecently);
    }

    /**
     * Tests the hasBeenPlayedInLastXHours method with a future playback.
     * The test creates a playback history with a timestamp 1 hour in the future
     * and verifies that it's correctly identified as having been played
     * within the last 5 hours.
     */
    @Test
    public void testHasBeenPlayedInLastXHours_WithFuturePlayback() {
        // Arrange: Create a playback history for a future playback
        LocalDateTime futurePlayback = LocalDateTime.now().plusHours(1);
        PlaybackHistory playbackHistory = new PlaybackHistory(3, 40, futurePlayback);

        // Act: Check if it has been played in the last 5 hours
        boolean playedRecently = playbackHistory.hasBeenPlayedInLastXHours(5);

        // Assert: Expect the playback to be considered recent
        assertTrue(playedRecently);
    }
}