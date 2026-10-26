package storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SettingsStorageTest {
    @TempDir
    Path tempDir;

    @Test
    void testSaveAndLoadUserSettings() {
        SettingsStorage storage = new SettingsStorage(tempDir);

        // Load default when nothing saved yet
        UserSettings defaultSettings = storage.loadOrDefault("bob");
        assertNotNull(defaultSettings);
        assertEquals(UserSettings.Theme.DARK, defaultSettings.getTheme());
        assertEquals(UserSettings.BoardTheme.CLASSIC_GREEN, defaultSettings.getBoardTheme());

        // Modify settings
        UserSettings bobSettings = new UserSettings();
        bobSettings.setTheme(UserSettings.Theme.LIGHT);
        bobSettings.setBoardTheme(UserSettings.BoardTheme.WOOD);
        bobSettings.setShowCoordinates(false);
        bobSettings.setHighlightLegalMoves(true);

        storage.save("bob", bobSettings);

        // Load back
        UserSettings loaded = storage.loadOrDefault("bob");
        assertEquals(UserSettings.Theme.LIGHT, loaded.getTheme());
        assertEquals(UserSettings.BoardTheme.WOOD, loaded.getBoardTheme());
        assertFalse(loaded.isShowCoordinates());
        assertTrue(loaded.isHighlightLegalMoves());
    }

    @Test
    void testUserIsolationForPreferences() {
        SettingsStorage storage = new SettingsStorage(tempDir);

        UserSettings aliceSettings = new UserSettings();
        aliceSettings.setBoardTheme(UserSettings.BoardTheme.BLUE);
        storage.save("alice", aliceSettings);

        UserSettings bobSettings = new UserSettings();
        bobSettings.setBoardTheme(UserSettings.BoardTheme.MONOCHROME);
        storage.save("bob", bobSettings);

        assertEquals(UserSettings.BoardTheme.BLUE, storage.loadOrDefault("alice").getBoardTheme());
        assertEquals(UserSettings.BoardTheme.MONOCHROME, storage.loadOrDefault("bob").getBoardTheme());
    }
}
