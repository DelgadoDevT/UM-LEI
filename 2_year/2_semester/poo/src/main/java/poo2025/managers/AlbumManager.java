/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.managers;

import poo2025.entities.Album.Album;
import poo2025.exceptions.AlbumException;
import poo2025.entities.Music.Music;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class, AlbumManager, is responsible for managing a collection of albums, indexed by unique integer identifiers.
 * It provides functionalities for storing, retrieving, and manipulating the collection of albums. The class supports
 * deep cloning to maintain data integrity and encapsulation. Additionally, AlbumManager integrates features for handling
 * album-related operations, ensuring consistency and validation of data throughout its lifecycle
 */
public class AlbumManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** A data structure that maps unique integer identifiers to {@link Album} objects */
    private Map<Integer, Album> albums;

    /** Represents the total number of albums managed within the AlbumManager */
    private int totalAlbums;

    /**
     * Default constructor for the AlbumManager class.
     * Initializes the internal data structure to store albums and sets the total
     * number of albums to zero.
     */
    public AlbumManager() {
        this.albums = new HashMap<>();
        this.totalAlbums = 0;
    }

    /**
     * Constructs an AlbumManager with the specified albums and total number of albums.
     * Creates a deep copy of the provided albums map to ensure encapsulation.
     *
     * @param albums a map containing album IDs as keys and corresponding Album objects as values
     * @param totalAlbums the total number of albums managed by this instance
     * @throws AlbumException if the album's map is null or if the totalAlbums value is negative
     */
    public AlbumManager(Map<Integer, Album> albums, int totalAlbums) throws AlbumException {
        if (albums == null) {
            throw new AlbumException("Albums map cannot be null");
        }
        if (totalAlbums < 0) {
            throw new AlbumException("Total albums cannot be negative");
        }
        this.albums = new HashMap<>();
        for (Map.Entry<Integer, Album> entry : albums.entrySet())
            this.albums.put(entry.getKey(), entry.getValue().clone());
        this.totalAlbums = totalAlbums;
    }

    /**
     * Constructs a new AlbumManager object by cloning the data from another AlbumManager object.
     * This involves creating deep copies of all albums in the provided AlbumManager and
     * copying the value of totalAlbums.
     *
     * @param other the AlbumManager object to clone. Must not be null.
     * @throws AlbumException if the provided AlbumManager object is null, or if there is any
     *                        issue while cloning the albums.
     */
    public AlbumManager(AlbumManager other) throws AlbumException {
        if (other == null) {
            throw new AlbumException("Cannot clone null AlbumManager");
        }
        this.albums = new HashMap<>();
        for (Map.Entry<Integer, Album> entry : other.getAlbums().entrySet())
            this.albums.put(entry.getKey(), entry.getValue().clone());
        this.totalAlbums = other.getTotalAlbums();
    }

    /**
     * Retrieves a deep copy of the albums managed by this instance.
     * Each album within the returned map is a cloned instance, ensuring
     * isolation from the original data structure.
     *
     * @return a map where the keys are album IDs (Integer) and the values are
     *         cloned Album objects corresponding to those IDs.
     */
    public Map<Integer, Album> getAlbums() {
        Map<Integer, Album> clone = new HashMap<>();
        for (Map.Entry<Integer, Album> entry : this.albums.entrySet())
            clone.put(entry.getKey(), entry.getValue().clone());
        return clone;
    }

    /**
     * Sets the album's map with the provided data. This method replaces the current albums
     * map with a new one that is a deep clone of the provided map. Both keys and values
     * from the input map are cloned into the internal albums map.
     *
     * @param albums the map of album identifiers to Album objects to set. Each album in the
     *               map is cloned before being added to the internal album data structure.
     * @throws AlbumException if the provided albums map is null.
     */
    public void setAlbums(Map<Integer, Album> albums) throws AlbumException {
        if (albums == null) {
            throw new AlbumException("Albums map cannot be null");
        }
        this.albums = new HashMap<>();
        for (Map.Entry<Integer, Album> entry : albums.entrySet())
            this.albums.put(entry.getKey(), entry.getValue().clone());
    }

    /**
     * Retrieves the total number of albums currently managed.
     *
     * @return the total number of albums.
     */
    public int getTotalAlbums() {
        return this.totalAlbums;
    }

    /**
     * Sets the total number of albums. The value must be non-negative.
     *
     * @param totalAlbums the total number of albums to set
     * @throws AlbumException if the provided totalAlbums value is negative
     */
    public void setTotalAlbums(int totalAlbums) throws AlbumException {
        if (totalAlbums < 0) {
            throw new AlbumException("Total albums cannot be negative");
        }
        this.totalAlbums = totalAlbums;
    }

    /**
     * Compares this AlbumManager object to the specified object for equality.
     * Two AlbumManager objects are considered equal if they have the same total number of albums
     * and the albums contained within them are also equal.
     *
     * @param o the object to compare this AlbumManager against
     * @return {@code true} if the specified object is equal to this AlbumManager;
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbumManager that)) return false;
        return this.totalAlbums == that.totalAlbums &&
                Objects.equals(this.getAlbums(), that.getAlbums());
    }

    /**
     * Computes the hash code for this AlbumManager instance based on its fields.
     *
     * @return an integer value representing the hash code of this AlbumManager instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.albums, this.totalAlbums);
    }

    /**
     * Returns a string representation of the AlbumManager object,
     * including the total number of albums and the string representation of each individual album.
     *
     * @return a string that contains the total album count and the details of all albums managed by this instance.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total albums: ").append(this.totalAlbums).append("\n");
        for (Album album : this.albums.values())
            sb.append(album.toString()).append("\n");
        return sb.toString();
    }

    /**
     * Creates and returns a copy of this AlbumManager instance. The copy is initialized
     * using this instance's data through the copy constructor. If an error occurs during
     * the cloning process, such as an {@code AlbumException}, the method will return null.
     *
     * @return a new AlbumManager that is a clone of this instance or null if cloning fails
     */
    @Override
    public AlbumManager clone() {
        try {
            return new AlbumManager(this);
        } catch (AlbumException e) {
            return null;
        }
    }

    /**
     * Adds an album to the collection. If the album does not already exist in the collection,
     * it is cloned and added. Increments the total number of albums upon successful addition.
     *
     * @param album The album to be added. Must not be null.
     * @throws AlbumException If the album is null or if there is an error during the addition process.
     */
    public void addAlbum(Album album) throws AlbumException {
        if (album == null) {
            throw new AlbumException("Album cannot be null");
        }
        if (!this.albums.containsKey(album.getIdentifier())) {
            try {
                this.albums.put(album.getIdentifier(), album.clone());
                this.totalAlbums++;
            } catch (Exception e) {
                throw new AlbumException("Error adding album", e);
            }
        }
    }

    /**
     * Removes an album with the specified ID from the album manager and updates the associated user manager
     * to reflect the removal for all users. If the album ID is null or does not exist in the album manager,
     * an exception is thrown.
     *
     * @param id The identifier of the album to be removed.
     * @param u The user manager that will be updated to remove the album from all users.
     * @throws AlbumException If the album ID is null, or the album does not exist in the album manager.
     */
    public void removeAlbum(Integer id, UserManager u) throws AlbumException {
        if (id == null) {
            throw new AlbumException("Album ID cannot be null");
        }
        if (!this.albums.containsKey(id)) {
            throw new AlbumException("Album with ID " + id + " does not exist");
        }
        this.albums.remove(id);
        u.removeAlbumFromAllUsers(id);
        this.totalAlbums--;
    }

    /**
     * Retrieves an album by its ID.
     *
     * @param id the unique identifier of the album to retrieve, must not be null
     * @return the {@code Album} object corresponding to the given ID
     * @throws AlbumException if the provided ID is null, or if no album exists for the given ID
     */
    public Album getAlbum(Integer id) throws AlbumException {
        if (id == null) {
            throw new AlbumException("Album ID cannot be null");
        }
        Album album = this.albums.get(id);
        if (album == null) {
            throw new AlbumException("Album with ID " + id + " does not exist");
        }
        return album;
    }

    /**
     * Adds a music track to an album.
     *
     * This method associates a specific {@code Music} instance with an album identified
     * by its unique {@code albumId}. Validation is performed to ensure the existence
     * of the album, compatibility of the music's interpreter, and non-null parameters.
     * An {@code AlbumException} is thrown if any validation fails.
     *
     * @param albumId the unique identifier of the album to which the music will be added
     * @param music the music track to be added to the album
     * @return {@code true} if the music was successfully added to the album, {@code false} if it was already present
     * @throws AlbumException if {@code albumId} or {@code music} is null, the album does not exist,
     *                        or the music's interpreter does not match the album's artist
     */
    public boolean addMusicToAlbum(Integer albumId, Music music) throws AlbumException {
        if (albumId == null)
            throw new AlbumException("Album ID cannot be null");
        if (music == null)
            throw new AlbumException("Music cannot be null");
        if (!this.albums.containsKey(albumId))
            throw new AlbumException("Album with ID " + albumId + " does not exist");
        
        Album album = this.albums.get(albumId);

        for (Music existingSong : album.getSongs())
            if (existingSong.getIdentifier().equals(music.getIdentifier()))
                return false;

        if (!music.getInterpreter().equalsIgnoreCase(album.getArtist()))
            throw new AlbumException("Music interpreter does not match album artist");
        
        album.addSong(music);
        return true;
    }

    /**
     * Updates the name of an album identified by its unique ID. If the album is not
     * found, an exception is thrown. The album's name is updated only if the new name
     * is not empty.
     *
     * @param albumId the unique identifier of the album to be updated, must not be null
     * @param name the new name for the album, must not be empty
     * @throws AlbumException if the album is not found or if an error occurs during the update
     */
    public void updateAlbum(Integer albumId, String name) throws AlbumException {
        Album album = albums.get(albumId);
        if (album == null) throw new AlbumException("Album not found");

        if (!name.isEmpty()) album.setName(name);
    }

    /**
     * Checks if an album with the specified ID exists in the album collection.
     *
     * @param albumId the ID of the album to check for existence, which should be an integer
     * @return {@code true} if the album exists in the collection and the provided ID is not null,
     *         otherwise {@code false}
     */
    public boolean existsAlbum(Integer albumId) throws AlbumException{
        if (albumId == null) {
            throw new AlbumException("Album ID cannot be null");
        }
        return this.albums.containsKey(albumId);
    }
}