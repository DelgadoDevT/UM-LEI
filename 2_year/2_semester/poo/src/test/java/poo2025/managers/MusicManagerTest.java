/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.managers;

import org.junit.jupiter.api.Test;
import poo2025.entities.Music.Music;
import poo2025.managers.MusicManager;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link MusicManager} class, specifically focusing on testing the functionality
 * of the getMusics() method.
 * <p>
 * This test suite includes various test cases to verify the behavior of the getMusics method
 * in different scenarios, ensuring proper handling of empty maps, cloning behavior, and 
 * immutability of the original data.
 */
public class MusicManagerTest {

    /**
     * Unit tests for the getMusics method in the MusicManager class.
     * <p>
     * The getMusics method is responsible for returning a clone of the musics map in the MusicManager object.
     * Each entry in the map (key-value pair) corresponds to a music entity and its corresponding identifier.
     */

    /**
     * Tests the behavior of getMusics() when the music collection is empty.
     * <p>
     * This test verifies that:
     * <ul>
     *   <li>The returned map is not null</li>
     *   <li>The returned map is empty</li>
     * </ul>
     */
    @Test
    public void testGetMusicsWhenMusicsAreEmpty() {
        // Arrange
        MusicManager musicManager = new MusicManager();

        // Act
        Map<Integer, Music> result = musicManager.getMusics();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The musics map should be empty.");
    }

    /**
     * Tests that getMusics() returns a properly cloned map of the music collection.
     * <p>
     * This test verifies that:
     * <ul>
     *   <li>The returned map is not null</li>
     *   <li>The returned map has the correct size</li>
     *   <li>The returned map contains all expected keys</li>
     *   <li>The returned map contains the correct music objects</li>
     *   <li>Each music object in the map is properly cloned</li>
     *   <li>The returned map is a different instance from the original</li>
     * </ul>
     *
     * @throws Exception if an error occurs during test execution
     */
    @Test
    public void testGetMusicsReturnsClonedMap() throws Exception {
        // Arrange
        Music mockMusic1 = mock(Music.class);
        Music mockMusic2 = mock(Music.class);

        when(mockMusic1.clone()).thenReturn(mockMusic1);
        when(mockMusic2.clone()).thenReturn(mockMusic2);

        Map<Integer, Music> musics = new HashMap<>();
        musics.put(1, mockMusic1);
        musics.put(2, mockMusic2);

        MusicManager musicManager = new MusicManager(musics, musics.size());

        // Act
        Map<Integer, Music> result = musicManager.getMusics();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(2));
        assertEquals(mockMusic1, result.get(1));
        assertEquals(mockMusic2, result.get(2));

        // Verify that clone of each music object was created twice
        // Once during MusicManager construction and once during getMusics()
        verify(mockMusic1, times(2)).clone();
        verify(mockMusic2, times(2)).clone();

        // Ensure the returned map is not the same instance as the original musics map
        assertNotSame(musics, result);
    }

    /**
     * Tests that modifications to the map returned by getMusics() do not affect the original map.
     * <p>
     * This test verifies that:
     * <ul>
     *   <li>Removing an element from the returned map does not affect the original map</li>
     *   <li>The original map maintains its size and content after modification of the returned map</li>
     *   <li>The integrity of the original music collection is preserved</li>
     * </ul>
     *
     * @throws Exception if an error occurs during test execution
     */
    @Test
    public void testGetMusicsDoesNotMutateOriginalMap() throws Exception {
        // Arrange
        Music mockMusic1 = mock(Music.class);
        Music mockMusic2 = mock(Music.class);

        when(mockMusic1.clone()).thenReturn(mockMusic1);
        when(mockMusic2.clone()).thenReturn(mockMusic2);

        Map<Integer, Music> musics = new HashMap<>();
        musics.put(1, mockMusic1);
        musics.put(2, mockMusic2);

        MusicManager musicManager = new MusicManager(musics, musics.size());

        // Act
        Map<Integer, Music> result = musicManager.getMusics();
        result.remove(1);  // Modify the returned map

        // Assert
        // Ensure the original musics map in the MusicManager is not modified
        Map<Integer, Music> originalMusics = musicManager.getMusics();
        assertEquals(2, originalMusics.size());
        assertTrue(originalMusics.containsKey(1));
        assertTrue(originalMusics.containsKey(2));
    }
}