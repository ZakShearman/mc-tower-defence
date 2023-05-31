package pink.zak.minestom.towerdefence.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TowerStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(TowerStorage.class);

    private static final Path TOWERS_PATH = Path.of("towers");

    private final Map<TowerType, Tower> towers;

    public TowerStorage() {
        try {
            this.towers = this.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load towers", e);
        }
    }

    private Map<TowerType, Tower> load() throws IOException {
        try (Stream<Path> pathStream = Files.list(TOWERS_PATH)) {
            return pathStream.filter(Files::isDirectory)
                    .filter(path -> {
                        String enumName = path.getFileName().toString().toUpperCase();
                        try {
                            TowerType.valueOf(enumName);
                            return true;
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("Could not find tower type: " + enumName);
                            return false;
                        }
                    })
                    .map(path -> {
                        String enumName = path.getFileName().toString().toUpperCase();
                        TowerType towerType = TowerType.valueOf(enumName);

                        return this.loadTowerType(path, towerType);
                    })
                    .collect(Collectors.toUnmodifiableMap(Tower::getType, tower -> tower));
        }
    }

    private Tower loadTowerType(@NotNull Path folder, @NotNull TowerType towerType) {
        String towerTypeName = towerType.toString().toLowerCase();

        JsonObject towerJson;
        try (BufferedReader reader = Files.newBufferedReader(folder.resolve(towerTypeName + ".json"))) {
            towerJson = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load tower type: %s".formatted(towerTypeName), e);
        }

        Map<Integer, JsonObject> levelJson = new HashMap<>();
        for (int level = 1; level <= 10; level++) {
            Path levelJsonPath = folder.resolve(level + ".json");
            if (!Files.exists(levelJsonPath)) break;

            try (BufferedReader reader = Files.newBufferedReader(levelJsonPath)) {
                levelJson.put(level, JsonParser.parseReader(reader).getAsJsonObject());
            } catch (IOException e) {
                break;
            }
        }

        return towerType.getTowerFunction().apply(towerJson, levelJson);
    }

    public Map<TowerType, Tower> getTowers() {
        return this.towers;
    }

    public Tower getTower(TowerType towerType) {
        return this.towers.get(towerType);
    }

    public @Nullable Tower getTower(int guiSlot) {
        for (Tower tower : this.towers.values()) {
            if (tower.getGuiSlot() == guiSlot) return tower;
        }
        return null;
    }
}
