package pink.zak.minestom.towerdefence.storage;

import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class MapStorage {
    private final Path filePath;
    private final TowerMap map;

    public MapStorage(TowerDefenceModule plugin) {
        this.filePath = Path.of("extensions/TowerDefence").resolve("map.json");
        if (Files.exists(this.filePath)) {
            this.map = this.load();
        } else {
            this.map = new TowerMap();
        }
    }

    private TowerMap load() {
        return TowerMap.fromJson(FileUtils.fileToJsonObject(this.filePath.toFile()));
    }

    public void save() {
        FileUtils.saveJsonObject(this.filePath, this.map.toJson());
    }

    public TowerMap getMap() {
        return this.map;
    }
}
