/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.User;
import poo2025.entities.Music.Music;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract class representing a user in the system.
 * This class implements Serializable for object persistence and provides basic user functionality.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Counter for generating unique user identifiers */
    private static Integer lastId = 0;

    /** Unique identifier for the user */
    private final Integer identifier;
    /** Name of the user */
    private String name;
    /** Age of the user */
    private int age;
    /** Email address of the user */
    private String email;
    /** Physical address of the user */
    private String address;
    /** Points accumulated by the user */
    private double points;
    /** Number of music plays by the user */
    private int plays;

    /**
     * Default constructor that initializes a user with default values.
     * Generates a new unique identifier for the user.
     */
    public User() {
        this.identifier = lastId++;
        this.name = "";
        this.age = 0;
        this.email = "";
        this.address = "";
        this.points = 0.0;
        this.plays = 0;
    }

    /**
     * Parameterized constructor to create a user with specific values.
     *
     * @param name    The name of the user
     * @param age     The age of the user
     * @param email   The email address of the user
     * @param address The physical address of the user
     * @param points  The initial points of the user
     * @param plays   The initial number of plays
     */
    public User(String name, int age, String email, String address, double points, int plays) {
        this.identifier = lastId++;
        this.name = name;
        this.age = age;
        this.email = email;
        this.address = address;
        this.points = points;
        this.plays = plays;
    }

    /**
     * Copy constructor that creates a clone of an existing user.
     * This constructor maintains the same identifier as the original user.
     *
     * @param user The user to be cloned
     */
    public User(User user) {
        this.identifier = user.getIdentifier();
        this.name = user.getName();
        this.age = user.getAge();
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.points = user.getPoints();
        this.plays = user.getPlays();
    }

    /**
     * Calculates the points for the user based on implementation-specific logic.
     *
     * @return The calculated points
     */
    public abstract double calcPoints();

    /**
     * Creates a clone of the current user.
     *
     * @return A new User object that is a copy of this user
     */
    public abstract User clone();
    
    /**
     * Retrieves the last assigned ID for users.
     *
     * @return The last assigned ID as an Integer
     */
    public static Integer getLastId() {return lastId;}

    /**
     * Sets the last assigned ID for users.
     *
     * @param newLastId The new value to set as the last assigned ID
     */
    public static void setLastId(Integer newLastId) {
        lastId = newLastId;
    }

    /**
     * Gets the unique identifier of the user.
     *
     * @return The user's identifier
     */
    public Integer getIdentifier() {
        return this.identifier;
    }

    /**
     * Gets the name of the user.
     *
     * @return The user's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the user.
     *
     * @param name The new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the age of the user.
     *
     * @return The user's age
     */
    public int getAge() {
        return this.age;
    }

    /**
     * Sets the age of the user.
     *
     * @param age The new age to set
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The user's email address
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The new email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the physical address of the user.
     *
     * @return The user's address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * Sets the physical address of the user.
     *
     * @param address The new address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the points accumulated by the user.
     *
     * @return The user's points
     */
    public double getPoints() {
        return this.points;
    }

    /**
     * Sets the points for the user.
     *
     * @param points The new points value to set
     */
    public void setPoints(double points) {
        this.points = points;
    }

    /**
     * Gets the number of music plays by the user.
     *
     * @return The number of plays
     */
    public int getPlays() {
        return this.plays;
    }

    /**
     * Sets the number of music plays for the user.
     *
     * @param plays The new number of plays to set
     */
    public void setPlays(int plays) {
        this.plays = plays;
    }

    /**
     * Plays a music track and updates the user's plays and points.
     *
     * @param music The music track to play
     * @return A message indicating the result of playing the music
     */
    public String playMusic(Music music) {
        String message = "Error playing the music";
        if (music != null) {
            message = music.play();
            this.plays++;
            double updatedPoints = calcPoints();
            this.setPoints(updatedPoints);
        }
        return message;
    }

    /**
     * Adds a specified number of plays and updates the user's points accordingly.
     *
     * @param playsToAdd The number of plays to add
     */
    public void addPlaysAndUpdatePoints(int playsToAdd) {
        setPlays(getPlays() + playsToAdd);
        double newPoints = calcPoints();
        setPoints(newPoints);
    }


    /**
     * Returns a string representation of the user.
     *
     * @return A string representing the user
     */
    @Override
    public abstract String toString();

    /**
     * Checks if this user is equal to another object.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && Double.compare(points, user.points) == 0 && plays == user.plays && Objects.equals(identifier, user.identifier) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(address, user.address);
    }

    /**
     * Generates a hash code for the user.
     *
     * @return The hash code value for the user
     */
    @Override
    public int hashCode() {
        return Objects.hash(identifier, name, age, email, address, points, plays);
    }
}