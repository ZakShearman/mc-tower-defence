package pink.zak.minestom.towerdefence.model.tower;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Tower {
    private final TowerType type;
    private final Map<Integer, TowerLevel> levels;

    private final Material displayMaterial;
    private final ItemStack menuItem;

    public Tower(TowerType type, Map<Integer, TowerLevel> levels, Material displayMaterial) {
        this.type = type;
        this.levels = levels;
        this.displayMaterial = displayMaterial;

        TowerLevel level = this.getLevel(1);
        this.menuItem = this.createMenuItem(level.name(), level.description(), level.cost(), displayMaterial);
    }

    private ItemStack createMenuItem(String name, List<String> description, int cost, Material displayMaterial) {
        List<String> usedDescription = new ArrayList<>(description);
        usedDescription.add(0, "");
        return ItemStack.builder(displayMaterial)
            .displayName(Component.text(name + " ($" + cost + ")", NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false))
            .lore(
                usedDescription.stream()
                    .map(line -> Component.text(line, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                    .collect(Collectors.toList())
            )
            .build();
    }

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

    public TowerType getType() {
        return this.type;
    }

    public Map<Integer, TowerLevel> getLevels() {
        return this.levels;
    }

    public TowerLevel getLevel(int level) {
        return this.levels.get(level);
    }

    public Material getDisplayMaterial() {
        return this.displayMaterial;
    }

    public ItemStack getMenuItem() {
        return this.menuItem;
    }

    public static Tower fromJsonObject(JsonObject jsonObject) {
        TowerType type = TowerType.valueOf(jsonObject.get("type").getAsString());
        String displayMaterial = jsonObject.get("displayMaterial").getAsString();

        Set<TowerLevel> levels = StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(TowerLevel::fromJsonObject)
            .collect(Collectors.toUnmodifiableSet());
        Map<Integer, TowerLevel> levelMap = Maps.newHashMap();

        for (TowerLevel level : levels)
            levelMap.put(level.level(), level);

        return new Tower(type, levelMap, Material.fromNamespaceId(displayMaterial));
    }
}
