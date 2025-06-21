/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.entities.Album;

import poo2025.entities.Music.Music;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a musical album that contains a collection of songs and information
 * about its name and artist. Each album is uniquely identified by an immutable identifier.
 * Provides functionality for managing album attributes and interacting with its songs.
 */
public class Album implements Serializable {
    private static final long serialVersionUID = 1L;
    /** A static field used to track the last assigned unique identifier for albums */
    private static Integer lastId = 0;

    /** A unique identifier for the album instance */
    private final Integer identifier;

    /** Represents the name of the album */
    private String name;

    /** Represents a collection of songs associated with the album */
    private List<Music> songs;

    /** Represents the artist or group associated with the album */
    private String artist;

    /**
     * Default constructor for the Album class.
     *
     * Initializes an Album instance with a unique identifier, an empty name,
     * an empty list of songs, and an empty artist name.
     */
    public Album() {
        this.identifier = lastId++;
        this.name = "";
        this.songs = new ArrayList<>();
        this.artist = "";
    }

    /**
     * Constructs a new Album instance with the specified name, list of songs, and artist.
     *
     * @param name   the name of the album to set
     * @param songs  the list of music tracks on the album
     * @param artist the artist associated with the album
     */
    public Album(String name, List<Music> songs, String artist) {
        this.identifier = lastId++;
        this.name = name;
        this.songs = new ArrayList<>(songs);
        this.artist = artist;
    }

    /**
     * Creates a new Album instance as a copy of another Album. The new instance
     * will share the same identifier as the source Album but will be a distinct
     * object with its own copies of data fields.
     *
     * This method cannot be used to create new, uniquely identified albums.
     *
     * @param other the Album object to be copied. Its identifier, name, songs,
     *              and artist will be cloned into the new instance.
     */
    public Album(Album other) {
        this.identifier = other.getIdentifier();
        this.name = other.name;
        this.songs = new ArrayList<>(other.getSongs());
        this.artist = other.artist;
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
     * Retrieves the identifier of the album.
     *
     * @return the unique identifier associated with the album, or null if not set.
     */
    public Integer getIdentifier() { return this.identifier; }

    /**
     * Retrieves the name of the entity.
     *
     * @return the name of the entity as a String
     */
    public String getName() { return this.name; }

    /**
     * Sets the name of the album.
     *
     * @param name the name to set for the album
     */
    public void setName(String name) { this.name = name; }

    /**
     * Retrieves a list of songs associated with the album.
     *
     * @return a list of {@link Music} objects representing the songs in the album.
     */
    public List<Music> getSongs() {
        return new ArrayList<>(this.songs);
    }

    /**
     * Updates the list of songs for the album.
     * Creates a new list containing the provided songs to ensure encapsulation.
     *
     * @param songs the list of {@code Music} objects to be set as the songs of the album
     */
    public void setSongs(List<Music> songs) {
        this.songs = new ArrayList<>(songs);
    }

    /**
     * Retrieves the artist of the album.
     *
     * @return the artist of the album as a String
     */
    public String getArtist() { return this.artist; }

    /**
     * Sets the artist or group name associated with the album.
     *
     * @param artist the name of the artist or group to associate with the album
     */
    public void setArtist(String artist) { this.artist = artist; }

    /**
     * Compares this album to the specified object for equality. This method returns {@code true}
     * if and only if the specified object is also an instance of {@code Album} and all corresponding
     * attributes of the two albums are equal.
     *
     * @param o the object to be compared for equality with this album
     * @return {@code true} if the specified object is equal to this album; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album album)) return false;
        return Objects.equals(this.identifier, album.getIdentifier()) &&
                Objects.equals(this.name, album.getName()) &&
                Objects.equals(this.songs, album.getSongs()) &&
                Objects.equals(this.artist, album.getArtist());
    }

    /**
     * Computes the hash code for this Album object using its identifier, name, songs, and artist fields.
     *
     * @return an integer representing the hash code of this Album object
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.identifier, this.name, this.songs, this.artist);
    }

    /**
     * Generates a string representation of the album, including its identifier, name, artist,
     * and a list of its songs in a simplified view.
     *
     * @return a string representation of the album with formatted details of its fields and songs.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(this.identifier)
                .append(" | Name: ").append(this.name)
                .append(" | Artist: ").append(this.artist)
                .append(" | Songs: \n");

        for (Music song : this.songs)
            sb.append("  ").append(song.simpleView()).append("\n");

        return sb.toString();
    }

    /**
     * Creates and returns a copy of this Album object.
     * The new Album instance is initialized with the same attributes as the original.
     *
     * @return a new Album object that is a duplicate of this instance.
     */
    @Override
    public Album clone() {
        return new Album(this);
    }

    /**
     * Adds a specified song to the album if the song's interpreter matches the album's artist.
     *
     * @param song the song to be added to the album. It must not be null, and its interpreter must match the album's artist.
     */
    public void addSong(Music song) {
        if (song != null && song.getInterpreter().equals(this.artist)) this.songs.add(song);
    }

    /**
     * Determines if music by the given artist is allowed on the album.
     *
     * @param artist the name of the artist to check
     * @return true if the given artist matches the artist of the album (case-insensitive), false otherwise
     */
    public boolean isMusicAllowedOnAlbum(String artist) {
        if (artist == null || this.artist == null)
            return false;
        return artist.equalsIgnoreCase(this.artist);
    }

    /**
     * Retrieves the identifier of the song at the specified position in the album's song list.
     * If the position is invalid or the song at the position is null, returns null.
     *
     * @param pos the position of the song in the album's song list
     * @return the identifier of the song at the specified position, or null if the position is invalid or the song is null
     */
    public Integer getCurrentSoundId(int pos) {
        if (pos < 0 || pos >= songs.size()) return null;
        Music song = songs.get(pos);
        return song != null ? song.getIdentifier() : null;
    }
}