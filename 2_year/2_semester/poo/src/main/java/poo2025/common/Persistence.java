/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */

package poo2025.common;

import poo2025.mvc.SpotifUM;
import poo2025.entities.User.User;
import poo2025.entities.Music.Music;
import poo2025.entities.Playlist.Playlist;
import poo2025.entities.Album.Album;

import java.io.*;

/**
 * The Persistence class provides utility methods for saving and loading the state
 * of a SpotifUM object to and from a file. This is a utility class and should not
 * be instantiated.
 */
public final class Persistence {
    /**
     * Constructs a Persistence object.
     * This constructor is private and is not meant to be instantiated.
     *
     * @throws UnsupportedOperationException if an attempt to instantiate this utility class is made.
     */
    public Persistence() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * Saves the state of the specified SpotifUM object to a file.
     *
     * @param spotifUM the SpotifUM object to be saved
     * @param filename the name of the file where the state will be saved
     */
    public static void saveState(SpotifUM spotifUM, String filename) {
        File saveDirectory = new File("save");
        if (!saveDirectory.exists())
            saveDirectory.mkdir();
        File saveFile = new File(saveDirectory, filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            oos.writeObject(spotifUM);
            oos.writeInt(User.getLastId());
            oos.writeInt(Music.getLastId());
            oos.writeInt(Album.getLastId());
            oos.writeInt(Playlist.getLastId());
            oos.flush();
        } catch (IOException e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }
    }

    /**
     * Loads the state of a SpotifUM object from the specified file.
     * The method attempts to read a serialized SpotifUM object from the given file
     * and returns it if successfully deserialized. If the file does not exist,
     * contains an incompatible object type, or an error occurs during reading,
     * the method will return null.
     *
     * @param filename the name of the file from which the SpotifUM object is to be loaded
     * @return the deserialized SpotifUM object, or null if the file is not found,
     *         contains invalid data, or an error occurs during deserialization
     */
    public static SpotifUM loadState(String filename) {
        SpotifUM spotifUM = null;
        File saveDirectory = new File("save");
        File file = new File(saveDirectory, filename);

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object readObj = ois.readObject();
                if (readObj instanceof SpotifUM) {
                    spotifUM = (SpotifUM) readObj;
                } else {
                    System.out.println("File content is not of type: SpotifUM");
                }
                User.setLastId(ois.readInt());
                Music.setLastId(ois.readInt());
                Album.setLastId(ois.readInt());
                Playlist.setLastId(ois.readInt());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Failed to load state: " + e.getMessage());
            }
        } else {
            System.out.println("File not found: " + filename);
        }

        return spotifUM;
    }
}