package pink.zak.minestom.towerdefence.utils.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.utils.storage.Repository;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class JsonRepository<ID, T> implements Repository<ID, T>, IdStringConverter<ID> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRepository.class);
    protected static final Gson GSON = new GsonBuilder().create();
    protected final Path basePath;

    public JsonRepository(Path folder) {
        this.basePath = folder;

        if (Files.notExists(folder))
            folder.toFile().mkdirs();
    }

    protected abstract T deserialize(JsonElement json);

    protected abstract JsonElement serialize(T entity);

    @Override
    public <S extends T> @NotNull S save(@NotNull ID id, @NotNull S entity) {
        Path path = this.basePath.resolve(this.getIdAsString(id) + ".json");
        File file = path.toFile();

        try {
            file.createNewFile();
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this.serialize(entity), writer);
            }
        } catch (IOException ex) {
            LOGGER.error("Error writing Json to file: ", ex);
        }

        return entity;
    }

    @Override
    public @NotNull Optional<T> findById(@NotNull ID id) {
        Path path = this.basePath.resolve(this.getIdAsString(id) + ".json");
        File file = path.toFile();

        if (!file.exists())
            return Optional.empty();

        return Optional.ofNullable(this.parseFile(file));
    }

    @Override
    public @NotNull Iterable<T> findAll() {
        try (Stream<Path> stream = Files.list(this.basePath)) {
            return stream
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".json"))
                .map(this::parseFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException ex) {
            LOGGER.error("Error listing files: ", ex);
            return (Iterable<T>) Collections.emptyIterator();
        }
    }

    @Override
    public boolean existsById(@NotNull ID id) {
        return Files.exists(this.getEntityPath(id));
    }

    @Override
    public long count() {
        try (Stream<Path> stream = Files.list(this.basePath)) {
            return stream
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".json"))
                .count();
        } catch (IOException ex) {
            LOGGER.error("Error counting files: ", ex);
            return -1;
        }
    }

    @Override
    public void deleteById(@NotNull ID id) {
        Path path = this.getEntityPath(id);
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            LOGGER.error("Error deleting file by ID: ", ex);
        }
    }

    private T parseFile(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            JsonElement json = JsonParser.parseReader(fileReader);
            return this.deserialize(json);
        } catch (IOException ex) {
            LOGGER.error("Error reading file as Json: ", ex);
        }

        return null;
    }

    private Path getEntityPath(ID id) {
        return this.basePath.resolve(this.getIdAsString(id) + ".json");
    }

    @Override
    public void deleteAll() {
        try (Stream<Path> stream = Files.list(this.basePath)) {
            stream
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".json"))
                .forEach(File::delete);
        } catch (IOException ex) {
            LOGGER.error("Error deleting files: ", ex);
        }
    }
}
