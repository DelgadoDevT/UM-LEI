/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import poo2025.entities.Music.Music;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for the User entity.
 * This class contains unit tests to verify the functionality of the User class,
 * particularly focusing on music playback and points calculation features.
 *
 * @see User
 */
@DisplayName("User Class Tests")
class UserTest {
    /** Enables ByteBuddy experimental features for mocking */
    static {System.setProperty("net.bytebuddy.experimental", "true");}

    /** Mock object representing a Music instance for testing */
    private Music mockMusic;

    /** Test instance of User class */
    private User testUser;

    /**
     * Sets up the test environment before each test.
     * Initializes a new mock Music object.
     */
    @BeforeEach
    void setUp() {mockMusic = mock(Music.class);}

    /**
     * Nested test class containing all music playback related tests.
     */
    @Nested
    @DisplayName("Play Music Tests")
    class PlayMusicTests {

        /**
         * Tests successful music playback and points update.
         * Verifies that playing music correctly updates the play count and points.
         */
        @Test
        @DisplayName("Should play music successfully and update points")
        void testPlayMusicSuccessfully() {
            // Arrange
            when(mockMusic.play()).thenReturn("Playing music successfully!");
            testUser = createTestUser("John Doe", 10.0);

            // Act
            String result = testUser.playMusic(mockMusic);

            // Assert
            assertEquals("Playing music successfully!", result);
            assertEquals(1, testUser.getPlays());
            assertEquals(10.0, testUser.getPoints());
            verify(mockMusic, times(1)).play();
        }

        /**
         * Tests handling of null music input.
         * Verifies that the system handles null music objects gracefully without changing state.
         */
        @Test
        @DisplayName("Should handle null music gracefully")
        void testPlayMusicWithNullMusic() {
            // Arrange
            testUser = createTestUser("Jane Doe", 5.0);

            // Act
            String result = testUser.playMusic(null);

            // Assert
            assertEquals("Error playing the music", result);
            assertEquals(0, testUser.getPlays());
            assertEquals(0.0, testUser.getPoints());
        }

        /**
         * Tests points calculation after multiple music plays.
         * Verifies that points are correctly calculated based on the number of plays.
         */
        @Test
        @DisplayName("Should update points correctly after multiple plays")
        void testPlayMusicUpdatesPointsCorrectly() {
            // Arrange
            when(mockMusic.play()).thenReturn("Music is playing.");
            testUser = createTestUser("Alice", 7.0);
            testUser.setPlays(3); // Start with 3 plays

            // Act
            String result = testUser.playMusic(mockMusic);

            // Assert
            assertEquals("Music is playing.", result);
            assertEquals(4, testUser.getPlays());
            assertEquals(28.0, testUser.getPoints()); // 4 plays * 7.0 points per play
            verify(mockMusic, times(1)).play();
        }
    }

    /**
     * Creates a test user with specified name and points per play.
     * This helper method creates an anonymous implementation of the User class
     * with custom point calculation logic for testing purposes.
     *
     * @param name          The name of the test user
     * @param pointsPerPlay The number of points awarded per play
     * @return A new User instance configured for testing
     */
    private User createTestUser(String name, double pointsPerPlay) {
        return new User(name, 30, name.toLowerCase() + "@example.com",
                "123 Street", 0.0, 0) {
            @Override
            public double calcPoints() {
                return getPlays() * pointsPerPlay;
            }

            @Override
            public User clone() {
                User cloned = new User(getName(), getAge(), getEmail(),
                        getAddress(), getPoints(), getPlays()) {
                    @Override
                    public double calcPoints() {
                        return getPlays() * pointsPerPlay;
                    }

                    @Override
                    public User clone() {
                        return this;  // Para testes, retornar this é suficiente
                    }

                    @Override
                    public String toString() {
                        return getName();
                    }
                };
                return cloned;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}