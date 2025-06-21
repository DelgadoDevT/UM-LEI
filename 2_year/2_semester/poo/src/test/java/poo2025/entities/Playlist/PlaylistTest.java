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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains unit tests for the {@code Playlist} class.
 * It validates the behavior of different constructors of the {@code Playlist} class,
 * including the default constructor, the parameterized constructor, and the copy constructor.
 * The tests ensure the proper generation or retention of unique identifiers for Playlist instances.
 */
class PlaylistTest {
    /**
     * Tests that the default constructor of the Playlist class assigns a unique identifier to each new instance.
     *
     * The test creates two instances of the Playlist class using the default constructor and retrieves their identifiers.
     * It asserts that:
     * - The identifier of the first playlist is not null.
     * - The identifier of the second playlist is not null.
     * - The identifiers of the two playlists are not equal, ensuring each instance has a unique identifier.
     */
    @Test
    void testDefaultConstructorAssignsUniqueIdentifier() {
        Playlist playlist = new Playlist();
        Integer firstIdentifier = playlist.getIdentifier();

        Playlist anotherPlaylist = new Playlist();
        Integer secondIdentifier = anotherPlaylist.getIdentifier();

        assertNotNull(firstIdentifier, "The identifier of the first playlist should not be null.");
        assertNotNull(secondIdentifier, "The identifier of the second playlist should not be null.");
        assertNotEquals(firstIdentifier, secondIdentifier, "Each playlist should have a unique identifier.");
    }

    /**
     * Tests that the parameterized constructor of the Playlist class assigns a unique identifier
     * to each instance. Specifically, it verifies that:
     * - The identifier assigned to the first playlist is not null.
     * - The identifier assigned to the second playlist is not null.
     * - The identifiers of two different playlists are unique and do not match.
     *
     * This ensures that each instance of the Playlist class, when created using the parameterized
     * constructor, has a distinct identifier to differentiate it from other instances.
     */
    @Test
    void testParameterizedConstructorAssignsUniqueIdentifier() {
        Playlist playlist = new Playlist("My Playlist", Collections.emptyList(), true, 1);
        Integer firstIdentifier = playlist.getIdentifier();

        Playlist anotherPlaylist = new Playlist("Another Playlist", Collections.emptyList(), false, 2);
        Integer secondIdentifier = anotherPlaylist.getIdentifier();

        assertNotNull(firstIdentifier, "The identifier of the first playlist should not be null.");
        assertNotNull(secondIdentifier, "The identifier of the second playlist should not be null.");
        assertNotEquals(firstIdentifier, secondIdentifier, "Each playlist should have a unique identifier.");
    }

    /**
     * Tests that the copy constructor of the {@code Playlist} class correctly maintains the same identifier
     * for the copied object as the original object.
     *
     * Verifies that:
     * 1. The identifier of the original playlist is not null.
     * 2. The identifier of the copied playlist is not null.
     * 3. The identifier of the copied playlist matches the identifier of the original playlist.
     */
    @Test
    void testCopyConstructorMaintainsSameIdentifier() {
        Playlist originalPlaylist = new Playlist("Original", Collections.emptyList(), true, 1);
        Playlist copiedPlaylist = new Playlist(originalPlaylist);

        Integer originalIdentifier = originalPlaylist.getIdentifier();
        Integer copiedIdentifier = copiedPlaylist.getIdentifier();

        assertNotNull(originalIdentifier, "The identifier of the original playlist should not be null.");
        assertNotNull(copiedIdentifier, "The identifier of the copied playlist should not be null.");
        assertEquals(originalIdentifier, copiedIdentifier, "The copied playlist should have the same identifier as the original.");
    }
}