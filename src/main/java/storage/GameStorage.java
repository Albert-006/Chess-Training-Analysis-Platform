package storage;

import java.nio.file.Path;
import java.util.List;

public class GameStorage {
    private static final String EXTENSION = ".game";

    private final FileManager<SavedGame> fileManager;

    public GameStorage(Path dataRoot) {
        this.fileManager = new FileManager<>(dataRoot.resolve("games"));
    }

    public void save(SavedGame game) {
        fileManager.save(safeFileName(game.getId()) + EXTENSION, game);
    }

    public List<SavedGame> loadAll() {
        return fileManager.loadAll(EXTENSION);
    }

    private String safeFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
