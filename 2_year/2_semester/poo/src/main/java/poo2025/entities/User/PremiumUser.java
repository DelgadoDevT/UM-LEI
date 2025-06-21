/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.entities.User;

import poo2025.entities.Album.Album;
import poo2025.entities.Playlist.Playlist;
import poo2025.entities.SubscriptionPlan.PremiumBase;
import poo2025.entities.SubscriptionPlan.SubscriptionPlan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a premium user in the music streaming system.
 * This class extends the basic User class and implements additional functionality
 * specific to premium users, such as managing playlists and albums.
 * Premium users have access to a subscription plan that determines their privileges.
 *
 * @see User
 * @see SubscriptionPlan
 */
public class PremiumUser extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private SubscriptionPlan plan;
    private List<Album> albums;
    private List<Playlist> playlists;

    /**
     * Default constructor that initializes a PremiumUser with a PremiumBase plan
     * and empty lists for albums and playlists.
     */
    public PremiumUser() {
        super();
        this.plan = new PremiumBase();
        this.albums = new ArrayList<>();
        this.playlists = new ArrayList<>();
    }

    /**
     * Constructs a PremiumUser with specified attributes.
     *
     * @param name      the user's name
     * @param age       the user's age
     * @param email     the user's email address
     * @param address   the user's physical address
     * @param points    the user's accumulated points
     * @param plays     the number of tracks played by the user
     * @param plan      the user's subscription plan
     * @param albums    the list of albums in the user's library
     * @param playlists the list of playlists created by the user
     */
    public PremiumUser(String name, int age, String email, String address, double points, int plays, SubscriptionPlan plan,
                      List<Album> albums, List<Playlist> playlists) {
        super(name, age, email, address, points, plays);
        this.plan = plan;
        this.albums = (albums != null) ? new ArrayList<>(albums) : new ArrayList<>();
        this.playlists = (playlists != null) ? new ArrayList<>(playlists) : new ArrayList<>();
    }

    /**
     * Copy constructor that creates a deep copy of another PremiumUser.
     *
     * @param premiumUser the PremiumUser to copy
     */
    public PremiumUser(PremiumUser premiumUser) {
        super(premiumUser);
        this.plan = premiumUser.plan != null ? premiumUser.plan.clone() : null;
        this.albums = (premiumUser.albums != null) ? new ArrayList<>(premiumUser.albums) : new ArrayList<>();
        this.playlists = (premiumUser.playlists != null) ? new ArrayList<>(premiumUser.playlists) : new ArrayList<>();
    }

    /**
     * Constructs a PremiumUser from a basic User and a subscription plan.
     *
     * @param user the basic User to upgrade to premium
     * @param plan the subscription plan to assign to the user
     */
    public PremiumUser(User user, SubscriptionPlan plan) {
        super(user);
        this.plan = plan != null ? plan.clone() : null;
        this.albums = new ArrayList<>();
        this.playlists = new ArrayList<>();
    }

    // Getters and Setters

    /**
     * Gets the user's identifier.
     *
     * @return the user's unique identifier
     */
    @Override
    public Integer getIdentifier() {
        return super.getIdentifier();
    }

    /**
     * Gets the user's name.
     *
     * @return the user's name
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Sets the user's name.
     *
     * @param name the new name to set
     */
    @Override
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * Gets the user's age.
     *
     * @return the user's age
     */
    @Override
    public int getAge() {
        return super.getAge();
    }

    /**
     * Sets the user's age.
     *
     * @param age the new age to set
     */
    @Override
    public void setAge(int age) {
        super.setAge(age);
    }

    /**
     * Gets the user's email address.
     *
     * @return the user's email address
     */
    @Override
    public String getEmail() {
        return super.getEmail();
    }

    /**
     * Sets the user's email address.
     *
     * @param email the new email address to set
     */
    @Override
    public void setEmail(String email) {
        super.setEmail(email);
    }

    /**
     * Gets the user's physical address.
     *
     * @return the user's address
     */
    @Override
    public String getAddress() {
        return super.getAddress();
    }

    /**
     * Sets the user's physical address.
     *
     * @param address the new address to set
     */
    @Override
    public void setAddress(String address) {
        super.setAddress(address);
    }

    /**
     * Gets the user's accumulated points.
     *
     * @return the user's points
     */
    @Override
    public double getPoints() {
        return super.getPoints();
    }

    /**
     * Sets the user's points.
     *
     * @param points the new points value to set
     */
    @Override
    public void setPoints(double points) {
        super.setPoints(points);
    }

    /**
     * Gets the number of tracks played by the user.
     *
     * @return the number of plays
     */
    @Override
    public int getPlays() {
        return super.getPlays();
    }

    /**
     * Sets the number of tracks played by the user.
     *
     * @param plays the new number of plays to set
     */
    @Override
    public void setPlays(int plays) {
        super.setPlays(plays);
    }

    /**
     * Gets the user's subscription plan.
     *
     * @return the user's current subscription plan
     */
    public SubscriptionPlan getPlan() {
        return this.plan;
    }

    /**
     * Sets the user's subscription plan.
     *
     * @param plan the new subscription plan to set
     */
    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    /**
     * Gets a copy of the user's album library.
     *
     * @return a new ArrayList containing the user's albums
     */
    public List<Album> getAlbums() {
        return this.albums != null ? new ArrayList<>(albums) : new ArrayList<>();
    }

    /**
     * Sets the user's album library.
     *
     * @param albums the new list of albums to set
     */
    public void setAlbums(List<Album> albums) {
        this.albums = albums != null ? new ArrayList<>(albums) : new ArrayList<>();
    }

    /**
     * Sets the user's playlist library.
     *
     * @param playlists the new list of playlists to set
     */
    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists != null ? new ArrayList<>(playlists) : new ArrayList<>();
    }

    /**
     * Gets a copy of the user's playlist library.
     *
     * @return a new ArrayList containing the user's playlists
     */
    public List<Playlist> getPlaylists() {
        return this.playlists != null ? new ArrayList<>(playlists) : new ArrayList<>();
    }

    /**
     * Creates and returns a clone of this PremiumUser.
     *
     * @return a new PremiumUser object with the same attributes as this one
     */
    @Override
    public PremiumUser clone() {
        return new PremiumUser(this);
    }

    /**
     * Calculates the user's points based on their subscription plan.
     *
     * @return the calculated points value
     */
    @Override
    public double calcPoints() {
        return plan.calculatePoints(super.getPoints(), super.getPlays());
    }

    /**
     * Adds a playlist to the user's library if allowed by their subscription plan.
     *
     * @param playlist the playlist to add
     */
    public void addPlaylistToUser(Playlist playlist) {
        if (this.playlists != null && playlist != null && plan.isAllowsPublicPlaylists())
            this.playlists.add(playlist);
    }

    /**
     * Adds an album to the user's library if allowed by their subscription plan.
     *
     * @param album the album to add
     */
    public void addAlbumToUser(Album album) {
        if (this.albums != null && album != null && plan.isAllowsPublicPlaylists()) {
            this.albums.add(album);
        }
    }

    /**
     * Removes a playlist from the user's library by its ID.
     *
     * @param playlistId the ID of the playlist to remove
     */
    public void removePlaylistById(Integer playlistId) {
        if (this.playlists != null)
            playlists.removeIf(p -> p.getIdentifier().equals(playlistId));
    }

    /**
     * Removes an album from the user's library by its ID.
     *
     * @param albumId the ID of the album to remove
     */
    public void removeAlbumById(Integer albumId) {
        if (this.albums != null)
            albums.removeIf(a -> a.getIdentifier().equals(albumId));
    }

    /**
     * Helper method to build a string representation of the user's profile.
     *
     * @param detailed whether to include detailed information
     * @return a formatted string containing the user's profile information
     */
    private String buildProfileString(boolean detailed) {
        StringBuilder sb = new StringBuilder();
        sb.append(detailed ? "\n===== MY PROFILE =====\n" : "\n=== User Profile ===\n");
        if (detailed) {
            sb.append("ID: ").append(this.getIdentifier()).append("\n");
            sb.append("Name: ").append(this.getName()).append("\n");
            sb.append("Age: ").append(this.getAge()).append("\n");
            sb.append("Email: ").append(this.getEmail()).append("\n");
            sb.append("Address: ").append(this.getAddress()).append("\n");
            sb.append("Points: ").append(String.format("%.2f", this.getPoints())).append("\n");
            sb.append("Plays: ").append(this.getPlays()).append("\n");
            sb.append("Plan Type: ").append(this.plan != null ? this.plan.getType() : "No Plan").append("\n");
            sb.append("Playlists:\n");
            for (Playlist playlist : this.playlists)
                sb.append("  -").append(playlist.getName()).append("\n");
            sb.append("Albums:\n");
            for (Album album : this.albums)
                sb.append("  -").append(album.getName()).append("\n");
        } else {
            sb.append("ID: ").append(this.getIdentifier())
                    .append("; Name: ").append(this.getName())
                    .append("; Age: ").append(this.getAge())
                    .append("; Email: ").append(this.getEmail())
                    .append("; Address: ").append(this.getAddress())
                    .append("; Points: ").append(String.format("%.2f", this.getPoints()))
                    .append("; Plays: ").append(this.getPlays())
                    .append("; Plan Type: ").append(this.plan != null ? this.plan.getType() : "No Plan")
                    .append("; Playlists: ").append(this.playlists != null ? this.playlists.size() : 0)
                    .append("; Albums: ").append(this.albums != null ? this.albums.size() : 0)
                    .append("\n");
        }

        sb.append(detailed ? "======================" : "====================");
        return sb.toString();
    }

    /**
     * Returns a string representation of this PremiumUser.
     *
     * @return a string containing basic profile information
     */
    @Override
    public String toString() {
        return buildProfileString(false);
    }

    /**
     * Returns a detailed view of the user's profile.
     *
     * @return a string containing detailed profile information
     */
    public String viewMyProfile() {
        return buildProfileString(true);
    }

    /**
     * Compares this PremiumUser with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PremiumUser that = (PremiumUser) o;
        return Objects.equals(plan.getType(), that.plan.getType()) &&
                Objects.equals(albums, that.albums) &&
                Objects.equals(playlists, that.playlists);
    }

    /**
     * Generates a hash code for this PremiumUser.
     *
     * @return an integer hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), plan.getType(), albums, playlists);
    }
}