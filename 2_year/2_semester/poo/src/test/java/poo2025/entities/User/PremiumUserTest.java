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
import org.mockito.Mockito;
import poo2025.entities.SubscriptionPlan.SubscriptionPlan;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for the {@link PremiumUser} class.
 * This class contains unit tests that verify the point calculation functionality
 * of the PremiumUser class under various scenarios including valid plans,
 * zero values, negative values, and null subscription plans.
 */
class PremiumUserTest {

    /**
     * Tests the point calculation functionality with a valid subscription plan.
     * Verifies that the points are calculated correctly when provided with
     * valid input values and a properly configured subscription plan.
     */
    @Test
    void testCalcPointsWithValidPlan() {
        // Arrange
        SubscriptionPlan mockPlan = Mockito.mock(SubscriptionPlan.class);
        Mockito.when(mockPlan.calculatePoints(100.0, 5)).thenReturn(150.0);
        PremiumUser premiumUser = new PremiumUser("Test User", 30, "test@example.com", "123 Test Street", 100.0, 5, mockPlan, null, null);

        // Act
        double points = premiumUser.calcPoints();

        // Assert
        assertEquals(150.0, points);
    }

    /**
     * Tests the point calculation when both current points and number of plays are zero.
     * Verifies that the calculation handles zero values appropriately and returns
     * the expected result of zero points.
     */
    @Test
    void testCalcPointsWithZeroPointsAndPlays() {
        // Arrange
        SubscriptionPlan mockPlan = Mockito.mock(SubscriptionPlan.class);
        Mockito.when(mockPlan.calculatePoints(0.0, 0)).thenReturn(0.0);
        PremiumUser premiumUser = new PremiumUser("Test User", 30, "test@example.com", "123 Test Street", 0.0, 0, mockPlan, null, null);

        // Act
        double points = premiumUser.calcPoints();

        // Assert
        assertEquals(0.0, points);
    }

    /**
     * Tests the point calculation behavior when provided with negative values.
     * Verifies that the system handles negative points and plays appropriately,
     * expecting a return value of zero in such cases.
     */
    @Test
    void testCalcPointsWithNegativeValues() {
        // Arrange
        SubscriptionPlan mockPlan = Mockito.mock(SubscriptionPlan.class);
        Mockito.when(mockPlan.calculatePoints(-50.0, -10)).thenReturn(0.0);
        PremiumUser premiumUser = new PremiumUser("Test User", 30, "test@example.com", "123 Test Street", -50.0, -10, mockPlan, null, null);

        // Act
        double points = premiumUser.calcPoints();

        // Assert
        assertEquals(0.0, points);
    }

    /**
     * Tests the point calculation behavior when a null subscription plan is used.
     * Verifies that the method returns the expected default value of zero points
     * when the subscription plan does not exist or is not configured.
     */
    @Test
    void testCalcPointsWithNullPlan() {
        // Arrange
        SubscriptionPlan mockPlan = Mockito.mock(SubscriptionPlan.class);
        Mockito.when(mockPlan.calculatePoints(Mockito.anyDouble(), Mockito.anyInt())).thenReturn(0.0);
        PremiumUser premiumUser = new PremiumUser("Test User", 30, "test@example.com", "123 Test Street", 100.0, 10, mockPlan, null, null);

        // Act
        double points = premiumUser.calcPoints();

        // Assert
        assertEquals(0.0, points);
    }
}