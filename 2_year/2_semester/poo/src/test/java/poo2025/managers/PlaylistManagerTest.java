package poo2025.managers;

import org.junit.jupiter.api.Test;
import poo2025.entities.Playlist.Playlist;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test suite for the PlaylistManager class.
 *
 * This class is designed to verify the functionality of the PlaylistManager's
 * methods, ensuring that they behave as expected in various scenarios. The tests
 * cover cases such as handling empty playlists, retrieving initialized playlists,
 * ensuring immutability of returned data, and verifying deep cloning of playlists.
 */
public class PlaylistManagerTest {
    /**
     * Tests the behavior of the getPlaylists method when no playlists are present.
     *
     * The test ensures that:
     * 1. The returned map is not null.
     * 2. The returned map is empty when no playlists have been added.
     */
    @Test
    public void testGetPlaylists_NoPlaylists() {
        PlaylistManager playlistManager = new PlaylistManager();
        Map<Integer, Playlist> result = playlistManager.getPlaylists();
        assertNotNull(result, "Returned playlist map should not be null");
        assertTrue(result.isEmpty(), "Returned playlist map should be empty");
    }

    /**
     * Tests the behavior of the PlaylistManager when playlists are initialized and retrieved.
     *
     * Validates that the playlists are correctly returned and match the ones provided during
     * initialization. Also checks that the size and keys of the returned map are consistent
     * with the initial data.
     *
     * The test ensures:
     * - The returned playlist map is not null.
     * - The size of the returned map is correct.
     * - The keys of the returned map match those used during initialization.
     * - The playlists retrieved from the map match the original playlists.
     */
    @Test
    public void testGetPlaylists_WithPlaylists() {
        Playlist playlistMock1 = mock(Playlist.class);
        Playlist playlistMock2 = mock(Playlist.class);
        when(playlistMock1.clone()).thenReturn(playlistMock1);
        when(playlistMock2.clone()).thenReturn(playlistMock2);

        Map<Integer, Playlist> initialPlaylists = new HashMap<>();
        initialPlaylists.put(1, playlistMock1);
        initialPlaylists.put(2, playlistMock2);

        PlaylistManager playlistManager = new PlaylistManager(initialPlaylists, 2);
        Map<Integer, Playlist> result = playlistManager.getPlaylists();

        assertNotNull(result, "Returned playlist map should not be null");
        assertEquals(2, result.size(), "Returned playlist map size should match the initialized size");

        assertTrue(result.containsKey(1) && result.containsKey(2), "Returned map should contain the same keys as initialized map");
        assertEquals(playlistMock1, result.get(1), "Playlist at key 1 should match the initialized playlist");
        assertEquals(playlistMock2, result.get(2), "Playlist at key 2 should match the initialized playlist");
    }

    /**
     * Tests the immutability of the playlist map returned by the `getPlaylists` method in the `PlaylistManager` class.
     *
     * The method initializes a `PlaylistManager` instance with a predefined map of playlists
     * and verifies if the map returned by `getPlaylists` can be modified outside the manager's scope.
     * Attempting to modify the returned map should not affect the original map inside the `PlaylistManager`.
     *
     * Test Process:
     * 1. Mock a `Playlist` instance.
     * 2. Create and populate a map with the mocked playlist.
     * 3. Instantiate the `PlaylistManager` with this map.
     * 4. Retrieve the playlist map using `getPlaylists` and attempt to modify it.
     * 5. Ensure the original playlist map in the `PlaylistManager` remains unchanged by asserting its size.
     *
     * Assertions:
     * - The size of the playlist map inside the `PlaylistManager` should remain unaffected by any
     *   external modifications attempted on the returned map.
     */
    @Test
    public void testGetPlaylists_ImmutableResultMap() {
        Playlist playlistMock = mock(Playlist.class);
        when(playlistMock.clone()).thenReturn(playlistMock);

        Map<Integer, Playlist> initialPlaylists = new HashMap<>();
        initialPlaylists.put(1, playlistMock);

        PlaylistManager playlistManager = new PlaylistManager(initialPlaylists, 1);
        Map<Integer, Playlist> result = playlistManager.getPlaylists();

        result.put(2, mock(Playlist.class)); // Try modifying the returned map

        assertEquals(1, playlistManager.getPlaylists().size(), "Original playlist map in PlaylistManager should remain unchanged");
    }

    /**
     * Validates that the `getPlaylists` method of the `PlaylistManager` class returns a map where
     * each playlist is a clone of the original playlist stored in the manager.
     *
     * Specifically, this test ensures that:
     * 1. The playlists retrieved from the `getPlaylists` method are different instances
     *    as the original playlists stored within the `PlaylistManager`.
     * 2. The playlists retrieved from the `getPlaylists` method are clones (deep copies)
     *    of the original objects.
     *
     * Mocks and stubs are used to simulate the behavior of the `Playlist` class and its `clone` method.
     * Assertions verify the behavior and integrity of the returned playlists.
     */
    @Test
    public void testGetPlaylists_CloneOfOriginalPlaylists() {
        Playlist playlistMock = mock(Playlist.class);
        Playlist clonedPlaylistMock = mock(Playlist.class);
        when(playlistMock.clone()).thenReturn(clonedPlaylistMock);

        Map<Integer, Playlist> initialPlaylists = new HashMap<>();
        initialPlaylists.put(1, playlistMock);

        PlaylistManager playlistManager = new PlaylistManager(initialPlaylists, 1);
        Map<Integer, Playlist> result = playlistManager.getPlaylists();

        assertNotSame(playlistMock, result.get(1), "Returned playlist should be a clone and not the original object");
        assertSame(clonedPlaylistMock, result.get(1), "Returned playlist should match the cloned instance");
    }
}