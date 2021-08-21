package pink.zak.minestom.towerdefence.model.mob;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.Map;
import java.util.stream.StreamSupport;

public record EnemyMob(EntityType entityType,
                       ItemStack displayItem,
                       boolean flying,
                       Map<Integer, EnemyMobLevel> levels) {

    public EnemyMobLevel level(int level) {
        return this.levels.get(level);
    }

    public static EnemyMob fromJsonObject(JsonObject jsonObject) {
        EntityType entityType = EntityType.fromNamespaceId(jsonObject.get("entityType").getAsString());
        ItemStack displayItem = ItemUtils.fromJsonObject(jsonObject.get("displayItem").getAsJsonObject());
        boolean flying = jsonObject.get("flying").getAsBoolean();
        Map<Integer, EnemyMobLevel> levels = Maps.newHashMap();

        StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .map(EnemyMobLevel::fromJsonObject)
            .forEach(mobLevel -> levels.put(mobLevel.level(), mobLevel));

        return new EnemyMob(entityType, displayItem, flying, levels);
    }
}
