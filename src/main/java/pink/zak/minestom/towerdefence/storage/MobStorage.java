package pink.zak.minestom.towerdefence.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class MobStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobStorage.class);
    private static final Path MOBS_PATH = Path.of("mobs");

    private final List<EnemyMob> enemyMobs;

    public MobStorage() {
        this.enemyMobs = this.load();
    }

    private List<EnemyMob> load() {
        try (Stream<Path> files = Files.list(MOBS_PATH)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::loadMob)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull EnemyMob loadMob(@NotNull Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return new EnemyMob(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mob: %s".formatted(path), e);
        }
    }

    public List<EnemyMob> getEnemyMobs() {
        return this.enemyMobs;
    }
}
