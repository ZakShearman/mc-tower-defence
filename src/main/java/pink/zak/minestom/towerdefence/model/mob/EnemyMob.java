package pink.zak.minestom.towerdefence.model.mob;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.minimessage.Template;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.Map;
import java.util.stream.StreamSupport;

public record EnemyMob(EntityType entityType,
                       String commonName,
                       int slot,
                       boolean flying,
                       ItemStack unownedItem,
                       Map<Integer, EnemyMobLevel> levels) {

    public EnemyMobLevel level(int level) {
        return this.levels.get(level);
    }

    public int maxLevel() {
        return this.levels.keySet().stream().max(Integer::compareTo).orElseThrow();
    }

    public static EnemyMob fromJsonObject(JsonObject jsonObject) {
        EntityType entityType = EntityType.fromNamespaceId(jsonObject.get("entityType").getAsString());
        String commonName = jsonObject.get("commonName").getAsString();
        int slot = jsonObject.get("guiSlot").getAsInt();
        boolean flying = jsonObject.get("flying").getAsBoolean();
        Map<Integer, EnemyMobLevel> levels = Maps.newHashMap();

        StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .map(EnemyMobLevel::fromJsonObject)
            .forEach(mobLevel -> levels.put(mobLevel.level(), mobLevel));

        ItemStack unownedItem = jsonObject.has("unownedItem") ?
            ItemUtils.fromJsonObject(jsonObject.get("unownedItem").getAsJsonObject(), Template.of("cost", String.valueOf(levels.get(1).manaCost())))
            : null;
        return new EnemyMob(entityType, commonName, slot, flying, unownedItem, levels);
    }
}
