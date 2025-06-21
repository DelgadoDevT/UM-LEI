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
 * Test class for the {@link MultimediaMusic} class.
 * This class contains unit tests that verify the functionality of the identifier generation
 * and management in the MultimediaMusic class. The tests ensure that identifiers are
 * properly assigned, unique, and maintained across different scenarios.
 *
 * @see MultimediaMusic
 */
public class MultimediaMusicTest {
    /** Expected identifier value used for testing purposes */
    private static int expectedId = 0;

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
     * Verifies that the identifier assigned to a new MultimediaMusic instance
     * is a non-negative number when created with the default constructor.
     */
    @Test
    public void testGetIdentifier_DefaultConstructor() {
        // Given
        MultimediaMusic multimediaMusic = new MultimediaMusic();
        
        // When
        int identifier = multimediaMusic.getIdentifier();
        
        // Then
        assertTrue(identifier >= 0, "Identifier should be a non-negative number");
    }

    /**
     * Tests the getIdentifier method using the parameterized constructor.
     * Verifies that the identifier is properly assigned when creating a
     * MultimediaMusic instance with all parameters specified.
     */
    @Test
    public void testGetIdentifier_CustomConstructor() {
        // Given
        MultimediaMusic multimediaMusic = new MultimediaMusic(
                "Test Song", "Test Artist", "Test Lyrics", "Test Sound", "Test Publisher", "Test Genre",
                180, 5, "https://example.com/video", "720p", true
        );
        
        // When
        int identifier = multimediaMusic.getIdentifier();
        
        // Then
        assertTrue(identifier >= 0, "Identifier should be a non-negative number");
    }

    /**
     * Tests the uniqueness of identifiers across multiple instances.
     * Verifies that different instances of MultimediaMusic receive different
     * identifiers, and that all identifiers are non-negative.
     */
    @Test
    public void testGetIdentifier_MultipleInstances() {
        // Given
        MultimediaMusic first = new MultimediaMusic();
        MultimediaMusic second = new MultimediaMusic();
        
        // When
        int firstId = first.getIdentifier();
        int secondId = second.getIdentifier();
        
        // Then
        assertNotEquals(firstId, secondId, "Different instances should have different identifiers");
        assertTrue(firstId >= 0 && secondId >= 0, "Identifiers should be non-negative");
    }

    /**
     * Tests the immutability of identifiers after object modification.
     * Verifies that the identifier remains unchanged when other properties
     * of the MultimediaMusic instance are modified.
     */
    @Test
    public void testGetIdentifier_AfterModification() {
        // Given
        MultimediaMusic multimediaMusic = new MultimediaMusic();
        int originalId = multimediaMusic.getIdentifier();
        
        // When
        multimediaMusic.setName("Modified Name");
        int newId = multimediaMusic.getIdentifier();
        
        // Then
        assertEquals(originalId, newId, "Identifier should not change after modifying other properties");
    }
}