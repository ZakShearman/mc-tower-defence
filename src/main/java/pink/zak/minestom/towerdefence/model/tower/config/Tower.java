package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.enums.TowerType;

import java.util.HashMap;
import java.util.Map;

public class Tower {
    private final TowerType type;
    private final String name;
    private final int guiSlot;
    private final Map<Integer, ? extends TowerLevel> levels;
    private final int maxLevel;

    public Tower(JsonObject jsonObject, Map<Integer, JsonObject> levelJsonMap) {
        this.type = TowerType.valueOf(jsonObject.get("type").getAsString());
        this.name = jsonObject.get("name").getAsString();
        this.guiSlot = jsonObject.get("guiSlot").getAsInt();

        Map<Integer, TowerLevel> levelMap = new HashMap<>();

        for (JsonObject levelJson : levelJsonMap.values()) {
            TowerLevel level = this.type.getTowerLevelFunction().apply(levelJson);
            levelMap.put(level.getLevel(), level);
        }
        this.levels = levelMap;

        this.maxLevel = this.levels.keySet().stream().max(Integer::compareTo).orElseThrow();
    }

    public boolean isSpaceClear(Instance instance, Point basePoint, Material towerBaseMaterial) {
        int checkDistance = this.type.getSize().getCheckDistance();
        for (int x = basePoint.blockX() - checkDistance; x <= basePoint.blockX() + checkDistance; x++) {
            for (int z = basePoint.blockZ() - checkDistance; z <= basePoint.blockZ() + checkDistance; z++) {
                Block first = instance.getBlock(x, basePoint.blockY(), z);
                if (first.registry().material() != towerBaseMaterial || first.properties().containsKey("towerId"))
                    return false;
                Material second = instance.getBlock(x, basePoint.blockY() + 1, z).registry().material();
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

    public int getGuiSlot() {
        return this.guiSlot;
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
