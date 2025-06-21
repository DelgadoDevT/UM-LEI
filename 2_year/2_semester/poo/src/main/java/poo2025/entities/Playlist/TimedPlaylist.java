/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.entities.Playlist;


import poo2025.entities.Music.Music;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The TimedPlaylist class represents a specialized type of playlist that has a maximum
 * allowable duration and is associated with a specific genre. This class extends
 * the Playlist class and incorporates additional functionality to enforce restrictions
 * related to the playlist's duration and genre. The class also keeps track of the
 * current playback time to ensure compliance with its constraints.
 */
public class TimedPlaylist extends Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Represents the maximum allowed duration of the playlist in minutes */
    private final int maxTime;

    /** Represents the genre associated with the timed playlist */
    private final String genre;

    /** Represents the current playback time of the TimedPlaylist object in seconds */
    private int currentTime;

    /**
     * Default constructor for the TimedPlaylist class.
     *
     * This constructor initializes a TimedPlaylist object with default values: The maximum*/
    public TimedPlaylist(){
        super();
        this.maxTime = 0;
        this.genre = "";
        this.currentTime = 0;
    }

    /**
     * Constructs a TimedPlaylist object with the specified details, extending the Playlist class.
     *
     * @param name the name of the playlist
     * @param songs the list of music tracks included in the playlist; if null, an empty list will be initialized
     * @param isPublic determines*/
    public TimedPlaylist(String name, List<Music> songs, Boolean isPublic, Integer creator, int maxTime, String genre, int currentTime) {
        super(name, songs, isPublic, creator);
        this.maxTime = maxTime;
        this.genre = genre;
        this.currentTime = currentTime;
    }

    /**
     * Constructs a new TimedPlaylist with a specified maximum time and genre.
     *
     * @param maxTime the maximum duration of the playlist in seconds
     * @param genre the musical genre of the playlist
     */
    public TimedPlaylist(int maxTime, String genre) {
        super();
        this.maxTime = maxTime;
        this.genre = genre;
        this.currentTime = 0;
    }

    /**
     * Constructs a new TimedPlaylist object as a deep copy of the provided TimedPlaylist instance.
     * Copies the properties such as maximum duration, genre, and current time from
     * the given Timed*/
    public TimedPlaylist(TimedPlaylist playlist){
        super(playlist);
        maxTime = playlist.getMaxTime();
        genre = playlist.getGenre();
        currentTime = playlist.getCurrentTime();
    }


    /**
     * Retrieves the maximum allowable time for the TimedPlaylist.
     *
     * @return the maximum time of the playlist in seconds as an integer.
     */
    public int getMaxTime() {return maxTime;}

    /**
     * Retrieves the genre of the playlist.
     *
     * @return the genre of the playlist as a String
     */
    public String getGenre() {return genre;}

    /**
     * Retrieves the current time of the playlist in minutes.
     *
     * @return the current time of the playlist as an integer value.
     */
    public int getCurrentTime() {return currentTime;}

    /**
     * Updates the current playback time of the playlist.
     *
     * @param currentTime the new current playback time in seconds
     */
    public void setCurrentTime(int currentTime) {this.currentTime = currentTime;}

    /**
     * Retrieves the unique identifier of the TimedPlaylist.
     *
     * @return the unique identifier as an Integer
     */
    public Integer getIdentifier() {return super.getIdentifier();}

    /**
     * Retrieves the name of the TimedPlaylist.
     *
     * @return the name of the TimedPlaylist as a String.
     */
    public String getName() {return super.getName();}

    /**
     * Sets the name of the playlist.
     *
     * @param name the new name for the playlist
     */
    public void setName(String name) {super.setName(name);}

    /**
     * Retrieves the list of songs contained in the playlist.
     *
     * @return a List of Music objects representing the songs in the playlist.
     */
    public List<Music> getSongs() {return super.getSongs();}

    /**
     * Sets the list of songs for the TimedPlaylist.
     *
     * @param songs the list of Music objects to be assigned to the playlist
     */
    public void setSongs(List<Music> songs) {super.setSongs(songs);}

    /**
     * Retrieves the public visibility status of the playlist.
     *
     * @return a Boolean indicating whether the playlist is public (true) or private (false).
     */
    public Boolean getPublic() {return super.getPublic();}

    /**
     * Sets the public visibility status of the TimedPlaylist.
     *
     * @param aPublic Boolean value indicating whether the playlist should be public (true) or private (false).
     */
    public void setPublic(Boolean aPublic) {super.setPublic(aPublic);}

    /**
     * Retrieves the ID of the creator associated with this TimedPlaylist.
     *
     * @return the creator's ID as an Integer
     */
    public Integer getCreator() {return super.getCreator();}

    /**
     * Sets the creator's unique identifier for this TimedPlaylist.
     *
     * @param creator the unique identifier of the creator to be associated with this playlist.
     */
    public void setCreator(Integer creator) {super.setCreator(creator);}

    /**
     * Compares this TimedPlaylist object with another object to determine equality.
     * The comparison includes the superclass's fields and the specific fields
     * maxTime, genre, and currentTime of the TimedPlaylist class.
     *
     * @param o the object to compare with this TimedPlaylist
     * @return true*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TimedPlaylist that = (TimedPlaylist) o;
        return maxTime == that.getMaxTime() && genre.equals(that.getGenre()) && currentTime == that.currentTime;
    }

    /**
     * Computes and returns the hash code for this TimedPlaylist instance.
     * The hash code is derived from the hash code of its superclass and
     * the values of the maxTime, genre, and currentTime fields.
     *
     * @return an integer representing the hash code of this TimedPlaylist instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxTime, genre, currentTime);
    }

    /**
     * Returns a string representation of the object, including its various attributes
     * such as ID, name, visibility, creator, maximum time, genre, current time,
     * and the list of songs with their simplified view.
     *
     * @return a string representation of the object and its attributes
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(getIdentifier())
                .append(" | Name: ").append(getName())
                .append(" | Is Public: ").append(getPublic())
                .append(" | Creator: ").append(getCreator())
                .append(" | Max Time: ").append(maxTime)
                .append(" | Genre: ").append(genre)
                .append(" | Current Time: ").append(currentTime)
                .append(" | Songs: \n");

        for (Music song : getSongs())
            sb.append("  ").append(song.simpleView()).append("\n");

        return sb.toString();
    }

    /**
     * Creates and returns a copy of this TimedPlaylist object.
     *
     * @return a new TimedPlaylist instance that is a clone of the current object.
     */
    @Override
    public TimedPlaylist clone() {
        return new TimedPlaylist(this);
    }

    /**
     * Determines whether a given song can be added to the playlist based on its genre,
     * duration, and the maximum allowable time for the playlist.
     *
     * @param song the music object to evaluate for addition to the playlist
     * @return true if the song can be added to the playlist, false otherwise
     */
    public boolean canAddSong(Music song) {
        return song != null && currentTime + song.getDurationS() <= maxTime && song.getGenre().equalsIgnoreCase(genre);
    }

    /**
     * Adds a song to the list of songs if it can be added.
     * Updates the total duration of the current collection if the song is successfully added.
     *
     * @param song the Music object to be added to the collection. It should not conflict
     *             with any rules defined in the `canAddSong` method.
     */
    public void addSong(Music song) {
        if (canAddSong(song)) {
            super.getSongs().add(song);
            currentTime += song.getDurationS();
        }
    }

    /**
     * Calculates the total duration of all songs in the playlist and updates the currentTime field.
     *
     * This method iterates through the list of songs, retrieves the duration of each song in seconds,
     * and computes the cumulative total. The total is then set to the currentTime field.
     */
    public void calcAndUpdateCurrentTime() {
        int currentTime = 0;
        for (Music m : this.getSongs())
            currentTime += m.getDurationS();
        this.currentTime = currentTime;
    }
}