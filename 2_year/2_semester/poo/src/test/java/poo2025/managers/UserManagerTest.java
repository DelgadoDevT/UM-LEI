/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.managers;

import org.junit.jupiter.api.Test;
import poo2025.entities.User.User;
import poo2025.exceptions.UserException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link UserManager} class. This class contains unit tests that verify
 * the functionality of the UserManager's methods, particularly focusing on the user map
 * management and data integrity aspects.
 * 
 * <p>The tests in this class specifically verify:</p>
 * <ul>
 *     <li>Proper cloning of user maps when retrieving users</li>
 *     <li>Correct handling of empty user scenarios</li>
 *     <li>Protection against modifications of the original user map</li>
 * </ul>
 * 
 * <p>These tests use Mockito framework for mocking {@link User} objects and verifying
 * their behavior.</p>
 */
class UserManagerTest {

    /**
     * Tests that {@link UserManager#getUsers()} returns a properly cloned map of users.
     * 
     * <p>This test verifies that:</p>
     * <ul>
     *     <li>The returned map has the correct size</li>
     *     <li>The returned map is a different instance than the original</li>
     *     <li>The user objects in the returned map are cloned</li>
     *     <li>The clone() method is called exactly once for each user</li>
     * </ul>
     *
     * @throws UserException if there's an error during user management operations
     */
    @Test
    void testGetUsersReturnsClonedMap() throws UserException {
        // Arrange
        Map<Integer, User> originalUsers = new HashMap<>();
        User mockUser1 = mock(User.class);
        User mockUser1Clone = mock(User.class);
        User mockUser2 = mock(User.class);
        User mockUser2Clone = mock(User.class);

        when(mockUser1.clone()).thenReturn(mockUser1Clone);
        when(mockUser2.clone()).thenReturn(mockUser2Clone);

        originalUsers.put(1, mockUser1);
        originalUsers.put(2, mockUser2);

        UserManager userManager = new UserManager(originalUsers, 2);

        // Act
        Map<Integer, User> returnedUsers = userManager.getUsers();

        // Assert
        assertEquals(2, returnedUsers.size());
        assertNotSame(originalUsers, returnedUsers);
        assertNotSame(originalUsers.get(1), returnedUsers.get(1));
        assertNotSame(originalUsers.get(2), returnedUsers.get(2));

        // Verify each user's clone method was called
        verify(mockUser1, times(1)).clone();
        verify(mockUser2, times(1)).clone();
    }

    /**
     * Tests that {@link UserManager#getUsers()} returns an empty map when no users exist.
     * 
     * <p>This test verifies that a newly created UserManager with no users
     * returns an empty map when getUsers() is called.</p>
     */
    @Test
    void testGetUsersReturnsEmptyMapWhenNoUsers() {
        // Arrange
        UserManager userManager = new UserManager();

        // Act
        Map<Integer, User> returnedUsers = userManager.getUsers();

        // Assert
        assertTrue(returnedUsers.isEmpty());
    }

    /**
     * Tests that modifications to the map returned by {@link UserManager#getUsers()}
     * do not affect the original users map in the UserManager.
     * 
     * <p>This test verifies that:</p>
     * <ul>
     *     <li>The returned map can be modified without affecting the original data</li>
     *     <li>Adding a new user to the returned map doesn't affect the UserManager's internal state</li>
     * </ul>
     *
     * @throws UserException if there's an error during user management operations
     */
    @Test
    void testGetUsersDoesNotModifyOriginalUsersMap() throws UserException {
        // Arrange
        Map<Integer, User> originalUsers = new HashMap<>();
        User mockUser = mock(User.class);
        when(mockUser.clone()).thenReturn(mockUser);

        originalUsers.put(1, mockUser);

        UserManager userManager = new UserManager(originalUsers, 1);

        // Act
        Map<Integer, User> returnedUsers = userManager.getUsers();
        returnedUsers.put(2, mock(User.class));

        // Assert
        assertFalse(userManager.getUsers().containsKey(2));
    }
}