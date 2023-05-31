package pink.zak.minestom.towerdefence.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.utils.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobStorage {
    private static final String MOBS_PATH = "mobs";

    private final List<EnemyMob> enemyMobs;

    public MobStorage() {
        this.enemyMobs = this.load();
    }

    private List<EnemyMob> load() {
        List<EnemyMob> enemyMobs = new ArrayList<>();

        List<String> fileNames;
        try {
            fileNames = ResourceUtils.listResources(MOBS_PATH).stream()
                    .filter(fileName -> fileName.endsWith(".json"))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String fileName : fileNames) {
            try {
                InputStream inputStream = TowerDefenceModule.class.getClassLoader().getResourceAsStream("%s/%s".formatted(MOBS_PATH, fileName));

                JsonObject json = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                EnemyMob enemyMob = new EnemyMob(json);
                enemyMobs.add(enemyMob);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load mob: %s".formatted(fileName), ex);
            }
        }


        return Collections.unmodifiableList(enemyMobs);
    }

    public List<EnemyMob> getEnemyMobs() {
        return this.enemyMobs;
    }
}
