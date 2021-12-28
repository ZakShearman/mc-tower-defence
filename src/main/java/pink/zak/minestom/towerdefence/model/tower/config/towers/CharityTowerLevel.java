package pink.zak.minestom.towerdefence.model.tower.config.towers;

import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;

public class CharityTowerLevel extends TowerLevel {
    private final double multiplier;

    public CharityTowerLevel(JsonObject jsonObject) {
        super(jsonObject);
        this.multiplier = jsonObject.get("multiplier").getAsDouble();
    }

    public double getMultiplier() {
        return this.multiplier;
    }
}
