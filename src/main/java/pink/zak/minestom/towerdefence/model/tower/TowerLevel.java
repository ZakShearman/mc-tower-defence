package pink.zak.minestom.towerdefence.model.tower;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.Set;

public record TowerLevel(int level, int cost, int fireDelay, float damage, double range,
                         ItemStack menuItem, ItemStack ownedUpgradeItem, ItemStack buyUpgradeItem,
                         ItemStack cantAffordUpgradeItem, Set<RelativeBlock> relativeBlocks) {

    public static TowerLevel fromJsonObject(JsonObject jsonObject) {
        int level = jsonObject.get("level").getAsInt();
        int cost = jsonObject.get("cost").getAsInt();
        int fireDelay = jsonObject.has("fireDelay") ? jsonObject.get("fireDelay").getAsInt() : -1;
        float damage = jsonObject.has("damage") ? jsonObject.get("damage").getAsFloat() : -1;
        double range = jsonObject.has("range") ? jsonObject.get("range").getAsDouble() : -1;

        ItemStack menuItem = ItemUtils.fromJsonObject(jsonObject.get("menuItem").getAsJsonObject());

        ItemStack ownedUpgradeItem = ItemUtils.fromJsonObject(jsonObject.get("upgradeItem").getAsJsonObject());

        ownedUpgradeItem = ownedUpgradeItem.withDisplayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GREEN));
        ItemStack buyUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.ORANGE_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GOLD))
            .build();
        ItemStack cantAffordUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.RED_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.RED))
            .build();

        Set<RelativeBlock> relativeBlocks = RelativeBlock.setFromJson(jsonObject.get("relativeBlocks").getAsJsonArray());

        return new TowerLevel(level, cost,
            fireDelay, damage, range,
            menuItem, ownedUpgradeItem, buyUpgradeItem, cantAffordUpgradeItem, relativeBlocks);
    }
}
