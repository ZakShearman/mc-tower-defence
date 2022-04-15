package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.TDDamageType;

public class AttackingTower extends Tower {
    private final @NotNull TDDamageType damageType;

    public AttackingTower(JsonObject jsonObject) {
        super(jsonObject);
        this.damageType = TDDamageType.valueOf(jsonObject.get("damageType").getAsString());
    }

    public @NotNull TDDamageType getDamageType() {
        return this.damageType;
    }
}
