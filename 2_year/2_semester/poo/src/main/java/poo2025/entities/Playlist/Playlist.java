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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a musical playlist, with attributes such as a unique identifier,
 * name, collection of songs, visibility status, and creator's identifier.
 * This class supports operations including adding and removing songs, cloning,
 * and retrieving song details. Playlists can be public or private, and their
 * details can be modified as needed.
 */
public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    /** A static variable used to store and manage the last assigned unique identifier */
    private static Integer lastId = 0;

    /** Represents the unique identifier for a playlist */
    private final Integer identifier;

    /** Represents the name of the playlist */
    private String name;

    /** Represents the collection of songs contained in the playlist */
    private List<Music> songs;

    /** Indicates whether the playlist is public or private */
    private Boolean isPublic;

    /** Represents the identifier of the creator of this playlist */
    private Integer creator;

    /**
     * Creates a new instance of the Playlist class with default initialization.
     *
     * This constructor assigns a unique ID to the playlist by incrementing a
     * static counter. It initializes the playlist with an empty name, an empty
     * list of songs, sets the playlist to public visibility, and the creator as
     * undefined.
     */
    public Playlist(){
        this.identifier = lastId++;
        this.name = "";
        this.songs = new ArrayList<>();
        this.isPublic = true;
        this.creator = 0;
    }

    /**
     * Constructs a Playlist object with the specified details.
     *
     * @param name the name of the playlist
     * @param songs the list of music tracks included in the playlist; if null, an empty list will be initialized
     * @param isPublic determines if the playlist is publicly accessible
     * @param creator the ID of the user who created the playlist
     */
    public Playlist(String name, List<Music> songs, Boolean isPublic, Integer creator){
        this.identifier = lastId++;
        this.name = name;
        this.songs = (songs != null) ? new ArrayList<>(songs) : new ArrayList<>();
        this.isPublic = isPublic;
        this.creator = creator;
    }

    /**
     * Constructs a new Playlist object by copying the details from an existing Playlist.
     * Creates a deep copy of the provided Playlist's song's list.
     *
     * @param playlist the Playlist instance to copy. It provides the values for ID, name,
     *                 songs, public visibility, and creator of the new Playlist.
     */
    public Playlist(Playlist playlist){
        this.identifier = playlist.getIdentifier();
        this.name = playlist.name;
        this.songs = new ArrayList<>(playlist.getSongs());
        this.isPublic = playlist.isPublic;
        this.creator = playlist.creator;
    }

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
     * Retrieves the unique identifier of the playlist.
     *
     * @return the identifier of the playlist as an Integer
     */
    public Integer getIdentifier() {return identifier;}

    /**
     * Retrieves the name of the playlist.
     *
     * @return the name of the playlist as a String.
     */
    public String getName() {return name;}

    /**
     * Sets the name of the playlist.
     *
     * @param name the new name for the playlist
     */
    public void setName(String name) {this.name = name;}

    /**
     * Retrieves the list of songs in the playlist.
     *
     * @return a list of Music objects representing the songs in the playlist.
     */
    public List<Music> getSongs() {return songs;}

    /**
     * Sets the list of songs in the playlist.
     *
     * @param songs the list of {@code Music} objects to be assigned to the playlist
     */
    public void setSongs(List<Music> songs) {this.songs = songs;}

    /**
     * Retrieves the public visibility status of the playlist.
     *
     * @return a Boolean indicating whether the playlist is public (true) or private (false).
     */
    public Boolean getPublic() {return isPublic;}

    /**
     * Sets the public visibility status of the playlist.
     *
     * @param aPublic Boolean value indicating whether the playlist should be public (true) or private (false).
     */
    public void setPublic(Boolean aPublic) {isPublic = aPublic;}

    /**
     * Returns the ID of the creator associated with this playlist.
     *
     * @return the creator's ID as an Integer
     */
    public Integer getCreator() {return creator;}

    /**
     * Sets the creator identifier for this playlist.
     *
     * @param creator the unique identifier of the creator to be associated with this playlist
     */
    public void setCreator(Integer creator) {this.creator = creator;}

    /**
     * Compares this playlist with another object to determine equality.
     * This method checks if the provided object is of the same class and compares
     * the fields of both objects for equality.
     *
     * @param o the object to compare with this playlist
     * @return true if the given object is equal to this playlist, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if(o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(identifier, playlist.identifier) && Objects.equals(name, playlist.name) && Objects.equals(songs, playlist.songs) && Objects.equals(isPublic, playlist.isPublic) && Objects.equals(creator, playlist.creator);
    }

    /**
     * Calculates and returns a hash code value for the current object.
     * The hash code is computed based on the object's fields that are considered relevant for equality comparisons.
     *
     * @return an integer representing the hash code for this object
     */
    @Override
    public int hashCode() {return Objects.hash(identifier, name, songs, isPublic, creator);}

    /**
     * Generates a string representation of the Playlist object, including its ID, name,
     * public status, creator, and a list of its songs. Each song entry is represented
     * in a simplified view format.
     *
     * @return A string containing the detailed information of the Playlist and its songs.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(this.identifier)
                .append(" | Name: ").append(this.name)
                .append(" | Is Public: ").append(this.isPublic)
                .append(" | Creator: ").append(this.creator)
                .append(" | Songs: \n");

        for (Music song : this.songs)
            sb.append("  ").append(song.simpleView()).append("\n");

        return sb.toString();
    }

    /**
     * Creates and returns a deep clone of the current Playlist object.
     *
     * @return a new Playlist object that is a copy of the current instance.
     */
    @Override
    public Playlist clone() {
        return new Playlist(this);
    }

    /**
     * Adds a song to the playlist.
     *
     * @param song the {@code Music} object to be added to the playlist
     */
    public void addSong(Music song){
        List<Music> songs = getSongs();
        songs.add(song);
    }

    /**
     * Removes the specified song from the playlist.
     *
     * @param song the Music instance to be removed from the playlist
     */
    private void removeSong(Music song){
        List<Music> songs = getSongs();
        songs.remove(song);
    }

    /**
     * Retrieves the identifier of the sound at the specified position in the song's list.
     *
     * @param pos the position of the song in the list; must be non-negative and less than the size of the list
     * @return the identifier of the sound if the position is valid and the song exists, otherwise null
     */
    public Integer getCurrentSoundId(int pos) {
        if (pos < 0 || pos >= songs.size()) return null;
        Music song = songs.get(pos);
        return song != null ? song.getIdentifier() : null;
    }
}