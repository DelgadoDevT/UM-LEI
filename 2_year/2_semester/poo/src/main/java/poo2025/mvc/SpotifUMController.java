/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.mvc;

import poo2025.common.Menu;
import poo2025.exceptions.AuthenticatorException;
import poo2025.common.Persistence;
import poo2025.entities.Album.Album;
import poo2025.entities.Music.ExplicitMusic;
import poo2025.entities.Music.MultimediaMusic;
import poo2025.entities.Music.Music;
import poo2025.entities.Playlist.Playlist;
import poo2025.entities.SubscriptionPlan.PremiumBase;
import poo2025.entities.SubscriptionPlan.PremiumTop;
import poo2025.entities.SubscriptionPlan.SubscriptionPlan;
import poo2025.entities.User.FreeUser;
import poo2025.entities.User.PremiumUser;
import poo2025.entities.User.User;

import java.util.Objects;
import java.util.function.Function;

/**
 * The SpotifUMController class manages user interactions and operations
 * related to the SpotifUM music service. It provides functionalities for
 * user profile management, music playback, collection handling, and more,
 * based on the user's subscription type and preferences.
 */
public class SpotifUMController {
    /**
     * Represents an instance of the SpotifUM class, which may encapsulate
     * functionalities related to managing or interacting with a music or
     * media streaming service.
     */
    private SpotifUM spotifum;

    /**
     * Constructor for the SpotifUMController class.
     * Initializes a new instance of the SpotifUMController by creating
     * a new instance of the SpotifUM class.
     */
    public SpotifUMController() {
        this.spotifum = new SpotifUM();
    }

    /**
     * Constructor for the SpotifUMController class.
     *
     * @param s the SpotifUM object to be used by the controller
     */
    public SpotifUMController(SpotifUM s) {
        this.spotifum = s;
    }

    /**
     * Constructs a new SpotifUMController based on an existing SpotifUMController instance.
     *
     * @param sc the existing SpotifUMController instance from which to copy data
     */
    public SpotifUMController(SpotifUMController sc) {
        this.spotifum = sc.getSpotifum();
    }

    /**
     * Retrieves the SpotifUM instance.
     *
     * @return the SpotifUM instance associated with this object
     */
    public SpotifUM getSpotifum() {
        return spotifum;
    }

    /**
     * Sets the SpotifUM instance for this object.
     *
     * @param spotifum the SpotifUM instance to be set
     */
    public void setSpotifum(SpotifUM spotifum) {
        this.spotifum = spotifum;
    }

    /**
     * Creates and returns a copy of this SpotifUMController instance.
     *
     * @return a new SpotifUMController object that is a copy of this instance.
     */
    @Override
    public SpotifUMController clone() {
        return new SpotifUMController(this);
    }

    /**
     * Returns a string representation of the SpotifUMController object,
     * including the string representation of the spotifum attribute.
     *
     * @return a string representation of the SpotifUMController instance
     */
    @Override
    public String toString() {
        return "poo2025.mvc.SpotifUMController{spotifum=" + spotifum.toString() + '}';
    }

    /**
     * Compares this object with the specified object for equality.
     *
     * @param o the object to be compared for equality with this instance
     *
     * @return true if the specified object is equal to this instance; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SpotifUMController that = (SpotifUMController) o;
        return Objects.equals(spotifum, that.getSpotifum());
    }

    /**
     * Returns the hash code value for this object.
     * The hash code is computed using the object's property values to ensure consistent hashing.
     *
     * @return the hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(spotifum);
    }

    /**
     * Handles the action of adding a public playlist to a user's library. This feature
     * is only available for premium users. If the user is not a premium user, a message
     * encouraging upgrading to premium will be returned.
     *
     * @param userId the unique identifier of the user attempting to add a playlist to their library
     *
     * @return a string message indicating the success or failure of the action, or a message
     *         indicating that this feature is restricted to premium users
     */
    public String handleAddPublicPlaylistToLibrary(Integer userId) {
        String res = "This feature is VIP only. Become Premium and join the cool club 😎.";
        if (spotifum.isPremiumUser(userId)) {
            Integer playlistId = Menu.getIntWithMessage("Enter the playlist id to add to library: ");
            if (spotifum.isPublicPlaylist(playlistId))
                res = spotifum.addPlaylistToPremiumUserLibrary(userId, playlistId) ?
                        "Playlist added with success" :
                        "Playlist insertion failed";
        }
        return res;
    }

    /**
     * Handles the generation of a recommended playlist for a user based on their plan and preferences.
     *
     * @param userId the unique identifier of the user for whom the recommended playlist is requested
     *
     * @return a description or link to the generated playlist, or an appropriate message if the user is not eligible
     */
    public String handleRecommendedPlaylist(Integer userId) {
        if (!spotifum.userExists(userId)) return "User not found";
        if (!spotifum.isPremiumUser(userId)) return "This feature is VIP only. Become Premium and join the cool club 😎.";
        if (!spotifum.isPremiumTop(userId)) return "This feature is PremiumTop plan only";

        String playlistType;
        do {
            playlistType = Menu.getStringWithMessage("Enter recommended playlist type (simple/timed/explicit): ");
        } while (!playlistType.equalsIgnoreCase("simple") && !playlistType.equalsIgnoreCase("timed") && !playlistType.equalsIgnoreCase("explicit"));

        if (playlistType.equalsIgnoreCase("simple")) {
            int maxMusics = Menu.getIntWithMessage("How many musics: ");
            return spotifum.generateRecommendedPlaylist(userId, maxMusics);
        } else if (playlistType.equalsIgnoreCase("timed")) {
            int maxTime = Menu.getIntWithMessage("Set playlist max time: ");
            String genre = Menu.getStringWithMessage("Enter the playlist genre: ");
            return spotifum.generateRecommendedTimedPlaylist(userId, maxTime, genre);
        } else {
            int maxMusics = Menu.getIntWithMessage("How many musics: ");
            return spotifum.generateRecommendedExplicitPlaylist(userId, maxMusics);
        }
    }

    /**
     * Handles the user profile operations by updating points for the specified user
     * and returning the updated user profile information.
     *
     * @param userId the unique identifier of the user whose profile is being handled
     *
     * @return a String representation of the user's profile after updating points
     */
    public String handleUserProfile(Integer userId) {
        spotifum.updatePointsForUser(userId);
        return spotifum.viewUserProfile(userId);
    }

    /**
     * Handles music playback for a user based on their subscription type.
     * If the user is a premium user, it provides premium playback options,
     * otherwise, it plays a random music track.
     *
     * @param userId the unique identifier of the user requesting music playback
     *
     * @return a String indicating the status or result of the music playback operation
     */
    public String handleMusicPlayback(Integer userId) {
        if (spotifum.isPremiumUser(userId)) {
            return handlePremiumPlayback(userId);
        }
        return spotifum.playRandomMusic(userId);
    }

    /**
     * Handles the playback functionality for premium users based on their selected source.
     * The user can choose to play music by its ID, from an album, or from a playlist.
     *
     * @param userId the unique identifier of the user requesting playback
     *
     * @return a string indicating the playback status or result
     */
    private String handlePremiumPlayback(Integer userId) {
        String playFrom = Menu.getPlaybackSource("playlist/album/id");

        if (playFrom.equalsIgnoreCase("id")) {
            Integer musicId = Menu.getIntWithMessage("Enter the id of the music that you want to listen: ");
            return spotifum.playMusic(userId, musicId);
        } else if (playFrom.equalsIgnoreCase("album"))
            return handleCollectionPlayback(userId, false);
        else
            return handleCollectionPlayback(userId, true);
    }

    /**
     * Handles the playback of a collection (playlist or album) by prompting the user
     * to enter the collection ID and initiating playback for the specified collection type.
     *
     * @param userId the ID of the user initiating the playback
     * @param isPlaylist a flag indicating whether the collection is a playlist (true) or an album (false)
     *
     * @return a String indicating the result or status of the playback process
     */
    private String handleCollectionPlayback(Integer userId, boolean isPlaylist) {
        Integer collectionId = Menu.getIntWithMessage("Enter " + (isPlaylist ? "playlist" : "album") + " id: ");
        return playFromCollection(userId, collectionId, isPlaylist);
    }

    /**
     * Plays music from a collection (playlist or album) for a specified user.
     * The method validates the existence and accessibility of the user and collection
     * before allowing playback. Users can navigate between tracks using specific controls.
     *
     * @param userId the ID of the user attempting to play the collection
     * @param collectionId the ID of the collection to be played; could be a playlist or an album
     * @param isPlaylist a flag indicating whether the collection is a playlist (true) or an album (false)
     *
     * @return a string containing the final status or result of the playback operation (e.g., last played track message or error message)
     */
    private String playFromCollection(Integer userId, Integer collectionId, boolean isPlaylist) {
        if (!spotifum.userExists(userId))
            return "Invalid collection or user";

        if (isPlaylist) {
            if (!spotifum.playlistExists(collectionId) || (!spotifum.isPublicPlaylist(collectionId) && !spotifum.isUserPlaylist(collectionId, userId)))
                return "Invalid collection or user";
        } else {
            if (!spotifum.albumExists(collectionId))
                return "Invalid collection or user";
        }

        int position = 0;
        String control;
        String result = "";

        do {
            Integer musicId = isPlaylist
                    ? spotifum.getCurrentPlaylistSoundId(collectionId, position)
                    : spotifum.getCurrentAlbumSoundId(collectionId, position);

            if (musicId == null) break;

            result = spotifum.playMusic(userId, musicId);
            System.out.println(result);

            control = Menu.getStringWithMessage("Enter control (next/prev/exit): ");
            if (control.equalsIgnoreCase("prev")) {
                position = Math.max(0, position - 1);
            } else if (control.equalsIgnoreCase("next")) {
                position++;
            } else {
                control = "exit";
            }
        } while (!control.equalsIgnoreCase("exit"));

        return result;
    }

    /**
     * Handles the creation of a playlist for a given user.
     * If the user is a premium user, a new playlist is created and added to the user's library.
     * If the user is not a premium user, a message is returned indicating that the feature is only available to premium users.
     *
     * @param userId the unique identifier of the user attempting to create a playlist
     *
     * @return a message indicating the result of the playlist creation process
     */
    public String handlePlaylistCreation(Integer userId) {
        String res = "This feature is VIP only. Become Premium and join the cool club 😎.";
        if (spotifum.isPremiumUser(userId)) {
            Playlist pl = createPlaylist();
            Integer id = spotifum.createPlaylist(userId, pl);
            spotifum.addPlaylistToPremiumUserLibrary(userId, pl.getIdentifier());
            res = "Playlist with id " + id + " added";
        }
        return res;
    }

    /**
     * Handles the addition of musics to a user's playlist. This feature is available
     * only to premium users. If the user is a premium user and the playlist is either
     * public or belongs to the user, the user is prompted to add musics to the specified playlist.
     *
     * @param userId the ID of the user attempting to add musics to a playlist
     *
     * @return a message indicating the result of the operation. If the user is not a premium user,
     *         a message encouraging them to upgrade is returned. If the operation succeeds or fails,
     *         respective messages are provided.
     */
    public String handleAddMusicsToPlaylist(Integer userId) {
        String res = "This feature is VIP only. Become Premium and join the cool club 😎.";
        if (spotifum.isPremiumUser(userId)) {
            Integer playlistId = Menu.getIntWithMessage("Enter the playlist id to add musics: ");
            if (spotifum.isPublicPlaylist(playlistId) || spotifum.isUserPlaylist(playlistId, userId)) {
                addMusicsToPlaylistById(playlistId);
                res = "Musics added to playlist";
            } else {
                res = "Playlist is private or does not exist, music addition failed";
            }
        }
        return res;
    }

    /**
     * Handles the addition of an album to the user's library. If the user is a premium user,
     * they can add an album to their library by providing the album ID. Otherwise, the method
     * returns a message prompting the user to upgrade to a premium plan.
     *
     * @param userId the unique identifier of the user attempting to add the album to their library
     *
     * @return a message indicating whether the album was successfully added to the library
     *         or why the action was not completed
     */
    public String handleAddAlbumToLibrary(Integer userId) {
        String res = "This feature is VIP only. Become Premium and join the cool club 😎.";
        if (spotifum.isPremiumUser(userId)) {
            Integer albumId = Menu.getIntWithMessage("Enter the album id to add to library: ");
            res = spotifum.addAlbumToPremiumUserLibrary(userId, albumId) ?
                    "Album added with success" :
                    "Album insertion failed";
        }
        return res;
    }

    /**
     * Executes an operation on an entity based on the provided operation code and entity type.
     *
     * @param operation the operation code determining the action to perform (e.g., add, modify, remove, list).
     *                  Valid values are:
     *                  1 - Add entity
     *                  2 - Modify entity
     *                  3 - Remove entity
     *                  4 - List entities
     *                  0 - Return operation
     *                  Other values result in an "Invalid option" message.
     * @param type the type of entity on which the operation is performed. The meaning of this parameter
     *             depends on the specific implementation of entity handling.
     *
     * @return a string indicating the result of the operation, such as success messages, a list of entities,
     *         or an error message for invalid operations.
     */
    public String entityOperations(int operation, int type) {
        return switch (operation) {
            case 1 -> this.addEntity(type);
            case 2 -> this.modifyEntity(type);
            case 3 -> this.removeEntity(type);
            case 4 -> this.listEntities(type);
            case 0 -> "Returning";
            default -> "Invalid option.";
        };
    }

    /**
     * Creates and returns a new User object based on user input. The user can be created
     * as either a FreeUser or PremiumUser depending on the provided account type.
     *
     * @return a User object, either FreeUser or PremiumUser, with the specified attributes
     * and configurations entered by the user.
     */
    private User createUser() {
        String name = Menu.getStringWithMessage("Enter user name: ");
        int age = Menu.getIntWithMessage("Enter user age: ");
        String email = Menu.getStringWithMessage("Enter your email: ");
        String address = Menu.getStringWithMessage("Enter user address: ");

        String userType;
        do {
            userType = Menu.getStringWithMessage("Enter account type (Free/Premium): ");
        } while (!userType.equalsIgnoreCase("premium") && !userType.equalsIgnoreCase("free"));

        if (userType.equalsIgnoreCase("free")) {
            return new FreeUser(name, age, email, address, 0, 0);
        }

        PremiumUser user = new PremiumUser();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setAddress(address);
        user.setPlan(createSubscriptionPlan());

        return user;
    }

    /**
     * Creates a subscription plan based on the user's selected plan type.
     * It retrieves the input from the user using the getPlanTypeInput method
     * and returns the corresponding subscription plan instance.
     *
     * @return a SubscriptionPlan instance, either PremiumBase or PremiumTop,
     *         depending on the user's selection.
     */
    private SubscriptionPlan createSubscriptionPlan() {
        String plan = Menu.getPlanTypeInput();
        return plan.equalsIgnoreCase("base") ? new PremiumBase() : new PremiumTop();
    }

    /**
     * Handles the addition of playlists and albums to the library for premium users.
     * Prompts the user to confirm actions before proceeding.
     *
     * @param userId the unique identifier of the premium user for whom the collections are being handled
     */
    private void handlePremiumUserCollections(Integer userId) {
        if (Menu.addYesNoInput("Do you want to add a playlist to library").equalsIgnoreCase("yes")) {
            addUserPlaylists(userId);
        }

        if (Menu.addYesNoInput("Do you want to add album to library").equalsIgnoreCase("yes")) {
            addUserAlbums(userId);
        }
    }

    /**
     * Adds playlists to the user's library by prompting the user to enter playlist IDs.
     * The method continues to request playlist IDs until the user inputs -1.
     * If a valid playlist ID is entered, the playlist is added to the user's library.
     * If the playlist ID is invalid, an error message is displayed.
     *
     * @param userid the ID of the user to whom playlists are being added
     */
    private void addUserPlaylists(Integer userid) {
        int playlistId;
        do {
            playlistId = Menu.getIntWithMessage("Enter playlist ID to add to library (or -1 to finish): ");
            if (playlistId != -1 && !spotifum.addPlaylistToPremiumUserLibrary(userid, playlistId))
                Menu.printOnTerminal("Invalid playlist");
            else
                Menu.printOnTerminal("Playlist added");
        } while (playlistId != -1);
    }

    /**
     * Adds albums to the library of the specified user by prompting for album IDs.
     * Users can repeatedly input album IDs to add to their library until they
     * choose to stop by entering -1.
     *
     * @param userId the ID of the user to whom the albums are being added
     */
    private void addUserAlbums(Integer userId) {
        int albumId;
        do {
            albumId = Menu.getIntWithMessage("Enter album ID to add to library (or -1 to finish): ");
            if (albumId != -1 && !spotifum.addAlbumToPremiumUserLibrary(userId, albumId))
                Menu.printOnTerminal("Invalid album");
            else
                Menu.printOnTerminal("Album added");
        } while (albumId != -1);
    }

    /**
     * Creates a playlist based on user input, including its name, privacy settings,
     * and whether it is a standard or timed playlist.
     *
     * @return a Playlist object with the specified configurations
     */
    private Playlist createPlaylist() {
        String name = Menu.getStringWithMessage("Enter playlist name: ");
        boolean isPublic = getPlaylistPrivacy();
        boolean isTimed = Menu.addYesNoInput("Do you want this playlist to be generated as a timed playlist:").equalsIgnoreCase("yes");

        Playlist playlist = isTimed ? createTimedPlaylist() : new Playlist();
        playlist.setName(name);
        playlist.setPublic(isPublic);
        playlist.setCreator(-1);
        return playlist;
    }

    /**
     * Creates a playlist with a specified maximum time and genre.
     *
     * This method prompts the user to input a maximum time (in minutes) and a genre,
     * then generates a playlist matching these criteria using the Spotifum library.
     *
     * @return A Playlist object that follows the specified time and genre constraints.
     */
    private Playlist createTimedPlaylist() {
        int maxTime = Menu.getIntWithMessage("Set playlist max time: ");
        String genre = Menu.getStringWithMessage("Enter the playlist genre: ");
        return spotifum.generateTimedPlaylist(maxTime, genre);
    }

    /**
     * Determines the privacy setting of a playlist based on user input.
     * The user is prompted to specify either "public" or "private".
     * If the input is invalid, the prompt will be repeated until a valid response is provided.
     *
     * @return true if the playlist is set to public, false if set to private
     */
    private boolean getPlaylistPrivacy() {
        String privacy;
        do {
            privacy = Menu.getStringWithMessage("Playlist privacy (public/private): ");
        } while (!privacy.equalsIgnoreCase("private") && !privacy.equalsIgnoreCase("public"));
        return privacy.equalsIgnoreCase("public");
    }

    /**
     * Adds one or more music tracks to a playlist by its ID. The user is prompted to enter
     * music IDs in a loop, and each valid music ID is added to the specified playlist.
     * The loop terminates when the user enters -1.
     *
     * @param playlistId The unique identifier of the playlist to which the music is to be added.
     */
    private void addMusicsToPlaylistById(Integer playlistId) {
        int musicId;
        do {
            musicId = Menu.getIntWithMessage("Enter music ID to add (or -1 to finish): ");
            if (musicId != -1) {
                if (!spotifum.addMusicToPlaylist(playlistId, musicId))
                    Menu.printOnTerminal("Invalid music");
                else
                    Menu.printOnTerminal("Music added successfully");
            }
        } while (musicId != -1);
    }

    /**
     * Adds multiple music tracks to an album identified by its ID. The user is prompted
     * to enter the IDs of the music tracks one by one. The process continues until the
     * user enters -1 to indicate they are finished. If the music ID is invalid or the
     * music's artist does not match the album's artist, an error message is displayed.
     *
     * @param albumId the unique identifier of the album to which music tracks will be added
     */
    private void addMusicsToAlbumById(Integer albumId) {
        int musicId;
        do {
            musicId = Menu.getIntWithMessage("Enter music ID to add (or -1 to finish): ");
            if (musicId != -1) {
                if (!spotifum.addMusicToAlbum(albumId, musicId))
                    Menu.printOnTerminal("Invalid Music / Music and album artists must match");
                else
                    Menu.printOnTerminal("Music added successfully");
            }
        } while (musicId != -1);
    }

    /**
     * Adds an entity based on the specified type.
     *
     * @param type an integer representing the type of entity to add.
     *             Valid values are:
     *             1 - User
     *             2 - Music
     *             3 - Playlist
     *             4 - Album
     *
     * @return a String containing the result of the addition process.
     *         If the type is invalid, it returns "Invalid entity type".
     */
    private String addEntity(int type) {
        return switch (type) {
            case 1 -> addUser();
            case 2 -> addMusic();
            case 3 -> addPlaylist();
            case 4 -> addAlbum();
            default -> "Invalid entity type";
        };
    }

    /**
     * Retrieves and returns the specified collection of the user based on the provided type.
     *
     * @param userId The unique identifier of the user whose collection is to be retrieved.
     * @param type The type of collection to be viewed (e.g., playlists, favorite tracks).
     *
     * @return A string representing the user's requested collection, based on the specified type.
     */
    public String viewUserCollection(Integer userId, String type) {
        return spotifum.viewUserCollection(userId, type);
    }

    /**
     * Retrieves and returns a list of music items available in the Spotifum application.
     *
     * @return a string representation of the available music items.
     */
    public String listMusics() {
        return spotifum.listMusics();
    }

    /**
     * Retrieves a list of public playlists available in the system.
     *
     * @return A string containing the list of public playlists.
     */
    public String listPublicPlaylists() {
        return spotifum.listPublicPlaylists();
    }

    /**
     * Retrieves a list of albums as a string representation.
     *
     * @return A string containing the list of albums.
     */
    public String listAlbums() {
        return spotifum.listAlbums();
    }

    /**
     * Retrieves the history of a user based on their unique user ID.
     *
     * @param userId the unique identifier of the user whose history is to be retrieved
     *
     * @return a string containing the user's history
     */
    public String getUserHistory(Integer userId) {
        return spotifum.getUserHistory(userId);
    }

    /**
     * Adds a new user with the specified password and generates a unique ID for the user.
     * Prompts the user to enter a password and creates the user in the system with a unique identifier.
     *
     * @return A message indicating that the user has been created along with the generated user ID.
     */
    private String addUser() {
        String password = Menu.getStringWithMessage("Enter user password: ");
        Integer id = addUserGeneric(password);
        return "User with ID " + id + " created.";
    }

    /**
     * Adds a generic user to the system with the provided password.
     * This method assigns an ID to the created user and handles additional
     * setup for premium users if applicable.
     *
     * @param password the password to be assigned to the new user
     *
     * @return the unique identifier of the newly created user
     */
    public Integer addUserGeneric(String password) {
        User user = createUser();
        Integer id = spotifum.createUser(user, password);
        if (user instanceof PremiumUser) handlePremiumUserCollections(id);
        return id;
    }

    /**
     * Allows users to add a new music track to a specified album. The method collects details
     * about the music, such as its title, artist, publisher, lyrics, sound, genre, duration, and type.
     * It validates the input, ensures the provided album exists, and that the artist matches
     * the album's associated artist. Based on the specified music type, the method creates
     * an appropriate music object and stores it in the system.
     *
     * @return A message indicating the outcome of the operation. Possible results include:
     *         "Before adding musics you have to create albums" if no albums exist;
     *         "Failed: Music artist and album artist do not match" if the artist details are inconsistent;
     *         "Album does not exist, music cannot be added" if the given album ID is invalid;
     *         or a success message containing the created music's ID.
     */
    private String addMusic() {
        if (spotifum.isAlbumsEmpty())
            return "Before adding musics you have to create albums";

        String name = Menu.getStringWithMessage("Enter music title: ");
        String artist = Menu.getStringWithMessage("Enter artist name: ");
        String publisher = Menu.getStringWithMessage("Enter publisher name: ");
        String lyrics = Menu.getStringWithMessage("Enter the lyrics: ");
        String sound = Menu.getStringWithMessage("Enter the sound: ");
        String genre = Menu.getStringWithMessage("Enter the music genre: ");
        int duration = Menu.getIntWithMessage("Enter music duration (in seconds): ");

        Integer albumId = Menu.getIntWithMessage("Enter album id to add music: ");
        if (!spotifum.isMusicAllowedOnPlaylist(artist, albumId)) return "Failed: Music artist and album artist do not match";

        String type;
        do {
            type = Menu.getStringWithMessage("Enter music type (normal/explicit/multimedia): ");
        } while (!type.equalsIgnoreCase("normal") &&
                !type.equalsIgnoreCase("explicit") &&
                !type.equalsIgnoreCase("multimedia"));

        Music music = switch (type.toLowerCase()) {
            case "normal" -> new Music(name, artist, publisher, lyrics, sound, genre, duration, 0);
            case "explicit" -> createExplicitMusic(name, artist, publisher, lyrics, sound, genre, duration);
            case "multimedia" -> createMultimediaMusic(name, artist, publisher, lyrics, sound, genre, duration);
            default -> throw new IllegalStateException("Invalid music type");
        };

        Integer musicId = spotifum.createMusic(music, albumId);
        if (musicId == -1) return "Failed: Music artist and album artist do not match";
        else if (musicId == -2) return "Album does not exist, music cannot be added";
        else return "Music with ID " + musicId + " created.";
    }

    /**
     * Creates an instance of ExplicitMusic with the given details. The method allows the user
     * to either automatically calculate the age restriction for the music or manually specify it.
     *
     * @param name           the name of the music
     * @param artist         the name of the artist
     * @param publisher      the publisher of the music
     * @param lyrics         the lyrics of the music
     * @param sound          the sound information of the music
     * @param genre          the genre of the music
     * @param duration       the duration of the music in seconds
     *
     * @return a new ExplicitMusic instance with the specified details and age restriction settings
     */
    private Music createExplicitMusic(String name, String artist, String publisher,
                                      String lyrics, String sound, String genre, int duration) {
        if (Menu.addYesNoInput("Do you want to auto-calc music age restriction").equalsIgnoreCase("yes")) {
            ExplicitMusic music = new ExplicitMusic(name, artist, publisher, lyrics, sound, genre, duration, 0, true, 0, "unknown");
            music.detectAndAssignAgeRestriction();
            return music;
        }

        int ageRestriction = Menu.getIntWithMessage("Enter the age restriction: ");
        String ratingSource = Menu.getStringWithMessage("Enter the age restriction rating source: ");
        return new ExplicitMusic(name, artist, publisher, lyrics, sound, genre, duration, 0, true, ageRestriction, ratingSource);
    }

    /**
     * Creates a MultimediaMusic object with the given parameters and additional video-related details.
     *
     * @param name The name of the music.
     * @param artist The artist of the music.
     * @param publisher The publisher of the music.
     * @param lyrics The lyrics of the music.
     * @param sound The sound file for the music.
     * @param genre The genre of the music.
     * @param duration The duration of the music in seconds.
     *
     * @return A MultimediaMusic object with all the specified properties including video details.
     */
    private Music createMultimediaMusic(String name, String artist, String publisher,
                                        String lyrics, String sound, String genre, int duration) {
        String videoUrl = Menu.getStringWithMessage("Enter the video URL: ");
        String resolution = Menu.getStringWithMessage("Enter video resolution: ");
        boolean subtitles = Menu.addYesNoInput("The video has subtitles").equalsIgnoreCase("yes");
        return new MultimediaMusic(name, artist, publisher, lyrics, sound, genre, duration, 0, videoUrl, resolution, subtitles);
    }

    /**
     * Creates a new playlist and adds it to the playlist manager. Optionally allows the user
     * to add music tracks to the newly created playlist.
     *
     * @return A message indicating that the playlist was created, including its ID.
     */
    private String addPlaylist() {
        Playlist playlist = createPlaylist();
        String res = "Playlist with ID " + playlist.getIdentifier() + " created.";
        spotifum.addPlaylistToManager(playlist);

        if (Menu.addYesNoInput("Do you want to add musics to playlist").equalsIgnoreCase("yes")) {
            addMusicsToPlaylistById(playlist.getIdentifier());
        }

        return res;
    }

    /**
     * Creates a new album with the specified name and artist/group name, adds it to the album manager,
     * and optionally allows the user to add music tracks to the album.
     *
     * @return A string message indicating the result of the album creation process, either confirming
     *         successful creation with the album ID or reporting an error if the creation failed.
     */
    private String addAlbum() {
        Album album = new Album();
        album.setName(Menu.getStringWithMessage("Enter album name: "));
        album.setArtist(Menu.getStringWithMessage("Enter album artist/group name: "));

        Integer albumId = spotifum.createAlbum(album);

        if (albumId != null) {
            spotifum.addAlbumToManager(album);
            String res = "Album with ID " + albumId + " created.";

            if (Menu.addYesNoInput("Do you want to add musics to album").equalsIgnoreCase("yes")) {
                addMusicsToAlbumById(album.getIdentifier());
            }

            return res;
        } else {
            return "Error creating album: Album creation failed";
        }
    }

    /**
     * Removes an entity of the specified type.
     *
     * @param type the type of entity to be removed, where:
     *             1 represents a "user",
     *             2 represents "music",
     *             3 represents a "playlist",
     *             4 represents an "album".
     *
     * @return a string indicating the result of the entity removal
     *         operation or an error message if the type is invalid.
     */
    private String removeEntity(int type) {
        return switch (type) {
            case 1 -> removeGenericEntity("user", spotifum::deleteUser);
            case 2 -> removeGenericEntity("music", spotifum::deleteMusic);
            case 3 -> removeGenericEntity("playlist", spotifum::deletePlaylist);
            case 4 -> removeGenericEntity("album", spotifum::deleteAlbum);
            default -> "Invalid entity type.";
        };
    }

    /**
     * Removes a generic entity based on the provided entity name and delete function.
     *
     * @param entityName the name of the entity to be removed
     * @param deleteFunction a function that takes an entity ID as input and returns a boolean
     *                       indicating whether the delete operation was successful
     *
     * @return a message indicating the result of the delete operation, formatted with the
     *         capitalized entity name and success or failure status
     */
    private String removeGenericEntity(String entityName, Function<Integer, Boolean> deleteFunction) {
        Integer id = Menu.getIntWithMessage("Enter the " + entityName + " id: ");
        boolean success = deleteFunction.apply(id);
        String capitalized = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        return capitalized + " deleted " + (success ? "successfully" : "unsuccessfully");
    }

    /**
     * Modifies an entity based on the provided type.
     *
     * @param type an integer representing the type of entity to modify.
     *             Valid types are:
     *             1 - User
     *             2 - Music
     *             3 - Playlist
     *             4 - Album
     *
     * @return a string indicating the result of the modification for the specified entity type.
     *         If the type is invalid, it returns "Invalid entity type".
     */
    private String modifyEntity(int type) {
        return switch (type) {
            case 1 -> modifyUser();
            case 2 -> modifyMusic();
            case 3 -> modifyPlaylist();
            case 4 -> modifyAlbum();
            default -> "Invalid entity type";
        };
    }

    /**
     * Modifies the details of an existing user in the system.
     * Prompts for new user details such as name, email, address, and age
     * and allows updating the subscription plan. Retains current values if
     * the user opts not to modify specific fields.
     *
     * @return A message indicating whether the user modification was successful or not.
     */
    private String modifyUser() {
        Integer userId = Menu.getIntWithMessage("Enter user id to modify: ");
        if (!spotifum.userExists(userId)) return "User does not exist";

        String name = Menu.getStringWithMessage("Enter new name (or press Enter to keep current): ");
        String email = Menu.getStringWithMessage("Enter new email (or press Enter to keep current): ");
        String address = Menu.getStringWithMessage("Enter new address (or press Enter to keep current): ");
        int age = Menu.getIntWithMessage("Enter new age (or 0 to keep current): ");

        boolean isPremium = spotifum.isPremiumUser(userId);
        SubscriptionPlan newPlan = handlePlanModification(isPremium);

        if (spotifum.modifyUser(userId, name, email, address, age, newPlan)) {
            handlePremiumUserCollections(userId);
            return "User modification successfully";
        }
        else return "User modification failed";
    }

    /**
     * Handles the modification of a subscription plan based on the user's input
     * and current subscription status.
     *
     * @param isPremium a boolean indicating whether the user currently has a premium subscription
     * @return a SubscriptionPlan object if the plan is modified or created successfully;
     *         returns null if no changes are made to the subscription
     */
    private SubscriptionPlan handlePlanModification(boolean isPremium) {
        if (isPremium) {
            return handlePremiumUserModification();
        } else if (Menu.addYesNoInput("Do you want to upgrade to premium").equalsIgnoreCase("yes")) {
            return createSubscriptionPlan();
        }
        return null;
    }

    /**
     * Handles the modification of a premium user's subscription plan.
     * Prompts the user to confirm if they want to change their plan and,
     * if confirmed, creates and returns a new subscription plan.
     *
     * @return the newly created SubscriptionPlan if the user chooses to modify their plan,
     *         or null if the user does not wish to change their plan.
     */
    private SubscriptionPlan handlePremiumUserModification() {
        SubscriptionPlan newPlan = null;

        if (Menu.addYesNoInput("Do you want to change plan").equalsIgnoreCase("yes")) {
            newPlan = createSubscriptionPlan();
        }

        return newPlan;
    }

    /**
     * Modifies an existing music entry in the SpotifUM system based on user input.
     * This method prompts the user for the music ID and checks if it exists.
     * Users can update various fields such as title, artist, publisher, genre,
     * and duration. If a specific field needs no changes, the user can leave it blank
     * or enter a specific value (e.g., 0 for duration) to retain the current value.
     * Special modification handling is applied depending on the type of music.
     *
     * @return a message indicating the result of the modification process, which can be:
     *         "Music modified successfully", "Music modification failed", or
     *         "Music does not exist".
     */
    private String modifyMusic() {
        Integer musicId = Menu.getIntWithMessage("Enter music id to modify: ");
        if (!spotifum.musicExists(musicId)) return "Music does not exist";

        String name = Menu.getStringWithMessage("Enter new title (or press Enter to keep current): ");
        String lyrics = Menu.getStringWithMessage("Enter new lyrics (or press Enter to keep current): ");
        String publisher = Menu.getStringWithMessage("Enter new publisher (or press Enter to keep current): ");
        String genre = Menu.getStringWithMessage("Enter new genre (or press Enter to keep current): ");
        int duration = Menu.getIntWithMessage("Enter new duration in seconds (or 0 to keep current): ");

        handleSpecialMusicModification(musicId);

        return spotifum.modifyMusic(musicId, name, lyrics, publisher, genre, duration) ?
                "Music modified successfully" :
                "Music modification failed";
    }

    /**
     * Handles the modification of special types of music in the SpotifUM system.
     * The method identifies the music type (e.g., explicit or multimedia) based on the given music ID
     * and delegates the modification process to the appropriate handler.
     *
     * @param musicId the unique identifier of the music to be modified
     *                - If the music type is "explicit", the method calls {@code handleExplicitMusicModification}.
     *                - If the music type is "multimedia", the method calls {@code handleMultimediaMusicModification}.
     *                - If the music type is null, no action is performed.
     */
    private void handleSpecialMusicModification(Integer musicId) {
        String musicType = spotifum.getMusicType(musicId);
        if (musicType == null) return;

        switch (musicType) {
            case "explicit" -> handleExplicitMusicModification(musicId);
            case "multimedia" -> handleMultimediaMusicModification(musicId);
        }
    }

    /**
     * Handles the modification of explicit content settings for a specific music track
     * in the SpotifUM system based on user input. The method interacts with the user
     * to update age restrictions and rating source information or to calculate
     * the age restriction automatically.
     *
     * @param musicId the unique identifier of the music track whose explicit content
     *                settings are to be modified
     */
    private void handleExplicitMusicModification(Integer musicId) {
        if (Menu.addYesNoInput("Do you want to modify explicit content settings").equalsIgnoreCase("yes")) {
            if (Menu.addYesNoInput("Do you want to auto-calc age restriction").equalsIgnoreCase("yes")) {
                spotifum.modifyExplicitMusic(musicId, -1, null);
            } else {
                int ageRestriction = Menu.getIntWithMessage("Enter new age restriction (or 0 to keep current): ");
                String ratingSource = Menu.getStringWithMessage("Enter new rating source (or press Enter to keep current): ");
                spotifum.modifyExplicitMusic(musicId, ageRestriction, ratingSource);
            }
        }
    }

    /**
     * Handles the modification of multimedia music settings for a specified music ID.
     * Prompts the user to update video URL, resolution, and subtitle options. If confirmed,
     * the changes are applied using the SpotifUM system.
     *
     * @param musicId the unique identifier of the multimedia music to be modified
     */
    private void handleMultimediaMusicModification(Integer musicId) {
        if (Menu.addYesNoInput("Do you want to modify multimedia settings").equalsIgnoreCase("yes")) {
            String videoUrl = Menu.getStringWithMessage("Enter new video URL (or press Enter to keep current): ");
            String resolution = Menu.getStringWithMessage("Enter new resolution (or press Enter to keep current): ");
            boolean subtitles = Menu.addYesNoInput("Has subtitles").equalsIgnoreCase("yes");
            spotifum.modifyMultimediaMusic(musicId, videoUrl, resolution, subtitles);
        }
    }

    /**
     * Modifies a playlist's attributes such as name, privacy setting, and content.
     * It prompts the user for the playlist ID and checks if the playlist exists.
     * Users can update the name, adjust the privacy (public/private), and add songs
     * to the playlist based on their preference.
     *
     * @return a String message indicating the result of the modification.
     *         Possible outcomes include "Playlist modified successfully",
     *         "Playlist modification failed", or "Playlist does not exist".
     */
    private String modifyPlaylist() {
        Integer playlistId = Menu.getIntWithMessage("Enter playlist id to modify: ");
        if (!spotifum.playlistExists(playlistId)) return "Playlist does not exist";

        String name = Menu.getStringWithMessage("Enter new name (or press Enter to keep current): ");
        String privacy = "";

        if (Menu.addYesNoInput("Do you want to modify privacy setting").equalsIgnoreCase("yes")) {
            do {
                privacy = Menu.getStringWithMessage("Enter new privacy setting (public/private): ");
            } while (!privacy.equalsIgnoreCase("public") && !privacy.equalsIgnoreCase("private"));
        }

        if (Menu.addYesNoInput("Do you want to add musics").equalsIgnoreCase("yes")) {
            addMusicsToPlaylistById(playlistId);
        }

        return spotifum.modifyPlaylist(playlistId, name, privacy.equalsIgnoreCase("public")) ?
                "Playlist modified successfully" :
                "Playlist modification failed";
    }

    /**
     * Modifies an existing album within the SpotifUM system based on user input.
     * Allows users to update the album name and optionally add new music to the album.
     * If the provided album ID does not exist within the system, an appropriate message is returned.
     *
     * @return a message indicating the result of the modification process, such as
     *         "Album modified successfully", "Album modification failed", or
     *         "Album does not exist".
     */
    private String modifyAlbum() {
        Integer albumId = Menu.getIntWithMessage("Enter album id to modify: ");
        if (!spotifum.albumExists(albumId)) return "Album does not exist";

        String name = Menu.getStringWithMessage("Enter new name (or press Enter to keep current): ");

        if (Menu.addYesNoInput("Do you want to add musics").equalsIgnoreCase("yes")) {
            addMusicsToAlbumById(albumId);
        }

        return spotifum.modifyAlbum(albumId, name) ?
                "Album modified successfully" :
                "Album modification failed";
    }

    /**
     * Lists the entities in the SpotifUM system based on the specified entity type.
     * Depending on the type, it retrieves and returns a list of users, musics, playlists, or albums.
     * If the type is invalid, an appropriate message is returned.
     *
     * @param type an integer representing the type of entity to list. Valid values are:
     *         1: Users
     *         2: Musics
     *         3: Playlists
     *         4: Albums
     *         Any other number: Invalid entity type
     *
     * @return a String containing the list of entities for the specified type.
     *         If the type is invalid, the returned String will indicate the error.
     */
    private String listEntities(int type) {
        if (type == 1) spotifum.updatePointsForAllUsers();

        return switch (type) {
            case 1 -> spotifum.listUsers();
            case 2 -> spotifum.listMusics();
            case 3 -> spotifum.listPlaylists();
            case 4 -> spotifum.listAlbums();
            default -> "Invalid entity type.";
        };
    }

    /**
     * Executes a specific query based on the given query number and returns the result.
     * Each query number corresponds to a predefined query within the SpotifUM system.
     * If the query number is invalid or not supported, an appropriate message is returned.
     *
     * @param queryNumber the number representing the query to be executed. Valid values are:
     *                    1: Most played music
     *                    2: Most listened artist
     *                    3: Top user by listening time
     *                    4: Top user by points
     *                    5: Most popular music genre
     *                    6: List of all public playlists
     *                    7: User associated with playlists
     *                    0: Exits the query execution
     *                    Any other number: Invalid option
     * @return the result of the query execution as a String. If an error or invalid query
     *         is encountered, an appropriate user-friendly message is returned.
     */
    public String runQuery(int queryNumber) {
        return switch (queryNumber) {
            case 1 -> spotifum.runQueryOne();
            case 2 -> spotifum.runQueryTwo();
            case 3 -> spotifum.runQueryThree();
            case 4 -> spotifum.runQueryFour();
            case 5 -> spotifum.runQueryFive();
            case 6 -> spotifum.runQuerySix();
            case 7 -> spotifum.runQuerySeven();
            case 0 -> "Exiting...";
            default -> "Invalid option.";
        };
    }

    /**
     * Displays the history view options for the SpotifUM system.
     *
     * This method prompts the user to choose between viewing the global history
     * or the history of a specific user. Based on the selection:
     * - If "global" is chosen, the method returns the global history from the system.
     * - If "user" is chosen, the method requests a user ID and returns that user's history.
     *
     * @return the history data as a String, either global or user-specific, based on the user's choice
     */
    public String historyView() {
        String op;
        do {
            op = Menu.getStringWithMessage("See global or user history? (global/user): ");
        } while (!op.equalsIgnoreCase("global") && !op.equalsIgnoreCase("user"));

        if (op.equalsIgnoreCase("user")) {
            Integer userId = Menu.getIntWithMessage("Enter user ID: ");
            return spotifum.getUserHistory(userId);
        } else {
            return spotifum.getGlobalHistory();
        }
    }

    /**
     * Attempts to authenticate a user with their credentials.
     * This method uses the SpotifUM's authentication system to verify the provided
     * user ID and password. If the authentication fails due to an invalid user ID or
     * password, or if any error occurs during the authentication process, the method
     * returns false and logs the error details.
     *
     * @param userId   the unique identifier of the user attempting to log in
     * @param password the plaintext password associated with the user's account
     * @return true if the user is successfully authenticated; false otherwise
     */
    public boolean login(Integer userId, String password) {
        try {
            return spotifum.getAuthenticator().login(userId, password);
        } catch (AuthenticatorException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logs out the currently authenticated user from the SpotifUM application.
     *
     * This method invokes the logout functionality provided by the
     * application's authentication system. If an error occurs during the
     * logout process, an error message will be printed to the standard error stream
     */
    public void logout() {
        try {
            spotifum.getAuthenticator().logout();
        } catch (AuthenticatorException e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    /**
     * Loads the state of the application by deserializing a SpotifUM object
     * from a predefined file. This method uses the {@code Persistence.loadState}
     * utility method to retrieve the application's state from the file
     * "spotifumState.dat"
     */
    public void loadState() {
        spotifum = Persistence.loadState("spotifumState.dat");
    }

    /**
     * Saves the current state of the SpotifUM system to a specific file.
     * This method invokes the {@code Persistence.saveState} method to save
     * the serialized state of the SpotifUM instance to a file named "spotifumState.dat".
     * The saved state can later be reloaded to restore the system's state.
     */
    public void saveState() {
        Persistence.saveState(spotifum, "spotifumState.dat");
    }

    /**
     * Determines whether the SpotifUM application is configured to automatically save its state.
     *
     * @return true if auto-save is enabled, false otherwise.
     */
    public boolean spotifUMAutoSaveState() {
        return spotifum.getAutoSave();
    }

    /**
     * Sets the auto-save state for SpotifUM.
     *
     * @param autosave a boolean value indicating whether the auto-save feature
     *                 should be enabled (true) or disabled (false)
     */
    public void setSpotifUMAutoSaveState(boolean autosave) {
        spotifum.setAutosave(autosave);
    }
}