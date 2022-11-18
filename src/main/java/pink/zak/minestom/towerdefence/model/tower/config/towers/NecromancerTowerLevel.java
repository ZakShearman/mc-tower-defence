package pink.zak.minestom.towerdefence.model.tower.config.towers;

import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;

public class NecromancerTowerLevel extends TowerLevel {
    private final int maxNecromancedTroops;
    private final int necromancedHealth;
    private final double damageMultiplier;

    public NecromancerTowerLevel(JsonObject jsonObject) {
        super(jsonObject);

        this.maxNecromancedTroops = jsonObject.get("maxNecromancedTroops").getAsInt();
        this.necromancedHealth = jsonObject.get("necromancedHealth").getAsInt();
        this.damageMultiplier = jsonObject.get("necromancedDamageMultiplier").getAsDouble();
    }

    public int getMaxNecromancedMobs() {
        return this.maxNecromancedTroops;
    }

    public int getNecromancedHealth() {
        return this.necromancedHealth;
    }

    public double getDamageMultiplier() {
        return this.damageMultiplier;
    }
}
