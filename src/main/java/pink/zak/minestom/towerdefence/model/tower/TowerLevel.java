package pink.zak.minestom.towerdefence.model.tower;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record TowerLevel(int level, String name, int cost, int fireDelay, double damage, double range,
                         ItemStack menuItem, ItemStack ownedUpgradeItem, ItemStack buyUpgradeItem,
                         ItemStack cantAffordUpgradeItem, Set<RelativeBlock> relativeBlocks) {

    public static TowerLevel fromJsonObject(JsonObject jsonObject) {
        int level = jsonObject.get("level").getAsInt();
        String name = jsonObject.get("name").getAsString();
        int cost = jsonObject.get("cost").getAsInt();
        int fireDelay = jsonObject.get("fireDelay").getAsInt();
        double damage = jsonObject.get("damage").getAsDouble();
        double range = jsonObject.get("range").getAsDouble();

        ItemStack menuItem = ItemUtils.fromJsonObject(jsonObject.get("menuItem").getAsJsonObject());

        ItemStack ownedUpgradeItem = ItemUtils.fromJsonObject(jsonObject.get("upgradeItem").getAsJsonObject());

        ownedUpgradeItem = ownedUpgradeItem.withDisplayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GREEN));
        ItemStack buyUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.ORANGE_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GOLD))
            .build();
        ItemStack cantAffordUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.RED_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.RED))
            .build();

        Set<RelativeBlock> relativeBlocks = StreamSupport.stream(jsonObject.get("relativeBlocks").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativeBlock::fromJson)
            .collect(Collectors.toUnmodifiableSet());

        return new TowerLevel(level, name, cost,
            fireDelay, damage, range * range,
            menuItem, ownedUpgradeItem, buyUpgradeItem, cantAffordUpgradeItem, relativeBlocks);
    }
}
