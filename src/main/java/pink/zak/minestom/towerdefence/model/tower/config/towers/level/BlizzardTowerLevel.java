package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;

public class BlizzardTowerLevel extends AttackingTowerLevel {
    private final double speedModifier;
    private final int tickDuration;

    public BlizzardTowerLevel(JsonObject jsonObject) {
        super(jsonObject);

        this.speedModifier = jsonObject.get("speedModifier").getAsDouble();
        this.tickDuration = jsonObject.get("tickDuration").getAsInt();
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public int getTickDuration() {
        return this.tickDuration;
    }
}