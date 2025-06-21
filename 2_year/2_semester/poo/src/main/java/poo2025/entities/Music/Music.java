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
 * Represents a music track with various attributes such as name, interpreter, lyrics, etc.
 * This class implements Serializable to support object persistence.
 * Each music track has a unique identifier that is automatically assigned.
 */
public class Music implements Serializable {
    /** Serial version UID for serialization */
    private static final long serialVersionUID = 1L;
    
    /** Counter for generating unique identifiers for music tracks */
    private static Integer lastId = 0;

    /** Unique identifier for the music track */
    private final Integer identifier;
    
    /** Name of the music track */
    private String name;
    
    /** Artist or band performing the track */
    private String interpreter;
    
    /** Publishing entity of the track */
    private String publisher;
    
    /** Lyrics content of the track */
    private String lyrics;
    
    /** Sound file reference or content */
    private String sound;
    
    /** Musical genre of the track */
    private String genre;
    
    /** Duration of the track in seconds */
    private int durationS;
    
    /** Number of times the track has been played */
    private int plays;

    /**
     * Default constructor that initializes a music track with empty values.
     * Automatically assigns a unique identifier.
     */
    public Music() {
        this.identifier = lastId++;
        this.name = "";
        this.interpreter = "";
        this.publisher = "";
        this.lyrics = "";
        this.sound = "";
        this.genre = "";
        this.durationS = 0;
        this.plays = 0;
    }

    /**
     * Constructs a music track with specified attributes.
     *
     * @param name        the name of the track
     * @param interpreter the performer of the track
     * @param publisher   the publisher of the track
     * @param lyrics      the lyrics content
     * @param sound      the sound file reference
     * @param genre      the musical genre
     * @param durationS   the duration in seconds
     * @param plays      the number of plays
     */
    public Music(String name, String interpreter, String publisher, String lyrics, String sound, String genre, int durationS, int plays) {
        this.identifier = lastId++;
        this.name = name;
        this.interpreter = interpreter;
        this.lyrics = lyrics;
        this.sound = sound;
        this.publisher = publisher;
        this.genre = genre;
        this.durationS = durationS;
        this.plays = plays;
    }

    /**
     * Copy constructor that creates a clone of an existing music track.
     * The new instance will have the same identifier as the original.
     *
     * @param m the music track to copy
     */
    public Music(Music m) {
        this.identifier = m.getIdentifier();
        this.name = m.getName();
        this.interpreter = m.getInterpreter();
        this.lyrics = m.getLyrics();
        this.sound = m.getSound();
        this.publisher = m.getPublisher();
        this.genre = m.getGenre();
        this.durationS = m.getDurationS();
        this.plays = m.getPlays();
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
     * Gets the unique identifier of the music track.
     *
     * @return the identifier value
     */
    public Integer getIdentifier() {
        return this.identifier;
    }

    /**
     * Gets the name of the track.
     *
     * @return the track name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the track.
     *
     * @param name the new track name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the interpreter/performer of the track.
     *
     * @return the interpreter name
     */
    public String getInterpreter() {
        return this.interpreter;
    }

    /**
     * Sets the interpreter/performer of the track.
     *
     * @param interpreter the new interpreter name
     */
    public void setInterpreter(String interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Gets the lyrics content of the track.
     *
     * @return the lyrics text
     */
    public String getLyrics() {
        return this.lyrics;
    }

    /**
     * Sets the lyrics content of the track.
     *
     * @param lyrics the new lyrics text
     */
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * Gets the sound file reference.
     *
     * @return the sound file reference
     */
    public String getSound() {
        return this.sound;
    }

    /**
     * Sets the sound file reference.
     *
     * @param sound the new sound file reference
     */
    public void setSound(String sound) {
        this.sound = sound;
    }

    /**
     * Gets the publisher of the track.
     *
     * @return the publisher name
     */
    public String getPublisher() {
        return this.publisher;
    }

    /**
     * Sets the publisher of the track.
     *
     * @param publisher the new publisher name
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Gets the musical genre of the track.
     *
     * @return the genre name
     */
    public String getGenre() {
        return this.genre;
    }

    /**
     * Sets the musical genre of the track.
     *
     * @param genre the new genre name
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Gets the duration of the track in seconds.
     *
     * @return the duration in seconds
     */
    public int getDurationS() {
        return this.durationS;
    }

    /**
     * Sets the duration of the track in seconds.
     *
     * @param durationS the new duration in seconds
     */
    public void setDurationS(int durationS) {
        this.durationS = durationS;
    }

    /**
     * Gets the number of times the track has been played.
     *
     * @return the play count
     */
    public int getPlays() {
        return this.plays;
    }

    /**
     * Sets the number of plays for the track.
     *
     * @param plays the new play count
     */
    public void setPlays(int plays) {
        this.plays = plays;
    }

    /**
     * Compares this music track with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Music music = (Music) o;
        return this.identifier.equals(music.getIdentifier()) && this.durationS == music.getDurationS() && this.plays == music.getPlays() && Objects.equals(this.name, music.getName()) && Objects.equals(this.interpreter, music.getInterpreter()) && Objects.equals(this.lyrics, music.getLyrics()) && Objects.equals(this.sound, music.getSound()) && Objects.equals(this.publisher, music.getPublisher()) && Objects.equals(this.genre, music.getGenre());
    }

    /**
     * Generates a hash code for the music track.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.identifier, this.name, this.interpreter, this.publisher, this.lyrics, this.sound, this.genre, this.durationS, this.plays);
    }

    /**
     * Returns a string representation of the music track with all its attributes.
     *
     * @return a formatted string containing all track information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(this.getIdentifier())
                .append(" | Name: ").append(this.getName())
                .append(" | Interpreter: ").append(this.getInterpreter())
                .append(" | Publisher: ").append(this.getPublisher())
                .append(" | Lyrics: ").append(this.getLyrics())
                .append(" | Sound: ").append(this.getSound())
                .append(" | Genre: ").append(this.getGenre())
                .append(" | Duration: ").append(this.getDurationS()).append(" seconds")
                .append(" | Plays: ").append(this.getPlays())
                .append("\n");
        return sb.toString();
    }

    /**
     * Returns a simplified string representation of the music track.
     *
     * @return a formatted string with basic track information
     */
    public String simpleView() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(this.getIdentifier())
                .append(" | Name: ").append(this.getName())
                .append(" | Interpreter: ").append(this.getInterpreter())
                .append(" | Genre: ").append(this.getGenre());
        return sb.toString();
    }

    /**
     * Simulates playing the track by incrementing the play count and returning play information.
     *
     * @return a formatted string indicating the track is playing
     */
    public String play() {
        this.setPlays(this.getPlays() + 1);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Music \"")
                .append(this.getName())
                .append("\" by ")
                .append(this.getInterpreter())
                .append(" playing ===\n")
                .append(this.getLyrics());
        return sb.toString();
    }

    /**
     * Adds a specified number of plays to the track's play count.
     *
     * @param x the number of plays to add
     */
    public void addPlays(int x) {
        this.setPlays(this.getPlays() + x);
    }

    /**
     * Creates and returns a copy of this music track.
     *
     * @return a new Music instance with the same attributes as this one
     */
    @Override
    public Music clone() {
        return new Music(this);
    }
}