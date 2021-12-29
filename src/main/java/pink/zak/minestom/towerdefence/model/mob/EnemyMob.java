package pink.zak.minestom.towerdefence.model.mob;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class EnemyMob {
    private final EntityType entityType;
    private final String commonName;
    private final int slot;
    private final boolean flying;
    private final double unitCost;
    private final ItemStack unownedItem;
    private final Map<Integer, EnemyMobLevel> levels = new HashMap<>();

    public EnemyMob(JsonObject jsonObject) {
        this.entityType = EntityType.fromNamespaceId(jsonObject.get("entityType").getAsString());
        this.commonName = jsonObject.get("commonName").getAsString();
        this.slot = jsonObject.get("guiSlot").getAsInt();
        this.flying = jsonObject.get("flying").getAsBoolean();
        this.unitCost = jsonObject.get("unitCost").getAsDouble();

        this.unownedItem = jsonObject.has("unownedItem") ? ItemUtils.fromJsonObject(jsonObject.get("unownedItem").getAsJsonObject()) : null;

        StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .map(EnemyMobLevel::new)
            .forEach(mobLevel -> this.levels.put(mobLevel.getLevel(), mobLevel));
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public String getCommonName() {
        return this.commonName;
    }

    public int getSlot() {
        return this.slot;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public double getUnitCost() {
        return this.unitCost;
    }

    public ItemStack getUnownedItem() {
        return this.unownedItem;
    }

    public Map<Integer, EnemyMobLevel> getLevels() {
        return this.levels;
    }

    public EnemyMobLevel getLevel(int level) {
        return this.levels.get(level);
    }

    public int getMaxLevel() {
        return this.levels.keySet().stream().max(Integer::compareTo).orElseThrow();
    }
}
