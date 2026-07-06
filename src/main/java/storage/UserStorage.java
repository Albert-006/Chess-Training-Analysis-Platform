package storage;

import model.User;
import util.PasswordHasher;

import java.nio.file.Path;
import java.util.Optional;

public class UserStorage {
    private static final String EXTENSION = ".user";

    private final FileManager<User> fileManager;

    public UserStorage(Path dataRoot) {
        this.fileManager = new FileManager<>(dataRoot.resolve("users"));
    }

    public User register(String username, String password) {
        User user = new User(username, PasswordHasher.hash(password));
        save(user);
        return user;
    }

    public void save(User user) {
        fileManager.save(fileName(user.getUsername()), user);
    }

    public Optional<User> find(String username) {
        String fileName = fileName(username);
        if (!fileManager.exists(fileName)) {
            return Optional.empty();
        }
        return Optional.of(fileManager.load(fileName));
    }

    public Optional<User> authenticate(String username, String password) {
        Optional<User> user = find(username);
        return user.filter(value -> PasswordHasher.verify(password, value.getPasswordHash()));
    }

    private String fileName(String username) {
        return username.replaceAll("[^a-zA-Z0-9._-]", "_") + EXTENSION;
    }
}
