package storage;

import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageTest {
    @TempDir
    Path tempDir;

    @Test
    void userCanBeSavedAndAuthenticated() {
        UserStorage storage = new UserStorage(tempDir);

        User user = storage.register("alice", "secret");

        assertEquals("alice", user.getUsername());
        assertTrue(storage.authenticate("alice", "secret").isPresent());
    }

    @Test
    void gameRoundTripUsesFileHandling() {
        GameStorage storage = new GameStorage(tempDir);
        SavedGame savedGame = new SavedGame(
                "game-1",
                "alice",
                List.of("e2e4", "e7e5"),
                LocalDateTime.now(),
                "1-0",
                "Open Game",
                92.5,
                1210,
                1200);

        storage.save(savedGame);
        List<SavedGame> games = storage.loadAll();

        assertEquals(1, games.size());
        assertEquals("game-1", games.get(0).getId());
    }
}
