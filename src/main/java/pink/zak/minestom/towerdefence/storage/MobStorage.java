package pink.zak.minestom.towerdefence.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minestom.server.entity.EntityType;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class MobStorage {
    private final TowerDefencePlugin plugin;

    private final Path folderPath;
    private final Map<EntityType, EnemyMob> enemyMobs = Maps.newHashMap();

    public MobStorage(TowerDefencePlugin plugin) {
        this.plugin = plugin;

        this.folderPath = plugin.getDataDirectory().resolve("mobs");
        if (!Files.exists(this.folderPath))
            this.createDefaultFiles();
        this.load();
    }

    private void load() {
        for (File file : this.folderPath.toFile().listFiles()) {
            JsonObject jsonObject = FileUtils.fileToJsonObject(file);
            EnemyMob enemyMob = new EnemyMob(jsonObject);

            this.enemyMobs.put(enemyMob.getEntityType(), enemyMob);
        }
    }

    private void createDefaultFiles() {
        Set<String> enemyMobNames = Sets.newHashSet(
            "zombie.json",
            "skeleton.json",
            "llama.json"
        );

        for (String towerName : enemyMobNames) {
            TowerDefencePlugin.LOGGER.info("Saving packaged resource mobs\\" + towerName);
            this.plugin.savePackagedResource(Path.of("mobs").resolve(towerName));
        }
    }

    public Map<EntityType, EnemyMob> getEnemyMobs() {
        return this.enemyMobs;
    }

    public EnemyMob getTower(EntityType entityType) {
        return this.enemyMobs.get(entityType);
    }
}
