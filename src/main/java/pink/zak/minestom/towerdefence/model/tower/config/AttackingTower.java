package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.mob.DamageType;

public class AttackingTower extends Tower {
    private final DamageType damageType;

    public AttackingTower(JsonObject jsonObject) {
        super(jsonObject);
        this.damageType = DamageType.valueOf(jsonObject.get("damageType").getAsString());
    }

    public DamageType getDamageType() {
        return this.damageType;
    }
}
