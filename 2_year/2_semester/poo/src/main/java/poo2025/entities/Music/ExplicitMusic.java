/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.entities.Music;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a music track with explicit content ratings and age restrictions.
 * This class extends the base Music class and implements additional functionality
 * for managing explicit content, age restrictions, and content rating sources.
 * It implements Serializable to support object persistence.
 *
 * @see Music
 * @see Serializable
 */
public class ExplicitMusic extends Music implements Serializable {
    /** Serial version UID for serialization */
    private static final long serialVersionUID = 1L;

    /** Flag indicating whether the music contains explicit lyrics */
    private boolean explicitLyrics;
    
    /** Age restriction for the music content (0 means no restriction) */
    private int ageRestriction;
    
    /** Source of the content rating (e.g., "AutoDetectTool", "Manual", etc.) */
    private String ratingSource;

    /**
     * Default constructor that initializes an ExplicitMusic instance with default values.
     * Sets explicitLyrics to false, ageRestriction to 0, and ratingSource to "Unknown".
     */
    public ExplicitMusic() {
        super();
        this.explicitLyrics = false;
        this.ageRestriction = 0;
        this.ratingSource = "Unknown";
    }

    /**
     * Constructs an ExplicitMusic instance with specified attributes.
     *
     * @param name            the name of the track
     * @param interpreter     the performer of the track
     * @param lyrics         the lyrics content
     * @param sound          the sound file reference
     * @param publisher      the publisher of the track
     * @param genre          the musical genre
     * @param durationS      the duration in seconds
     * @param plays          the number of plays
     * @param explicitLyrics whether the track contains explicit lyrics
     * @param ageRestriction the minimum age required to access the content
     * @param ratingSource   the source of the content rating
     */
    public ExplicitMusic(String name, String interpreter, String lyrics, String sound,
                         String publisher, String genre, int durationS, int plays,
                         boolean explicitLyrics, int ageRestriction, String ratingSource) {
        super(name, interpreter, lyrics, sound, publisher, genre, durationS, plays);
        this.explicitLyrics = explicitLyrics;
        this.ageRestriction = ageRestriction;
        this.ratingSource = ratingSource;
    }

    /**
     * Copy constructor that creates a deep copy of an existing ExplicitMusic instance.
     *
     * @param m the ExplicitMusic instance to copy
     */
    public ExplicitMusic(ExplicitMusic m) {
        super(m);
        this.explicitLyrics = m.isExplicitLyrics();
        this.ageRestriction = m.getAgeRestriction();
        this.ratingSource = m.getRatingSource();
    }

    // Getters and setters for inherited properties
    
    /**
     * {@inheritDoc}
     */
    public Integer getIdentifier() {
        return super.getIdentifier();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return super.getName();
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getInterpreter() {
        return super.getInterpreter();
    }

    /**
     * {@inheritDoc}
     */
    public void setInterpreter(String interpreter) {
        super.setInterpreter(interpreter);
    }

    /**
     * {@inheritDoc}
     */
    public String getLyrics() {
        return super.getLyrics();
    }

    /**
     * {@inheritDoc}
     */
    public void setLyrics(String lyrics) {
        super.setLyrics(lyrics);
    }

    /**
     * {@inheritDoc}
     */
    public String getSound() {
        return super.getSound();
    }

    /**
     * {@inheritDoc}
     */
    public void setSound(String sound) {
        super.setSound(sound);
    }

    /**
     * {@inheritDoc}
     */
    public String getPublisher() {
        return super.getPublisher();
    }

    /**
     * {@inheritDoc}
     */
    public void setPublisher(String publisher) {
        super.setPublisher(publisher);
    }

    /**
     * {@inheritDoc}
     */
    public String getGenre() {
        return super.getGenre();
    }

    /**
     * {@inheritDoc}
     */
    public void setGenre(String genre) {
        super.setGenre(genre);
    }

    /**
     * {@inheritDoc}
     */
    public int getDurationS() {
        return super.getDurationS();
    }

    /**
     * {@inheritDoc}
     */
    public void setDurationS(int durationS) {
        super.setDurationS(durationS);
    }

    /**
     * {@inheritDoc}
     */
    public int getPlays() {
        return super.getPlays();
    }

    /**
     * {@inheritDoc}
     */
    public void setPlays(int plays) {
        super.setPlays(plays);
    }

    // Getters and setters for ExplicitMusic specific properties

    /**
     * Checks if the music track contains explicit lyrics.
     *
     * @return true if the track contains explicit lyrics, false otherwise
     */
    public boolean isExplicitLyrics() {
        return this.explicitLyrics;
    }

    /**
     * Sets whether the music track contains explicit lyrics.
     *
     * @param explicitLyrics true if the track contains explicit lyrics, false otherwise
     */
    public void setExplicitLyrics(boolean explicitLyrics) {
        this.explicitLyrics = explicitLyrics;
    }

    /**
     * Gets the age restriction for the music track.
     *
     * @return the minimum age required to access the content
     */
    public int getAgeRestriction() {
        return this.ageRestriction;
    }

    /**
     * Sets the age restriction for the music track.
     *
     * @param ageRestriction the minimum age required to access the content
     */
    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    /**
     * Gets the source of the content rating.
     *
     * @return the name or identifier of the rating source
     */
    public String getRatingSource() {
        return this.ratingSource;
    }

    /**
     * Sets the source of the content rating.
     *
     * @param ratingSource the name or identifier of the rating source
     */
    public void setRatingSource(String ratingSource) {
        this.ratingSource = ratingSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExplicitMusic that = (ExplicitMusic) o;
        return this.explicitLyrics == that.isExplicitLyrics() && 
               this.ageRestriction == that.getAgeRestriction() && 
               Objects.equals(this.ratingSource, that.getRatingSource());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.explicitLyrics, this.ageRestriction, this.ratingSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(getIdentifier())
                .append(" | Name: ").append(getName())
                .append(" | Interpreter: ").append(getInterpreter())
                .append(" | Publisher: ").append(getPublisher())
                .append(" | Lyrics: ").append(getLyrics())
                .append(" | Sound: ").append(getSound())
                .append(" | Genre: ").append(getGenre())
                .append(" | Duration: ").append(getDurationS()).append(" seconds")
                .append(" | Plays: ").append(getPlays())
                .append(" | Explicit Lyrics: ").append(this.explicitLyrics)
                .append(" | Age Restriction: ").append(this.ageRestriction)
                .append(" | Rating Source: ").append(this.ratingSource)
                .append("\n");
        return sb.toString();
    }

    /**
     * Checks if the content is appropriate for a given age.
     *
     * @param age the age to check against the restriction
     * @return true if the age meets or exceeds the restriction, false otherwise
     */
    private boolean isAppropriateForAge(int age) {
        return age >= this.ageRestriction;
    }

    /**
     * Generates a formatted display of the advisory information.
     *
     * @return a string containing the rating source, age restriction, and explicit lyrics status
     */
    private String displayAdvisoryInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rating Source: ").append(this.ratingSource).append("\n")
                .append("Age Restriction: ").append(this.ageRestriction).append("+\n")
                .append("Explicit Lyrics: ").append(this.explicitLyrics ? "Yes" : "No");
        return sb.toString();
    }

    /**
     * Gets a descriptive text for the age rating category.
     *
     * @return a string describing the content rating category
     */
    private String getAgeRatingDescription() {
        if (ageRestriction <= 13)
            return "Suitable for all ages";
        else if (ageRestriction <= 17)
            return "Parental Advisory";
        else
            return "Explicit Content";
    }

    /**
     * Automatically detects and assigns age restrictions based on lyrics content.
     * Analyzes the lyrics for mild and strong explicit content and updates
     * the explicitLyrics, ageRestriction, and ratingSource properties accordingly.
     */
    public void detectAndAssignAgeRestriction() {
        String[] mildExplicitWords = {"POO", "Herança", "Encapsulamento", "Abstração", "Modularidade"};
        String[] strongExplicitWords = {"Projeto", "Teste", "Interface", "Polimorfismo", "Composição"};
        boolean hasStrongContent = false;
        boolean hasMildContent = false;

        for (int i = 0; i < strongExplicitWords.length && !hasStrongContent; i++)
            if (this.getLyrics().toLowerCase().contains(strongExplicitWords[i].toLowerCase()))
                hasStrongContent = true;

        for (int i = 0; i < mildExplicitWords.length && !hasMildContent; i++)
            if (this.getLyrics().toLowerCase().contains(mildExplicitWords[i].toLowerCase()))
                hasMildContent = true;

        if (hasStrongContent) {
            this.explicitLyrics = true;
            this.ageRestriction = 18;
        } else if (hasMildContent) {
            this.explicitLyrics = true;
            this.ageRestriction = 13;
        } else {
            this.explicitLyrics = false;
            this.ageRestriction = 0;
        }

        this.ratingSource = "AutoDetectTool";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExplicitMusic clone() {
        return new ExplicitMusic(this);
    }
}