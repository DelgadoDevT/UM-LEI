/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.common;

import poo2025.exceptions.AuthenticatorException;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The Authenticator class provides user authentication functionality, including user management,
 * login/logout operations, and password hashing. It maintains a collection of user credentials
 * and tracks the currently logged-in user.
 */
public class Authenticator implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Stores user credentials as a mapping of user IDs to their hashed passwords */
    private Map<Integer, String> userCredentials;

    /** Stores the ID of the currently logged-in user */
    private Integer loggedInUserId;

    /**
     * Constructs a new Authenticator with an empty user credentials map and no logged-in user.
     * Automatically initializes an admin user with ID -1 and password "1234".
     */
    public Authenticator() {
        userCredentials = new HashMap<>();
        loggedInUserId = null;

        // We know that hardcode the admin profile is not a good idea, but the danger that comes with this implementation will
        // be not considered for the sake of simplicity
        String adminPassword = "1234";
        try {
            this.userCredentials.put(-1, this.hashPassword(adminPassword));
        } catch (AuthenticatorException e) {
            System.err.println("Error initializing admin credentials: " + e.getMessage());
        }
    }

    /**
     * Constructs a new Authenticator with the specified user credentials and logged-in user ID.
     *
     * @param userCredentials a map of user IDs to their hashed passwords
     * @param loggedInUserId the ID of the currently logged-in user, or null if none
     */
    public Authenticator(Map<Integer, String> userCredentials, Integer loggedInUserId) {
        this.userCredentials = new HashMap<>();
        this.userCredentials.putAll(userCredentials);
        this.loggedInUserId = loggedInUserId;
    }

    /**
     * Constructs a new Authenticator as a deep copy of another Authenticator.
     *
     * @param a the Authenticator to copy
     */
    public Authenticator(Authenticator a) {
        this.userCredentials = new HashMap<>();
        this.userCredentials.putAll(a.getUserCredentials());
        this.loggedInUserId = a.getLoggedInUserId();
    }

    /**
     * Returns a copy of the user credentials map.
     *
     * @return a new HashMap containing all user credentials
     */
    protected Map<Integer, String> getUserCredentials() {
        return new HashMap<>(this.userCredentials);
    }

    /**
     * Replaces the current user credentials with a copy of the provided map.
     *
     * @param userCredentials a map of user IDs to their hashed passwords
     */
    protected void setUserCredentials(Map<Integer, String> userCredentials) {
        this.userCredentials = new HashMap<>();
        this.userCredentials.putAll(userCredentials);
    }

    /**
     * Returns the ID of the currently logged-in user.
     *
     * @return the logged-in user ID, or null if no user is logged in
     */
    public Integer getLoggedInUserId() {
        return this.loggedInUserId;
    }

    /**
     * Sets the currently logged-in user ID.
     *
     * @param loggedInUserId the user ID to set as logged in, or null to indicate no user is logged in
     */
    protected void setLoggedInUserId(Integer loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    /**
     * Returns a string representation of the Authenticator.
     *
     * @return a string containing the user credentials and logged-in user ID
     */
    @Override
    public String toString() {
        return "Authenticator{" +
                "userCredentials=" + this.userCredentials +
                ", loggedInUserId=" + this.loggedInUserId +
                '}';
    }

    /**
     * Compares this Authenticator to another object for equality.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Authenticator that = (Authenticator) obj;
        return Objects.equals(this.userCredentials, that.userCredentials) && Objects.equals(this.loggedInUserId, that.loggedInUserId);
    }

    /**
     * Returns a hash code value for the Authenticator.
     *
     * @return a hash code value based on the user credentials and logged-in user ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.userCredentials, this.loggedInUserId);
    }

    /**
     * Adds a new user to the authenticator with the specified ID and password.
     *
     * @param userId the ID of the user to add
     * @param password the user's plaintext password (will be hashed before storage)
     * @throws AuthenticatorException if the user ID already exists
     */
    public void addUser(Integer userId, String password) throws AuthenticatorException {
        if (!userCredentials.containsKey(userId)) {
            String hashedPassword = this.hashPassword(password);
            userCredentials.put(userId, hashedPassword);
        } else {
            throw new AuthenticatorException("User ID already exists");
        }
    }

    /**
     * Removes a user from the authenticator.
     *
     * @param userId the ID of the user to remove
     * @throws AuthenticatorException if the user ID does not exist
     */
    public void removeUser(Integer userId) throws AuthenticatorException {
        if (userCredentials.containsKey(userId)) {
            this.userCredentials.remove(userId);
        } else {
            throw new AuthenticatorException("User ID does not exist");
        }
    }

    /**
     * Attempts to log in a user with the specified credentials.
     *
     * @param userId the ID of the user to log in
     * @param password the plaintext password to verify
     * @return true if login is successful
     * @throws AuthenticatorException if the user ID doesn't exist or the password is invalid
     */
    public boolean login(Integer userId, String password) throws AuthenticatorException {
        String hashedPassword = this.hashPassword(password);
        if (!this.userCredentials.containsKey(userId)) {
            throw new AuthenticatorException("User ID does not exist");
        }
        if (!this.userCredentials.get(userId).equals(hashedPassword)) {
            throw new AuthenticatorException("Invalid password");
        }
        this.loggedInUserId = userId;
        return true;
    }

    /**
     * Logs out the currently logged-in user.
     *
     * @throws AuthenticatorException if no user is currently logged in
     */
    public void logout() throws AuthenticatorException {
        if (this.loggedInUserId == null) {
            throw new AuthenticatorException("No user is currently logged in");
        }
        this.loggedInUserId = null;
    }

    /**
     * Checks whether a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return this.loggedInUserId != null;
    }

    /**
     * Hashes a password using SHA-256 algorithm.
     *
     * @param password the plaintext password to hash
     * @return the hashed password as a hexadecimal string
     * @throws AuthenticatorException if the hashing algorithm is not available
     */
    private String hashPassword(String password) throws AuthenticatorException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AuthenticatorException("Unable to hash password: " + e.getMessage());
        }
    }

    /**
     * Creates and returns a deep copy of this Authenticator.
     *
     * @return a new Authenticator instance with the same user credentials and logged-in state
     */
    @Override
    public Authenticator clone() {
        return new Authenticator(this);
    }
}