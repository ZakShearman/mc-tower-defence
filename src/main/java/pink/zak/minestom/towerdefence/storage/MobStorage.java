package pink.zak.minestom.towerdefence.storage;

import net.minestom.server.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class MobStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MobStorage.class);
    private final TowerDefencePlugin plugin;

    private final Path folderPath;
    private final Map<EntityType, EnemyMob> enemyMobs = new HashMap<>();

    public MobStorage(TowerDefencePlugin plugin) {
        this.plugin = plugin;

        this.folderPath = plugin.getDataDirectory().resolve("mobs");
        if (!Files.exists(this.folderPath))
            this.createDefaultFiles();
        this.load();
    }

    private void load() {
        try (Stream<Path> stream = Files.list(this.folderPath)) {
            stream
                .map(Path::toFile)
                .map(FileUtils::fileToJsonObject)
                .map(EnemyMob::new)
                .forEach(enemyMob -> this.enemyMobs.put(enemyMob.getEntityType(), enemyMob));
        } catch (IOException ex) {
            LOGGER.error("Error whilst loading EnemyMobs: ", ex);
        }
    }

    private void createDefaultFiles() {
        Set<String> enemyMobNames = Set.of(
            "zombie.json",
            "skeleton.json",
            "llama.json"
        );

        for (String towerName : enemyMobNames) {
            LOGGER.info("Saving packaged resource mobs\\" + towerName);
            this.plugin.savePackagedResource(Path.of("mobs").resolve(towerName));
        }
    }

    public Map<EntityType, EnemyMob> getEnemyMobs() {
        return this.enemyMobs;
    }

    public EnemyMob getEnemyMob(EntityType entityType) {
        return this.enemyMobs.get(entityType);
    }
}
