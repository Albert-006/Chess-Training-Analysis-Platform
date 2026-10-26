package storage;

import java.nio.file.Path;
import java.util.Optional;

public class SettingsStorage {
    private static final String EXTENSION = ".settings";
    private final FileManager<UserSettings> fileManager;

    public SettingsStorage(Path dataRoot) {
        this.fileManager = new FileManager<>(dataRoot.resolve("settings"));
    }

    public void save(String username, UserSettings settings) {
        fileManager.save(fileName(username), settings);
    }

    public UserSettings loadOrDefault(String username) {
        String name = fileName(username);
        if (!fileManager.exists(name)) {
            return new UserSettings();
        }
        try {
            UserSettings settings = fileManager.load(name);
            return settings != null ? settings : new UserSettings();
        } catch (Exception e) {
            return new UserSettings();
        }
    }

    private String fileName(String username) {
        String clean = (username == null || username.isBlank()) ? "default" : username;
        return clean.replaceAll("[^a-zA-Z0-9._-]", "_") + EXTENSION;
    }
}
