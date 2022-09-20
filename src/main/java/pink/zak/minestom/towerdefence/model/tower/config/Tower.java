package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.enums.TowerType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Tower {
    private final TowerType type;
    private final String name;
    private final Map<Integer, ? extends TowerLevel> levels;
    private final int maxLevel;

    public Tower(JsonObject jsonObject) {
        this.type = TowerType.valueOf(jsonObject.get("type").getAsString());
        this.name = jsonObject.get("name").getAsString();

        Set<? extends TowerLevel> levels = StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), true)
                .map(JsonElement::getAsJsonObject)
                .map(o -> this.type.getTowerLevelFunction().apply(o))
                .collect(Collectors.toUnmodifiableSet());
        Map<Integer, TowerLevel> levelMap = new HashMap<>();

        for (TowerLevel level : levels)
            levelMap.put(level.getLevel(), level);

        this.levels = levelMap;

        this.maxLevel = this.levels.keySet().stream().max(Integer::compareTo).orElseThrow();
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

    public ItemStack getBaseMenuItem() {
        return this.levels.get(1).getMenuItem();
    }

    public TowerType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public Map<Integer, ? extends TowerLevel> getLevels() {
        return this.levels;
    }

    public TowerLevel getLevel(int level) {
        return this.levels.get(level);
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }
}
