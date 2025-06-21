package poo2025.managers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import poo2025.entities.Album.Album;
import poo2025.exceptions.AlbumException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit test class for the AlbumManager class.
 *
 * This class contains various test cases to verify the functionality,
 * exception handling, and constraints associated with the AlbumManager class.
 */
class AlbumManagerTest {
    /**
     * Verifies that an album can be successfully added to the AlbumManager and the associated
     * internal state is updated correctly.
     *
     * This test ensures that when a valid album is added:
     * 1. The album is stored in the internal album collection within the AlbumManager.
     * 2. The album can be retrieved using its identifier.
     * 3. The total count of albums in the AlbumManager is incremented.
     *
     * Preconditions:
     * - The album provided has a valid identifier (non-null, non-empty).
     * - The album supports cloning, and the clone operation does not throw an exception.
     *
     * Expected Outcome:
     * - The album is successfully added to the AlbumManager.
     * - The album is accessible via its identifier in the manager's album collection.
     * - The total count of albums reflects the addition of the album.
     *
     * @throws AlbumException if the album addition process fails due to internal errors or constraints.
     */
    @Test
    void testAddAlbumSuccessfully() throws AlbumException {
        AlbumManager albumManager = new AlbumManager();
        Album album = Mockito.mock(Album.class);
        when(album.getIdentifier()).thenReturn(1);
        when(album.clone()).thenReturn(album);

        albumManager.addAlbum(album);

        assertTrue(albumManager.getAlbums().containsKey(1));
        assertEquals(album, albumManager.getAlbums().get(1));
        assertEquals(1, albumManager.getTotalAlbums());
    }

    /**
     * Tests that adding a null album to the AlbumManager throws an AlbumException.
     *
     * This test verifies that the method addAlbum in the AlbumManager class
     * correctly handles null input by throwing the appropriate AlbumException.
     *
     * The expected behavior is that invoking addAlbum with a null parameter
     * results in an exception of type AlbumException being thrown.
     *
     * @throws AlbumException if a null album is added
     */
    @Test
    void testAddNullAlbumThrowsException() {
        AlbumManager albumManager = new AlbumManager();

        assertThrows(AlbumException.class, () -> albumManager.addAlbum(null));
    }

    /**
     * Tests that attempting to add a duplicate album to the album manager does not
     * increase the total count of albums.
     *
     * This test ensures that the album manager prevents duplicate entries based
     * on a unique identifier for each album. While the same album instance is
     * added twice, the total album count remains unchanged, and the album is
     * stored exactly once.
     *
     * @throws AlbumException if an error occurs during the execution of the
     *                         `addAlbum` method, such as issues with album
     *                         processing or validation.
     */
    @Test
    void testAddDuplicateAlbumDoesNotIncreaseTotalCount() throws AlbumException {
        AlbumManager albumManager = new AlbumManager();
        Album album = Mockito.mock(Album.class);
        when(album.getIdentifier()).thenReturn(1);
        when(album.clone()).thenReturn(album);
        albumManager.addAlbum(album);

        albumManager.addAlbum(album);

        assertEquals(1, albumManager.getTotalAlbums());
        assertTrue(albumManager.getAlbums().containsKey(1));
    }

    /**
     * Tests that the {@code addAlbum} method in the {@code AlbumManager} class throws an {@code AlbumException}
     * when an error occurs during the cloning process of the provided {@code Album} object.
     *
     * This test ensures proper exception handling and validation to prevent an invalid
     * state in the application when cloning fails.
     *
     * Key Verifications:
     * - An {@code AlbumException} is thrown with the expected message ("Error adding album").
     * - The album is not added to the album manager's internal collection.
     * - The total album count remains unchanged.
     *
     * @throws AlbumException if an error occurs during the test execution related to album addition.
     */
    @Test
    void testAddAlbumThrowsExceptionOnErrorDuringClone() throws AlbumException {
        AlbumManager albumManager = new AlbumManager();
        Album album = Mockito.mock(Album.class);
        when(album.getIdentifier()).thenReturn(1);
        when(album.clone()).thenThrow(new RuntimeException("Clone failed"));

        AlbumException exception = assertThrows(AlbumException.class, () -> albumManager.addAlbum(album));
        assertEquals("Error adding album", exception.getMessage());
        assertFalse(albumManager.getAlbums().containsKey(1));
        assertEquals(0, albumManager.getTotalAlbums());
    }
}