/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.mvc;

import poo2025.common.Authenticator;
import poo2025.exceptions.AuthenticatorException;
import poo2025.entities.Album.Album;
import poo2025.exceptions.AlbumException;
import poo2025.entities.Music.Music;
import poo2025.exceptions.MusicException;
import poo2025.entities.PlaybackHistory;
import poo2025.entities.Playlist.Playlist;
import poo2025.exceptions.PlaylistException;
import poo2025.entities.Playlist.TimedPlaylist;
import poo2025.entities.SubscriptionPlan.PremiumTop;
import poo2025.entities.SubscriptionPlan.SubscriptionPlan;
import poo2025.entities.User.FreeUser;
import poo2025.entities.User.PremiumUser;
import poo2025.entities.User.User;
import poo2025.exceptions.UserException;
import poo2025.managers.AlbumManager;
import poo2025.managers.MusicManager;
import poo2025.managers.PlaylistManager;
import poo2025.managers.UserManager;

import java.io.Serializable;
import java.util.*;

/**
 * SpotifUM is the core model class of a music streaming application implementing the MVC pattern.
 * It provides comprehensive functionality for managing users, music, playlists, albums, and playback history.
 * The class serves as the central coordination point for all music streaming operations.
 *
 * <p>Key features include:
 * <ul>
 *     <li>User management and authentication</li>
 *     <li>Music library management</li>
 *     <li>Playlist creation and modification</li>
 *     <li>Album organization</li>
 *     <li>Playback history tracking</li>
 *     <li>Music recommendations</li>
 *     <li>Statistical queries and analytics</li>
 * </ul>
 *
 * @see UserManager
 * @see MusicManager
 * @see PlaylistManager
 * @see AlbumManager
 * @see Authenticator
 */
public class SpotifUM implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Manages user accounts and related operations */
    private UserManager userManager;

    /** Handles music track storage and operations */
    private MusicManager musicManager;

    /** Manages playlist creation and modifications */
    private PlaylistManager playlistManager;

    /** Handles album organization and operations */
    private AlbumManager albumManager;

    /** Stores playback history records */
    private List<PlaybackHistory> history;

    /** Handles user authentication */
    private Authenticator authenticator;

    /** Controls automatic saving functionality */
    private boolean autosave;

    // Constructors
    /**
     * Default constructor initializing all managers and components with default values.
     */
    public SpotifUM() {
        this.userManager = new UserManager();
        this.musicManager = new MusicManager();
        this.playlistManager = new PlaylistManager();
        this.albumManager = new AlbumManager();
        this.authenticator = new Authenticator();
        this.history = new ArrayList<>();
        this.autosave = false;
    }

    /**
     * Parameterized constructor initializing the system with provided components.
     *
     * @param userManager User management component
     * @param musicManager Music management component
     * @param playlistManager Playlist management component
     * @param albumManager Album management component
     * @param history List of playback history records
     * @param authenticator Authentication component
     * @param autosave Flag for automatic saving
     */
    public SpotifUM(UserManager userManager, MusicManager musicManager, PlaylistManager playlistManager,
                    AlbumManager albumManager, List<PlaybackHistory> history, Authenticator authenticator, boolean autosave) {
        this.userManager = userManager;
        this.musicManager = musicManager;
        this.playlistManager = playlistManager;
        this.albumManager = albumManager;
        this.history = cloneHistory(history);
        this.authenticator = authenticator;
        this.autosave = autosave;
    }

    /**
     * Copy constructor creating a deep copy of another SpotifUM instance.
     *
     * @param other The SpotifUM instance to copy
     */
    public SpotifUM(SpotifUM other) {
        this.userManager = new UserManager(other.getUserManager());
        try {
            this.musicManager = new MusicManager(other.getMusicManager());
        } catch (MusicException e) {
            this.musicManager = new MusicManager();
        }
        this.playlistManager = new PlaylistManager(other.getPlaylistManager());
        this.albumManager = new AlbumManager();
        try {
            if (other.getAlbumManager() != null) {
                this.albumManager = new AlbumManager(other.getAlbumManager());
            }
        } catch (AlbumException e) {
            this.albumManager = new AlbumManager();
        }
        this.history = cloneHistory(other.getHistory());
        this.authenticator = new Authenticator(other.getAuthenticator());
        this.autosave = other.getAutoSave();
    }

    /**
     * Creates a deep copy of a playback history list.
     *
     * @param original The original history list to clone
     * @return A new list containing copies of all history entries
     */
    private List<PlaybackHistory> cloneHistory(List<PlaybackHistory> original) {
        List<PlaybackHistory> cloned = new ArrayList<>();
        try {
            for (PlaybackHistory entry : original) {
                cloned.add(entry.clone());
            }
        } catch (Exception e) {
            // Ignore cloning errors
        }
        return cloned;
    }

    /**
     * Retrieves the UserManager instance associated with this system.
     *
     * @return the UserManager used to handle user-related operations.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Sets the UserManager instance for the SpotifUM system.
     *
     * @param userManager the UserManager instance to be associated with the system
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Retrieves the MusicManager instance associated with this system.
     *
     * @return the MusicManager used to handle music-related operations.
     */
    public MusicManager getMusicManager() {
        return musicManager;
    }

    /**
     * Sets the MusicManager instance for the SpotifUM system.
     *
     * @param musicManager the MusicManager instance to be associated with the system
     */
    public void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    /**
     * Retrieves the PlaylistManager instance associated with this system.
     *
     * @return the PlaylistManager used to handle playlist-related operations.
     */
    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    /**
     * Sets the PlaylistManager instance for the SpotifUM system.
     *
     * @param playlistManager the PlaylistManager instance to be associated with the system
     */
    public void setPlaylistManager(PlaylistManager playlistManager) {
        this.playlistManager = playlistManager;
    }

    /**
     * Retrieves the AlbumManager instance associated with this system.
     *
     * @return the AlbumManager used to handle album-related operations.
     */
    public AlbumManager getAlbumManager() {
        return albumManager;
    }

    /**
     * Sets the AlbumManager instance for the SpotifUM system.
     *
     * @param albumManager the AlbumManager instance to be associated with the system
     */
    public void setAlbumManager(AlbumManager albumManager) {
        this.albumManager = albumManager;
    }

    /**
     * Retrieves the playback history as a list of PlaybackHistory objects.
     *
     * @return A deep copy of the playback history, ensuring the original list remains unmodified.
     */
    public List<PlaybackHistory> getHistory() {
        return cloneHistory(history);
    }

    /**
     * Updates the playback history with a new list of records.
     *
     * @param history The list of PlaybackHistory objects to set as the new playback history.
     */
    public void setHistory(List<PlaybackHistory> history) {
        this.history = cloneHistory(history);
    }

    /**
     * Retrieves the Authenticator instance associated with this system.
     *
     * @return the Authenticator used for authentication purposes.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the Authenticator instance for the SpotifUM system.
     *
     * @param authenticator the Authenticator instance to be associated with the system
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Retrieves the current state of the auto-save setting.
     *
     * @return true if auto-save is enabled, false otherwise
     */
    public boolean getAutoSave() {
        return autosave;
    }

    /**
     * Sets the autosave feature of the application.
     *
     * @param autosave a boolean value representing whether the autosave feature
     *                 should be enabled (true) or disabled (false)
     */
    public void setAutosave(boolean autosave) {
        this.autosave = autosave;
    }

    // Entity Creation Methods
    /**
     * Creates a new user in the system with authentication credentials.
     *
     * @param user The user object to add
     * @param password The user's password for authentication
     * @return The user's identifier if successful, -2 if failed
     */
    public Integer createUser(User user, String password) {
        try {
            if (user == null) return -2;
            Integer id = user.getIdentifier();
            userManager.addUser(user);
            authenticator.addUser(id, password);
            return id;
        } catch (UserException | AuthenticatorException e) {
            return -2;
        }
    }

    /**
     * Creates a new music track and associates it with an album.
     *
     * @param music The music object to add
     * @param albumId The identifier of the album to associate with
     * @return The music identifier if successful, -2 if music is null or album doesn't exist,
     *         -1 if music can't be added to the album
     */
    public Integer createMusic(Music music, Integer albumId) {
        try {
            if (music == null || !albumExists(albumId))
                return -2;
            if (!albumManager.getAlbum(albumId).isMusicAllowedOnAlbum(music.getInterpreter()))
                return -1;
            Integer id = music.getIdentifier();
            musicManager.addMusic(music);
            musicManager.addMusicToAlbum(albumManager, albumId, id);
            return id;
        } catch (AlbumException | MusicException e) {
            return -1;
        }
    }

    /**
     * Verifies if a music track by a specific artist can be added to an album.
     *
     * @param artist The artist name
     * @param albumId The album identifier
     * @return true if the music can be added to the album, false otherwise
     */
    public boolean isMusicAllowedOnPlaylist(String artist, Integer albumId) {
        if (artist == null) return false;
        try {
            return albumManager.getAlbum(albumId).isMusicAllowedOnAlbum(artist);
        } catch (AlbumException e) {
            return false;
        }
    }

    /**
     * Creates a new playlist for a specific user.
     *
     * @param userId The identifier of the user creating the playlist
     * @param playlist The playlist object to create
     * @return The playlist identifier if successful, -2 if playlist is null, -1 if creation fails
     */
    public Integer createPlaylist(Integer userId, Playlist playlist) {
        try {
            if (playlist == null) return -2;
            playlist.setCreator(userId);
            Integer id = playlist.getIdentifier();
            playlistManager.addPlaylist(playlist);
            return id;
        } catch (PlaylistException e) {
            return -1;
        }
    }

    /**
     * Adds a playlist to the playlist manager.
     *
     * @param playlist The playlist to add
     */
    public void addPlaylistToManager(Playlist playlist) {
        try {
            this.playlistManager.addPlaylist(playlist);
        } catch (PlaylistException e) {
            // Silently handle exception
        }
    }

    /**
     * Creates a new album in the system.
     *
     * @param album The album object to be created
     * @return The album identifier if successful, -2 if album is null, -1 if creation fails
     */
    public Integer createAlbum(Album album) {
        try {
            if (album == null) {
                return -2;
            }
            Integer id = album.getIdentifier();
            albumManager.addAlbum(album);
            return id;
        } catch (AlbumException e) {
            return -1;
        }
    }

    /**
     * Adds an album to the album manager. If an AlbumException occurs during the process,
     * it is silently handled.
     *
     * @param album the Album object to be added to the album manager
     */
    public void addAlbumToManager(Album album) {
        try {
            this.albumManager.addAlbum(album);
        } catch (AlbumException e) {
            // Silently handle album exception
        }
    }

    /**
     * Deletes a user from the system by their unique identifier.
     *
     * @param userId the unique identifier of the user to be deleted
     * @return true if the user was successfully deleted, false otherwise
     */
    // Entity Deletion Methods
    public boolean deleteUser(Integer userId) {
        try {
            authenticator.removeUser(userId);
            return userManager.removeUser(userId);
        } catch (AuthenticatorException | UserException e) {
            return false;
        }
    }

    /**
     * Deletes a music entry identified by its unique ID.
     *
     * @param musicId the unique identifier of the music entry to be deleted
     * @return true if the music entry was successfully deleted, false otherwise
     */
    public boolean deleteMusic(Integer musicId) {
        try {
            return musicManager.removeMusic(musicId);
        } catch (MusicException e) {
            return false;
        }
    }

    /**
     * Deletes a playlist with the specified playlist ID.
     *
     * This method attempts to remove a playlist by interacting with the playlist manager.
     * If the playlist cannot be deleted due to an exception, the method returns false.
     *
     * @param playlistId the unique identifier of the playlist to be deleted
     * @return true if the playlist was successfully deleted, false otherwise
     */
    public boolean deletePlaylist(Integer playlistId) {
        try {
            return playlistManager.removePlaylist(playlistId, userManager);
        } catch (PlaylistException e) {
            return false;
        }
    }

    /**
     * Deletes an album identified by its ID, along with all associated songs.
     * If the album exists, all associated songs are removed, and the album itself
     * is deleted. If the album does not exist or an exception occurs, the method
     * returns false.
     *
     * @param albumId the unique identifier of the album to be deleted
     * @return true if the album and its associated songs were successfully deleted,
     *         false otherwise
     */
    public boolean deleteAlbum(Integer albumId) {
        try {
            Album album = albumManager.getAlbum(albumId);
            if (album != null) {
                for (Music music : album.getSongs()) {
                    try {
                        musicManager.removeMusic(music.getIdentifier());
                    } catch (Exception ignored) {
                    }
                }
                albumManager.removeAlbum(albumId, userManager);
                return true;
            }
            return false;
        } catch (AlbumException e) {
            return false;
        }
    }

    /**
     * Retrieves a string representation of the list of users.
     *
     * @return a string containing the details of all users managed by the system
     */
    // Entity Listing Methods
    public String listUsers() {
        return userManager.toString();
    }

    /**
     * Retrieves a string representation of the music list managed by the music manager.
     *
     * @return A string containing the details of all musics in the music manager.
     */
    public String listMusics() {
        return musicManager.toString();
    }

    /**
     * Retrieves a string representation of all playlists managed by the playlist manager.
     *
     * @return A string containing the details of all playlists.
     */
    public String listPlaylists() {
        return playlistManager.toString();
    }

    /**
     * Retrieves a list of all public playlists as a string representation.
     *
     * @return A string containing the list of all public playlists. If an error occurs,
     *         returns an error message indicating the issue.
     */
    public String listPublicPlaylists() {
        try {
            return playlistManager.getAllPublic().toString();
        } catch (PlaylistException e) {
            return "Error listing public playlists: " + e.getMessage();
        }
    }

    /**
     * Retrieves a string representation of the list of albums.
     *
     * @return a string containing details of all albums managed by the albumManager.
     */
    public String listAlbums() {
        return albumManager.toString();
    }

    /**
     * Processes the given query result and returns it as a string. In case the result
     * is null or an Integer with a value of 0, the provided emptyMessage is returned.
     *
     * @param result the query result to handle, can be of any type
     * @param emptyMessage the message to return if the result is null or an Integer with a value of 0
     * @return the string representation of the result or the emptyMessage if the result is null
     *         or an Integer with a value of 0
     */
    // Query Methods
    private <T> String handleQuery(T result, String emptyMessage) {
        if (result == null || (result instanceof Integer && ((Integer) result) == 0)) {
            return emptyMessage;
        }
        return result.toString();
    }

    /**
     * Executes a query to retrieve the most played music from the music system.
     * Handles any exceptions that occur during the query execution and returns
     * an appropriate message if the system contains no music or if an error occurs.
     *
     * @return A string representing the most played music if available,
     *         a message indicating no music in the system, or an error message.
     */
    public String runQueryOne() {
        try {
            return handleQuery(musicManager.getMostPlayed(), "There are no musics in the system.");
        } catch (MusicException e) {
            return "An error occurred while getting most played music";
        }
    }

    /**
     * Executes a query to fetch the most listened artist from the music manager.
     * If no music exists in the system, a default message is returned.
     * Handles exceptions that might occur during the query execution and returns
     * an appropriate error message in such cases.
     *
     * @return A string containing the most listened artist, a default message if no music exists,
     *         or an error message if an exception occurs.
     */
    public String runQueryTwo() {
        try {
            return handleQuery(musicManager.getMostListenedArtist(), "There are no musics in the system.");
        } catch (MusicException e) {
            return "An error occurred while getting most listened artist";
        }
    }

    /**
     * Executes a predefined query to retrieve the top user by listening time.
     * If no users exist in the system, an appropriate message is returned.
     *
     * @return A string representing the top user by listening time or a message indicating that no users are present.
     */
    public String runQueryThree() {
        return handleQuery(userManager.getTopUserByListeningTime(), "There are no users in the system.");
    }

    /**
     * Executes query four which retrieves the top user by points and formats
     * the result. If no users exist in the system, a default message is returned.
     *
     * @return A string containing the top user's information or a message indicating
     *         that no users are present in the system.
     */
    public String runQueryFour() {
        return handleQuery(userManager.getTopUserByPoints(), "There are no users in the system.");
    }

    /**
     * Executes a query to retrieve the top music genre from the system.
     * If no music exists in the system, a default message is returned.
     * In case of an exception, an error message is provided.
     *
     * @return A string representing the top music genre, a default message if empty
     *         or an error message in case of an exception.
     */
    public String runQueryFive() {
        try {
            return handleQuery(musicManager.getTopGenre(), "There are no musics in the system.");
        } catch (MusicException e) {
            return "An error occurred while getting top genre";
        }
    }

    /**
     * Executes a query to retrieve all public playlists and handles the results.
     * If no public playlists are available, a predefined message is returned.
     * If a PlaylistException occurs during the process, an error message is returned.
     *
     * @return A string representing the processed result of the query for public playlists.
     *         It could either be the data retrieved, a message indicating no playlists exist,
     *         or an error message in case of a failure.
     */
    public String runQuerySix() {
        try {
            return handleQuery(playlistManager.getAllPublic(), "There are no public playlists.");
        } catch (PlaylistException e) {
            return "Error retrieving public playlists: " + e.getMessage();
        }
    }

    /**
     * Executes query seven, which retrieves the top user based on playlist activity
     * and handles the corresponding operations. This method utilizes user and playlist
     * managers to determine the top user and returns the result of the query.
     *
     * @return A string containing the result of the query or an error message if an
     *         exception occurs during execution.
     */
    public String runQuerySeven() {
        try {
            return handleQuery(userManager.getUser(playlistManager.getTopUser()), "There are no playlists in the system");
        } catch (UserException e) {
            return "Error getting top user";
        }
    }

    /**
     * Retrieves and returns the profile information of a user based on their user ID.
     * The profile information content may vary depending on whether the user is a
     * PremiumUser or FreeUser.
     *
     * @param userId the unique identifier of the user whose profile is to be viewed.
     * @return the profile details of the user as a String if retrieval is successful.
     *         Returns "Some error occurred" if any exception is encountered.
     */
    // User Profile and Collection Methods
    public String viewUserProfile(Integer userId) {
        try {
            User user = userManager.getUser(userId);
            if (user instanceof PremiumUser) {
                return ((PremiumUser) user).viewMyProfile();
            } else if (user instanceof FreeUser) {
                return ((FreeUser) user).viewMyProfile();
            }
        } catch (UserException e) {
            // Handle error silently
        }
        return "Some error occurred";
    }

    /**
     * Provides a string representation of a user's collection based on the provided type
     * (e.g., playlists or albums). This method is only available for premium users.
     *
     * @param userId the unique identifier of the user whose collection is being accessed
     * @param type the type of collection to view (e.g., "playlists" or "albums")
     * @return a string representation of the requested collection if the user is premium,
     *         an informational message otherwise. Returns an error message in case of invalid
     *         input, unknown collection type, or unexpected exceptions.
     */
    public String viewUserCollection(Integer userId, String type) {
        try {
            User user = userManager.getUser(userId);
            if (user == null) return "Some error occurred.";

            if (user instanceof PremiumUser premiumUser) {
                List<?> collection = switch (type.toLowerCase()) {
                    case "playlists" -> premiumUser.getPlaylists();
                    case "albums" -> premiumUser.getAlbums();
                    default -> null;
                };

                if (collection == null) return "Unknown collection type requested.";
                if (collection.isEmpty()) return "No items in this collection.";

                StringBuilder sb = new StringBuilder().append(type).append(": ").append(collection.size()).append("\n");
                for (Object item : collection) sb.append(item.toString()).append("\n");
                return sb.toString();
            }
            return "This feature is VIP only. Become Premium and join the cool club 😎.";
        } catch (UserException e) {
            return "Error accessing user collection";
        }
    }

    /**
     * Plays the specified music for the given user and updates playback history, user points,
     * and music play count if applicable.
     *
     * @param userId the unique identifier of the user attempting to play the music
     * @param musicId the unique identifier of the music to be played
     * @return a String indicating the playback result; it could either be the result of playing the music
     *         or an error or failure message
     */
    // Music Playback Methods
    public String playMusic(Integer userId, Integer musicId) {
        try {
            User user = userManager.getUser(userId);
            Music music = musicManager.getMusic(musicId);
            if (user != null && music != null) {
                try {
                    PlaybackHistory h = new PlaybackHistory();
                    h.setUserId(userId);
                    h.setMusicId(musicId);
                    this.history.add(h);
                } catch (Exception e) {
                    // Handle PlaybackHistory error silently
                }
                userManager.addPlaysAndUpdatePoints(userId, 1);
                musicManager.addPlay(musicId, 1);
                return user.playMusic(music);
            }
            return "Can't play this music";
        } catch (UserException | MusicException e) {
            return "Error playing music: " + e.getMessage();
        }
    }

    /**
     * Plays a random music selection for the given user.
     * This method retrieves a random music ID and attempts to play it for the specified user.
     * If an error occurs during the playback process, it returns an error message.
     *
     * @param userId the unique identifier of the user for whom the random music is to be played
     * @return a confirmation message regarding the music playback or an error message if the operation fails
     */
    public String playRandomMusic(Integer userId) {
        try {
            return playMusic(userId, musicManager.getRandomMusicId());
        } catch (MusicException e) {
            return "Error playing random music: " + e.getMessage();
        }
    }

    /**
     * Adds a music track to a specified playlist.
     *
     * @param playlistId the unique identifier of the playlist to which the music should be added
     * @param musicId the unique identifier of the music track to add to the playlist
     * @return true if the music track was successfully added to the playlist, false otherwise
     */
    // Library Management Methods
    public boolean addMusicToPlaylist(Integer playlistId, Integer musicId) {
        try {
            return musicManager.addMusicToPlaylist(playlistManager, playlistId, musicId);
        } catch (MusicException | PlaylistException e) {
            return false;
        }
    }

    /**
     * Adds a music track to a specific album.
     *
     * @param albumId the unique identifier of the album to which the music will be added
     * @param musicId the unique identifier of the music track to be added
     * @return true if the music track was successfully added to the album, false otherwise
     */
    public boolean addMusicToAlbum(Integer albumId, Integer musicId) {
        try {
            return musicManager.addMusicToAlbum(albumManager, albumId, musicId);
        } catch (MusicException | AlbumException e) {
            return false;
        }
    }

    /**
     * Adds a playlist to the library of a premium user.
     *
     * @param userId the unique identifier of the user
     * @param playlistId the unique identifier of the playlist to be added
     * @return true if the playlist was successfully added to the user's library, false otherwise
     */
    public boolean addPlaylistToPremiumUserLibrary(Integer userId, Integer playlistId) {
        try {
            User user = userManager.getUser(userId);
            if (!(user instanceof PremiumUser premiumUser)) return false;

            Playlist playlist = playlistManager.getPlaylist(playlistId);
            if (playlist == null) return false;

            premiumUser.addPlaylistToUser(playlist);
            userManager.updateUser(premiumUser);
            return true;
        } catch (UserException | PlaylistException e) {
            return false;
        }
    }

    /**
     * Adds an album to the library of a premium user.
     *
     * @param userId  The unique identifier of the user. This user must be a premium user.
     * @param albumId The unique identifier of the album to be added to the user's library.
     * @return {@code true} if the album was successfully added to the premium user's library;
     *         {@code false} if the user is not a premium user, the album does not exist,
     *         or an exception occurs during the process.
     */
    public boolean addAlbumToPremiumUserLibrary(Integer userId, Integer albumId) {
        try {
            User user = userManager.getUser(userId);
            if (!(user instanceof PremiumUser premiumUser)) return false;

            Album album = albumManager.getAlbum(albumId);
            if (album == null) return false;

            premiumUser.addAlbumToUser(album);
            userManager.updateUser(premiumUser);
            return true;
        } catch (AlbumException | UserException e) {
            return false;
        }
    }

    /**
     * Generates a playlist with a total duration less than or equal to the specified maximum time and filtered by genre.
     *
     * @param maxTime the maximum allowable duration (in seconds) for the playlist
     * @param genre the genre of music to include in the playlist
     * @return a {@code Playlist} object matching the specified criteria, or {@code null} if an error occurs during generation
     */
    public Playlist generateTimedPlaylist(int maxTime, String genre) {
        try {
            return musicManager.generateTimedPlaylist(this.authenticator.getLoggedInUserId(), maxTime, genre, playlistManager.getTotalPlaylists());
        } catch (PlaylistException e) {
            return null;
        }
    }

    /**
     * Generates a playlist for a user based on their preferences and input parameters.
     *
     * @param userId        The unique identifier of the user for whom the playlist is to be generated.
     * @param maxMusics     The maximum number of songs to be included in the generated playlist.
     * @param explicit      A flag indicating whether explicit content is allowed in the playlist.
     * @param maxTime       The maximum duration of the playlist in seconds. A value of -1 indicates no time limit.
     * @param genre         The music genre to filter recommendations by.
     * @param namePrefix    A prefix to be used for the playlist name.
     * @return A message indicating the result of the playlist generation process,
     *                      or an error message if playlist generation fails.
     */
    private String generatePlaylist(Integer userId, int maxMusics, boolean explicit, int maxTime, String genre, String namePrefix) {
        try {
            List<Music> recommendedMusic = musicManager.recommendByGenreAndArtist(userId, history, maxMusics, explicit, maxTime, genre);

            if (!recommendedMusic.isEmpty()) {
                if (maxTime == -1) {
                    Playlist recommended = new Playlist();
                    recommended.setCreator(userId);
                    recommended.setPublic(false);
                    recommended.setSongs(recommendedMusic);
                    recommended.setName(namePrefix + recommended.getIdentifier());

                    Integer playlistId = createPlaylist(userId, recommended);
                    addPlaylistToPremiumUserLibrary(userId, playlistId);
                    return "Your exciting new " + (explicit ? "explicit " : "") + "recommended playlist has been created. Check it out in your playlist library!";
                } else {
                    TimedPlaylist recommended = new TimedPlaylist(maxTime, genre);
                    recommended.setCreator(userId);
                    recommended.setPublic(false);
                    recommended.setSongs(recommendedMusic);
                    recommended.setName(namePrefix + recommended.getIdentifier());
                    recommended.calcAndUpdateCurrentTime();

                    Integer playlistId = createPlaylist(userId, recommended);
                    addPlaylistToPremiumUserLibrary(userId, playlistId);
                    return "Your exciting new timed recommended playlist has been created. Check it out in your playlist library!";
                }
            }
            return "No recommendations available.";
        } catch (Exception e) {
            return "Error generating playlist: " + e.getMessage();
        }
    }

    /**
     * Generates a recommended playlist for a user based on their preferences or listening history.
     *
     * @param userId the unique identifier of the user for whom the playlist is being generated
     * @param maxMusics the maximum number of songs to include in the playlist
     * @return a string representation of the recommended playlist
     */
    public String generateRecommendedPlaylist(Integer userId, int maxMusics) {
        return generatePlaylist(userId, maxMusics, false, -1, null, "normalR");
    }

    /**
     * Generates a recommended explicit playlist for a given user.
     * The method creates a playlist containing music tracks marked as explicit
     * based on the user's preferences or listening patterns.
     *
     * @param userId the unique identifier of the user for whom the playlist is generated
     * @param maxMusics the maximum number of music tracks to include in the playlist
     * @return a String representation of the generated explicit playlist
     */
    public String generateRecommendedExplicitPlaylist(Integer userId, int maxMusics) {
        return generatePlaylist(userId, maxMusics, true, -1, null, "explicitR");
    }

    /**
     * Generates a recommended timed playlist based on the user's preferences, maximum allowed time, and specified genre.
     *
     * @param userId the unique identifier of the user for whom the playlist is being generated
     * @param maxTime the maximum duration of the playlist in minutes
     * @param genre the genre of music to include in the playlist
     * @return a string representation of the recommended timed playlist
     */
    public String generateRecommendedTimedPlaylist(Integer userId, int maxTime, String genre) {
        return generatePlaylist(userId, Integer.MAX_VALUE, true, maxTime, genre, "timedR");
    }

    /**
     * Updates the points for all users in the system.
     *
     * This method delegates the points update functionality to the userManager.
     * It ensures that the points for every user are recalculated and updated
     * according to the defined logic.
     */
    // User Points Management
    public void updatePointsForAllUsers() {
        userManager.updatePointsForAllUsers();
    }

    /**
     * Updates the points for a specific user identified by their user ID.
     * This method interacts with the user management system to update points.
     * Any exceptions occurring during the process are caught and handled silently.
     *
     * @param userId the unique identifier of the user whose points will be updated
     */
    public void updatePointsForUser(Integer userId) {
        try {
            userManager.updatePointsForUser(userId);
        } catch (UserException e) {
            // Silently handle error
        }
    }

    /**
     * Determines whether the user with the specified user ID has a premium membership status.
     *
     * @param userId the unique identifier of the user whose premium status is to be checked
     * @return {@code true} if the user is a premium user; {@code false} otherwise or in case of an exception
     */
    public boolean isPremiumUser(Integer userId) {
        try {
            return userManager.isPremiumUser(userId);
        } catch (UserException e) {
            return false;
        }
    }

    /**
     * Determines if the user associated with the given userId has a PremiumTop plan.
     *
     * @param userId the unique identifier of the user to check
     * @return true if the user is a PremiumUser with a PremiumTop plan, false otherwise
     */
    public boolean isPremiumTop(Integer userId) {
        try {
            User user = userManager.getUser(userId);
            return user instanceof PremiumUser premiumUser && premiumUser.getPlan() instanceof PremiumTop;
        } catch (UserException e) {
            return false;
        }
    }

    /**
     * Modifies the user details and updates the user record in the system.
     * The method allows updating user attributes such as name, email, address, age,
     * and subscription plan. If the user is a FreeUser and a new subscription plan is provided,
     * the user is upgraded to a PremiumUser.
     *
     * @param userId the unique identifier of the user to modify
     * @param name the new name for the user; set to null or an empty string to retain the current value
     * @param email the new email address for the user; set to null or an empty string to retain the current value
     * @param address the new address for the user; set to null or an empty string to retain the current value
     * @param age the new age for the user; set to 0 to retain the current value
     * @param newPlan the new subscription plan for the user; set to null to retain the current plan
     * @return true if the user is successfully modified and updated, false otherwise
     */
    // Entity Modification Methods
    public boolean modifyUser(Integer userId, String name, String email, String address, int age, SubscriptionPlan newPlan) {
        try {
            User user = userManager.getUser(userId);
            if (user == null) return false;

            if (name != null && !name.isEmpty()) user.setName(name);
            if (email != null && !email.isEmpty()) user.setEmail(email);
            if (address != null && !address.isEmpty()) user.setAddress(address);
            if (age != 0) user.setAge(age);

            if (newPlan != null && user instanceof PremiumUser) {
                ((PremiumUser) user).setPlan(newPlan);
            } else if (newPlan != null && user instanceof FreeUser) {
                user = new PremiumUser(user, newPlan);
            }

            userManager.updateUser(user);
            return true;
        } catch (UserException e) {
            return false;
        }
    }

    /**
     * Modifies the details of a music entry in the music manager. Updates attributes such
     * as title, artist, publisher, genre, and duration for the specified music ID.
     *
     * @param musicId  the unique identifier of the music to modify
     * @param title    the new title of the music; ignored if empty
     * @param lyrics   the new lyrics of the music; ignored if empty
     * @param publisher the new publisher of the music; ignored if empty
     * @param genre    the new genre of the music; ignored if empty
     * @param duration the new duration of the music in seconds; ignored if 0
     * @return true if the modification was successful, false otherwise
     */
    public boolean modifyMusic(Integer musicId, String title, String lyrics, String publisher, String genre, int duration) {
        try {
            musicManager.updateMusic(musicId, title, lyrics, publisher, genre, duration);
            return true;
        } catch (MusicException e) {
            return false;
        }
    }

    /**
     * Modifies the properties of an explicit music track based on the provided parameters.
     * This method updates the age restriction and the rating source of the specific explicit music
     * identified by the given music ID.
     *
     * @param musicId the unique identifier of the music track to be modified
     * @param ageRestriction the age restriction to be set; if value is -1, age restriction is
     *                       automatically detected and assigned; if 0, no age restriction is updated
     * @param ratingSource the source of the rating information; an empty string means no update
     */
    public void modifyExplicitMusic(Integer musicId, int ageRestriction, String ratingSource) {
        try {
            musicManager.updateExplicitFields(musicId, ageRestriction, ratingSource);
        } catch (MusicException e) {
            // Silently handle exceptions
        }
    }

    /**
     * Modifies the properties of a multimedia music object. Updates the video URL,
     * resolution, and subtitles availability if applicable.
     *
     * @param musicId   the unique identifier of the music to be modified
     * @param videoUrl  the new video URL to set; ignored if empty
     * @param resolution the new resolution to set; ignored if empty
     * @param subtitles  a boolean indicating whether subtitles are available
     */
    public void modifyMultimediaMusic(Integer musicId, String videoUrl, String resolution, boolean subtitles) {
        try {
            musicManager.updateMultimediaFields(musicId, videoUrl, resolution, subtitles);
        } catch (MusicException e) {
            // Silently handle exceptions
        }
    }

    /**
     * Modifies the details of an existing playlist identified by its ID.
     * Updates the playlist's name and privacy settings if applicable.
     *
     * @param playlistId the unique identifier of the playlist to modify
     * @param name the new name for the playlist; ignored if empty
     * @param privacy the new privacy setting for the playlist
     * @return true if the playlist was successfully modified, false if an error occurred
     */
    public boolean modifyPlaylist(Integer playlistId, String name, boolean privacy) {
        try {
            playlistManager.updatePlaylist(playlistId, name, privacy);
            return true;
        } catch (PlaylistException e) {
            return false;
        }
    }

    /**
     * Modifies the details of an existing album by updating its name.
     *
     * @param albumId the unique identifier of the album to be modified
     * @param name the new name to be assigned to the album
     * @return true if the album was successfully modified, false otherwise
     */
    public boolean modifyAlbum(Integer albumId, String name) {
        try {
            albumManager.updateAlbum(albumId, name);
            return true;
        } catch (AlbumException e) {
            return false;
        }
    }

    /**
     * Retrieves the current sound ID from a playlist at the specified position.
     *
     * @param playlistId the unique identifier of the playlist
     * @param pos the position of the sound in the playlist
     * @return the ID of the current sound at the given position, or null if an exception occurs
     */
    // Current Sound Methods
    public Integer getCurrentPlaylistSoundId(Integer playlistId, int pos) {
        try {
            Playlist playlist = playlistManager.getPlaylist(playlistId);
            return playlist.getCurrentSoundId(pos);
        } catch (PlaylistException e) {
            return null;
        }
    }

    /**
     * Retrieves the current sound ID for a specific position in the album.
     *
     * @param albumId the unique identifier of the album
     * @param pos the position of the sound in the album
     * @return the sound ID at the specified position in the album,
     *         or null if the album does not exist or an error occurs
     */
    public Integer getCurrentAlbumSoundId(Integer albumId, int pos) {
        try {
            Album album = albumManager.getAlbum(albumId);
            return album != null ? album.getCurrentSoundId(pos) : null;
        } catch (AlbumException e) {
            return null;
        }
    }

    /**
     * Retrieves the type of music as a string based on the provided music ID.
     *
     * @param musicId the unique identifier of the music
     * @return the class name of the music type if found, or null if the music is not found or an exception occurs
     */
    public String getMusicType(Integer musicId) {
        try {
            Music music = musicManager.getMusic(musicId);
            return music != null ? music.getClass().getSimpleName() : null;
        } catch (MusicException e) {
            return null;
        }
    }

    /**
     * Checks if a user exists based on the given user ID.
     *
     * @param userId the unique identifier of the user to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(Integer userId) {
        return userManager.existsUser(userId);
    }

    /**
     * Checks whether the music with the given ID exists in the system.
     *
     * @param musicId the ID of the music to be checked
     * @return true if the music exists, false otherwise
     */
    public boolean musicExists(Integer musicId) {
        try {
            return musicManager.existsMusic(musicId);
        } catch (MusicException e) {
            return false;
        }
    }

    /**
     * Checks whether a playlist with the given ID exists.
     *
     * @param playlistId the unique identifier of the playlist to check
     * @return true if the playlist exists, false if it does not exist or if an error occurs
     */
    public boolean playlistExists(Integer playlistId) {
        try {
            return playlistManager.existsPlaylist(playlistId);
        } catch (PlaylistException e) {
            return false;
        }
    }

    /**
     * Checks if an album exists in the album manager with the specified album ID.
     *
     * @param albumId the unique identifier of the album to check for existence
     * @return true if the album exists, false otherwise
     */
    public boolean albumExists(Integer albumId) {
        try {
            return albumManager.existsAlbum(albumId);
        } catch (AlbumException e) {
            return false;
        }
    }

    /**
     * Retrieves the global playback history, listing all users who played music,
     * the music name, and the playback time. The method iterates through the stored
     * playback history and fetches associated user and music information.
     *
     * @return A string representation of the global playback history where each
     *         entry includes the user name, the music played, and the playback time.
     *         If any information cannot be retrieved due to exceptions, that entry
     *         will be ignored.
     */
    public String getGlobalHistory() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Global Playback History ===\n");
        for (PlaybackHistory entry : this.history) {
            try {
                User user = userManager.getUser(entry.getUserId());
                Music music = musicManager.getMusic(entry.getMusicId());
                if (user != null && music != null) {
                    sb.append(String.format("%s played '%s' at %s%n",
                            user.getName(),
                            music.getName(),
                            entry.getPlaybackDate()));
                }
            } catch (UserException | MusicException ignored) {
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves the playback history for a specific user based on their user ID.
     *
     * @param userId the unique identifier of the user whose playback history is being retrieved
     * @return a string containing the playback history for the user, or an appropriate message if the user is not found or an error occurs during retrieval
     */
    public String getUserHistory(Integer userId) {
        if (!userExists(userId)) return "User not found";

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Playback History for User ===\n");

        try {
            for (PlaybackHistory entry : this.history) {
                if (entry.getUserId().equals(userId)) {
                    Music music = musicManager.getMusic(entry.getMusicId());
                    if (music != null) {
                        sb.append(String.format("Played '%s' at %s%n",
                                music.getName(),
                                entry.getPlaybackDate()));
                    }
                }
            }
        } catch (MusicException e) {
            return "Error retrieving user history";
        }

        return sb.toString();
    }

    /**
     * Determines whether a playlist is public based on the given playlist ID.
     *
     * @param playlistId the unique identifier of the playlist to be checked
     * @return {@code true} if the playlist is public, {@code false} otherwise
     */
    public boolean isPublicPlaylist(Integer playlistId) {
        try {
            return playlistManager.isPlaylistPublic(playlistId);
        } catch (PlaylistException e) {
            return false;
        }
    }

    /**
     * Determines if a playlist belongs to a specific user.
     *
     * @param playlistId the ID of the playlist to check
     * @param userId the ID of the user to verify ownership
     * @return {@code true} if the user owns the playlist, {@code false} otherwise
     */
    public boolean isUserPlaylist(Integer playlistId, Integer userId) {
        try {
            return this.playlistManager.isUserOwnPlaylist(playlistId, userId);
        } catch (PlaylistException e) {
            return false;
        }
    }

    /**
     * Checks whether the albums list is empty.
     *
     * @return true if the album manager is null or the total number of albums is zero; false otherwise.
     */
    public boolean isAlbumsEmpty() {
        return this.albumManager == null || this.albumManager.getTotalAlbums() == 0;
    }

    // Object Methods
    /**
     * Creates and returns a deep copy of this SpotifUM instance.
     *
     * @return A new SpotifUM instance with the same data
     */
    @Override
    public SpotifUM clone() {
        return new SpotifUM(this);
    }

    /**
     * Compares this SpotifUM instance with another object for equality.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SpotifUM other = (SpotifUM) o;
        return Objects.equals(userManager, other.userManager) &&
                Objects.equals(musicManager, other.musicManager) &&
                Objects.equals(playlistManager, other.playlistManager) &&
                Objects.equals(history, other.getHistory()) &&
                Objects.equals(authenticator, other.authenticator) &&
                Objects.equals(autosave, other.autosave);
    }

    /**
     * Generates a hash code for this SpotifUM instance.
     *
     * @return A hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(userManager, musicManager, playlistManager, history, authenticator, autosave);
    }

    /**
     * Returns a string representation of this SpotifUM instance.
     *
     * @return A string containing the main components' information
     */
    @Override
    public String toString() {
        return "SpotifUM{" +
                "userManager=" + userManager +
                ", musicManager=" + musicManager +
                ", playlistManager=" + playlistManager +
                ", historyManager=" + history +
                ", authenticator=" + authenticator +
                ", autosave=" + autosave +
                '}';
    }
}