package poo2025.mvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import poo2025.common.Menu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link SpotifUMView} that validates the view component's behavior in the SpotifUM application.
 * This class uses Mockito framework to mock dependencies and verify interactions between the view and its
 * controller, as well as the menu system.
 *
 * <p>The tests focus on various user scenarios including authentication, menu navigation,
 * and different types of user interactions with the system.</p>
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifUMViewTest {
    /**
     * Mock object for the static Menu class functionality
     */
    private MockedStatic<Menu> mockedMenu;

    /**
     * Mock object for the SpotifUMController
     */
    @Mock
    private SpotifUMController mockController;

    /**
     * Sets up the test environment before each test.
     * Initializes the mocked Menu static methods and creates a mock controller.
     */
    @BeforeEach
    void setUp() {
        mockedMenu = mockStatic(Menu.class);
        mockController = mock(SpotifUMController.class);
    }

    /**
     * Cleans up resources after each test.
     * Closes the mocked Menu static context.
     */
    @AfterEach
    void tearDown() {
        mockedMenu.close();
    }

    /**
     * Tests that the view correctly displays welcome and goodbye messages
     * during program execution.
     * Verifies that the appropriate styling methods are called and
     * the authentication menu is displayed.
     */
    @Test
    void testRun_DisplaysWelcomeAndGoodbye() {
        // Arrange
        SpotifUMView view = new SpotifUMView(mockController);

        mockedMenu.when(() -> Menu.printAuthenticationMenu()).thenReturn("Authentication Menu");
        mockedMenu.when(() -> Menu.applyColorAndStyle(anyString(), any(), any())).thenReturn("Styled Text");
        mockedMenu.when(() -> Menu.runMenu(anyInt(), anyInt())).thenReturn(0);

        // Act
        view.run();

        // Assert
        mockedMenu.verify(() -> Menu.applyColorAndStyle(anyString(), any(), any()), times(2));
        mockedMenu.verify(() -> Menu.printAuthenticationMenu(), times(1));
    }

    /**
     * Tests the authentication menu's exit functionality.
     * Verifies that when the user chooses to exit, no login attempt is made
     * and the state is not saved.
     */
    @Test
    void testRun_AuthenticationMenuExits() {
        // Arrange
        SpotifUMView view = new SpotifUMView(mockController);

        mockedMenu.when(() -> Menu.printAuthenticationMenu()).thenReturn("Authentication Menu");
        mockedMenu.when(() -> Menu.applyColorAndStyle(anyString(), any(), any())).thenReturn("Styled Welcome Text");
        mockedMenu.when(() -> Menu.runMenu(0, 2)).thenReturn(0);

        // Act
        view.run();

        // Assert
        mockedMenu.verify(() -> Menu.runMenu(0, 2), times(1));
        mockedMenu.verify(() -> Menu.printAuthenticationMenu(), times(1));
        verify(mockController, never()).login(anyInt(), anyString());
        verify(mockController, never()).saveState();
    }

    /**
     * Tests successful admin login functionality.
     * Verifies that the system correctly handles admin authentication
     * using the special admin ID (-1).
     */
    @Test
    void testRun_ValidLoginForAdmin() {
        // Arrange
        SpotifUMView view = new SpotifUMView(mockController);

        mockedMenu.when(() -> Menu.printAuthenticationMenu()).thenReturn("Authentication Menu");
        mockedMenu.when(() -> Menu.applyColorAndStyle(anyString(), any(), any())).thenReturn("Styled Welcome Text");
        mockedMenu.when(() -> Menu.getIntWithMessage(anyString())).thenReturn(-1);
        mockedMenu.when(() -> Menu.getStringWithMessage(anyString())).thenReturn("adminPassword");
        mockedMenu.when(() -> Menu.runMenu(0, 2)).thenReturn(1).thenReturn(0);

        when(mockController.login(-1, "adminPassword")).thenReturn(true);

        // Act
        view.run();

        // Assert
        verify(mockController, times(1)).login(-1, "adminPassword");
    }

    /**
     * Tests the system's behavior when invalid login credentials are provided.
     * Verifies that the system properly handles failed login attempts and
     * continues to operate without saving state.
     */
    @Test
    void testRun_InvalidLogin() {
        // Arrange
        SpotifUMView view = new SpotifUMView(mockController);

        mockedMenu.when(() -> Menu.printAuthenticationMenu()).thenReturn("Authentication Menu");
        mockedMenu.when(() -> Menu.applyColorAndStyle(anyString(), any(), any())).thenReturn("Styled Welcome Text");
        mockedMenu.when(() -> Menu.getIntWithMessage(anyString())).thenReturn(123);
        mockedMenu.when(() -> Menu.getStringWithMessage(anyString())).thenReturn("wrongPassword");
        mockedMenu.when(() -> Menu.runMenu(0, 2)).thenReturn(1).thenReturn(0);

        when(mockController.login(123, "wrongPassword")).thenReturn(false);
        when(mockController.spotifUMAutoSaveState()).thenReturn(false);

        // Act
        view.run();

        // Assert
        verify(mockController, times(1)).login(123, "wrongPassword");
        verify(mockController, never()).saveState();
    }

    /**
     * Tests the new user registration process.
     * Verifies that the system correctly handles user registration,
     * assigns an ID, and automatically logs in the new user.
     */
    @Test
    void testRun_RegistersNewUser() {
        // Arrange
        SpotifUMView view = new SpotifUMView(mockController);

        mockedMenu.when(() -> Menu.printAuthenticationMenu()).thenReturn("Authentication Menu");
        mockedMenu.when(() -> Menu.applyColorAndStyle(anyString(), any(), any())).thenReturn("Styled Welcome Text");
        mockedMenu.when(() -> Menu.runMenu(0, 2)).thenReturn(2).thenReturn(0);
        mockedMenu.when(() -> Menu.getStringWithMessage(anyString())).thenReturn("newPassword");

        when(mockController.addUserGeneric("newPassword")).thenReturn(456);
        when(mockController.login(456, "newPassword")).thenReturn(true);

        // Act
        view.run();

        // Assert
        verify(mockController, times(1)).addUserGeneric("newPassword");
        verify(mockController, times(1)).login(456, "newPassword");
        mockedMenu.verify(() -> Menu.getStringWithMessage(anyString()), times(1));
    }
}