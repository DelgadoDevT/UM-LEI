/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 * License: MIT
 */
package poo2025.entities.SubscriptionPlan;

import java.io.Serializable;

/**
 * The PremiumTop class represents a specific subscription plan with predefined attributes
 * and additional features such as custom and public playlist creation, as well as
 * algorithmic recommendations. It extends the SubscriptionPlan class and implements
 * the Serializable interface
 */
public class PremiumTop extends SubscriptionPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance of the PremiumTop subscription plan.
     *
     * This constructor initializes the PremiumTop plan with the following predefined attributes:
     * - Type: "PremiumTop"
     * - Allows the creation of custom playlists: true
     * - Allows the creation of public playlists: true
     * - Includes algorithmic recommendations: true
     */
    public PremiumTop() {
        super("PremiumTop", true, true, true);
    }

    /**
     * Creates a new PremiumTop instance by copying the attributes from an existing PremiumTop object.
     *
     * @param premiumTop the PremiumTop object whose attributes will be copied to create the new instance
     */
    public PremiumTop(PremiumTop premiumTop) {
        super(premiumTop);
    }

    /**
     * Constructs a PremiumTop subscription plan with the specified attributes.
     *
     * @param type The type of the PremiumTop subscription plan.
     * @param allowsCustomPlaylists Whether the subscription plan allows creating custom playlists.
     * @param allowsPublicPlaylists Whether the subscription plan permits creating public playlists.
     * @param hasAlgorithmicRecommendations Whether the subscription plan includes algorithmic recommendations.
     */
    public PremiumTop(String type, boolean allowsCustomPlaylists,
                      boolean allowsPublicPlaylists, boolean hasAlgorithmicRecommendations) {
        super(type, allowsCustomPlaylists, allowsPublicPlaylists, hasAlgorithmicRecommendations);
    }

    /**
     * Retrieves the type of the subscription plan.
     *
     * @return the type of the subscription plan as a String.
     */
    public String getType() {
        return super.getType();
    }

    /**
     * Sets the type for the subscription plan.
     *
     * @param type the type of the subscription plan
     */
    public void setType(String type) {
        super.setType(type);
    }

    /**
     * Determines if the subscription plan allows the creation of custom playlists.
     *
     * @return true if the subscription plan permits custom playlists, false otherwise.
     */
    public boolean isAllowsCustomPlaylists() {
        return super.isAllowsCustomPlaylists();
    }

    /**
     * Configures whether the premium plan allows the creation of custom playlists.
     *
     * @param allowsCustomPlaylists a boolean indicating whether custom playlists
     *                              are permitted (true) or not (false)
     */
    public void setAllowsCustomPlaylists(boolean allowsCustomPlaylists) {
        super.setAllowsCustomPlaylists(allowsCustomPlaylists);
    }

    /**
     * Checks if the subscription plan allows public playlists.
     *
     * @return true if public playlists are allowed; false otherwise.
     */
    public boolean isAllowsPublicPlaylists() {
        return super.isAllowsPublicPlaylists();
    }

    /**
     * Sets whether public playlists are allowed for this subscription plan.
     *
     * @param allowsPublicPlaylists a boolean value indicating if public playlists are allowed.
     */
    public void setAllowsPublicPlaylists(boolean allowsPublicPlaylists) {
        super.setAllowsPublicPlaylists(allowsPublicPlaylists);
    }

    /**
     * Determines if the PremiumTop subscription plan includes algorithmic recommendations.
     *
     * @return true if the PremiumTop subscription plan has algorithmic recommendations, false otherwise.
     */
    public boolean isHasAlgorithmicRecommendations() {
        return super.isHasAlgorithmicRecommendations();
    }

    /**
     * Calculates the total points based on the current points balance and the number of plays.
     *
     * @param currentPoints the current points balance of the subscription plan
     * @param plays the number of plays performed
     * @return the updated total points computed using the formula
     */
    @Override
    public double calculatePoints(double currentPoints, int plays) {
        return 100 + (currentPoints * 0.025 * plays);
    }

    /**
     * Creates and returns a copy of this PremiumTop subscription plan.
     * The clone is a new instance with the same properties as the original.
     *
     * @return a new instance of PremiumTop that is a copy of this subscription plan
     */
    @Override
    public SubscriptionPlan clone() {
        return new PremiumTop(this);
    }

    /**
     * Returns a string representation of the PremiumTop object.
     * This includes the string representation of its superclass and appends "PremiumTop{}".
     *
     * @return a string representation of the PremiumTop object.
     */
    @Override
    public String toString() {
        return super.toString() + "PremiumTop{}";
    }
}