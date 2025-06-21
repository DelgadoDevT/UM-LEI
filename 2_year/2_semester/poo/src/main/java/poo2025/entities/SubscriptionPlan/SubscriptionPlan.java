/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.entities.SubscriptionPlan;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an abstract blueprint for a subscription plan in a media service.
 * A SubscriptionPlan provides several configurable attributes such as type,
 * the ability to create custom or public playlists, and inclusion of algorithmic recommendations.
 * Subclasses must implement specific features and behaviors such as calculating
 * points for the plan and providing a way to clone the object.
 */
public abstract class SubscriptionPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Represents the type of subscription plan */
    private String type;

    /** Indicates whether the subscription plan allows users to create custom playlists */
    private boolean allowsCustomPlaylists;

    /** Indicates whether the subscription plan allows users to create public playlists */
    private boolean allowsPublicPlaylists;

    /** Indicates whether the subscription plan includes algorithmic recommendations */
    private boolean hasAlgorithmicRecommendations;

    /**
     * Constructs a SubscriptionPlan with the specified attributes.
     *
     * @param type The type of subscription plan.
     * @param allowsCustomPlaylists Whether the subscription plan allows creating custom playlists.
     * @param allowsPublicPlaylists Whether the subscription plan allows creating public playlists.
     * @param hasAlgorithmicRecommendations Whether the subscription plan includes algorithmic recommendations.
     */
    public SubscriptionPlan(String type, boolean allowsCustomPlaylists,
                            boolean allowsPublicPlaylists, boolean hasAlgorithmicRecommendations) {
        this.type = type;
        this.allowsCustomPlaylists = allowsCustomPlaylists;
        this.allowsPublicPlaylists = allowsPublicPlaylists;
        this.hasAlgorithmicRecommendations = hasAlgorithmicRecommendations;
    }

    /**
     * Default constructor for the SubscriptionPlan class.
     * Initializes the plan with default values:
     * - Type is set to an empty string.
     * - Custom playlists are not allowed.
     * - Public playlists are not allowed.
     * - Algorithmic recommendations are not available.
     */
    public SubscriptionPlan() {
        this.type = "";
        this.allowsCustomPlaylists = false;
        this.allowsPublicPlaylists = false;
        this.hasAlgorithmicRecommendations = false;
    }

    /**
     * Creates a new SubscriptionPlan instance by copying the attributes from an existing plan.
     *
     * @param plan the SubscriptionPlan object whose attributes will be copied to create the new instance
     */
    public SubscriptionPlan(SubscriptionPlan plan) {
        this.type = plan.getType();
        this.allowsCustomPlaylists = plan.isAllowsCustomPlaylists();
        this.allowsPublicPlaylists = plan.isAllowsPublicPlaylists();
        this.hasAlgorithmicRecommendations = plan.isHasAlgorithmicRecommendations();
    }

    /**
     * Abstract method to calculate the total points for a subscription plan.
     * The calculation depends on specific implementation details of the subclass.
     *
     * @param currentPoints the current points accumulated by the user.
     * @param plays the number of plays or interactions to factor into the calculation.
     * @return the calculated total points based on the current points and plays.
     */
    public abstract double calculatePoints(double currentPoints, int plays);

    /**
     * Creates and returns a deep copy of this SubscriptionPlan instance.
     *
     * @return a new SubscriptionPlan object that is a clone of this instance.
     */
    public abstract SubscriptionPlan clone();

    /**
     * Retrieves the type of the subscription plan.
     *
     * @return the type of the subscription plan as a String.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type of the subscription plan.
     *
     * @param type the type of the subscription plan
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Checks if custom playlists are allowed for the subscription plan.
     *
     * @return true if custom playlists are allowed, false otherwise.
     */
    public boolean isAllowsCustomPlaylists() {
        return this.allowsCustomPlaylists;
    }

    /**
     * Updates the setting that determines whether custom playlists are allowed
     * for the subscription plan.
     *
     * @param allowsCustomPlaylists a boolean indicating whether custom playlists
     *                              are permitted (true) or not (false)
     */
    public void setAllowsCustomPlaylists(boolean allowsCustomPlaylists) {
        this.allowsCustomPlaylists = allowsCustomPlaylists;
    }

    /**
     * Indicates whether the subscription plan allows public playlists.
     *
     * @return true if public playlists are allowed; false otherwise.
     */
    public boolean isAllowsPublicPlaylists() {
        return this.allowsPublicPlaylists;
    }

    /**
     * Sets whether public playlists are allowed for this subscription plan.
     *
     * @param allowsPublicPlaylists a boolean value indicating if public playlists are allowed.
     */
    public void setAllowsPublicPlaylists(boolean allowsPublicPlaylists) {
        this.allowsPublicPlaylists = allowsPublicPlaylists;
    }

    /**
     * Checks whether the subscription plan includes algorithmic recommendations.
     *
     * @return true if the subscription plan has algorithmic recommendations, false otherwise.
     */
    public boolean isHasAlgorithmicRecommendations() {
        return this.hasAlgorithmicRecommendations;
    }

    /**
     * Sets whether the subscription plan has algorithmic recommendations enabled.
     *
     * @param hasAlgorithmicRecommendations a boolean value indicating whether
     *                  algorithmic recommendations are enabled for the subscription plan
     */
    public void setHasAlgorithmicRecommendations(boolean hasAlgorithmicRecommendations) {
        this.hasAlgorithmicRecommendations = hasAlgorithmicRecommendations;
    }

    /**
     * Compares this SubscriptionPlan object with another object to determine equality.
     * Two SubscriptionPlan objects are considered equal if they have the same type,
     * and their allowsCustomPlaylists, allowsPublicPlaylists, and hasAlgorithmicRecommendations
     * values are identical.
     *
     * @param o the object to compare with this SubscriptionPlan object for equality
     * @return true if the specified object is equal to this SubscriptionPlan; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionPlan that = (SubscriptionPlan) o;
        return allowsCustomPlaylists == that.allowsCustomPlaylists &&
                allowsPublicPlaylists == that.allowsPublicPlaylists &&
                hasAlgorithmicRecommendations == that.hasAlgorithmicRecommendations &&
                Objects.equals(type, that.type);
    }

    /**
     * Computes the hash code for the SubscriptionPlan object. The hash code is
     * computed based on the type, allowsCustomPlaylists, allowsPublicPlaylists,
     * and hasAlgorithmicRecommendations attributes.
     *
     * @return an integer representing the hash code of this SubscriptionPlan instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, allowsCustomPlaylists, allowsPublicPlaylists, hasAlgorithmicRecommendations);
    }

    /**
     * Provides a string representation of the SubscriptionPlan object, including its type,
     * whether it allows custom playlists, public playlists, and if it has algorithmic recommendations.
     *
     * @return a string representation of the SubscriptionPlan object.
     */
    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "type='" + type + '\'' +
                ", allowsCustomPlaylists=" + allowsCustomPlaylists +
                ", allowsPublicPlaylists=" + allowsPublicPlaylists +
                ", hasAlgorithmicRecommendations=" + hasAlgorithmicRecommendations +
                '}';
    }
}