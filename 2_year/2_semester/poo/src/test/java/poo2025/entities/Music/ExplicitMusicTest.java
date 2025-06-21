/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.Music;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the {@link ExplicitMusic} class.
 * This class contains unit tests that verify the behavior of the getIdentifier() method
 * and related functionality in the ExplicitMusic class. It ensures that identifiers
 * are properly assigned, maintained, and handled across different scenarios.
 *
 * @see ExplicitMusic
 */
class ExplicitMusicTest {

    /**
     * Sets up the test environment before each test case.
     * Currently, this method is a placeholder for potential static state reset.
     */
    @BeforeEach
    public void setup() {
        // Reset any static state if needed
    }

    /**
     * Tests the getIdentifier method using the default constructor.
     * Verifies that the identifier assigned to a new ExplicitMusic instance
     * is a non-negative number when created with the default constructor.
     */
    @Test
    void testGetIdentifier_DefaultConstructor() {
        // Given
        ExplicitMusic music = new ExplicitMusic();
        
        // When
        int identifier = music.getIdentifier();
        
        // Then
        assertTrue(identifier >= 0, "Identifier should be a non-negative number");
    }

    /**
     * Tests the getIdentifier method using the parameterized constructor.
     * Verifies that the identifier is properly assigned when creating an
     * ExplicitMusic instance with all parameters specified.
     */
    @Test
    void testGetIdentifier_CustomConstructor() {
        // Given
        ExplicitMusic music = new ExplicitMusic("Song Name", "Artist", "Sample lyrics", "Sound File",
                "Publisher", "Genre", 240, 3000, true, 18, "Custom Source");
        
        // When
        int identifier = music.getIdentifier();
        
        // Then
        assertTrue(identifier >= 0, "Identifier should be a non-negative number");
    }

    /**
     * Tests the uniqueness of identifiers across multiple instances.
     * Verifies that different instances of ExplicitMusic receive different
     * identifiers, and that all identifiers are non-negative.
     */
    @Test
    void testGetIdentifier_MultipleInstances() {
        // Given
        ExplicitMusic first = new ExplicitMusic();
        ExplicitMusic second = new ExplicitMusic();
        
        // When
        int firstId = first.getIdentifier();
        int secondId = second.getIdentifier();
        
        // Then
        assertNotEquals(firstId, secondId, "Different instances should have different identifiers");
        assertTrue(firstId >= 0 && secondId >= 0, "Identifiers should be non-negative");
    }

    /**
     * Tests the behavior of identifiers during object cloning.
     * Verifies that a cloned ExplicitMusic instance maintains the same
     * identifier as its original object.
     */
    @Test
    void testGetIdentifier_AfterClone() {
        // Given
        ExplicitMusic original = new ExplicitMusic("Original Song", "Original Artist", "Original lyrics", "Sound File",
                "Original Publisher", "Genre", 180, 500, false, 0, "Original Source");
        
        // When
        ExplicitMusic cloned = original.clone();
        
        // Then
        assertEquals(original.getIdentifier(), cloned.getIdentifier(),
                "Cloned object's identifier should match the original object's identifier");
    }

    /**
     * Tests the immutability of identifiers after object modification.
     * Verifies that the identifier remains unchanged when other properties
     * of the ExplicitMusic instance are modified.
     */
    @Test
    void testGetIdentifier_AfterModification() {
        // Given
        ExplicitMusic music = new ExplicitMusic();
        int originalId = music.getIdentifier();
        
        // When
        music.setName("Modified Name");
        int newId = music.getIdentifier();
        
        // Then
        assertEquals(originalId, newId, "Identifier should not change after modifying other properties");
    }
}