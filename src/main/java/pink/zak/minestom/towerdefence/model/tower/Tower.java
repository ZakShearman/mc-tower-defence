package pink.zak.minestom.towerdefence.model.tower;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.enums.TowerType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record Tower(TowerType type,
                    String name,
                    Map<Integer, TowerLevel> levels) {

    public boolean isSpaceClear(Instance instance, Point baseBlock, Material towerPlaceMaterial) {
        int checkDistance = this.type.getSize().getCheckDistance();
        for (int x = baseBlock.blockX() - checkDistance; x <= baseBlock.blockX() + checkDistance; x++) {
            for (int z = baseBlock.blockZ() - checkDistance; z <= baseBlock.blockZ() + checkDistance; z++) {
                Block first = instance.getBlock(x, baseBlock.blockY(), z);
                if (first.registry().material() != towerPlaceMaterial || first.properties().containsKey("towerId"))
                    return false;
                Material second = instance.getBlock(x, baseBlock.blockY() + 1, z).registry().material();
                if (second != null && second != Material.AIR)
                    return false;
            }
        }
        return true;
    }

    public int maxLevel() {
        return this.levels.keySet().stream().max(Integer::compareTo).orElseThrow();
    }

    public TowerLevel level(int level) {
        return this.levels.get(level);
    }

    public ItemStack baseMenuItem() {
        return this.level(1).menuItem();
    }

    public static Tower fromJsonObject(JsonObject jsonObject) {
        TowerType type = TowerType.valueOf(jsonObject.get("type").getAsString());
        String name = jsonObject.get("name").getAsString();

        Set<TowerLevel> levels = StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(TowerLevel::fromJsonObject)
            .collect(Collectors.toUnmodifiableSet());
        Map<Integer, TowerLevel> levelMap = Maps.newHashMap();

        for (TowerLevel level : levels)
            levelMap.put(level.level(), level);

        return new Tower(type, name, levelMap);
    }
}
