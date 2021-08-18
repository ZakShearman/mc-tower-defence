package pink.zak.minestom.towerdefence.storage;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TowerStorage {
    private final TowerDefencePlugin plugin;

    private final Path folderPath;
    private final Map<TowerType, Tower> towers = Maps.newHashMap();

    public TowerStorage(TowerDefencePlugin plugin) {
        this.plugin = plugin;

        this.folderPath = plugin.getDataDirectory().resolve("towers");
        if (!Files.exists(this.folderPath))
            this.createDefaultFiles();
        this.load();
    }

    private void load() {
        for (File file : this.folderPath.toFile().listFiles()) {
            JsonObject jsonObject = FileUtils.fileToJsonObject(file);
            Tower tower = Tower.fromJsonObject(jsonObject);

            this.towers.put(tower.getType(), tower);
        }
    }

    private void createDefaultFiles() {
        Set<String> towerNames = Arrays.stream(TowerType.values())
            .map(TowerType::name)
            .map(String::toLowerCase)
            .map(name -> name.concat(".json"))
            .collect(Collectors.toUnmodifiableSet());

        for (String towerName : towerNames) {
            System.out.println("Saving packaged resource towers\\" + towerName);
            this.plugin.savePackagedResource(Path.of("towers").resolve(towerName));
        }
    }

    public Map<TowerType, Tower> getTowers() {
        return this.towers;
    }

    public Tower getTower(TowerType towerType) {
        return this.towers.get(towerType);
    }
}
