/*
 * Copyright (c) 2025. João Delgado, Nelson Mendes, Simão Mendes
 *
 * License: MIT
 *
 * Permission is granted to use, copy, modify, and distribute this work,
 * provided that the copyright notice and this license are included in all copies.
 */
package poo2025.common;

import org.junit.jupiter.api.Test;
import poo2025.common.Persistence;
import poo2025.mvc.SpotifUM;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@code PersistenceTest} class contains unit test cases to validate the functionality of
 * the {@code Persistence} class's methods, specifically focusing on the {@code loadState} method.
 *
 * These tests ensure that the {@code loadState} method behaves as expected under various conditions,
 * such as handling valid and invalid serialized files, empty files, non-existent files, and corrupt files.
 *
 * Each test is designed to verify the proper deserialization of {@code SpotifUM} objects or
 * the appropriate handling of error scenarios. Tests include proper file setup, validation of outcomes,
 * and cleanup of test artifacts to maintain a clean test environment.
 */
public class PersistenceTest {
    /**
     * Tests that the method {@code Persistence.loadState} correctly loads a valid serialized file
     * and returns an instance of {@code SpotifUM} that matches the expected state.
     *
     * The test verifies the following conditions:
     * - The returned {@code SpotifUM} instance is not null.
     * - The returned {@code SpotifUM} instance matches the expected state.
     *
     * A temporary serialized file is created for testing. After the test execution, the file
     * is deleted to ensure that no residual test artifacts are left in the file system.
     */
    @Test
    public void testLoadState_ValidFile_ReturnsSpotifUMInstance() {
        String validFileName = "valid-state.ser";
        SpotifUM expectedSpotifUM = new SpotifUM();
        Persistence.saveState(expectedSpotifUM, validFileName);

        SpotifUM loadedSpotifUM = Persistence.loadState(validFileName);

        assertNotNull(loadedSpotifUM, "Loaded SpotifUM instance should not be null.");
        assertEquals(expectedSpotifUM, loadedSpotifUM, "Loaded SpotifUM instance should match the expected state.");

        new File(validFileName).delete();
    }

    /**
     * Tests the behavior of the `Persistence.loadState` method when attempting to load
     * a file containing invalid serialized content.
     *
     * This test ensures that the method returns null when the content of the file is
     * not a valid serialized representation of a `SpotifUM` object.
     *
     * Scenario:
     * - A file is created with content that is not a serialized `SpotifUM` object.
     * - The `loadState` method is called with the file's path.
     * - The expected outcome is a `null` return value, indicating that the invalid
     *   content could not be deserialized.
     *
     * Assertions:
     * - The method should return `null` when the file content is invalid.
     *
     * Cleanup:
     * - The test ensures that the created file is deleted after execution.
     */
    @Test
    public void testLoadState_InvalidFileContent_ReturnsNull() {
        String invalidFileName = "invalid-content.ser";
        try (java.io.FileWriter writer = new java.io.FileWriter(invalidFileName)) {
            writer.write("This is not a serialized SpotifUM object.");
        } catch (Exception e) {
            fail("Test setup failed: Could not create a file with invalid content.");
        }

        SpotifUM loadedSpotifUM = Persistence.loadState(invalidFileName);

        assertNull(loadedSpotifUM, "Loaded SpotifUM instance should be null for invalid file content.");

        new File(invalidFileName).delete();
    }

    /**
     * Tests the behavior of the `loadState` method when attempting to load a non-existent file.
     * The method should return null in this scenario.
     *
     * This test ensures:
     * - If the specified file does not exist, the `loadState` method will not attempt to load any data.
     * - The method should handle the case of a missing file gracefully by returning null without throwing an exception.
     *
     * Preconditions:
     * - The file specified by the `nonExistentFileName` does not exist.
     *
     * Expected Outcome:
     * - The `loadState` method returns null.
     */
    @Test
    public void testLoadState_FileDoesNotExist_ReturnsNull() {
        String nonExistentFileName = "non-existent-file.ser";

        SpotifUM loadedSpotifUM = Persistence.loadState(nonExistentFileName);

        assertNull(loadedSpotifUM, "Loaded SpotifUM instance should be null when file does not exist.");
    }

    /**
     * Tests the behavior of the Persistence.loadState method when attempting to load
     * the state from an empty file. Ensures that the method returns null in such scenarios.
     *
     * The test performs the following steps:
     * 1. Creates an empty file with a predefined name.
     * 2. Calls the loadState method to load a SpotifUM instance from the empty file.
     * 3. Asserts that the returned SpotifUM instance is null.
     * 4. Deletes the created empty file after the test execution.
     *
     * Failing scenarios:
     * - If the test setup fails to create the empty file, the test will mark the setup as failed.
     * - If the loadState method returns a non-null value for an empty file, the assertion fails.
     */
    @Test
    public void testLoadState_EmptyFile_ReturnsNull() {
        String emptyFileName = "empty.ser";
        try {
            new File(emptyFileName).createNewFile();
        } catch (Exception e) {
            fail("Test setup failed: Could not create an empty file.");
        }

        SpotifUM loadedSpotifUM = Persistence.loadState(emptyFileName);

        assertNull(loadedSpotifUM, "Loaded SpotifUM instance should be null for an empty file.");

        new File(emptyFileName).delete();
    }

    /**
     * Tests the behavior of the {@code Persistence.loadState(String)} method when attempting
     * to load a corrupt serialized file.
     *
     * The method verifies that when a corrupt file is provided, the method correctly
     * returns {@code null}, indicating it could not load a valid state.
     *
     * Preconditions:
     * - A file with the name "corrupt.ser" is created, and invalid binary data is written to it
     *   to simulate a corrupt serialized file.
     *
     * Validation:
     * - Asserts that the returned value from {@code Persistence.loadState(String)} for the corrupt
     *   file is {@code null}.
     *
     * Cleanup:
     * - Deletes the created corrupt file after the test execution to maintain a clean test environment.
     */
    @Test
    public void testLoadState_CorruptFile_ReturnsNull() {
        String corruptFileName = "corrupt.ser";
        try {
            java.nio.file.Files.write(new File(corruptFileName).toPath(), new byte[]{0x00, 0x01, 0x02});
        } catch (Exception e) {
            fail("Test setup failed: Could not create a corrupt file.");
        }

        SpotifUM loadedSpotifUM = Persistence.loadState(corruptFileName);

        assertNull(loadedSpotifUM, "Loaded SpotifUM instance should be null for a corrupt file.");

        new File(corruptFileName).delete();
    }
}