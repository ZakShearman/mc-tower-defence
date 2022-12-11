package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

public class AttackingTowerLevel extends TowerLevel {
    private final int fireDelay;
    private final float damage;

    public AttackingTowerLevel(JsonObject jsonObject) {
        super(jsonObject);
        this.fireDelay = jsonObject.get("fireDelay").getAsInt();
        this.damage = jsonObject.get("damage").getAsFloat();
    }

    public int getFireDelay() {
        return this.fireDelay;
    }

    public float getDamage() {
        return this.damage;
    }
}
