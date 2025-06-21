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
 * Test class for the Music class.
 * This class contains comprehensive unit tests for verifying the functionality
 * of the Music class implementation. It tests all constructors, getters, setters,
 * and utility methods to ensure proper behavior and data integrity.
 *
 * The test suite includes verification of:
 * - Default constructor initialization
 * - Parameterized constructor with all fields
 * - Copy constructor functionality
 * - Getter and setter methods for all properties
 * - Music playback simulation
 * - Object cloning
 * - Equality comparison
 * - String representation methods
 */
class MusicTest {
    /** Instance for testing default constructor behavior */
    private Music defaultMusic;
    
    /** Instance for testing parameterized constructor and general methods */
    private Music parameterizedMusic;

    /** Test constant for music name */
    private static final String TEST_NAME = "Test Song";
    
    /** Test constant for music interpreter */
    private static final String TEST_INTERPRETER = "Test Artist";
    
    /** Test constant for music publisher */
    private static final String TEST_PUBLISHER = "Test Label";
    
    /** Test constant for music lyrics */
    private static final String TEST_LYRICS = "Test lyrics";
    
    /** Test constant for music sound file */
    private static final String TEST_SOUND = "test.mp3";
    
    /** Test constant for music genre */
    private static final String TEST_GENRE = "Test Genre";
    
    /** Test constant for music duration in seconds */
    private static final int TEST_DURATION = 180;
    
    /** Test constant for initial play count */
    private static final int TEST_PLAYS = 0;

    /**
     * Sets up the test environment before each test.
     * Creates two Music instances:
     * - One using the default constructor
     * - One using the parameterized constructor with test constants
     */
    @BeforeEach
    void setUp() {
        defaultMusic = new Music();
        parameterizedMusic = new Music(
                TEST_NAME,
                TEST_INTERPRETER,
                TEST_PUBLISHER,
                TEST_LYRICS,
                TEST_SOUND,
                TEST_GENRE,
                TEST_DURATION,
                TEST_PLAYS
        );
    }

    /**
     * Tests the default constructor.
     * Verifies that:
     * - The created instance is not null
     * - The identifier is non-negative
     * - All string fields are initialized to empty strings
     * - Numeric fields are initialized to zero
     */
    @Test
    void testDefaultConstructor() {
        assertNotNull(defaultMusic);
        assertTrue(defaultMusic.getIdentifier() >= 0);
        assertEquals("", defaultMusic.getName());
        assertEquals("", defaultMusic.getInterpreter());
        assertEquals("", defaultMusic.getPublisher());
        assertEquals("", defaultMusic.getLyrics());
        assertEquals("", defaultMusic.getSound());
        assertEquals("", defaultMusic.getGenre());
        assertEquals(0, defaultMusic.getDurationS());
        assertEquals(0, defaultMusic.getPlays());
    }

    /**
     * Tests the parameterized constructor.
     * Verifies that:
     * - The identifier is properly assigned
     * - All fields are correctly initialized with the provided values
     */
    @Test
    void testParameterizedConstructor() {
        assertTrue(parameterizedMusic.getIdentifier() >= 0);
        assertEquals(TEST_NAME, parameterizedMusic.getName());
        assertEquals(TEST_INTERPRETER, parameterizedMusic.getInterpreter());
        assertEquals(TEST_PUBLISHER, parameterizedMusic.getPublisher());
        assertEquals(TEST_LYRICS, parameterizedMusic.getLyrics());
        assertEquals(TEST_SOUND, parameterizedMusic.getSound());
        assertEquals(TEST_GENRE, parameterizedMusic.getGenre());
        assertEquals(TEST_DURATION, parameterizedMusic.getDurationS());
        assertEquals(TEST_PLAYS, parameterizedMusic.getPlays());
    }

    /**
     * Tests the copy constructor.
     * Verifies that:
     * - All fields are correctly copied from the source object
     * - The copied object maintains the same identifier as the source
     */
    @Test
    void testCopyConstructor() {
        Music copiedMusic = new Music(parameterizedMusic);

        assertEquals(parameterizedMusic.getIdentifier(), copiedMusic.getIdentifier());
        assertEquals(parameterizedMusic.getName(), copiedMusic.getName());
        assertEquals(parameterizedMusic.getInterpreter(), copiedMusic.getInterpreter());
        assertEquals(parameterizedMusic.getPublisher(), copiedMusic.getPublisher());
        assertEquals(parameterizedMusic.getLyrics(), copiedMusic.getLyrics());
        assertEquals(parameterizedMusic.getSound(), copiedMusic.getSound());
        assertEquals(parameterizedMusic.getGenre(), copiedMusic.getGenre());
        assertEquals(parameterizedMusic.getDurationS(), copiedMusic.getDurationS());
        assertEquals(parameterizedMusic.getPlays(), copiedMusic.getPlays());
    }

    /**
     * Tests the identifier generation mechanism.
     * Verifies that:
     * - Identifiers are non-negative
     * - Sequential instances receive increasing identifier values
     */
    @Test
    void testGetIdentifier() {
        Music first = new Music();
        Music second = new Music();

        assertTrue(first.getIdentifier() >= 0);
        assertTrue(second.getIdentifier() > first.getIdentifier());
    }

    /**
     * Tests all setter and getter methods.
     * Verifies that:
     * - Values can be set and retrieved correctly for all fields
     * - The identifier remains valid after modifications
     */
    @Test
    void testSettersAndGetters() {
        Music music = new Music();
        assertTrue(music.getIdentifier() >= 0);

        music.setName(TEST_NAME);
        assertEquals(TEST_NAME, music.getName());

        music.setInterpreter(TEST_INTERPRETER);
        assertEquals(TEST_INTERPRETER, music.getInterpreter());

        music.setPublisher(TEST_PUBLISHER);
        assertEquals(TEST_PUBLISHER, music.getPublisher());

        music.setLyrics(TEST_LYRICS);
        assertEquals(TEST_LYRICS, music.getLyrics());

        music.setSound(TEST_SOUND);
        assertEquals(TEST_SOUND, music.getSound());

        music.setGenre(TEST_GENRE);
        assertEquals(TEST_GENRE, music.getGenre());

        music.setDurationS(TEST_DURATION);
        assertEquals(TEST_DURATION, music.getDurationS());

        music.setPlays(TEST_PLAYS);
        assertEquals(TEST_PLAYS, music.getPlays());
    }

    /**
     * Tests that the play() method increments the play count.
     * Verifies that the play count increases by exactly one after calling play()
     */
    @Test
    void testPlayIncrementsPlays() {
        int initialPlays = parameterizedMusic.getPlays();
        parameterizedMusic.play();
        assertEquals(initialPlays + 1, parameterizedMusic.getPlays());
    }

    /**
     * Tests the output format of the play() method.
     * Verifies that the returned string contains:
     * - The song name
     * - The interpreter name
     * - The lyrics
     */
    @Test
    void testPlayReturnsExpectedOutput() {
        String playResult = parameterizedMusic.play();
        assertTrue(playResult.contains(TEST_NAME));
        assertTrue(playResult.contains(TEST_INTERPRETER));
        assertTrue(playResult.contains(TEST_LYRICS));
    }

    /**
     * Tests the addPlays() method.
     * Verifies that the play count is correctly incremented by the specified amount
     */
    @Test
    void testAddPlays() {
        int initialPlays = parameterizedMusic.getPlays();
        int playsToAdd = 5;

        parameterizedMusic.addPlays(playsToAdd);
        assertEquals(initialPlays + playsToAdd, parameterizedMusic.getPlays());
    }

    /**
     * Tests the simpleView() method.
     * Verifies that the returned string contains:
     * - The identifier
     * - The name
     * - The interpreter
     * - The genre
     */
    @Test
    void testSimpleView() {
        String simpleView = parameterizedMusic.simpleView();

        assertTrue(simpleView.contains(String.valueOf(parameterizedMusic.getIdentifier())));
        assertTrue(simpleView.contains(TEST_NAME));
        assertTrue(simpleView.contains(TEST_INTERPRETER));
        assertTrue(simpleView.contains(TEST_GENRE));
    }

    /**
     * Tests the clone() method.
     * Verifies that:
     * - The cloned object is different from the original
     * - The cloned object has equal content to the original
     */
    @Test
    void testClone() {
        Music clonedMusic = parameterizedMusic.clone();

        assertNotSame(parameterizedMusic, clonedMusic);
        assertEquals(parameterizedMusic, clonedMusic);
    }

    /**
     * Tests the equals() method.
     * Verifies that:
     * - A music object is not equal to null
     * - A music object is not equal to an object of a different class
     */
    @Test
    void testEquals() {
        Music music1 = new Music(TEST_NAME, TEST_INTERPRETER, TEST_PUBLISHER, TEST_LYRICS,
                TEST_SOUND, TEST_GENRE, TEST_DURATION, TEST_PLAYS);
        Music music2 = new Music(TEST_NAME, TEST_INTERPRETER, TEST_PUBLISHER, TEST_LYRICS,
                TEST_SOUND, TEST_GENRE, TEST_DURATION, TEST_PLAYS);

        assertNotEquals(music1, null);
        assertNotEquals(music1, new Object());
    }
}