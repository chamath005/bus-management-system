package com.busapp.storage;

import com.busapp.exception.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** File handling: saves the whole DataStore using Java object streams. */
public class DataStorage {
    private final Path file;
    public DataStorage(Path file) { this.file = file; }

    public DataStore load() throws StorageException {
        if (!Files.exists(file)) return new DataStore();
        try (InputStream in = Files.newInputStream(file); ObjectInputStream ois = new ObjectInputStream(in)) {
            return (DataStore) ois.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new StorageException("Could not read " + file + " (delete it to start fresh): " + e.getMessage(), e);
        }
    }

    public void save(DataStore store) throws StorageException {
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp); ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(store);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);   // never leaves a half-written file
        } catch (IOException e) {
            throw new StorageException("Could not save data: " + e.getMessage(), e);
        }
    }
}
