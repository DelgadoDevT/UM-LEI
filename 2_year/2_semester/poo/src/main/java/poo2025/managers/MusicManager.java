/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.managers;

import poo2025.entities.Music.MultimediaMusic;
import poo2025.exceptions.AlbumException;
import poo2025.entities.Music.ExplicitMusic;
import poo2025.entities.Music.Music;
import poo2025.exceptions.MusicException;
import poo2025.entities.PlaybackHistory;
import poo2025.exceptions.PlaybackHistoryException;
import poo2025.exceptions.PlaylistException;
import poo2025.entities.Playlist.TimedPlaylist;
import poo2025.exceptions.UserException;

import java.io.Serializable;
import java.util.*;

/**
 * Manages a collection of music tracks in the system. This class provides functionality for
 * storing, retrieving, and manipulating music entities, as well as generating recommendations
 * and managing music-related operations.
 *
 * The class implements Serializable to support persistence operations and maintains
 * a map of music tracks indexed by their unique identifiers.
 */
public class MusicManager implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Maps music identifiers to their corresponding Music objects */
    private Map<Integer, Music> musics;

    /** Tracks the total number of music entries in the manager */
    private int totalMusics;

    /**
     * Constructs an empty MusicManager with no music tracks.
     * Initializes the music collection and sets the total count to zero.
     */
    public MusicManager() {
        this.musics = new HashMap<>();
        this.totalMusics = 0;
    }

    /**
     * Constructs a MusicManager with an initial collection of music tracks.
     *
     * @param musics the initial map of music tracks to store
     * @param totalMusics the initial total count of music tracks
     * @throws MusicException if the musics map is null or totalMusics is negative
     */
    public MusicManager(Map<Integer, Music> musics, int totalMusics) throws MusicException {
        if (musics == null) throw new MusicException("Musics map cannot be null");
        if (totalMusics < 0) throw new MusicException("Total musics cannot be negative");

        this.musics = new HashMap<>();
        for (Map.Entry<Integer, Music> entry : musics.entrySet())
            this.musics.put(entry.getKey(), entry.getValue().clone());
        this.totalMusics = totalMusics;
    }

    /**
     * Copy constructor that creates a new MusicManager as a deep copy of another one.
     *
     * @param m the MusicManager to copy
     * @throws MusicException if the provided MusicManager is null
     */
    public MusicManager(MusicManager m) throws MusicException {
        if (m == null) throw new MusicException("Music manager cannot be null");

        this.musics = new HashMap<>();
        for (Map.Entry<Integer, Music> entry : m.getMusics().entrySet())
            this.musics.put(entry.getKey(), entry.getValue().clone());
        this.totalMusics = m.getTotalMusics();
    }

    /**
     * Returns a deep copy of the music collection.
     *
     * @return a new map containing cloned Music objects
     */
    public Map<Integer, Music> getMusics() {
        Map<Integer, Music> m = new HashMap<>();
        for (Map.Entry<Integer, Music> entry : this.musics.entrySet())
            m.put(entry.getKey(), entry.getValue().clone());
        return m;
    }

    /**
     * Sets the music collection to a new map of music tracks.
     *
     * @param musics the new map of music tracks
     * @throws MusicException if the provided map is null
     */
    public void setMusics(Map<Integer, Music> musics) throws MusicException {
        if (musics == null) throw new MusicException("Musics map cannot be null");

        this.musics = new HashMap<>();
        for (Map.Entry<Integer, Music> entry : musics.entrySet())
            this.musics.put(entry.getKey(), entry.getValue().clone());
    }

    /**
     * Gets the total number of music tracks in the manager.
     *
     * @return the total number of music tracks
     */
    public int getTotalMusics() {
        return this.totalMusics;
    }

    /**
     * Sets the total number of music tracks.
     *
     * @param totalMusics the new total number of music tracks
     * @throws MusicException if the provided value is negative
     */
    public void setTotalMusics(int totalMusics) throws MusicException {
        if (totalMusics < 0) throw new MusicException("Total musics cannot be negative");
        this.totalMusics = totalMusics;
    }

    /**
     * Compares this MusicManager with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MusicManager that = (MusicManager) o;
        Map<Integer, Music> m = that.getMusics();
        if (this.totalMusics != that.getTotalMusics() || this.musics.size() != m.size()) return false;
        for (Integer key : m.keySet()) {
            Music thatMusic = m.get(key);
            if (thatMusic == null || !this.musics.get(key).equals(thatMusic)) return false;
        }
        return true;
    }

    /**
     * Generates a hash code for this MusicManager.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.musics, this.totalMusics);
    }

    /**
     * Returns a string representation of all music tracks in the manager.
     *
     * @return a formatted string containing all music information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total musics: ").append(this.totalMusics).append("\n");
        for (Music music : this.musics.values())
            sb.append(music.toString()).append("\n");

        return sb.toString();
    }

    /**
     * Adds a new music track to the collection.
     *
     * @param m the music track to add
     * @throws MusicException if the music is null or already exists
     */
    public void addMusic(Music m) throws MusicException {
        if (m == null) throw new MusicException("Music cannot be null");

        if (!this.musics.containsKey(m.getIdentifier())) {
            this.musics.put(m.getIdentifier(), m.clone());
            this.totalMusics += 1;
        } else {
            throw new MusicException("Music already exists");
        }
    }

    /**
     * Removes a music track from the collection.
     *
     * @param m the identifier of the music to remove
     * @throws MusicException if the music ID is null
     * @return true if the music was removed, false if it wasn't found
     */
    public boolean removeMusic(Integer m) throws MusicException {
        if (m == null) throw new MusicException("Music ID cannot be null");

        if (this.musics.containsKey(m)) {
            this.musics.remove(m);
            this.totalMusics -= 1;
            return true;
        }
        return false;
    }

    /**
     * Retrieves a music track by its identifier.
     *
     * @param musicId the identifier of the music to retrieve
     * @throws MusicException if the music ID is null or the music is not found
     * @return a clone of the requested music track
     */
    public Music getMusic(Integer musicId) throws MusicException {
        if (musicId == null) throw new MusicException("Music ID cannot be null");

        Music music = this.musics.get(musicId);
        if (music == null) throw new MusicException("Music not found");
        return music.clone();
    }

    /**
     * Retrieves the most played music track in the collection.
     *
     * @throws MusicException if no music tracks are available
     * @return the most played music track, or null if no tracks exist
     */
    public Music getMostPlayed() throws MusicException {
        if (musics.isEmpty()) throw new MusicException("No musics available");

        int maxPlays = Integer.MIN_VALUE;
        Music mostPlayedMusic = null;

        for (Music music : musics.values())
            if (music.getPlays() > maxPlays) {
                maxPlays = music.getPlays();
                mostPlayedMusic = music;
            }

        return mostPlayedMusic != null ? mostPlayedMusic.clone() : null;
    }

    /**
     * Gets the most popular music genre based on play counts.
     *
     * @throws MusicException if no music tracks are available
     * @return a string indicating the top genre
     */
    public String getTopGenre() throws MusicException {
        if (musics.isEmpty()) throw new MusicException("No musics available");

        int maxPlays = Integer.MIN_VALUE;
        String topGenre = null;

        for (Music music : musics.values())
            if (music.getPlays() > maxPlays) {
                maxPlays = music.getPlays();
                topGenre = music.getGenre();
            }

        return "The top genre is: " + topGenre;
    }

    /**
     * Identifies the most listened-to artist based on total play counts.
     *
     * @throws MusicException if no music tracks are available
     * @return a string indicating the most listened artist
     */
    public String getMostListenedArtist() throws MusicException {
        if (musics.isEmpty()) throw new MusicException("No musics available");

        Map<String, Integer> artistPlays = new HashMap<>();

        for (Music music : musics.values())
            artistPlays.merge(music.getInterpreter(), music.getPlays(), Integer::sum);

        String mostListenedArtist = null;
        int maxPlays = Integer.MIN_VALUE;

        for (Map.Entry<String, Integer> entry : artistPlays.entrySet())
            if (entry.getValue() > maxPlays) {
                maxPlays = entry.getValue();
                mostListenedArtist = entry.getKey();
            }

        return "Most Listened Artist: " + mostListenedArtist;
    }

    /**
     * Updates the details of an existing music track. This method allows updating
     * the title, artist, publisher, genre, and duration of a music track identified
     * by its music ID. If a field is not intended to be updated, its corresponding
     * parameter can be left empty or zero for duration.
     *
     * @param musicId the identifier of the music track to update
     * @param title the new title of the music track; if empty, the title remains unchanged
     * @param artist the new artist of the music track; if empty, the artist remains unchanged
     * @param publisher the new publisher of the music track; if empty, the publisher remains unchanged
     * @param genre the new genre of the music track; if empty, the genre remains unchanged
     * @param duration the new duration of the music track in seconds; if zero, the duration remains unchanged
     * @throws MusicException if the music ID is null, or if no music track is found with the given ID
     */
    public void updateMusic(Integer musicId, String title, String lyrics, String publisher, String genre, int duration) throws MusicException {
        Music music = musics.get(musicId);
        if (music == null) throw new MusicException("Music not found");
        
        if (!title.isEmpty()) music.setName(title);
        if (!lyrics.isEmpty()) music.setLyrics(lyrics);
        if (!publisher.isEmpty()) music.setPublisher(publisher);
        if (!genre.isEmpty()) music.setGenre(genre);
        if (duration != 0) music.setDurationS(duration);
    }
    
    /**
     * Updates the explicit fields of a music instance, including age restriction and rating source.
     * Throws a MusicException if the music instance is not found or is not explicit.
     *
     * @param musicId         the unique identifier of the music item to be updated
     * @param ageRestriction  the new age restriction value; -1 to detect automatically, 0 to skip updates
     * @param ratingSouce     the source of the rating; an empty string indicates no update
     * @throws MusicException if the music instance is not found or is not explicit
     */
    public void updateExplicitFields(Integer musicId, int ageRestriction, String ratingSouce) throws MusicException {
        Music music = getMusic(musicId);
        if (music == null) throw new MusicException("Music not found");
        if (!(music instanceof ExplicitMusic e)) throw new MusicException("This music is not explicit");

        if (ageRestriction == -1) e.detectAndAssignAgeRestriction();
        else if (ageRestriction != 0) e.setAgeRestriction(ageRestriction);
        if (!ratingSouce.isEmpty()) e.setRatingSource(ratingSouce);
    }

    /**
     * Updates the multimedia fields of a specific music entry.
     *
     * @param musicId the unique identifier of the music.
     * @param videoUrl the new video URL to associate with the music.
     * @param resolution the resolution of the video (e.g., 1080p, 720p).
     * @param subtitles a boolean indicating whether the video has subtitles.
     * @throws MusicException if the music is not found or if the music is not a multimedia type.
     */
    public void updateMultimediaFields(Integer musicId, String videoUrl, String resolution, boolean subtitles) throws MusicException {
        Music music = getMusic(musicId);
        if (music == null) throw new MusicException("Music not found");
        if (!(music instanceof MultimediaMusic m)) throw new MusicException("This music is not multimedia");

        if (!videoUrl.isEmpty()) m.setVideoURL(videoUrl);
        if (!resolution.isEmpty()) m.setResolution(resolution);
        m.setHasSubtitles(subtitles);
    }

    /**
     * Compares two music tracks based on genre and artist frequency.
     *
     * @param m1 first music track to compare
     * @param m2 second music track to compare
     * @param genreFrequency map of genre frequencies
     * @param artistFrequency map of artist frequencies
     * @return negative if m1 is less than m2, positive if m1 is greater than m2, zero if equal
     */
    private int compareMusics(Music m1, Music m2, Map<String, Long> genreFrequency, Map<Integer, Long> artistFrequency) {
        Long g1 = genreFrequency.getOrDefault(m1.getGenre().toLowerCase(), 0L);
        Long g2 = genreFrequency.getOrDefault(m2.getGenre().toLowerCase(), 0L);
        int genreCompare = g2.compareTo(g1);

        if (genreCompare != 0) return genreCompare;

        Long a1 = artistFrequency.getOrDefault(m1.getIdentifier(), 0L);
        Long a2 = artistFrequency.getOrDefault(m2.getIdentifier(), 0L);
        return a2.compareTo(a1);
    }

    /**
     * Generates music recommendations based on user history, genre preferences, and other criteria.
     *
     * @param userId user for whom to generate recommendations
     * @param history playback history to consider
     * @param limit maximum number of recommendations to return
     * @param explicitOnly whether to include only explicit content
     * @param maxDuration maximum total duration of recommended tracks
     * @param genre specific genre to filter by
     * @return list of recommended music tracks
     * @throws UserException if the user ID is null
     * @throws PlaybackHistoryException if the history is null
     */
    public List<Music> recommendByGenreAndArtist(Integer userId, List<PlaybackHistory> history, int limit, boolean explicitOnly, int maxDuration, String genre) throws UserException, PlaybackHistoryException {
        if (userId == null) throw new UserException("User ID cannot be null");
        if (history == null) throw new PlaybackHistoryException("History cannot be null");
        if (limit <= 0) return Collections.emptyList();

        Map<String, Long> genreFrequency = new HashMap<>();
        Map<Integer, Long> artistFrequency = new HashMap<>();
        Set<Integer> playedMusicIds = new HashSet<>();

        // Prepare frequency maps and filter the user's history
        for (PlaybackHistory h : history)
            if (h.getUserId().equals(userId)) {
                Music music = musics.get(h.getMusicId());
                if (music != null) {
                    playedMusicIds.add(music.getIdentifier());
                    genreFrequency.merge(music.getGenre().toLowerCase(), 1L, Long::sum);
                    artistFrequency.merge(music.getIdentifier(), 1L, Long::sum);
                }
            }

        int totalDuration = 0;
        List<Music> recommendedMusic = new ArrayList<>();

        // Sort musics by genre frequency and artist frequency
        List<Music> sortedMusics = musics.values().stream()
                .filter(music -> !playedMusicIds.contains(music.getIdentifier()) &&
                        (!explicitOnly || music instanceof ExplicitMusic) &&
                        (maxDuration == -1 || music.getGenre().equalsIgnoreCase(genre)))
                .sorted((m1, m2) -> compareMusics(m1, m2, genreFrequency, artistFrequency))
                .toList();

        // Generate recommendations
        for (Music music : sortedMusics) {
            int musicDuration = music.getDurationS();

            if (maxDuration != -1 && totalDuration + musicDuration > maxDuration)
                continue;

            recommendedMusic.add(music);
            totalDuration += musicDuration;

            if (recommendedMusic.size() >= limit)
                break;
        }

        return recommendedMusic;
    }

    /**
     * Generates a random music identifier from the collection.
     *
     * @throws MusicException if no music tracks are available
     * @return a random music identifier
     */
    public Integer getRandomMusicId() throws MusicException {
        if (musics.isEmpty()) throw new MusicException("No musics available");
        if (this.totalMusics == 1) return musics.values().iterator().next().getIdentifier();
        return new Random().nextInt(0, this.totalMusics - 1);
    }

    /**
     * Adds a music track to a playlist.
     *
     * @param p the playlist manager
     * @param playlistId the identifier of the target playlist
     * @param musicId the identifier of the music to add
     * @return true if the music was added successfully, false otherwise
     * @throws PlaylistException if the playlist manager or ID is null
     * @throws MusicException if the music ID is null or not found
     */
    public boolean addMusicToPlaylist(PlaylistManager p, Integer playlistId, Integer musicId)
            throws PlaylistException, MusicException {
        if (p == null) throw new PlaylistException("Playlist manager cannot be null");
        if (playlistId == null) throw new PlaylistException("Playlist ID cannot be null");
        if (musicId == null) throw new MusicException("Music ID cannot be null");
        if (!this.musics.containsKey(musicId)) throw new MusicException("Music not found");

        return p.addMusicToPlaylist(playlistId, this.musics.get(musicId));
    }

    /**
     * Adds a music track to an album.
     *
     * @param a the album manager
     * @param albumId the identifier of the target album
     * @param musicId the identifier of the music to add
     * @return true if the music was added successfully, false otherwise
     * @throws AlbumException if the album manager or ID is null
     * @throws MusicException if the music ID is null or not found
     */
    public boolean addMusicToAlbum(AlbumManager a, Integer albumId, Integer musicId)
            throws AlbumException, MusicException {
        if (a == null) throw new AlbumException("Album manager cannot be null");
        if (albumId == null) throw new AlbumException("Album ID cannot be null");
        if (musicId == null) throw new MusicException("Music ID cannot be null");
        if (!this.musics.containsKey(musicId)) throw new MusicException("Music not found");

        return a.addMusicToAlbum(albumId, this.musics.get(musicId));
    }

    /**
     * Checks if a music track exists in the collection.
     *
     * @param musicId the identifier of the music to check
     * @return true if the music exists, false otherwise
     * @throws MusicException if the music ID is null
     */
    public boolean existsMusic(Integer musicId) throws MusicException {
        if (musicId == null) throw new MusicException("Music ID cannot be null");
        return this.musics.containsKey(musicId);
    }

    /**
     * Increments the play count for a specific music track.
     *
     * @param musicId the identifier of the music track
     * @param x the number of plays to add
     * @throws MusicException if the music ID is null, not found, or if x is negative
     */
    public void addPlay(Integer musicId, int x) throws MusicException {
        if (musicId == null) throw new MusicException("Music ID cannot be null");
        if (x < 0) throw new MusicException("Number of plays cannot be negative");

        Music music = this.musics.get(musicId);
        if (music == null) throw new MusicException("Music not found");
        music.addPlays(x);
    }

    /**
     * Generates a timed playlist based on specified criteria.
     *
     * @param creator the ID of the playlist creator
     * @param maxTime maximum duration of the playlist in seconds
     * @param genre the genre filter for the playlist
     * @param totalPlaylists the total number of existing playlists
     * @return a new TimedPlaylist instance
     * @throws PlaylistException if any of the parameters are invalid
     */
    public TimedPlaylist generateTimedPlaylist(Integer creator, int maxTime, String genre, int totalPlaylists) throws PlaylistException {
        if (creator == null) throw new PlaylistException("Cannot generate playlist with null creator");
        if (maxTime <= 0) throw new PlaylistException("Maximum time must be positive");
        if (genre == null || genre.trim().isEmpty()) throw new PlaylistException("Genre cannot be null or empty");

        TimedPlaylist timedPlaylist = new TimedPlaylist("recommended" + totalPlaylists, null, false, creator, maxTime, genre, 0);

        for (Music song : this.musics.values())
            if (timedPlaylist.canAddSong(song)) {
                timedPlaylist.addSong(song);
                if (timedPlaylist.getCurrentTime() >= maxTime)
                    return timedPlaylist;
            }

        return timedPlaylist;
    }

    /**
     * Creates and returns a deep copy of this MusicManager.
     *
     * @return a new MusicManager instance with the same data as this one
     */
    @Override
    public MusicManager clone() {
        try {
            return new MusicManager(this);
        } catch (MusicException e) {
            return null; // Should never happen since this is a valid instance
        }
    }
}