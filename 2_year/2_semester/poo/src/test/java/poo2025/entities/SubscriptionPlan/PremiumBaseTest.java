/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.SubscriptionPlan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The PremiumBaseTest class contains unit tests to validate the functionality
 * of the `calculatePoints` method in the `PremiumBase` class. These tests cover
 * scenarios including zero, positive, negative, and high numbers of plays to
 * ensure the method behaves as intended under a variety of conditions.
 *
 * Each test ensures proper calculation of points based on the formula
 * {@code 10 * plays}. Assertions verify the output matches expected results.
 *
 * Test cases include:
 * - Calculation when the number of plays is zero.
 * - Calculation with positive values for the number of plays.
 * - Calculation with a high number of plays to validate scalability.
 * - Calculation with negative values for the number of plays.
 *
 * The test suite aims to provide comprehensive coverage for the `calculatePoints` method.
 */
class PremiumBaseTest {
    /**
     * Tests the `calculatePoints` method of the `PremiumBase` class when the number of plays is zero.
     *
     * This test verifies that the points remain zero when no plays are performed. As `calculatePoints`
     * is expected to compute the total points as `10 * plays`, passing zero plays should result in a return value of 0.0.
     *
     * Steps:
     * 1. Instantiates a new `PremiumBase` object.
     * 2. Sets the initial `currentPoints` to 0.0 and the number of `plays` to 0.
     * 3. Calls the `calculatePoints` method and asserts that the result is equal to 0.0.
     *
     * Expected Behavior:
     * The `calculatePoints` method should return 0.0 since zero plays multiplied by any factor results in zero.
     *
     * Assertion:
     * Ensures that the calculated points match the expected output of 0.0.
     */
    @Test
    void testCalculatePointsWithZeroPlays() {
        PremiumBase premiumBase = new PremiumBase();
        double currentPoints = 0.0;
        int plays = 0;

        double calculatedPoints = premiumBase.calculatePoints(currentPoints, plays);

        assertEquals(0.0, calculatedPoints, "Points should be 0 when there are no plays.");
    }

    /**
     * Tests the calculation of points when a positive number of plays is provided.
     *
     * The method verifies that the `calculatePoints` function of the `PremiumBase` class
     * correctly computes points based on the formula (10 * plays) when given a positive integer
     * for the number of plays. Specifically, it ensures that the expected result aligns
     * with the calculated value.
     *
     * Preconditions:
     * - The number of plays is a positive integer.
     * - The initial points balance is zero.
     *
     * Expected Outcome:
     * - The calculated points should equal 10 multiplied by the number of plays.
     *
     * Assertions:
     * - Ensures the calculated value matches the expected points.
     */
    @Test
    void testCalculatePointsWithPositivePlays() {
        PremiumBase premiumBase = new PremiumBase();
        double currentPoints = 0.0;
        int plays = 5;

        double calculatedPoints = premiumBase.calculatePoints(currentPoints, plays);

        assertEquals(50.0, calculatedPoints, "Points should be correctly calculated as 10 * number of plays.");
    }

    /**
     * Tests the calculatePoints method of the PremiumBase class with a high number of plays.
     *
     * The test verifies if the point's calculation scales correctly for a large number of plays.
     * It checks that multiplying the number of plays (100) by 10 produces the expected result.
     *
     * Preconditions:
     * - A PremiumBase object is instantiated.
     * - The currentPoints are set to 0.0.
     * - The play's parameter is set to 100.
     *
     * Expected Behavior:
     * - The calculatePoints method should correctly calculate the points as 1000.0.
     * - The result is validated using the assertEquals method.
     */
    @Test
    void testCalculatePointsWithHighPlays() {
        PremiumBase premiumBase = new PremiumBase();
        double currentPoints = 0.0;
        int plays = 100;

        double calculatedPoints = premiumBase.calculatePoints(currentPoints, plays);

        assertEquals(1000.0, calculatedPoints, "Points calculation should scale appropriately with large numbers of plays.");
    }

    /**
     * Tests the behavior of the {@code calculatePoints} method in the {@code PremiumBase} class
     * when provided with a negative number of plays.
     *
     * This test verifies that the method calculates points correctly when the number of plays
     * is negative. The expected outcome is that the points should be calculated as
     * {@code 10 * plays}. For a negative value of plays, the resulting points should also
     * be negative.
     *
     * The test initializes a {@code PremiumBase} instance and invokes the {@code calculatePoints}
     * method with a negative value of plays. An assertion is made to confirm that the result
     * matches the expected calculated points.
     *
     * Expected behavior:
     * - If {@code plays} is -3, the calculated points should be -30.0.
     */
    @Test
    void testCalculatePointsWithNegativePlays() {
        PremiumBase premiumBase = new PremiumBase();
        double currentPoints = 0.0;
        int plays = -3;

        double calculatedPoints = premiumBase.calculatePoints(currentPoints, plays);

        assertEquals(-30.0, calculatedPoints, "Points should be correctly calculated with negative number of plays as 10 * plays.");
    }
}