/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.Album;

import org.junit.jupiter.api.Test;
import poo2025.entities.Music.Music;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@code equals} method of the {@code Album} class when comparing an object with itself.
 *
 * This test ensures that the {@code equals} method returns {@code true} when an {@code Album}
 * object is compared to itself. This is a fundamental property of the {@code equals} contract,
 * indicating that an object must always be equal to itself.
 *
 * The test creates an instance of the {@code Album} class and performs an equality check by
 * calling the {@code equals} method with the object itself as the argument.
 *
 * Test method: {@link Album#equals(Object)}
 */
public class AlbumTest {
    /**
     * Tests the `equals` method of the `Album` class by comparing two identical `Album` objects.
     *
     * This method creates two `Album` instances with identical properties, including identical songs
     * and artists, and verifies that they are considered equal using the `equals` method of the `Album` class.
     *
     * Assertions:
     * - Ensures that two `Album` objects with the same properties are deemed equal.
     *
     * Test Details:
     * - Creates two `Music` objects with different properties.
     * - Adds the `Music` objects to a list to populate the `Album`.
     * - Performs a deep copy of the first `Album` to create the second `Album`.
     * - Asserts that the two albums are equal.
     */
    @Test
    public void testEqualsForIdenticalAlbums() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        Music song2 = new Music("Song 2", "Artist 2", "Publisher 2", "Sample lyrics2", "sound_file2.mp3", "Rock", 200, 0);
        List<Music> songs = List.of(song1, song2);

        Album album1 = new Album("Album Name", songs, "Artist 1");
        Album album2 = new Album(album1);

        assertTrue(album1.equals(album2), "Albums with the same properties should be equal.");
    }

    /**
     * Tests the `equals` method of the `Album` class to ensure that two `Album` objects
     * with identical attributes but different unique identifiers are not considered equal.
     *
     * This test creates two `Album` instances with the same name, songs, and artist,
     * but ensures they have distinct identifiers. It then asserts that the `equals`
     * method correctly recognizes these albums as unequal.
     *
     * Assertions:
     * - The test asserts that calling `equals` on two albums with different identifiers
     *   returns `false`.
     */
    @Test
    public void testEqualsWithDifferentIdentifiers() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        List<Music> songs = List.of(song1);

        Album album1 = new Album("Album Name", songs, "Artist 1");
        Album album2 = new Album("Album Name", songs, "Artist 1");

        assertFalse(album1.equals(album2), "Albums with different identifiers should not be equal.");
    }

    /**
     * Tests the equality comparison of two Album objects with different names.
     *
     * This test ensures that the {@link Album#equals(Object)} method properly returns {@code false}
     * when comparing two Album instances that have identical content in terms of their song list
     * and artist but different album names.
     *
     * Assertions:
     * - Verifies that two Album objects with identical songs and artist but different names are not equal.
     */
    @Test
    public void testEqualsWithDifferentNames() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        List<Music> songs = List.of(song1);

        Album album1 = new Album("Album Name 1", songs, "Artist 1");
        Album album2 = new Album("Album Name 2", songs, "Artist 1");

        assertFalse(album1.equals(album2), "Albums with different names should not be equal.");
    }

    /**
     * Tests the equality of two Album objects containing different lists of songs.
     *
     * This method creates two Album instances with identical attributes except for
     * the lists of songs they contain, which are distinctly different. It then
     * validates using assertions that the two Album objects are not considered
     * equal, as the song lists differ.
     *
     * The primary aim of this test is to ensure that the {@code equals} method
     * implementation in the Album class correctly evaluates inequality when the
     * contained lists of songs differ.
     */
    @Test
    public void testEqualsWithDifferentSongs() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        Music song2 = new Music("Song 2", "Artist 2", "Publisher 2", "Sample lyrics2", "sound_file2.mp3", "Rock", 200, 0);
        List<Music> songs1 = List.of(song1);
        List<Music> songs2 = List.of(song2);

        Album album1 = new Album("Album Name", songs1, "Artist 1");
        Album album2 = new Album("Album Name", songs2, "Artist 1");

        assertFalse(album1.equals(album2), "Albums with different song lists should not be equal.");
    }

    /**
     * Tests the equality behavior of the Album class when comparing two albums
     * with the same name and songs but different artists.
     *
     * This test ensures that the equality check considers the artist field when
     * comparing two Album instances.
     *
     * It verifies that two Album objects with identical names and songs but with
     * different artists are not considered equal.
     */
    @Test
    public void testEqualsWithDifferentArtists() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        List<Music> songs = List.of(song1);

        Album album1 = new Album("Album Name", songs, "Artist 1");
        Album album2 = new Album("Album Name", songs, "Artist 2");

        assertFalse(album1.equals(album2), "Albums with different artists should not be equal.");
    }

    /**
     * Tests the {@code equals} method of the {@code Album} class to ensure that it correctly identifies
     * that an {@code Album} object is not equal to {@code null}.
     *
     * The method creates an instance of {@code Album} with a list containing a single {@code Music} object,
     * and then asserts that the {@code equals} method with a {@code null} argument returns {@code false}.
     */
    @Test
    public void testEqualsWithNullObject() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        List<Music> songs = List.of(song1);

        Album album = new Album("Album Name", songs, "Artist 1");

        assertFalse(album.equals(null), "An Album object should not be equal to null.");
    }

    /**
     * Verifies the behavior of the equals method in the Album class when the input object is of a different type.
     *
     * This test ensures that the equals implementation properly returns false when compared
     * to an object that is not an instance of the Album class, maintaining the contract of the equals method.
     *
     * The test creates an Album instance and compares it to a String object, asserting that the equals method
     * returns false in such a case.
     *
     * Test method: {@link Album#equals(Object)}
     */
    @Test
    public void testEqualsWithDifferentObjectType() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        List<Music> songs = List.of(song1);

        Album album = new Album("Album Name", songs, "Artist 1");

        assertFalse(album.equals("Some String"), "An Album object should not be equal to an object of a different type.");
    }

    /**
     * Tests the {@code equals} method of the {@code Album} class when comparing an object with itself.
     *
     * This test ensures that the {@code equals} method returns {@code true} when an {@code Album}
     * object is compared to itself. This is a fundamental property of the {@code equals} contract,
     * indicating that an object must always be equal to itself.
     *
     * The test creates an {@code Album} object by initializing it with a single {@code Music}
     * object and then verifies that the {@code equals} method behaves as expected.
     *
     * Assertions:
     * - Checks that the {@code equals} method returns {@code true} when the {@code Album} object is
     *   compared with itself.
     */
    @Test
    public void testEqualsWithSameObject() {
        Music song1 = new Music("Song 1", "Artist 1", "Publisher 1", "Sample lyrics", "sound_file.mp3", "Pop", 180, 0);
        List<Music> songs = List.of(song1);

        Album album = new Album("Album Name", songs, "Artist 1");

        assertTrue(album.equals(album), "An Album object should be equal to itself.");
    }
}