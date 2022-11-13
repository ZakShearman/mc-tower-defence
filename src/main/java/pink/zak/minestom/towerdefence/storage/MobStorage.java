package pink.zak.minestom.towerdefence.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.entity.EntityType;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MobStorage {
    private static final Set<String> MOB_FILES = Set.of(
            "bee.json",
            "llama.json",
            "skeleton.json",
            "zombie.json"
    );

    private final TowerDefenceModule plugin;

    private final Map<EntityType, EnemyMob> enemyMobs = new HashMap<>();

    public MobStorage(TowerDefenceModule plugin) {
        this.plugin = plugin;

        this.load();
    }

    private void load() {
        for (String fileName : MOB_FILES) {
            InputStream inputStream = TowerDefenceModule.class.getClassLoader().getResourceAsStream("mobs/" + fileName);

            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            EnemyMob enemyMob = new EnemyMob(json);
            this.enemyMobs.put(enemyMob.getEntityType(), enemyMob);

        }
    }

    public Map<EntityType, EnemyMob> getEnemyMobs() {
        return this.enemyMobs;
    }

    public EnemyMob getEnemyMob(EntityType entityType) {
        return this.enemyMobs.get(entityType);
    }
}
