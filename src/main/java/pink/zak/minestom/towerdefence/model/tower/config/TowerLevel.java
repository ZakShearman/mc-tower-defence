package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativeBlock;
import pink.zak.minestom.towerdefence.statdiff.Diffable;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.IntStatDiff;
import pink.zak.minestom.towerdefence.utils.ItemUtils;
import pink.zak.minestom.towerdefence.utils.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TowerLevel implements Diffable<TowerLevel> {
    private static final String UPGRADE_ITEM_NAME = "<i:false><%s><tower_name> <level_numeral> (<yellow>$<cost></yellow>)";

    private final @NotNull String towerName;

    private final int level;
    private final int cost;
    private final int range;

    private final ItemStack menuItem;

    private final Set<RelativeBlock> relativeBlocks;

    public TowerLevel(@NotNull String towerName, @NotNull JsonObject jsonObject) {
        this.towerName = towerName;

        this.level = jsonObject.get("level").getAsInt();
        this.cost = jsonObject.get("cost").getAsInt();
        this.range = jsonObject.get("range").getAsInt();

        // todo placeholders, allow for custom towers to add their own

        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.unparsed("cost", String.valueOf(this.cost))
        );

        this.menuItem = ItemUtils.fromJsonObject(jsonObject.get("menuItem").getAsJsonObject(), tagResolver);

        this.relativeBlocks = RelativeBlock.setFromJson(jsonObject.get("relativeBlocks").getAsJsonArray());
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

    public @NotNull String getTowerName() {
        return towerName;
    }

    public int getLevel() {
        return this.level;
    }

    public int getCost() {
        return this.cost;
    }

    public int getRange() {
        return this.range;
    }

    public ItemStack getMenuItem() {
        return this.menuItem;
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
     * @return the lore
     */
    private @NotNull List<Component> createStatLore() {
        List<Component> components = new ArrayList<>();
        components.add(Component.empty());
        components.addAll(this.generateDiff(this).generateStatLines());
        return components;
    }

    public Set<RelativeBlock> getRelativeBlocks() {
        return this.relativeBlocks;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel other) {
        return new StatDiffCollection().addDiff("Range", new IntStatDiff(this.range, other.range));
    }
}
