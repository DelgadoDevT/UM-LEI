/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.Playlist;

import org.junit.jupiter.api.Test;
import poo2025.entities.Music.Music;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@code TimedPlaylistTest} class contains unit tests for the {@code TimedPlaylist} class,
 * specifically verifying the functionality of the {@code canAddSong} method under various conditions.
 *
 * Each test case is designed to validate different scenarios where a song may or may not be
 * added to the playlist based on factors such as song duration, matching genre, and remaining time.
 *
 * The test methods utilize assertions to confirm the correctness of the {@code canAddSong}
 * method's behavior.
 */
public class TimedPlaylistTest {
    /**
     * Tests the behavior of the {@code canAddSong} method in the {@code TimedPlaylist} class
     * when the provided {@code Music} object is {@code null}.
     *
     * The test ensures that the method returns {@code false}, as adding a null song
     * should not be allowed in the playlist.
     *
     * Preconditions:
     * - A new {@code TimedPlaylist} object is instantiated with a maximum duration and a genre.
     *
     * Expected Behavior:
     * - The {@code canAddSong} method should return {@code false} when called with {@code null}.
     * - An appropriate assertion verifies the behavior.
     */
    @Test
    public void testCanAddSong_SongIsNull() {
        TimedPlaylist playlist = new TimedPlaylist(300, "Pop");

        Music song = null;
        boolean result = playlist.canAddSong(song);

        assertFalse(result, "canAddSong should return false when the song is null.");
    }

    /**
     * Tests the behavior of the {@code canAddSong} method when a song's duration exceeds
     * the maximum allowed time for the playlist.
     *
     * The scenario involves creating a playlist with a specified maximum duration and adding
     * a song whose duration is greater than the remaining allowable time. The expected result
     * is that the method should return {@code false}.
     *
     * Assertions:
     * - Verifies that {@code canAddSong} correctly identifies and rejects a song with a duration
     *   that exceeds the playlist's limit.
     */
    @Test
    public void testCanAddSong_DurationExceedsLimit() {
        TimedPlaylist playlist = new TimedPlaylist(300, "Pop");

        Music song = new Music("Song1", "Artist", "Publisher", "Lyrics", "Sound", "Pop", 350, 0);
        boolean result = playlist.canAddSong(song);

        assertFalse(result, "canAddSong should return false when the song's duration exceeds the maximum allowed time.");
    }

    /**
     * Tests the behavior of the {@code canAddSong} method when attempting to add a song that does not match the genre of the playlist.
     *
     * The test verifies that the {@code canAddSong} method returns {@code false} when the genre of the song differs from
     * the genre specified for the playlist. The playlist is initialized with a duration limit and a specific genre, while
     * the song being tested has a different genre.
     *
     * Expected Result:
     * - {@code canAddSong} should return {@code false}.
     */
    @Test
    public void testCanAddSong_MismatchedGenre() {
        TimedPlaylist playlist = new TimedPlaylist(300, "Pop");

        Music song = new Music("Song1", "Artist", "Publisher", "Lyrics", "Sound", "Rock", 150, 0);
        boolean result = playlist.canAddSong(song);

        assertFalse(result, "canAddSong should return false when the song's genre does not match the playlist's genre.");
    }

    /**
     * Tests whether a valid song can be added to the playlist.
     *
     * A song is considered valid if it meets all the following conditions:
     * - The song's genre matches the genre of the playlist.
     * - The song's duration does not exceed the remaining allowable time in the playlist.
     * - The total current time plus the song's duration does not exceed the playlist's maximum allowed time.
     *
     * This test covers the scenario where all conditions are met and ensures that
     * the `canAddSong` method returns true for a valid song.
     *
     * The test initializes a playlist with specified maximum duration and genre,
     * creates a song that fulfills all the criteria, and then asserts that
     * the `canAddSong` method returns true.
     *
     * An assertion failure will occur if `canAddSong` does not behave as expected for this valid song scenario.
     */
    @Test
    public void testCanAddSong_ValidSong() {
        TimedPlaylist playlist = new TimedPlaylist(300, "Pop");

        Music song = new Music("Song1", "Artist", "Publisher", "Lyrics", "Sound", "Pop", 150, 0);
        boolean result = playlist.canAddSong(song);

        assertTrue(result, "canAddSong should return true when the song meets all conditions.");
    }

    /**
     * Tests the {@code canAddSong} method of the {@code TimedPlaylist} class
     * to verify that a song can be added when its duration fits the remaining
     * available time of the playlist exactly.
     *
     * The test sets up a timed playlist with a specified maximum duration
     * and current elapsed time. It then attempts to add a song whose
     * duration exactly matches the remaining available time.
     * The test asserts that the {@code canAddSong} method returns {@code true},
     * ensuring that songs can be added in such cases.
     */
    @Test
    public void testCanAddSong_DurationFitsExactly() {
        TimedPlaylist playlist = new TimedPlaylist(150, "Pop");
        playlist.setCurrentTime(50);

        Music song = new Music("Song2", "Artist", "Publisher", "Lyrics", "Sound", "Pop", 100, 0);
        boolean result = playlist.canAddSong(song);

        assertTrue(result, "canAddSong should return true when the song's duration fits the remaining available time exactly.");
    }

    /**
     * Tests the behavior of the `canAddSong` method when adding a song causes the current time
     * to exceed the playlist's maximum allowed time.
     *
     * This test sets up a playlist with a maximum allowed time of 200 and a current time of 150.
     * It then attempts to add a song with a duration of 100. Since adding this song would result
     * in a total time of 250, which exceeds the maximum allowed time, the method should return false.
     *
     * Verifies that:
     * - The `canAddSong` method returns false when adding a song exceeds the allowed duration.
     */
    @Test
    public void testCanAddSong_CurrentTimeExceedsWithSong() {
        TimedPlaylist playlist = new TimedPlaylist(200, "Pop");
        playlist.setCurrentTime(150);

        Music song = new Music("Song3", "Artist", "Publisher", "Lyrics", "Sound", "Pop", 100, 0);
        boolean result = playlist.canAddSong(song);

        assertFalse(result, "canAddSong should return false when adding the song would exceed the maximum allowed time.");
    }
}