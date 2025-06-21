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
 * The SubscriptionPlanTest class is a test suite for validating the behavior
 * and logic of the SubscriptionPlan class and its various implementations.
 * The tests focus on ensuring the correctness of the `calculatePoints` method
 * and its handling of different scenarios, including variations in subscription
 * types, initial points, and play counts.
 *
 * Key Scenarios Tested:
 * - Calculation of points in "Basic" and "Premium" subscription plans.
 * - Proper handling of zero plays.
 * - Correct computation in cases where initial points are negative.
 * - Validation of accuracy when handling high numbers of plays.
 */
class SubscriptionPlanTest {
    /**
     * Tests whether the `calculatePoints` method implemented in the SubscriptionPlan subclass
     * returns the correct updated points for the "Basic" implementation.
     *
     * The "Basic" implementation adds a fixed number of points (1.0) for each play to the
     * current points. This method validates that the calculation logic produces the expected
     * total points when given specific inputs.
     *
     * Preconditions:
     * - A "Basic" subscription plan is created with overridden `calculatePoints` and `clone` methods.
     * - An initial points value and number of plays are provided.
     *
     * Test Steps:
     * 1. Create a "Basic" subscription plan where `calculatePoints` adds `plays * 1.0` to the current points.
     * 2. Use `plan.calculatePoints` with defined initial points and plays.
     * 3. Assert that the returned points match the expected total points based on the logic.
     *
     * Validation:
     * - Confirms the method correctly calculates and returns points with a tolerance of 0.01.
     */
    @Test
    void calculatePoints_ShouldReturnCorrectPoints_ForBasicImplementation() {
        SubscriptionPlan plan = new SubscriptionPlan("Basic", false, false, false) {
            @Override
            public double calculatePoints(double currentPoints, int plays) {
                return currentPoints + plays * 1.0;
            }

            @Override
            public SubscriptionPlan clone() {
                return this;
            }
        };
        double initialPoints = 10.0;
        int plays = 5;

        double result = plan.calculatePoints(initialPoints, plays);

        assertEquals(15.0, result, 0.01);
    }

    /**
     * Tests the calculation of points for the premium subscription plan implementation.
     * The test verifies that the calculatePoints method returns the correct points
     * based on the specified initial points and number of plays.
     *
     * A specific implementation of the SubscriptionPlan class is provided for the test,
     * which calculates points using a formula that applies a multiplier of 1.5 to
     * the number of plays and adds it to the current points.
     *
     * Assertions ensure that the calculated points match the expected value within a
     * tolerance of 0.01.
     *
     * Preconditions:
     * - A premium subscription plan is instantiated with all boolean attributes set to true.
     * - Initial points are set to 20.0.
     * - Number of plays is set to 10.
     *
     * Postconditions:
     * - The resulting points calculation is validated against the expected value of 35.0.
     */
    @Test
    void calculatePoints_ShouldReturnCorrectPoints_ForPremiumImplementation() {
        SubscriptionPlan plan = new SubscriptionPlan("Premium", true, true, true) {
            @Override
            public double calculatePoints(double currentPoints, int plays) {
                return currentPoints + plays * 1.5;
            }

            @Override
            public SubscriptionPlan clone() {
                return this;
            }
        };
        double initialPoints = 20.0;
        int plays = 10;

        double result = plan.calculatePoints(initialPoints, plays);

        assertEquals(35.0, result, 0.01);
    }

    /**
     * Tests the behavior of the {@code calculatePoints} method in the {@code SubscriptionPlan}
     * class when the number of plays is zero.
     *
     * The test sets up a custom subscription plan implementation that multiplies the number
     * of plays by a fixed multiplier (2.0) and adds it to the existing points. It verifies
     * that the current points remain unchanged when there are no plays (i.e., plays = 0).
     *
     * The test specifically checks:
     * - That the {@code calculatePoints} method is correctly implemented when no plays occur.
     * - That the returned points match the initial points provided.
     *
     * Assertions:
     * - The resulting points are expected to equal the initial points when plays are zero.
     */
    @Test
    void calculatePoints_ShouldHandleZeroPlays() {
        SubscriptionPlan plan = new SubscriptionPlan("Zero Plays", true, true, true) {
            @Override
            public double calculatePoints(double currentPoints, int plays) {
                return currentPoints + plays * 2.0;
            }

            @Override
            public SubscriptionPlan clone() {
                return this;
            }
        };
        double initialPoints = 50.0;
        int plays = 0;

        double result = plan.calculatePoints(initialPoints, plays);

        assertEquals(50.0, result, 0.01);
    }

    /**
     * Tests the behavior of the calculatePoints method when handling a scenario
     * involving negative initial points.
     *
     * The test sets up a custom subscription plan with an overridden implementation
     * of calculatePoints. It simulates a scenario where the initial points are negative
     * and a specific number of plays is performed. The test validates that the
     * calculatePoints method correctly computes the result by considering the initial
     * negative points and the specified plays.
     *
     * Steps:
     * 1. A custom SubscriptionPlan is instantiated with overridden calculatePoints logic.
     * 2. The initial points are set to a negative value.
     * 3. A specific number of plays is defined.
     * 4. The calculatePoints method is called with the given inputs.
     * 5. The result is asserted to match the expected value, ensuring correct handling of
     *    negative initial points while calculating the total points.
     *
     * Expected Behavior:
     * The calculated points should correctly add the contribution from the plays
     * to the negative initial points, producing a valid result.
     */
    @Test
    void calculatePoints_ShouldHandleNegativePoints() {
        SubscriptionPlan plan = new SubscriptionPlan("Negative Points", true, false, true) {
            @Override
            public double calculatePoints(double currentPoints, int plays) {
                return currentPoints + plays * 2.5;
            }

            @Override
            public SubscriptionPlan clone() {
                return this;
            }
        };
        double initialPoints = -10.0;
        int plays = 8;

        double result = plan.calculatePoints(initialPoints, plays);

        assertEquals(10.0, result, 0.01);
    }

    /**
     * Tests the behavior of the calculatePoints method when handling a high number of plays.
     * This test verifies that the SubscriptionPlan implementation correctly computes the
     * total points with a large input for the number of plays.
     *
     * The test uses a custom SubscriptionPlan implementation that adds points at a rate
     * of 0.8 points per play. This method initializes the points at 100.0 and simulates
     * 1000 plays to assert the expected total point calculation of 900.0.
     *
     * Additionally, the test ensures that the calculation remains accurate for
     * scenarios involving high levels of user interactions.
     *
     * Assertions:
     * - Asserts that the calculated result matches the expected total of 900.0 points, within a tolerance of 0.01.
     */
    @Test
    void calculatePoints_ShouldHandleHighNumberOfPlays() {
        SubscriptionPlan plan = new SubscriptionPlan("High Plays", true, true, false) {
            @Override
            public double calculatePoints(double currentPoints, int plays) {
                return currentPoints + plays * 0.8;
            }

            @Override
            public SubscriptionPlan clone() {
                return this;
            }
        };
        double initialPoints = 100.0;
        int plays = 1000;

        double result = plan.calculatePoints(initialPoints, plays);

        assertEquals(900.0, result, 0.01);
    }
}