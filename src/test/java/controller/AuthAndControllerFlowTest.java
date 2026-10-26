package controller;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import storage.SavedGame;
import storage.UserSettings;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthAndControllerFlowTest {
    @TempDir
    Path tempDir;

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(tempDir);
    }

    @Test
    void testInitialStateNotAuthenticated() {
        assertFalse(controller.isAuthenticated());
        assertNull(controller.getCurrentUser());
    }

    @Test
    void testSuccessfulRegistrationAndLogin() {
        User registered = controller.register("player1", "password123");
        assertNotNull(registered);
        assertEquals("player1", registered.getUsername());
        assertTrue(controller.isAuthenticated());
        assertEquals("player1", controller.getCurrentUser().getUsername());

        // Logout
        controller.logout();
        assertFalse(controller.isAuthenticated());
        assertNull(controller.getCurrentUser());

        // Login again
        User loggedIn = controller.login("player1", "password123");
        assertNotNull(loggedIn);
        assertTrue(controller.isAuthenticated());
        assertEquals("player1", controller.getCurrentUser().getUsername());
    }

    @Test
    void testFailedLoginThrowsExceptionAndLeavesUnauthenticated() {
        controller.register("player1", "password123");
        controller.logout();

        // Wrong password
        assertThrows(IllegalArgumentException.class, () -> controller.login("player1", "wrongpass"));
        assertFalse(controller.isAuthenticated());
        assertNull(controller.getCurrentUser());

        // Unknown user
        assertThrows(IllegalArgumentException.class, () -> controller.login("unknownUser", "password"));
        assertFalse(controller.isAuthenticated());
        assertNull(controller.getCurrentUser());
    }

    @Test
    void testUserDataAndSavedGamesIsolationBetweenUsers() {
        // User 1 logs in and saves a game
        controller.register("userOne", "pass123");
        SavedGame g1 = controller.saveCurrentGame(95.0);
        assertEquals("userOne", g1.getUsername());

        List<SavedGame> userOneGames = controller.savedGames();
        assertEquals(1, userOneGames.size());
        assertEquals(g1.getId(), userOneGames.get(0).getId());

        // Save preference for userOne
        UserSettings u1Settings = new UserSettings();
        u1Settings.setBoardTheme(UserSettings.BoardTheme.WOOD);
        controller.saveCurrentSettings(u1Settings);

        // Switch to User 2
        controller.logout();
        controller.register("userTwo", "pass456");

        // User 2 should NOT see user 1's saved games
        List<SavedGame> userTwoGames = controller.savedGames();
        assertEquals(0, userTwoGames.size());

        // User 2 should have default settings or their own, not user 1's WOOD theme
        assertEquals(UserSettings.BoardTheme.CLASSIC_GREEN, controller.getCurrentSettings().getBoardTheme());
    }
}
