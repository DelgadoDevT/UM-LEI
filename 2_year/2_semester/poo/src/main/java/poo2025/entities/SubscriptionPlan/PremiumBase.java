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

/**
 * The PremiumBase class offers specific features such as allowing the creation
 * of custom and public playlists, and includes options for algorithmic recommendations.
 * Additionally, it overrides methods from the SubscriptionPlan class to define
 * its own behavior for calculating points, cloning, and string representation.
 */
public class PremiumBase extends SubscriptionPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance of the PremiumBase subscription plan with predefined attributes.
     *
     * This constructor initializes the PremiumBase plan with the following predefined attributes:
     * - Type: "PremiumBase"
     * - Allows the creation of custom playlists: true
     * - Allows the creation of public playlists: true
     * - Includes algorithmic recommendations: false
     */
    public PremiumBase() {
        super("PremiumBase", true, true, false);
    }

    /**
     * Constructs a PremiumBase subscription plan with the specified attributes.
     *
     * @param type The type of the subscription plan.
     * @param allowsCustomPlaylists Whether the subscription plan allows creating custom playlists.
     * @param allowsPublicPlaylists Whether the subscription plan allows creating public playlists.
     * @param hasAlgorithmicRecommendations Whether the subscription plan includes algorithmic recommendations.
     */
    public PremiumBase(String type, boolean allowsCustomPlaylists,
                       boolean allowsPublicPlaylists, boolean hasAlgorithmicRecommendations) {
        super(type, allowsCustomPlaylists, allowsPublicPlaylists, hasAlgorithmicRecommendations);
    }

    /**
     * Creates a new PremiumBase instance by copying the attributes from an existing PremiumBase object.
     *
     * @param premiumBase the PremiumBase object whose attributes will be copied to create the new instance
     */
    public PremiumBase(PremiumBase premiumBase) {
        super(premiumBase);
    }

    /**
     * Retrieves the type of the subscription plan associated with the PremiumBase class.
     *
     * @return the type of the subscription plan as a String.
     */
    public String getType() {
        return super.getType();
    }

    /**
     * Sets the type of the subscription plan.
     *
     * @param type the type of the subscription plan
     */
    public void setType(String type) {
        super.setType(type);
    }

    /**
     * Determines if the subscription plan allows the creation of custom playlists.
     *
     * @return true if custom playlists are allowed, false otherwise.
     */
    public boolean isAllowsCustomPlaylists() {
        return super.isAllowsCustomPlaylists();
    }

    /**
     * Updates the setting that determines whether custom playlists are allowed
     * for the PremiumBase subscription plan.
     *
     * @param allowsCustomPlaylists a boolean indicating whether the creation of
     *                              custom playlists is permitted (true) or not (false)
     */
    public void setAllowsCustomPlaylists(boolean allowsCustomPlaylists) {
        super.setAllowsCustomPlaylists(allowsCustomPlaylists);
    }

    /**
     * Checks if the subscription plan allows the creation of public playlists.
     *
     * @return true if public playlists are allowed; false otherwise.
     */
    public boolean isAllowsPublicPlaylists() {
        return super.isAllowsPublicPlaylists();
    }

    /**
     * Updates the setting to determine whether public playlists are allowed
     * for this subscription plan.
     *
     * @param allowsPublicPlaylists a boolean indicating whether public playlists
     *                              are allowed (true) or not (false)
     */
    public void setAllowsPublicPlaylists(boolean allowsPublicPlaylists) {
        super.setAllowsPublicPlaylists(allowsPublicPlaylists);
    }

    /**
     * Determines whether the subscription plan includes algorithmic recommendations.
     *
     * @return true if the subscription plan includes algorithmic recommendations, false otherwise.
     */
    public boolean isHasAlgorithmicRecommendations() {
        return super.isHasAlgorithmicRecommendations();
    }

    /**
     * Sets whether the subscription plan includes algorithmic recommendations.
     *
     * @param hasAlgorithmicRecommendations a boolean indicating whether
     *                                       algorithmic recommendations are enabled
     */
    public void setHasAlgorithmicRecommendations(boolean hasAlgorithmicRecommendations) {
        super.setHasAlgorithmicRecommendations(hasAlgorithmicRecommendations);
    }

    /**
     * Calculates the total points based on the number of plays.
     *
     * @param currentPoints the current points balance of the subscription plan
     * @param plays the number of plays performed
     * @return the updated total points calculated as 10 multiplied by the number of plays
     */
    @Override
    public double calculatePoints(double currentPoints, int plays) { return 10 * plays; }

    /**
     * Creates and returns a copy of this PremiumBase subscription plan instance.
     * This method creates a new PremiumBase object by copying the attributes
     * of the current instance, ensuring that the new object is independent of the original.
     *
     * @return a new instance of the PremiumBase class with the same attributes as the original.
     */
    @Override
    public SubscriptionPlan clone() {
        return new PremiumBase(this);
    }

    /**
     * Returns a string representation of the PremiumBase object.
     * This includes the string representation of its superclass
     * and appends "PremiumBase{}".
     *
     * @return a string representation of the PremiumBase object.
     */
    @Override
    public String toString() {
        return super.toString() + "PremiumBase{}";
    }
}