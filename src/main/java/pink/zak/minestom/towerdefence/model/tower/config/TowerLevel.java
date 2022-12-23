package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativeBlock;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.Set;

public class TowerLevel {
    private final int level;
    private final int cost;
    private final double range;

    private final ItemStack menuItem;
    private final ItemStack cantAffordUpgradeItem;
    private final ItemStack ownedUpgradeItem;
    private final ItemStack buyUpgradeItem;

    private final Set<RelativeBlock> relativeBlocks;

    public TowerLevel(JsonObject jsonObject) {
        this.level = jsonObject.get("level").getAsInt();
        this.cost = jsonObject.get("cost").getAsInt();
        this.range = jsonObject.get("range").getAsDouble();

        // todo placeholders

        this.menuItem = ItemUtils.fromJsonObject(jsonObject.get("menuItem").getAsJsonObject(), null);

        ItemStack ownedUpgradeItem = ItemUtils.fromJsonObject(jsonObject.get("upgradeItem").getAsJsonObject(), null);
        this.ownedUpgradeItem = ownedUpgradeItem.withDisplayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GREEN));

        this.buyUpgradeItem = ItemUtils.withMaterialBuilder(this.ownedUpgradeItem, Material.ORANGE_STAINED_GLASS_PANE)
                .displayName(this.ownedUpgradeItem.getDisplayName().color(NamedTextColor.GOLD))
                .build();
        this.cantAffordUpgradeItem = ItemUtils.withMaterialBuilder(this.ownedUpgradeItem, Material.RED_STAINED_GLASS_PANE)
                .displayName(this.ownedUpgradeItem.getDisplayName().color(NamedTextColor.RED))
                .build();

        this.relativeBlocks = RelativeBlock.setFromJson(jsonObject.get("relativeBlocks").getAsJsonArray());
    }

    public int getLevel() {
        return this.level;
    }

    public int getCost() {
        return this.cost;
    }

    public double getRange() {
        return this.range;
    }

    public ItemStack getMenuItem() {
        return this.menuItem;
    }

    public ItemStack getCantAffordUpgradeItem() {
        return this.cantAffordUpgradeItem;
    }

    public ItemStack getOwnedUpgradeItem() {
        return this.ownedUpgradeItem;
    }

    public ItemStack getBuyUpgradeItem() {
        return this.buyUpgradeItem;
    }

    public Set<RelativeBlock> getRelativeBlocks() {
        return this.relativeBlocks;
    }
}
