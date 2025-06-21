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
 * Test class for the {@code PremiumTop} class.
 *
 * The {@code PremiumTopTest} class contains unit tests to validate the behavior of the
 * {@code calculatePoints} method in the {@code PremiumTop} class. These tests cover
 * various scenarios, including zero, positive, negative, and large inputs.
 *
 * Each test ensures that the method adheres to its defined behavior by comparing
 * the actual output to the expected result using assertions.
 */
class PremiumTopTest {
    /**
     * Tests the calculatePoints method of the PremiumTop class with zero current points and zero plays.
     *
     * This test verifies that the method correctly returns the base value of 100 when both
     * the current points and the number of plays are zero.
     *
     * Expected behavior:
     * The calculatePoints method should return 100, as the formula adds a base of 100 irrespective
     * of other inputs when both values are zero.
     */
    @Test
    void testCalculatePointsWithZeroPointsAndZeroPlays() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = 0;
        int plays = 0;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(100, result, "Points calculation should return the base value of 100 for zero current points and zero plays.");
    }

    /**
     * Tests the calculatePoints method in the PremiumTop class with zero current points
     * and a positive number of plays.
     *
     * The purpose of this test is to verify that the method correctly calculates and
     * returns the base value of 100 when the current points are zero, regardless of
     * the number of plays provided.
     *
     * Expected outcome:
     * - When the current points are 0 and the number of plays is positive (e.g., 10),
     *   the calculated points should be 100 as per the formula in the method.
     *
     * Assertions:
     * - Checks if the result of the calculatePoints method is equal to 100.
     * - Includes an assertion message to clarify the failure cause if the result
     *   does not match the expected value.
     */
    @Test
    void testCalculatePointsWithZeroPointsAndPositivePlays() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = 0;
        int plays = 10;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(100, result, "Points calculation should return the base value of 100 for zero current points, irrespective of plays.");
    }

    /**
     * Validates the behavior of the `calculatePoints` method when provided with
     * positive `currentPoints` and zero `plays`.
     *
     * The test ensures that the method correctly calculates points as the base value
     * of 100 when the number of plays is zero, regardless of the current points.
     *
     * Test scenario:
     * - `currentPoints` is set to a positive value (e.g., 500).
     * - `plays` is set to zero.
     * - The expected result is 100, as the formula should rely solely on the base
     *   value with no contribution from the play's multiplier.
     *
     * Assertions:
     * - Asserts that the result equals 100 with a descriptive message for expected behavior.
     */
    @Test
    void testCalculatePointsWithPositivePointsAndZeroPlays() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = 500;
        int plays = 0;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(100, result, "Points calculation should return the base value of 100 for zero plays, irrespective of current points.");
    }

    /**
     * Tests the `calculatePoints` method in the `PremiumTop` class when provided
     * with positive values for both the `currentPoints` and `plays` parameters.
     *
     * The test validates that the method calculates the points correctly based on its formula:
     * `100 + (currentPoints * 0.025 * plays)`. Specifically, it checks that the calculation
     * yields the expected result of 150 when `currentPoints` is 400 and `plays` is 5.
     *
     * Expected behavior:
     * - The method should return a correct computed value based on the provided inputs.
     *
     * Test data:
     * - `currentPoints`: 400
     * - `plays`: 5
     *
     * Assertions:
     * - Verifies that the calculated result matches the expected value of 150.
     */
    @Test
    void testCalculatePointsWithPositivePointsAndPositivePlays() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = 400;
        int plays = 5;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(150, result, "Points calculation should return the correct value based on the formula when both current points and plays are positive.");
    }

    /**
     * Tests the calculatePoints method with large values for current points and number of plays.
     *
     * This test ensures that the method can handle large input values without errors
     * and correctly calculates the total points based on the provided formula.
     *
     * Test case:
     * - Given a current points value of 1,000,000 and 200 plays
     * - Verifies that the calculated points match the expected value of 5,000,100
     *   based on the formula implemented in the calculatePoints method.
     *
     * Assert:
     * - The result matches the expected value, indicating that the method processes
     *   large input values correctly.
     */
    @Test
    void testCalculatePointsWithLargeValues() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = 1_000_000;
        int plays = 200;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(5_000_100, result, "Points calculation should handle large values correctly according to the formula.");
    }

    /**
     * Tests the `calculatePoints` method of the `PremiumTop` class when the current points
     * balance is negative. This scenario ensures that the method correctly handles and
     * computes the points based on the given formula when negative point values are provided.
     *
     * The method initializes a `PremiumTop` instance, provides a negative value for
     * the current points and a positive number of plays, and verifies the output
     * against the expected result using an assertion.
     *
     * Expected behavior:
     * - When the current points are negative, the formula should still compute the total points
     *   correctly, potentially resulting in a lower total points value.
     *
     * Assertion:
     * - Validates that the computed result matches the expected output of 75.
     */
    @Test
    void testCalculatePointsWithNegativePoints() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = -100;
        int plays = 10;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(75, result, "Points calculation should return the correct value when current points are negative.");
    }

    /**
     * Tests the `calculatePoints` method of the `PremiumTop` class when the number of plays is negative.
     *
     * This test verifies that the calculated points are accurate even when the input
     * for the number of plays is negative. It uses the following inputs:
     * - `currentPoints`: 200
     * - `plays`: -5
     *
     * The expected result for these inputs is 75, as per the calculation formula implemented
     * in the `calculatePoints` method.
     *
     * The test will pass if the result matches the expected value, validating that the method
     * handles negative plays correctly.
     */
    @Test
    void testCalculatePointsWithNegativePlays() {
        PremiumTop premiumTop = new PremiumTop();
        double currentPoints = 200;
        int plays = -5;

        double result = premiumTop.calculatePoints(currentPoints, plays);

        assertEquals(75, result, "Points calculation should return the correct value when plays are negative.");
    }
}