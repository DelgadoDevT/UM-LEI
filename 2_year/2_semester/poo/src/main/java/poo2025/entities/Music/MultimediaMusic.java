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
 * Represents a multimedia music entity that extends the basic Music class with video capabilities.
 * This class implements Serializable to support object serialization.
 */
public class MultimediaMusic extends Music implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The URL of the video associated with this music */
    private String videoURL;
    
    /** The resolution of the video (e.g., "1080p", "4K") */
    private String resolution;
    
    /** Indicates whether the video has subtitles available */
    private boolean hasSubtitles;

    /**
     * Default constructor that initializes a MultimediaMusic object with empty values.
     */
    public MultimediaMusic() {
        super();
        this.videoURL = "";
        this.resolution = "";
        this.hasSubtitles = false;
    }

    /**
     * Constructs a MultimediaMusic object with specified parameters.
     *
     * @param name The name of the music
     * @param interpreter The interpreter/artist of the music
     * @param lyrics The lyrics of the music
     * @param sound The sound file reference
     * @param publisher The publisher of the music
     * @param genre The genre of the music
     * @param durationS The duration in seconds
     * @param plays The number of times the music has been played
     * @param videoURL The URL of the associated video
     * @param resolution The video resolution
     * @param hasSubtitles Whether the video has subtitles
     */
    public MultimediaMusic(String name, String interpreter, String lyrics, String sound, String publisher, String genre, int durationS, int plays, String videoURL, String resolution, boolean hasSubtitles) {
        super(name, interpreter, lyrics, sound, publisher, genre, durationS, plays);
        this.videoURL = videoURL;
        this.resolution = resolution;
        this.hasSubtitles = hasSubtitles;
    }

    /**
     * Copy constructor that creates a new MultimediaMusic object from an existing one.
     *
     * @param m The MultimediaMusic object to copy
     */
    public MultimediaMusic(MultimediaMusic m) {
        super(m);
        this.videoURL = m.getVideoURL();
        this.resolution = m.getResolution();
        this.hasSubtitles = isHasSubtitles();
    }

    /**
     * Gets the unique identifier of the music.
     *
     * @return The identifier value
     */
    public Integer getIdentifier() {
        return super.getIdentifier();
    }

    /**
     * Gets the name of the music.
     *
     * @return The music name
     */
    public String getName() {
        return super.getName();
    }

    /**
     * Sets the name of the music.
     *
     * @param name The new name
     */
    public void setName(String name) {
        super.setName(name);
    }

    /**
     * Gets the interpreter/artist of the music.
     *
     * @return The interpreter name
     */
    public String getInterpreter() {
        return super.getInterpreter();
    }

    /**
     * Sets the interpreter/artist of the music.
     *
     * @param interpreter The new interpreter name
     */
    public void setInterpreter(String interpreter) {
        super.setInterpreter(interpreter);
    }

    /**
     * Gets the lyrics of the music.
     *
     * @return The lyrics text
     */
    public String getLyrics() {
        return super.getLyrics();
    }

    /**
     * Sets the lyrics of the music.
     *
     * @param lyrics The new lyrics text
     */
    public void setLyrics(String lyrics) {
        super.setLyrics(lyrics);
    }

    /**
     * Gets the sound file reference.
     *
     * @return The sound file reference
     */
    public String getSound() {
        return super.getSound();
    }

    /**
     * Sets the sound file reference.
     *
     * @param sound The new sound file reference
     */
    public void setSound(String sound) {
        super.setSound(sound);
    }

    /**
     * Gets the publisher of the music.
     *
     * @return The publisher name
     */
    public String getPublisher() {
        return super.getPublisher();
    }

    /**
     * Sets the publisher of the music.
     *
     * @param publisher The new publisher name
     */
    public void setPublisher(String publisher) {
        super.setPublisher(publisher);
    }

    /**
     * Gets the genre of the music.
     *
     * @return The music genre
     */
    public String getGenre() {
        return super.getGenre();
    }

    /**
     * Sets the genre of the music.
     *
     * @param genre The new music genre
     */
    public void setGenre(String genre) {
        super.setGenre(genre);
    }

    /**
     * Gets the duration in seconds.
     *
     * @return The duration in seconds
     */
    public int getDurationS() {
        return super.getDurationS();
    }

    /**
     * Sets the duration in seconds.
     *
     * @param durationS The new duration in seconds
     */
    public void setDurationS(int durationS) {
        super.setDurationS(durationS);
    }

    /**
     * Gets the number of times the music has been played.
     *
     * @return The play count
     */
    public int getPlays() {
        return super.getPlays();
    }

    /**
     * Sets the number of plays.
     *
     * @param plays The new play count
     */
    public void setPlays(int plays) {
        super.setPlays(plays);
    }

    /**
     * Gets the video URL.
     *
     * @return The video URL
     */
    public String getVideoURL() {
        return this.videoURL;
    }

    /**
     * Sets the video URL.
     *
     * @param videoURL The new video URL
     */
    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    /**
     * Gets the video resolution.
     *
     * @return The video resolution
     */
    public String getResolution() {
        return this.resolution;
    }

    /**
     * Sets the video resolution.
     *
     * @param resolution The new video resolution
     */
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * Checks if the video has subtitles.
     *
     * @return true if the video has subtitles, false otherwise
     */
    public boolean isHasSubtitles() {
        return this.hasSubtitles;
    }

    /**
     * Sets whether the video has subtitles.
     *
     * @param hasSubtitles The new subtitles status
     */
    public void setHasSubtitles(boolean hasSubtitles) {
        this.hasSubtitles = hasSubtitles;
    }

    /**
     * Compares this MultimediaMusic object with another object for equality.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MultimediaMusic that = (MultimediaMusic) o;
        return this.hasSubtitles == that.isHasSubtitles() && Objects.equals(this.videoURL, that.getVideoURL()) && Objects.equals(this.resolution, that.getResolution());
    }

    /**
     * Generates a hash code for this MultimediaMusic object.
     *
     * @return The hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.videoURL, this.resolution, this.hasSubtitles);
    }

    /**
     * Returns a string representation of this MultimediaMusic object.
     *
     * @return A string containing all the object's properties
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
                .append(" | Video URL: ").append(this.videoURL)
                .append(" | Resolution: ").append(this.resolution)
                .append(" | Has Subtitles: ").append(this.hasSubtitles)
                .append("\n");
        return sb.toString();
    }

    /**
     * Plays the video and returns a status message.
     *
     * @return A string containing the video playback status
     */
    private String playVideo() {
        super.addPlays(1); // Increment play count when video is played
        return "=== Playing video \"" + this.getName() + "\" from: " + this.videoURL + " ===" +
                "\nQuality: " + this.resolution +
                "\nSubtitles: " + (this.hasSubtitles ? "Enabled" : "Disabled");
    }

    /**
     * Pauses the video and returns a status message.
     *
     * @return A string containing the pause status and current play count
     */
    private String pauseVideo() {
        return "=== Video \"" + this.getName() + "\" paused ===\n" +
                "Current playback count: " + this.getPlays();
    }

    /**
     * Determines the video platform based on the URL.
     *
     * @return The name of the video platform or "Unknown platform" if not recognized
     */
    private String getVideoPlatform() {
        if (this.videoURL == null) return "No URL";
        if (this.videoURL.contains("youtube.com")) return "YouTube";
        if (this.videoURL.contains("vimeo.com")) return "Vimeo";
        return "Unknown platform";
    }

    /**
     * Checks if the video is of high quality (1080p or 4K).
     *
     * @return true if the video is high quality, false otherwise
     */
    private boolean isHighQualityVideo() {
        return this.resolution != null && (this.resolution.equals("1080p") || this.resolution.equals("4K"));
    }

    /**
     * Creates and returns a copy of this MultimediaMusic object.
     *
     * @return A new MultimediaMusic object with the same properties as this one
     */
    @Override
    public MultimediaMusic clone() {
        return new MultimediaMusic(this);
    }
}