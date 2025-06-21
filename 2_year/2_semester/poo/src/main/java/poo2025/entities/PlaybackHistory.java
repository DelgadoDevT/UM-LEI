/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a record of music playback history in the system.
 * This class stores information about when a specific user listened to a specific music track.
 * Each playback record contains the user ID, music ID, and the date/time of the playback.
 * The class implements Serializable to support persistence operations.
 */
public class PlaybackHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The identifier of the user who played the music */
    private Integer userId;

    /** The identifier of the music track that was played */
    private Integer musicId;

    /** The date and time when the playback occurred */
    private LocalDateTime playbackDate;

    /**
     * Default constructor that initializes a PlaybackHistory with default values.
     * Sets user ID and music ID to 0, and playback date to the current time.
     */
    public PlaybackHistory() {
        this.userId = 0;
        this.musicId = 0;
        this.playbackDate = LocalDateTime.now();
    }

    /**
     * Constructs a PlaybackHistory with specified values.
     *
     * @param userId the identifier of the user who played the music
     * @param musicId the identifier of the music track that was played
     * @param playbackDate the date and time when the playback occurred
     */
    public PlaybackHistory(Integer userId, Integer musicId, LocalDateTime playbackDate) {
        this.userId = userId;
        this.musicId = musicId;
        this.playbackDate = playbackDate;
    }

    /**
     * Copy constructor that creates a new PlaybackHistory as a copy of another one.
     *
     * @param h the PlaybackHistory object to copy
     */
    public PlaybackHistory(PlaybackHistory h) {
        this.userId = h.getUserId();
        this.musicId = h.getMusicId();
        this.playbackDate = h.getPlaybackDate();
    }

    /**
     * Gets the user identifier associated with this playback record.
     *
     * @return the user identifier
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Sets the user identifier for this playback record.
     *
     * @param userId the user identifier to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Gets the music identifier associated with this playback record.
     *
     * @return the music identifier
     */
    public Integer getMusicId() {
        return this.musicId;
    }

    /**
     * Sets the music identifier for this playback record.
     *
     * @param musicId the music identifier to set
     */
    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    /**
     * Gets the date and time when the playback occurred.
     *
     * @return the playback date and time
     */
    public LocalDateTime getPlaybackDate() {
        return this.playbackDate;
    }

    /**
     * Sets the date and time for this playback record.
     *
     * @param playbackDate the playback date and time to set
     */
    public void setPlaybackDate(LocalDateTime playbackDate) {
        this.playbackDate = playbackDate;
    }

    /**
     * Compares this PlaybackHistory with another object for equality.
     * Two PlaybackHistory objects are considered equal if they have the same
     * user ID, music ID, and playback date.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlaybackHistory that = (PlaybackHistory) o;
        return Objects.equals(this.userId, that.getUserId()) &&
               Objects.equals(this.musicId, that.getMusicId()) &&
               Objects.equals(this.playbackDate, that.getPlaybackDate());
    }

    /**
     * Generates a hash code for this PlaybackHistory.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.userId, this.musicId, this.playbackDate);
    }

    /**
     * Returns a string representation of this PlaybackHistory.
     *
     * @return a string containing the class name and all field values
     */
    @Override
    public String toString() {
        return "poo2025.entities.PlaybackHistory{" + "userId=" + this.userId +
               ", musicId=" + this.musicId +
               ", playbackDate=" + this.playbackDate +
               '}';
    }

    public boolean hasBeenPlayedInLastXHours(int hours) {
        return this.playbackDate.isAfter(LocalDateTime.now().minusHours(hours));
    }

    /**
     * Creates and returns a deep copy of this PlaybackHistory object.
     *
     * @return a new PlaybackHistory object with the same values as this one
     */
    @Override
    public PlaybackHistory clone() {
        return new PlaybackHistory(this);
    }
}