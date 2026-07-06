package storage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileManager<T extends Serializable> {
    private final Path directory;

    public FileManager(Path directory) {
        this.directory = directory;
    }

    public void save(String fileName, T object) {
        try {
            Files.createDirectories(directory);
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(directory.resolve(fileName)))) {
                output.writeObject(object);
            }
        } catch (IOException e) {
            throw new StorageException("Could not save " + fileName, e);
        }
    }

    @SuppressWarnings("unchecked")
    public T load(String fileName) {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(directory.resolve(fileName)))) {
            return (T) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new StorageException("Could not load " + fileName, e);
        }
    }

    public List<T> loadAll(String extension) {
        List<T> objects = new ArrayList<>();
        if (!Files.exists(directory)) {
            return objects;
        }
        try (Stream<Path> paths = Files.list(directory)) {
            paths
                    .filter(path -> path.getFileName().toString().endsWith(extension))
                    .forEach(path -> objects.add(load(path.getFileName().toString())));
            return objects;
        } catch (IOException e) {
            throw new StorageException("Could not list " + directory, e);
        }
    }

    public boolean exists(String fileName) {
        return Files.exists(directory.resolve(fileName));
    }
}
