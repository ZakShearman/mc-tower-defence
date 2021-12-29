package pink.zak.minestom.towerdefence.model.tower.config.towers;

import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

public class LightningTowerLevel extends AttackingTowerLevel {
    private final int maxTargets;
    private final RelativePoint relativeCastPoint;

    public LightningTowerLevel(JsonObject jsonObject) {
        super(jsonObject);
        this.maxTargets = jsonObject.get("maxTargets").getAsInt();
        this.relativeCastPoint = new RelativePoint(jsonObject.get("relativeCastPoint").getAsJsonObject());
    }

    public int getMaxTargets() {
        return this.maxTargets;
    }

    public RelativePoint getRelativeCastPoint() {
        return this.relativeCastPoint;
    }
}
