/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.User;
import java.io.Serializable;

/**
 * Represents a free user in the system with basic functionality.
 * This class extends the User class and implements Serializable for object persistence.
 * Free users have access to basic features and a simple points calculation system.
 */
public class FreeUser extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. Constructors
    /**
     * Default constructor that initializes a free user with default values.
     */
    public FreeUser() {
        super();
    }

    /**
     * Parameterized constructor to create a free user with specific values.
     *
     * @param name    The name of the user
     * @param age     The age of the user
     * @param email   The email address of the user
     * @param address The physical address of the user
     * @param points  The initial points of the user
     * @param plays   The initial number of plays
     */
    public FreeUser(String name, int age, String email, String address,
                    double points, int plays) {
        super(name, age, email, address, points, plays);
    }

    /**
     * Copy constructor that creates a clone of an existing free user.
     *
     * @param freeUser The free user to be cloned
     */
    public FreeUser(FreeUser freeUser) {
        super(freeUser);
    }

    // 2. Getters and Setters
    /**
     * Gets the unique identifier of the free user.
     *
     * @return The user's identifier
     */
    public Integer getIdentifier() {
        return super.getIdentifier();
    }

    /**
     * Gets the name of the free user.
     *
     * @return The user's name
     */
    public String getName() {
        return super.getName();
    }

    /**
     * Sets the name of the free user.
     *
     * @param name The new name to set
     */
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * Gets the age of the free user.
     *
     * @return The user's age
     */
    public int getAge() {
        return super.getAge();
    }

    /**
     * Sets the age of the free user.
     *
     * @param age The new age to set
     */
    public void setAge(int age) {
        super.setAge(age);
    }

    /**
     * Gets the email address of the free user.
     *
     * @return The user's email address
     */
    public String getEmail() {
        return super.getEmail();
    }

    /**
     * Sets the email address of the free user.
     *
     * @param email The new email to set
     */
    public void setEmail(String email) {
        super.setEmail(email);
    }

    /**
     * Gets the physical address of the free user.
     *
     * @return The user's address
     */
    public String getAddress() {
        return super.getAddress();
    }

    /**
     * Sets the physical address of the free user.
     *
     * @param address The new address to set
     */
    public void setAddress(String address) {
        super.setAddress(address);
    }

    /**
     * Gets the points accumulated by the free user.
     *
     * @return The user's points
     */
    public double getPoints() {
        return super.getPoints();
    }

    /**
     * Sets the points for the free user.
     *
     * @param points The new points value to set
     */
    public void setPoints(double points) {
        super.setPoints(points);
    }

    /**
     * Gets the number of music plays by the free user.
     *
     * @return The number of plays
     */
    public int getPlays() {
        return super.getPlays();
    }

    /**
     * Sets the number of music plays for the free user.
     *
     * @param plays The new number of plays to set
     */
    public void setPlays(int plays) {
        super.setPlays(plays);
    }

    // 3. Implementation of abstract methods
    /**
     * Calculates the points for the free user based on the number of plays.
     * Points are calculated by multiplying the number of plays by 5.0.
     *
     * @return The calculated points
     */
    @Override
    public double calcPoints() {
        int plays = super.getPlays();
        double points = 5.0;
        return points * plays;
    }

    /**
     * Builds a formatted string representation of the user profile.
     *
     * @param detailed whether to show detailed or summary information
     * @return a formatted string containing user information
     */
    private String buildProfileString(boolean detailed) {
        StringBuilder sb = new StringBuilder();
        sb.append(detailed ? "\n===== MY PROFILE =====\n" : "\n=== Free User Profile ===\n");
        if (detailed) {
            sb.append("ID: ").append(this.getIdentifier()).append("\n");
            sb.append("Name: ").append(this.getName()).append("\n");
            sb.append("Age: ").append(this.getAge()).append("\n");
            sb.append("Email: ").append(this.getEmail()).append("\n");
            sb.append("Address: ").append(this.getAddress()).append("\n");
            sb.append("Points: ").append(String.format("%.2f", this.getPoints())).append("\n");
            sb.append("Plays: ").append(this.getPlays()).append("\n");
        } else {
            sb.append("ID: ").append(this.getIdentifier())
                    .append("; Name: ").append(this.getName())
                    .append("; Age: ").append(this.getAge())
                    .append("; Email: ").append(this.getEmail())
                    .append("; Address: ").append(this.getAddress())
                    .append("; Points: ").append(String.format("%.2f", this.getPoints()))
                    .append("; Plays: ").append(this.getPlays())
                    .append("\n");
        }

        sb.append(detailed ? "======================" : "====================");
        return sb.toString();
    }

    /**
     * Returns a string representation of this FreeUser.
     *
     * @return a string containing basic profile information
     */
    @Override
    public String toString() {
        return buildProfileString(false);
    }

    /**
     * Generates a detailed view of the user's profile.
     *
     * @return A formatted string containing detailed user information
     */
    public String viewMyProfile() {
        return buildProfileString(true);
    }

    /**
     * Generates a hash code for the free user.
     *
     * @return The hash code value for the user
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Creates a deep copy of the current free user.
     *
     * @return A new FreeUser object that is a copy of this user
     */
    @Override
    public User clone() {
        return new FreeUser(this);
    }
}