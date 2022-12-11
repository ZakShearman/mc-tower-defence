package pink.zak.minestom.towerdefence.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MobStorage {
    private static final Set<String> MOB_FILES = Set.of(
            "bee.json",
            "golem.json",
            "llama.json",
//            "skeleton.json",
            "zombie.json"
    );

    private final List<EnemyMob> enemyMobs;

    public MobStorage() {
        this.enemyMobs = this.load();
    }

    private List<EnemyMob> load() {
        List<EnemyMob> enemyMobs = new ArrayList<>();
        for (String fileName : MOB_FILES) {
            InputStream inputStream = TowerDefenceModule.class.getClassLoader().getResourceAsStream("mobs/" + fileName);

            JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
            EnemyMob enemyMob = new EnemyMob(json);
            enemyMobs.add(enemyMob);
        }
        System.out.println("Loaded " + enemyMobs.size() + " mobs: " + enemyMobs);
        return Collections.unmodifiableList(enemyMobs);
    }

    public List<EnemyMob> getEnemyMobs() {
        return this.enemyMobs;
    }
}
