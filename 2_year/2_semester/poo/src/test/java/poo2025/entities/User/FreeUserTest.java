/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.User;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for the {@link FreeUser} class, specifically focusing on the points calculation functionality.
 * This class contains unit tests that verify the correct behavior of the {@code calcPoints} method
 * under various scenarios.
 *
 * @see FreeUser
 */
class FreeUserTest {

    /**
     * Tests the calculation of points when a free user has zero plays.
     * Verifies that the points calculation returns 0.0 when there are no plays recorded.
     */
    @Test
    void testCalcPointsWithZeroPlays() {
        // Arrange
        FreeUser freeUser = new FreeUser("TestUser", 25, "test@example.com", "123 Test St", 0.0, 0);

        // Act
        double calculatedPoints = freeUser.calcPoints();

        // Assert
        assertEquals(0.0, calculatedPoints, "Points should be 0.0 when plays are 0");
    }

    /**
     * Tests the calculation of points when a free user has exactly one play.
     * Verifies that the points calculation returns 5.0 points for a single play.
     */
    @Test
    void testCalcPointsWithOnePlay() {
        // Arrange
        FreeUser freeUser = new FreeUser("TestUser", 25, "test@example.com", "123 Test St", 0.0, 1);

        // Act
        double calculatedPoints = freeUser.calcPoints();

        // Assert
        assertEquals(5.0, calculatedPoints, "Points should be 5.0 when plays are 1");
    }

    /**
     * Tests the calculation of points when a free user has multiple plays.
     * Verifies that the points calculation correctly multiplies the number of plays by 5.0.
     */
    @Test
    void testCalcPointsWithMultiplePlays() {
        // Arrange
        FreeUser freeUser = new FreeUser("TestUser", 25, "test@example.com", "123 Test St", 0.0, 10);

        // Act
        double calculatedPoints = freeUser.calcPoints();

        // Assert
        assertEquals(50.0, calculatedPoints, "Points should be 50.0 when plays are 10");
    }

    /**
     * Tests the calculation of points when a free user has a large number of plays.
     * Verifies that the points calculation handles large numbers correctly without overflow or precision issues.
     */
    @Test
    void testCalcPointsWithLargeNumberOfPlays() {
        // Arrange
        FreeUser freeUser = new FreeUser("TestUser", 25, "test@example.com", "123 Test St", 0.0, 1000);

        // Act
        double calculatedPoints = freeUser.calcPoints();

        // Assert
        assertEquals(5000.0, calculatedPoints, "Points should be 5000.0 when plays are 1000");
    }

    /**
     * Tests the calculation of points when a free user has negative plays.
     * Verifies that the points calculation handles negative numbers by returning negative points.
     */
    @Test
    void testCalcPointsWithNegativePlays() {
        // Arrange
        FreeUser freeUser = new FreeUser("TestUser", 25, "test@example.com", "123 Test St", 0.0, -5);

        // Act
        double calculatedPoints = freeUser.calcPoints();

        // Assert
        assertEquals(-25.0, calculatedPoints, "Points should be -25.0 when plays are -5");
    }
}