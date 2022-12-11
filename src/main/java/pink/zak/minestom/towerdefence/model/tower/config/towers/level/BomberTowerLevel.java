package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

public class BomberTowerLevel extends AttackingTowerLevel {
    private final @NotNull RelativePoint relativeTntSpawnPoint;

    public BomberTowerLevel(JsonObject jsonObject) {
        super(jsonObject);

        this.relativeTntSpawnPoint = new RelativePoint(jsonObject.get("relativeTntSpawnPoint").getAsJsonObject());
    }

    public @NotNull RelativePoint getRelativeTntSpawnPoint() {
        return this.relativeTntSpawnPoint;
    }
}
