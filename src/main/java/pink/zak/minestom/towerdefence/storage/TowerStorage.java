package pink.zak.minestom.towerdefence.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TowerStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(TowerStorage.class);
    private static final Set<String> TOWER_NAMES = Arrays.stream(TowerType.values())
            .map(towerType -> towerType.name().toLowerCase() + ".json")
            .collect(Collectors.toUnmodifiableSet());

    private final TowerDefencePlugin plugin;

    private final Map<TowerType, Tower> towers = new HashMap<>();

    public TowerStorage(TowerDefencePlugin plugin) {
        this.plugin = plugin;

        this.load();
    }

    private void load() {
        for (String fileName : TOWER_NAMES) {
            InputStream inputStream = this.plugin.getPackagedResource("towers/" + fileName);
            if (inputStream == null) {
                LOGGER.error("Could not find tower file: " + fileName);
                continue;
            }

            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            TowerType towerType = TowerType.valueOf(jsonObject.get("type").getAsString());

            Tower tower = towerType.getTowerFunction().apply(jsonObject);

            this.towers.put(towerType, tower);
        }
    }

    public Map<TowerType, Tower> getTowers() {
        return this.towers;
    }

    public Tower getTower(TowerType towerType) {
        return this.towers.get(towerType);
    }
}
