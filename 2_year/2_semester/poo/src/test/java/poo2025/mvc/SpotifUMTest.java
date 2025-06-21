/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import poo2025.common.Authenticator;
import poo2025.managers.AlbumManager;
import poo2025.managers.MusicManager;
import poo2025.managers.PlaylistManager;
import poo2025.managers.UserManager;
import poo2025.entities.PlaybackHistory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link SpotifUM} class. This class contains unit tests that verify
 * the functionality of the SpotifUM class, particularly focusing on the core operations
 * and component management.
 *
 * <p>The test suite uses Mockito framework to create mock objects for all major components
 * of the SpotifUM system, including:</p>
 * <ul>
 *   <li>UserManager - For managing user-related operations</li>
 *   <li>MusicManager - For handling music-related functionality</li>
 *   <li>PlaylistManager - For playlist operations</li>
 *   <li>AlbumManager - For album-related functionality</li>
 *   <li>Authenticator - For authentication services</li>
 * </ul>
 *
 * @see SpotifUM
 * @see UserManager
 * @see MusicManager
 * @see PlaylistManager
 * @see AlbumManager
 * @see Authenticator
 * @see PlaybackHistory
 */
class SpotifUMTest {
    private SpotifUM spotifUM;
    private UserManager mockUserManager;
    private MusicManager mockMusicManager;
    private PlaylistManager mockPlaylistManager;
    private AlbumManager mockAlbumManager;
    private Authenticator mockAuthenticator;
    private List<PlaybackHistory> mockHistory;

    /**
     * Sets up the test environment before each test method execution.
     * Initializes mock objects and creates a new SpotifUM instance with these mocks.
     * This method is executed before each test to ensure a clean state.
     */
    @BeforeEach
    void setUp() {
        mockUserManager = mock(UserManager.class);
        mockMusicManager = mock(MusicManager.class);
        mockPlaylistManager = mock(PlaylistManager.class);
        mockAlbumManager = mock(AlbumManager.class);
        mockAuthenticator = mock(Authenticator.class);
        mockHistory = new ArrayList<>();

        spotifUM = new SpotifUM(
            mockUserManager,
            mockMusicManager,
            mockPlaylistManager,
            mockAlbumManager,
            mockHistory,
            mockAuthenticator,
            true
        );
    }

    /**
     * Tests the default constructor of SpotifUM.
     * Verifies that a new instance is created with non-null components.
     * This test ensures that the default constructor properly initializes all required components.
     */
    @Test
    void testDefaultConstructor() {
        SpotifUM defaultSpotifUM = new SpotifUM();
        
        assertNotNull(defaultSpotifUM.getUserManager());
        assertNotNull(defaultSpotifUM.getMusicManager());
        assertNotNull(defaultSpotifUM.getPlaylistManager());
        assertNotNull(defaultSpotifUM.getAlbumManager());
        assertNotNull(defaultSpotifUM.getHistory());
        assertNotNull(defaultSpotifUM.getAuthenticator());
    }

    /**
     * Tests the copy constructor of SpotifUM.
     * Verifies that a deep copy is created with all components properly cloned.
     * Ensures that the copied instance has independent components from the original.
     */
    @Test
    void testCopyConstructor() {
        SpotifUM copy = new SpotifUM(spotifUM);
        
        assertNotSame(spotifUM, copy);
        assertNotSame(spotifUM.getUserManager(), copy.getUserManager());
        assertNotSame(spotifUM.getMusicManager(), copy.getMusicManager());
        assertNotSame(spotifUM.getPlaylistManager(), copy.getPlaylistManager());
        assertNotSame(spotifUM.getAlbumManager(), copy.getAlbumManager());
        assertNotSame(spotifUM.getHistory(), copy.getHistory());
    }

    /**
     * Tests the getter and setter methods for UserManager.
     * Verifies that the UserManager component can be properly retrieved and updated.
     * Ensures that the set manager is correctly returned by the getter.
     */
    @Test
    void testUserManagerGetterAndSetter() {
        UserManager newUserManager = mock(UserManager.class);
        spotifUM.setUserManager(newUserManager);
        
        assertEquals(newUserManager, spotifUM.getUserManager());
    }

    /**
     * Tests the getter and setter methods for MusicManager.
     * Verifies that the MusicManager component can be properly retrieved and updated.
     * Ensures that the set manager is correctly returned by the getter.
     */
    @Test
    void testMusicManagerGetterAndSetter() {
        MusicManager newMusicManager = mock(MusicManager.class);
        spotifUM.setMusicManager(newMusicManager);
        
        assertEquals(newMusicManager, spotifUM.getMusicManager());
    }

    /**
     * Tests the getter and setter methods for PlaylistManager.
     * Verifies that the PlaylistManager component can be properly retrieved and updated.
     * Ensures that the set manager is correctly returned by the getter.
     */
    @Test
    void testPlaylistManagerGetterAndSetter() {
        PlaylistManager newPlaylistManager = mock(PlaylistManager.class);
        spotifUM.setPlaylistManager(newPlaylistManager);
        
        assertEquals(newPlaylistManager, spotifUM.getPlaylistManager());
    }

    /**
     * Tests the getter and setter methods for AlbumManager.
     * Verifies that the AlbumManager component can be properly retrieved and updated.
     * Ensures that the set manager is correctly returned by the getter.
     */
    @Test
    void testAlbumManagerGetterAndSetter() {
        AlbumManager newAlbumManager = mock(AlbumManager.class);
        spotifUM.setAlbumManager(newAlbumManager);
        
        assertEquals(newAlbumManager, spotifUM.getAlbumManager());
    }

    /**
     * Tests the getter and setter methods for Authenticator.
     * Verifies that the Authenticator component can be properly retrieved and updated.
     * Ensures that the set authenticator is correctly returned by the getter.
     */
    @Test
    void testAuthenticatorGetterAndSetter() {
        Authenticator newAuthenticator = mock(Authenticator.class);
        spotifUM.setAuthenticator(newAuthenticator);
        
        assertEquals(newAuthenticator, spotifUM.getAuthenticator());
    }

    /**
     * Tests the auto-save feature toggle functionality.
     * Verifies that the auto-save setting can be properly retrieved and updated.
     * Tests both enabling and disabling the auto-save feature.
     */
    @Test
    void testAutoSaveGetterAndSetter() {
        spotifUM.setAutosave(false);
        assertFalse(spotifUM.getAutoSave());
        
        spotifUM.setAutosave(true);
        assertTrue(spotifUM.getAutoSave());
    }

    /**
     * Tests the user existence check functionality.
     * Verifies that the method correctly delegates to UserManager.
     * Tests both existing and non-existing user scenarios.
     */
    @Test
    void testUserExists() {
        when(mockUserManager.existsUser(1)).thenReturn(true);
        when(mockUserManager.existsUser(2)).thenReturn(false);
        
        assertTrue(spotifUM.userExists(1));
        assertFalse(spotifUM.userExists(2));
        
        verify(mockUserManager).existsUser(1);
        verify(mockUserManager).existsUser(2);
    }

    /**
     * Tests the clone method of SpotifUM.
     * Verifies that a deep copy is created with all components properly cloned.
     * Ensures that the cloned instance has independent components from the original.
     */
    @Test
    void testClone() {
        SpotifUM clone = spotifUM.clone();
        
        assertNotSame(spotifUM, clone);
        assertNotSame(spotifUM.getUserManager(), clone.getUserManager());
        assertNotSame(spotifUM.getMusicManager(), clone.getMusicManager());
        assertNotSame(spotifUM.getPlaylistManager(), clone.getPlaylistManager());
        assertNotSame(spotifUM.getAlbumManager(), clone.getAlbumManager());
        assertNotSame(spotifUM.getHistory(), clone.getHistory());
    }

    /**
     * Tests the equals method of SpotifUM.
     * Verifies that two SpotifUM instances are considered equal when they have
     * equivalent components and settings.
     * Tests equality with identical objects, different objects, null, and objects of different types.
     */
    @Test
    void testEquals() {
        SpotifUM same = new SpotifUM(
            mockUserManager,
            mockMusicManager,
            mockPlaylistManager,
            mockAlbumManager,
            mockHistory,
            mockAuthenticator,
            true
        );
        
        SpotifUM different = new SpotifUM(
            mock(UserManager.class),
            mockMusicManager,
            mockPlaylistManager,
            mockAlbumManager,
            mockHistory,
            mockAuthenticator,
            true
        );
        
        assertEquals(spotifUM, same);
        assertNotEquals(spotifUM, different);
        assertNotEquals(spotifUM, null);
        assertNotEquals(spotifUM, new Object());
    }
}