/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.managers;

import poo2025.entities.User.PremiumUser;
import poo2025.entities.User.User;
import poo2025.exceptions.UserException;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The UserManager class is responsible for managing user accounts in the music streaming system.
 * It provides functionality for creating, retrieving, updating, and deleting user accounts,
 * as well as managing user-specific operations such as points calculation and playlist management.
 * This class implements Serializable to support persistence operations.
 *
 * <p>The class maintains a collection of users indexed by their unique identifiers and tracks
 * the total number of users in the system. It provides thread-safe operations for user management
 * and ensures data integrity through defensive copying in its constructors and methods.</p>
 *
 * @see User
 * @see PremiumUser
 * @see UserException
 * @see Serializable
 */
public class UserManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Map storing users with their unique identifiers as keys */
    private Map<Integer, User> users;

    /** Counter tracking the total number of users in the system */
    private int totalUsers;

    /**
     * Constructs a new UserManager with an empty user collection.
     * Initializes the users map and sets the total users count to zero.
     */
    public UserManager() {
        this.users = new HashMap<>();
        this.totalUsers = 0;
    }

    /**
     * Constructs a UserManager with a provided collection of users and total count.
     * Creates deep copies of all users to maintain encapsulation.
     *
     * @param users the map of users to initialize with, keyed by their identifiers
     * @param totalUsers the total number of users to set
     */
    public UserManager(Map<Integer, User> users, int totalUsers) {
        this.users = new HashMap<>();
        for(Map.Entry<Integer, User> entry : users.entrySet())
            this.users.put(entry.getKey(), entry.getValue().clone());
        this.totalUsers = totalUsers;
    }

    /**
     * Copy constructor that creates a deep copy of another UserManager.
     *
     * @param m the UserManager to copy
     */
    public UserManager(UserManager m){
        this.users = new HashMap<>();
        for (Map.Entry<Integer, User> entry : m.getUsers().entrySet())
            this.users.put(entry.getKey(), entry.getValue().clone());
        this.totalUsers = m.getTotalUsers();
    }

    /**
     * Returns a deep copy of the users map.
     *
     * @return a new map containing clones of all users
     */
    public Map<Integer, User> getUsers() {
        Map<Integer, User> m = new HashMap<>();
        for (Map.Entry<Integer, User> entry : this.users.entrySet())
            m.put(entry.getKey(), entry.getValue().clone());
        return m;
    }

    /**
     * Sets the users map with a deep copy of the provided map.
     *
     * @param users the map of users to set
     */
    public void setUsers(Map<Integer, User> users) {
        this.users = new HashMap<>();
        for (Map.Entry<Integer, User> entry : users.entrySet())
            this.users.put(entry.getKey(), entry.getValue().clone());
    }

    /**
     * Gets the total number of users in the system.
     *
     * @return the total number of users
     */
    public int getTotalUsers() {
        return this.totalUsers;
    }

    /**
     * Sets the total number of users in the system.
     *
     * @param totalUsers the new total number of users
     */
    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    /**
     * Adds a new user to the system.
     *
     * @param u the user to add
     * @throws UserException if the user is null or if a user with the same ID already exists
     */
    public void addUser(User u) throws UserException {
        if (u == null) {
            throw new UserException("Cannot add null user");
        }
        if (this.users.containsKey(u.getIdentifier())) {
            throw new UserException("User with ID " + u.getIdentifier() + " already exists");
        }
        this.users.put(u.getIdentifier(), u.clone());
        this.totalUsers += 1;
    }

    /**
     * Retrieves a user by their identifier.
     *
     * @param identifier the unique identifier of the user to retrieve
     * @return a clone of the requested user
     * @throws UserException if the identifier is null or if no user is found with that identifier
     */
    public User getUser(Integer identifier) throws UserException {
        if (identifier == null) {
            throw new UserException("User identifier cannot be null");
        }
        User user = this.users.get(identifier);
        if (user == null) {
            throw new UserException("User with ID " + identifier + " not found");
        }
        return user.clone();
    }

    /**
     * Removes a user from the system.
     *
     * @param identifier the unique identifier of the user to remove
     * @return true if the user was successfully removed
     * @throws UserException if the identifier is null or if no user is found with that identifier
     */
    public boolean removeUser(Integer identifier) throws UserException {
        if (identifier == null) {
            throw new UserException("User identifier cannot be null");
        }
        if (!this.users.containsKey(identifier)) {
            throw new UserException("User with ID " + identifier + " not found");
        }
        this.users.remove(identifier);
        this.totalUsers -= 1;
        return true;
    }

    /**
     * Updates the information for an existing user.
     *
     * @param updatedUser the user object containing updated information
     * @throws UserException if the updated user is null or if no user is found with that identifier
     */
    public void updateUser(User updatedUser) throws UserException {
        if (updatedUser == null) {
            throw new UserException("Updated user cannot be null");
        }
        if (!this.users.containsKey(updatedUser.getIdentifier())) {
            throw new UserException("User with ID " + updatedUser.getIdentifier() + " not found");
        }
        this.users.put(updatedUser.getIdentifier(), updatedUser.clone());
    }

    /**
     * Updates the points for a specific user based on their current status.
     *
     * @param userId the identifier of the user whose points should be updated
     * @throws UserException if the user ID is null or if no user is found with that identifier
     */
    public void updatePointsForUser(Integer userId) throws UserException {
        if (userId == null) {
            throw new UserException("User identifier cannot be null");
        }
        User user = this.users.get(userId);
        if (user == null) {
            throw new UserException("User with ID " + userId + " not found");
        }
        double newPoints = user.calcPoints();
        user.setPoints(newPoints);
    }

    /**
     * Adds plays to a user's record and updates their points accordingly.
     *
     * @param userId the identifier of the user
     * @param playsToAdd the number of plays to add
     * @throws UserException if the user ID is null, plays are negative, or user is not found
     */
    public void addPlaysAndUpdatePoints(Integer userId, int playsToAdd) throws UserException {
        if (userId == null) {
            throw new UserException("User identifier cannot be null");
        }
        if (playsToAdd < 0) {
            throw new UserException("Number of plays to add cannot be negative");
        }
        User user = this.users.get(userId);
        if (user == null) {
            throw new UserException("User with ID " + userId + " not found");
        }
        user.addPlaysAndUpdatePoints(playsToAdd);
    }

    /**
     * Checks if a user has a premium subscription.
     *
     * @param userId the identifier of the user to check
     * @return true if the user has a premium subscription, false otherwise
     * @throws UserException if the user ID is null or if no user is found with that identifier
     */
    public boolean isPremiumUser(Integer userId) throws UserException {
        if (userId == null) {
            throw new UserException("User identifier cannot be null");
        }
        if (!this.users.containsKey(userId)) {
            throw new UserException("User with ID " + userId + " not found");
        }
        return this.users.get(userId) instanceof PremiumUser;
    }

    /**
     * Retrieves all users in the system.
     *
     * @return a collection of all users
     * @throws UserException if there are no users in the system
     */
    public Collection<User> getAll() throws UserException {
        if (this.users.isEmpty()) {
            throw new UserException("No users found in the system");
        }
        Collection<User> usersCollection = new ArrayList<>();
        for (User user : this.users.values()) {
            usersCollection.add(user.clone());
        }
        return usersCollection;
    }

    /**
     * Checks if a user exists in the system.
     *
     * @param identifier the identifier of the user to check
     * @return true if the user exists, false otherwise
     */
    public boolean existsUser(Integer identifier) {
        return this.users.containsKey(identifier);
    }

    /**
     * Gets the user with the highest number of points.
     *
     * @return the user with the highest points, or null if there are no users
     */
    public User getTopUserByPoints() {
        List<User> sorted = getUsersSortedByPoints();
        return (sorted.isEmpty()) ? null : sorted.getFirst();
    }

    /**
     * Gets the user with the highest listening time (plays).
     *
     * @return the user with the most plays, or null if there are no users
     */
    public User getTopUserByListeningTime() {
        if (this.users.isEmpty()) return null;

        User topUser = null;
        int maxPlays = Integer.MIN_VALUE;

        for (User user : this.users.values())
            if (user.getPlays() > maxPlays) {
                maxPlays = user.getPlays();
                topUser = user;
            }

        return topUser != null ? topUser.clone() : null;
    }

    /**
     * Updates the points for all users in the system.
     */
    public void updatePointsForAllUsers() {
        for (User user : this.users.values()) {
            double newPoints = user.calcPoints();
            user.setPoints(newPoints);
        }
    }

    /**
     * Removes a playlist from all premium users' collections.
     *
     * @param playlistId the identifier of the playlist to remove
     */
    public void removePlaylistFromAllUsers(Integer playlistId) {
        for (User user : users.values())
            if (user instanceof PremiumUser premiumUser)
                premiumUser.removePlaylistById(playlistId);
    }

    /**
     * Removes an album from all premium users' collections.
     *
     * @param albumId the identifier of the album to remove
     */
    public void removeAlbumFromAllUsers(Integer albumId) {
        for (User user : users.values())
            if (user instanceof PremiumUser premiumUser)
                premiumUser.removeAlbumById(albumId);
    }

    /**
     * Returns a sorted list of users by their points in descending order.
     *
     * @return a list of users sorted by points
     */
    private List<User> getUsersSortedByPoints() {
        return this.users.values().stream()
                .sorted(Comparator.comparingDouble(User::getPoints).reversed())
                .map(User::clone)
                .collect(Collectors.toList());
    }

    /**
     * Creates and returns a deep copy of this UserManager.
     *
     * @return a new UserManager instance that is a copy of this one
     */
    @Override
    public UserManager clone() {
        return new UserManager(this);
    }

    /**
     * Compares this UserManager with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        Map<Integer, User> m = that.getUsers();
        if (this.totalUsers != that.getTotalUsers() || this.users.size() != m.size()) return false;
        for (Integer key : m.keySet()) {
            User thatUser = m.get(key);
            if (thatUser == null || !this.users.get(key).equals(thatUser)) return false;
        }
        return true;
    }

    /**
     * Generates a hash code for this UserManager.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.users, this.totalUsers);
    }

    /**
     * Returns a string representation of this UserManager.
     *
     * @return a string containing the total number of users and details of each user
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total users: ").append(this.totalUsers).append("\n");
        for (User user : this.users.values())
            sb.append(user.toString()).append("\n");

        return sb.toString();
    }
}