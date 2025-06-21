/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.common;

import org.junit.jupiter.api.Test;
import poo2025.exceptions.AuthenticatorException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the {@code Authenticator} class that validates its core authentication
 * functionalities such as user login, logout, and error handling under various scenarios.
 *
 * The tests in this class ensure that the {@code Authenticator} behaves as expected
 * when handling correct credentials, invalid credentials, user transitions, and edge cases.
 */
class AuthenticatorTest {
    /**
     * Tests the successful login functionality of the Authenticator class.
     *
     * This method verifies that a user with a valid user ID and password can log in successfully,
     * and that the logged-in user's ID is correctly set in the Authenticator instance.
     *
     * Preconditions:
     * - The Authenticator instance is initialized.
     * - A user with a specific user ID and password is added to the Authenticator.
     *
     * Actions:
     * - Logs in the user with the correct credentials.
     * - Verifies the login operation was successful.
     * - Confirms the logged-in user's ID matches the expected user ID.
     *
     * Expected Behavior:
     * - The login method should return true, indicating a successful login.
     * - The getLoggedInUserId method should return the correct user ID of the logged-in user.
     *
     * @throws AuthenticatorException if there is an unexpected failure during authentication operations
     */
    @Test
    void testLogin_SuccessfulLogin() throws AuthenticatorException {
        Authenticator authenticator = new Authenticator();
        Integer userId = 1;
        String password = "password123";

        authenticator.addUser(userId, password);
        assertTrue(authenticator.login(userId, password));
        assertEquals(userId, authenticator.getLoggedInUserId());
    }

    /**
     * Tests the behavior of the {@code login} method when attempting to log in
     * with a non-existent user ID.
     *
     * This test ensures that the system throws an {@code AuthenticatorException}
     * with the appropriate error message when a user ID that does not exist
     * in the authentication system is used. Furthermore, it verifies that
     * no user is marked as logged in after the failed login attempt.
     *
     * Expected behavior:
     * - An {@code AuthenticatorException} is thrown with the message
     *   "User ID does not exist".
     * - The value returned by {@code getLoggedInUserId()} is {@code null}.
     */
    @Test
    void testLogin_UserDoesNotExist() {
        Authenticator authenticator = new Authenticator();
        Integer nonExistentUserId = 2;
        String password = "password123";

        AuthenticatorException exception = assertThrows(AuthenticatorException.class, () -> {
            authenticator.login(nonExistentUserId, password);
        });
        assertEquals("User ID does not exist", exception.getMessage());
        assertNull(authenticator.getLoggedInUserId());
    }

    /**
     * Tests the behavior of the login functionality when an invalid password is provided.
     * This method validates that an {@code AuthenticatorException} is thrown with the
     * appropriate error message when attempting to log in using an incorrect password.
     * It also ensures that no user is logged in after the failed login attempt.
     *
     * @throws AuthenticatorException if there is an unexpected issue during authentication
     */
    @Test
    void testLogin_InvalidPassword() throws AuthenticatorException {
        Authenticator authenticator = new Authenticator();
        Integer userId = 3;
        String correctPassword = "correctPassword";
        String incorrectPassword = "incorrectPassword";

        authenticator.addUser(userId, correctPassword);

        AuthenticatorException exception = assertThrows(AuthenticatorException.class, () -> {
            authenticator.login(userId, incorrectPassword);
        });
        assertEquals("Invalid password", exception.getMessage());
        assertNull(authenticator.getLoggedInUserId());
    }

    /**
     * Tests the scenario where a user logs in after another user has already
     * been logged in and subsequently logged out.
     *
     * Verifies that the following behaviors occur:
     * - A user can successfully log in after another user has logged out.
     * - The logged-in user is accurately reflected after each login process.
     * - The system maintains a consistent state and properly handles user transitions.
     *
     * This test ensures:
     * - The login process correctly identifies and updates the currently logged-in user.
     * - State transitions between users do not introduce inconsistencies or errors.
     * - The logout process effectively clears the current session, enabling other users to log in.
     *
     * @throws AuthenticatorException if any issues occur during login, logout, or user addition processes
     */
    @Test
    void testLogin_AnotherUserAlreadyLoggedIn() throws AuthenticatorException {
        Authenticator authenticator = new Authenticator();
        Integer userId1 = 4;
        String password1 = "password1";
        Integer userId2 = 5;
        String password2 = "password2";

        authenticator.addUser(userId1, password1);
        authenticator.addUser(userId2, password2);

        authenticator.login(userId1, password1);
        assertTrue(authenticator.isLoggedIn());
        assertEquals(userId1, authenticator.getLoggedInUserId());

        authenticator.logout();

        authenticator.login(userId2, password2);
        assertTrue(authenticator.isLoggedIn());
        assertEquals(userId2, authenticator.getLoggedInUserId());
    }
}