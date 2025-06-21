/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.managers;

import poo2025.entities.Music.Music;
import poo2025.entities.Playlist.Playlist;
import poo2025.exceptions.PlaylistException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides functionality for managing a collection of playlists. Each playlist
 * is uniquely identified, and the class allows operations such as addition, removal, retrieval,
 * and querying of playlists. It also provides the ability to determine the user with the most
 * created playlists
 */
public class PlaylistManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Stores a collection of playlists, with each playlist identified by a unique integer key */
    private Map<Integer, Playlist> playlists;

    /** Represents the total number of playlists managed by the PlaylistManager */
    private int totalPlaylists;

    /**
     * Default constructor for the PlaylistManager class.
     * Initializes the PlaylistManager with an empty collection of playlists
     * and sets the total number of playlists to zero.
     */
    public PlaylistManager() {
        this.playlists = new HashMap<>();
        this.totalPlaylists = 0;
    }

    /**
     * Constructs a PlaylistManager with the provided playlists and total playlists count.
     *
     * @param playlists a map containing playlists with their unique identifiers as keys
     * @param totalPlaylists the total number of playlists
     */
    public PlaylistManager(Map<Integer, Playlist> playlists, int totalPlaylists) {
        this.playlists = new HashMap<>(playlists);
        this.totalPlaylists = totalPlaylists;
    }

    /**
     * Constructs a new PlaylistManager by copying the data from an existing PlaylistManager.
     *
     * @param p an instance of PlaylistManager from which the data, including playlists and total number
     *          of playlists, is to be copied. Each playlist in the provided PlaylistManager is cloned to
     *          ensure proper data encapsulation and immutability.
     */
    public PlaylistManager(PlaylistManager p) {
        this.playlists = new HashMap<>();
        for (Map.Entry<Integer, Playlist> entry : p.getPlaylists().entrySet())
            this.playlists.put(entry.getKey(), entry.getValue().clone());
        this.totalPlaylists = p.getTotalPlaylists();
    }

    /**
     * Retrieves a map of playlists where the key is the playlist ID and the value
     * is a clone of the corresponding Playlist object.
     *
     * @return a map containing copies of all playlists (ID as the key and cloned Playlist as the value).
     */
    public Map<Integer, Playlist> getPlaylists() {
        Map<Integer, Playlist> p = new HashMap<>();
        for (Map.Entry<Integer, Playlist> entry : this.playlists.entrySet())
            p.put(entry.getKey(), entry.getValue().clone());
        return p;
    }

    /**
     * Sets the playlists for the PlaylistManager instance. The provided map is cloned
     * to ensure any modifications to the original map or its content do not affect
     * the internal state of the PlaylistManager.
     *
     * @param playlists a map where the key is the playlist ID (Integer) and the value
     *                  is the corresponding Playlist object. Each playlist is cloned
     *                  before being set to ensure the immutability of the internal state.
     */
    public void setPlaylists(Map<Integer, Playlist> playlists) {
        this.playlists = new HashMap<>();
        for (Map.Entry<Integer, Playlist> entry : playlists.entrySet())
            this.playlists.put(entry.getKey(), entry.getValue().clone());
    }

    /**
     * Retrieves the total number of playlists managed by the system.
     *
     * @return the total number of playlists.
     */
    public int getTotalPlaylists() {
        return this.totalPlaylists;
    }

    /**
     * Sets the total number of playlists managed by this PlaylistManager instance.
     *
     * @param totalPlaylists the total number of playlists to set
     */
    public void setTotalPlaylists(int totalPlaylists) {
        this.totalPlaylists = totalPlaylists;
    }

    /**
     * Compares this PlaylistManager object to another object for equality.
     * The comparison is based on the size and content of the playlist's map.
     *
     * @param o the object to be compared for equality with this PlaylistManager
     * @return true if the specified object is equal to this PlaylistManager, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PlaylistManager that)) return false;

        Map<Integer, Playlist> thatPlaylists = that.getPlaylists();
        if (this.playlists.size() != thatPlaylists.size()) return false;
        for (Map.Entry<Integer, Playlist> entry : this.playlists.entrySet()) {
            Integer key = entry.getKey();
            Playlist value = entry.getValue();
            if (!thatPlaylists.containsKey(key)) return false;
            if (!value.equals(thatPlaylists.get(key))) return false;
        }
        return true;
    }

    /**
     * Generates a hash code for the PlaylistManager object.
     * The hash code is computed based on the fields {@code playlists} and {@code totalPlaylists}.
     *
     * @return an integer representing the hash code of the PlaylistManager object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.playlists, this.totalPlaylists);
    }

    /**
     * Returns a string representation of the PlaylistManager, including the total number
     * of playlists and the string representation of each playlist contained in the manager.
     *
     * @return a string representation of the PlaylistManager object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total playlists: ").append(this.totalPlaylists).append("\n");
        for (Playlist playlist : this.playlists.values())
            sb.append(playlist.toString()).append("\n");

        return sb.toString();
    }

    /**
     * Adds a playlist to the list of managed playlists.
     * Throws a PlaylistException if the playlist is null or if a playlist
     * with the same ID already exists in the manager.
     *
     * @param p the Playlist object to be added
     * @throws PlaylistException if the provided playlist is null, or if a playlist
     *         with the same ID already exists
     */
    public void addPlaylist(Playlist p) throws PlaylistException {
        if (p == null) throw new PlaylistException("Cannot add null playlist");
        if (this.playlists.containsKey(p.getIdentifier())) throw new PlaylistException("Playlist with ID " + p.getIdentifier() + " already exists");
        this.playlists.put(p.getIdentifier(), p.clone());
        this.totalPlaylists += 1;
    }

    /**
     * Removes a playlist from the manager and updates the associated user data.
     *
     * @param p the ID of the playlist to be removed
     * @param u the UserManager instance responsible for managing user data
     * @return true if the playlist was successfully removed
     * @throws PlaylistException if the playlist ID is null or the playlist does not exist
     */
    public boolean removePlaylist(Integer p, UserManager u) throws PlaylistException {
        if (p == null) throw new PlaylistException("Cannot remove playlist with null ID");
        if (!this.playlists.containsKey(p)) throw new PlaylistException("Playlist with ID " + p + " does not exist");
        this.playlists.remove(p);
        u.removePlaylistFromAllUsers(p);
        this.totalPlaylists -= 1;
        return true;
    }

    /**
     * Determines the user who has created the highest number of playlists.
     *
     * @return The ID of the user with the most created playlists. If no playlists are present, returns -1.
     */
    public Integer getTopUser() {
        Map<Integer, Integer> creatorCount = new HashMap<>();
        for (Playlist playlist : this.playlists.values()) {
            Integer creator = playlist.getCreator();
            creatorCount.put(creator, creatorCount.getOrDefault(creator, 0) + 1);
        }
        Integer topCreator = -1;
        int maxPlaylists = 0;
        for (Map.Entry<Integer, Integer> entry : creatorCount.entrySet()) {
            if (entry.getValue() > maxPlaylists) {
                maxPlaylists = entry.getValue();
                topCreator = entry.getKey();
            }
        }
        return topCreator;
    }

    /**
     * Retrieves the playlist associated with the given ID.
     *
     * @param id the unique identifier of the playlist to retrieve. Must not be null.
     * @return the {@code Playlist} associated with the specified ID.
     * @throws PlaylistException if the provided ID is null, or if no playlist exists with the given ID.
     */
    public Playlist getPlaylist(Integer id) throws PlaylistException {
        if (id == null) throw new PlaylistException("Cannot get playlist with null ID");
        Playlist p = this.playlists.get(id);
        if (p == null) throw new PlaylistException("Playlist with ID " + id + " does not exist");
        return p;
    }

    /**
     * Adds a music object to a specified playlist.
     *
     * @param playlistId the unique identifier of the playlist
     * @param music the music object to be added to the playlist
     * @return true if the music is successfully added to the playlist
     * @throws PlaylistException if the playlist ID is null, the music object is null,
     * or the playlist with the specified ID does not exist
     */
    public boolean addMusicToPlaylist(Integer playlistId, Music music) throws PlaylistException {
        if (playlistId == null) throw new PlaylistException("Cannot add music to playlist with null ID");
        if (music == null) throw new PlaylistException("Cannot add null music to playlist");
        if (!this.playlists.containsKey(playlistId)) throw new PlaylistException("Playlist with ID " + playlistId + " does not exist");
        Playlist playlist = this.playlists.get(playlistId);
        playlist.addSong(music);
        return true;
    }

    /**
     * Updates the details of a specific playlist, including its name and privacy status.
     * If the playlist with the specified ID does not exist, a {@code PlaylistException} is thrown.
     * The method also allows for conditional modification of the playlist name and privacy settings.
     *
     * @param playlistId the unique identifier of the playlist to update; must not be null
     * @param name the new name for the playlist; if empty, the name remains unchanged
     * @param privacy a boolean indicating whether the playlist should be public (true) or private (false)
     * @throws PlaylistException if the playlist ID is null or if the playlist does not exist
     */
    public void updatePlaylist(Integer playlistId, String name, boolean privacy) throws PlaylistException {
        Playlist playlist = playlists.get(playlistId);
        if (playlist == null) throw new PlaylistException("Playlist not found");

        if (!name.isEmpty()) playlist.setName(name);
        playlist.setPublic(privacy);
    }

    /**
     * Checks if a playlist exists in the collection based on the given playlist ID.
     *
     * @param playlistId the ID of the playlist to be checked; must not be null
     * @return true if the playlist exists in the collection, false otherwise
     * @throws PlaylistException if the provided playlistId is null
     */
    public boolean existsPlaylist(Integer playlistId) throws PlaylistException {
        if (playlistId == null) throw new PlaylistException("Cannot check playlist with null ID");
        return this.playlists.containsKey(playlistId);
    }

    /**
     * Retrieves all public playlists managed by this PlaylistManager instance.
     * The method collects all playlists that are flagged as public and returns
     * them within a new PlaylistManager instance.
     *
     * @return a PlaylistManager instance containing all public playlists
     * @throws PlaylistException if an error occurs while fetching or processing public playlists
     */
    public PlaylistManager getAllPublic() throws PlaylistException {
        PlaylistManager publicManager = new PlaylistManager();
        try {
            for (Map.Entry<Integer, Playlist> entry : this.playlists.entrySet()) {
                if (entry.getValue().getPublic()) publicManager.addPlaylist(entry.getValue().clone());
            }
        } catch (Exception e) {
            throw new PlaylistException("Error getting public playlists: " + e.getMessage());
        }
        return publicManager;
    }

    /**
     * Checks if a playlist identified by the given ID is public.
     *
     * @param playlistId the unique identifier of the playlist to check
     * @return {@code true} if the playlist is public, {@code false} otherwise
     * @throws PlaylistException if the playlist ID is null or does not exist
     */
    public boolean isPlaylistPublic(Integer playlistId) throws PlaylistException {
        if (playlistId == null) throw new PlaylistException("Cannot check publicity of playlist with null ID");
        if (!this.playlists.containsKey(playlistId)) throw new PlaylistException("Playlist with ID " + playlistId + " does not exist");
        return this.playlists.get(playlistId).getPublic();
    }

    /**
     * Checks if the given user is the creator of the specified playlist.
     *
     * @param playlistId the ID of the playlist to check ownership for; cannot be null
     * @param userId the ID of the user whose ownership is being verified; cannot be null
     * @return true if the user is the creator of the playlist, false otherwise
     * @throws PlaylistException if the playlistId is null or if the playlist does not exist
     */
    public boolean isUserOwnPlaylist(Integer playlistId, Integer userId) throws PlaylistException {
        if (playlistId == null) throw new PlaylistException("Cannot check publicity of playlist with null ID");
        if (!this.playlists.containsKey(playlistId)) throw new PlaylistException("Playlist with ID " + playlistId + " does not exist");
        return this.playlists.get(playlistId).getCreator().equals(userId);
    }

    /**
     * Creates and returns a copy of this PlaylistManager object.
     *
     * @return a new PlaylistManager instance, which is a copy of the current object.
     */
    @Override
    public PlaylistManager clone() {return new PlaylistManager(this);}
}