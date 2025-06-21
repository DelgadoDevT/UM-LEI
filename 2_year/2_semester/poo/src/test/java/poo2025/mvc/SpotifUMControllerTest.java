package poo2025.mvc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import poo2025.common.Menu;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit test class for validating the behavior of the {@code SpotifUMController} class.
 *
 * The {@code SpotifUMControllerTest} class includes various test cases to ensure
 * that the methods in {@code SpotifUMController} behave as expected under different
 * conditions and scenarios.
 *
 * It uses Mockito to mock required dependencies and simulate behavior for
 * interactions with external components, such as the {@code SpotifUM} service and user input via {@code Menu}.
 *
 * The test class follows the arrangement of the test setup, execution, and verification
 * to confirm that the tested method behaves correctly and meets defined requirements.
 */
class SpotifUMControllerTest {
    /**
     * An {@code AutoCloseable} resource used for managing Mockito's mocking framework
     * lifecycle in the {@code SpotifUMControllerTest} test class. This is used to
     * ensure proper cleanup of mocks and avoid resource leakage after each test execution.
     */
    private AutoCloseable mockitoCloseable;

    /**
     * Sets up the testing environment before each test method execution.
     *
     * This setup method initializes mock dependencies and prepares the necessary
     * configurations for the test environment. Specifically, it uses
     * Mockito to mock static calls to the {@code Menu} class and assigns the
     * resulting mock to {@code mockitoCloseable}.
     *
     * Ensures that the tests are isolated and that no residual state or
     * dependencies affect later test executions.
     */
    @BeforeEach
    void setUp() {mockitoCloseable = Mockito.mockStatic(Menu.class);}

    /**
     * Performs cleanup operations after each test method execution.
     *
     * Closes any resources initialized during the test setup phase,
     * ensuring proper resource management and preventing leaks or residual states
     * that might affect later tests.
     *
     * @throws Exception if an error occurs while closing resources.
     */
    @AfterEach
    void tearDown() throws Exception {mockitoCloseable.close();}

    /**
     * Tests the behavior of the {@code handleRecommendedPlaylist} method
     * when the user does not exist in the system.
     *
     * The test verifies that the method correctly checks for the existence
     * of the user by mocking the {@code userExists} method to return {@code false}.
     * It asserts that the appropriate error message ("User not found") is returned
     * and ensures that the {@code userExists} method is called with the correct parameters.
     *
     * Preconditions:
     * - The user does not exist in the system.
     *
     * Expected Outcome:
     * - The method returns the string "User not found".
     * - The {@code userExists} method is invoked once with the specified user ID.
     */
    @Test
    void testHandleRecommendedPlaylist_UserDoesNotExist() {
        SpotifUM mockSpotifUM = Mockito.mock(SpotifUM.class);
        SpotifUMController controller = new SpotifUMController(mockSpotifUM);

        when(mockSpotifUM.userExists(anyInt())).thenReturn(false);

        String result = controller.handleRecommendedPlaylist(1);

        assertEquals("User not found", result);
        verify(mockSpotifUM).userExists(1);
    }

    /**
     * Tests the behavior of the {@code handleRecommendedPlaylist} method in the
     * {@code SpotifUMController} class when a non-premium user attempts to access
     * the feature.
     *
     * Validates that:
     * - A user exists in the system.
     * - The user is identified as not being a premium user.
     * - The response indicates that the feature is restricted to premium users.
     *
     * Ensures that the appropriate service methods {@code userExists} and {@code isPremiumUser}
     * are invoked with the correct parameters.
     */
    @Test
    void testHandleRecommendedPlaylist_UserNotPremium() {
        SpotifUM mockSpotifUM = Mockito.mock(SpotifUM.class);
        SpotifUMController controller = new SpotifUMController(mockSpotifUM);

        when(mockSpotifUM.userExists(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumUser(anyInt())).thenReturn(false);

        String result = controller.handleRecommendedPlaylist(1);

        assertEquals("This feature is VIP only. Become Premium and join the cool club 😎.", result);
        verify(mockSpotifUM).userExists(1);
        verify(mockSpotifUM).isPremiumUser(1);
    }

    /**
     * Tests the `handleRecommendedPlaylist` method of the `SpotifUMController` class
     * for a scenario where the user exists and is a Premium user but not a PremiumTop user.
     * Verifies that the proper message is returned for users who are not eligible
     * for the PremiumTop feature.
     *
     * This test:
     * - Mocks the `SpotifUM` service to simulate user existence and subscription status.
     * - Ensures that the method returns the correct message when the user is not a PremiumTop user.
     * - Validates the interactions with the mocked `SpotifUM` instance to confirm correct method calls.
     *
     * Preconditions:
     * The user exists and is a Premium user but is not subscribed to the PremiumTop plan.
     *
     * Postconditions:
     * The result string indicates that the feature is restricted to PremiumTop plan users,
     * and related interactions with the mock object are verified.
     */
    @Test
    void testHandleRecommendedPlaylist_UserNotPremiumTop() {
        SpotifUM mockSpotifUM = Mockito.mock(SpotifUM.class);
        SpotifUMController controller = new SpotifUMController(mockSpotifUM);

        when(mockSpotifUM.userExists(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumUser(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumTop(anyInt())).thenReturn(false);

        String result = controller.handleRecommendedPlaylist(1);

        assertEquals("This feature is PremiumTop plan only", result);
        verify(mockSpotifUM).userExists(1);
        verify(mockSpotifUM).isPremiumUser(1);
        verify(mockSpotifUM).isPremiumTop(1);
    }

    /**
     * Tests the behavior of the SpotifUMController's handleRecommendedPlaylist method
     * when the recommended playlist to be handled is a simple playlist.
     *
     * The test uses Mockito to mock the SpotifUM system and simulate a scenario where:
     * 1. The user exists in the system.
     * 2. The user is a premium user.
     * 3. The user qualifies as a premium top user.
     * 4. The menu interaction returns an input indicating a simple playlist with a specific parameter.
     * 5. The SpotifUM system generates a "simple playlist" recommendation.
     *
     * Assertions are made to ensure:
     * - The generated playlist matches the expected output.
     * - The SpotifUM methods `userExists`, `isPremiumUser`, `isPremiumTop`,
     *   and `generateRecommendedPlaylist` are invoked with the correct parameters.
     *
     * This test ensures that the system correctly handles and processes a request for
     * a simple recommended playlist for a valid premium top user.
     */
    @Test
    void testHandleRecommendedPlaylist_SimplePlaylist() {
        SpotifUM mockSpotifUM = Mockito.mock(SpotifUM.class);
        SpotifUMController controller = new SpotifUMController(mockSpotifUM);

        when(mockSpotifUM.userExists(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumUser(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumTop(anyInt())).thenReturn(true);
        when(Menu.getStringWithMessage(anyString())).thenReturn("simple");
        when(Menu.getIntWithMessage(anyString())).thenReturn(5);
        when(mockSpotifUM.generateRecommendedPlaylist(anyInt(), anyInt())).thenReturn("Generated Simple Playlist");

        String result = controller.handleRecommendedPlaylist(1);

        assertEquals("Generated Simple Playlist", result);
        verify(mockSpotifUM).userExists(1);
        verify(mockSpotifUM).isPremiumUser(1);
        verify(mockSpotifUM).isPremiumTop(1);
        verify(mockSpotifUM).generateRecommendedPlaylist(1, 5);
    }

    /**
     * Tests the behavior of the {@code handleRecommendedPlaylist} method in the context of generating a
     * recommended timed playlist for a premium user who meets all necessary conditions.
     *
     * This test verifies the following:
     * - The user exists in the system.
     * - The user is a premium subscriber.
     * - The user is considered a premium top user.
     * - The correct input is handled to generate a timed playlist with specific parameters.
     * - The result of playlist generation matches the expected output.
     *
     * Mock dependencies used in this test:
     * - {@code SpotifUM}: Used to simulate behavior for checking user existence, premium status, and
     *   generating the recommended playlist.
     * - {@code Menu}: Simulates user input for a playlist type, duration, and genre.
     *
     * Assertions and validations:
     * - Confirms {@code handleRecommendedPlaylist} returns the correct playlist string generated by
     *   {@code SpotifUM}.
     * - Ensures all necessary methods in the mocked {@code SpotifUM} and {@code Menu} are invoked with the correct inputs.
     */
    @Test
    void testHandleRecommendedPlaylist_TimedPlaylist() {
        SpotifUM mockSpotifUM = Mockito.mock(SpotifUM.class);
        SpotifUMController controller = new SpotifUMController(mockSpotifUM);

        when(mockSpotifUM.userExists(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumUser(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumTop(anyInt())).thenReturn(true);
        when(Menu.getStringWithMessage(anyString())).thenReturn("timed", "rock");
        when(Menu.getIntWithMessage(anyString())).thenReturn(120);
        when(mockSpotifUM.generateRecommendedTimedPlaylist(anyInt(), anyInt(), anyString())).thenReturn("Generated Timed Playlist");

        String result = controller.handleRecommendedPlaylist(1);

        assertEquals("Generated Timed Playlist", result);
        verify(mockSpotifUM).userExists(1);
        verify(mockSpotifUM).isPremiumUser(1);
        verify(mockSpotifUM).isPremiumTop(1);
        verify(mockSpotifUM).generateRecommendedTimedPlaylist(1, 120, "rock");
    }

    /**
     * Tests the functionality of the {@code handleRecommendedPlaylist} method in the {@code SpotifUMController} class
     * when generating a recommended explicit playlist for a user.
     *
     * This test case verifies the following:
     * - The user exists.
     * - The user has a premium account.
     * - The user qualifies as a premium top user.
     * - Proper interactions with mocked objects, including the generation of an explicit playlist.
     * - The returned result matches the expected explicit playlist string.
     *
     * The method uses mocked dependencies to simulate the required behavior and validates the interactions
     * and outputs using assertions and verifications.
     */
    @Test
    void testHandleRecommendedPlaylist_ExplicitPlaylist() {
        SpotifUM mockSpotifUM = Mockito.mock(SpotifUM.class);
        SpotifUMController controller = new SpotifUMController(mockSpotifUM);

        when(mockSpotifUM.userExists(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumUser(anyInt())).thenReturn(true);
        when(mockSpotifUM.isPremiumTop(anyInt())).thenReturn(true);
        when(Menu.getStringWithMessage(anyString())).thenReturn("explicit");
        when(Menu.getIntWithMessage(anyString())).thenReturn(10);
        when(mockSpotifUM.generateRecommendedExplicitPlaylist(anyInt(), anyInt())).thenReturn("Generated Explicit Playlist");

        String result = controller.handleRecommendedPlaylist(1);

        assertEquals("Generated Explicit Playlist", result);
        verify(mockSpotifUM).userExists(1);
        verify(mockSpotifUM).isPremiumUser(1);
        verify(mockSpotifUM).isPremiumTop(1);
        verify(mockSpotifUM).generateRecommendedExplicitPlaylist(1, 10);
    }
}