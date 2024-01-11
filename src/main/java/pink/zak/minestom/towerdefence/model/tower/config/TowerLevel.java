package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.SchematicReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.statdiff.Diffable;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.IntStatDiff;
import pink.zak.minestom.towerdefence.utils.NumberUtils;

public class TowerLevel implements Diffable<TowerLevel>, Comparable<TowerLevel> {
    private static final BiFunction<String, String, Path> SCHEMATIC_PATH_FUNCTION = (towerName, level) ->
            Path.of("towers", towerName.toLowerCase(), "builds", level + ".schem");

    private static final String UPGRADE_ITEM_NAME = "<i:false><%s><tower_name> <level_numeral> (<yellow>$<cost></yellow>)";

    private final @NotNull String towerName;

    private final int level;
    private final int cost;
    private final int range;

    private final @NotNull Schematic schematic;

    public TowerLevel(@NotNull String towerName, @NotNull JsonObject jsonObject) {
        this.towerName = towerName;

        this.level = jsonObject.get("level").getAsInt();
        this.cost = jsonObject.get("cost").getAsInt();
        this.range = jsonObject.get("range").getAsInt();

        Path schematicPath = SCHEMATIC_PATH_FUNCTION.apply(towerName, String.valueOf(this.level));
        this.schematic = SchematicReader.read(schematicPath);
    }

    private ItemStack createOwnedUpgradeItem() {
        return ItemStack.builder(Material.GREEN_STAINED_GLASS_PANE)
                .displayName(MiniMessage.miniMessage().deserialize(UPGRADE_ITEM_NAME.formatted("green"),
                        Placeholder.unparsed("tower_name", this.towerName),
                        Placeholder.unparsed("level_numeral", NumberUtils.toRomanNumerals(this.level)),
                        Placeholder.unparsed("cost", String.valueOf(this.cost))))
                .lore(this.createStatLore())
                .build();
    }

    public int asInteger() {
        return this.level;
    }

    public int getCost() {
        return this.cost;
    }

    public int getRange() {
        return this.range;
    }

    public ItemStack getOwnedUpgradeItem() {
        return this.createOwnedUpgradeItem(); // todo this can be cached/replaced with a single instance for each level.
    }

    public @NotNull ItemStack createBuyUpgradeItem(boolean canAfford, int cost, @NotNull TowerLevel currentLevel) {
        String itemName = UPGRADE_ITEM_NAME.formatted(canAfford ? "gold" : "red");

        return ItemStack.builder(canAfford ? Material.ORANGE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                .displayName(MiniMessage.miniMessage().deserialize(itemName,
                        Placeholder.unparsed("tower_name", this.towerName),
                        Placeholder.unparsed("level_numeral", NumberUtils.toRomanNumerals(this.level)),
                        Placeholder.unparsed("cost", String.valueOf(cost))))
                .lore(this.createUpgradeLore(currentLevel))
                .build();
    }

    private @NotNull List<Component> createUpgradeLore(@NotNull TowerLevel currentLevel) {
        List<Component> components = new ArrayList<>();
        components.add(Component.empty());
        components.addAll(currentLevel.generateDiff(this).generateComparisonLines());
        return components;
    }

    /**
     * Creates the same lore as upgrades but without comparison to another level
     *
     * @return the lore
     */
    private @NotNull List<Component> createStatLore() {
        List<Component> components = new ArrayList<>();
        components.add(Component.empty());
        components.addAll(this.generateDiff(this).generateStatLines());
        return components;
    }

    public @NotNull Schematic getSchematic() {
        return schematic;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel other) {
        return new StatDiffCollection().addDiff("Range", new IntStatDiff(this.range, other.range));
    }

    @Override
    public int compareTo(@NotNull TowerLevel level) {
        return Integer.compare(this.level, level.level);
    }

}
