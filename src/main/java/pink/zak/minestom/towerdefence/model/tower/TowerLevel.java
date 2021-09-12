package pink.zak.minestom.towerdefence.model.tower;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.Template;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record TowerLevel(String name, int level, int cost, int fireDelay, double damage, double range,
                         List<String> description,
                         Set<RelativeBlock> relativeBlocks,
                         ItemStack ownedUpgradeItem, ItemStack buyUpgradeItem, ItemStack cantAffordUpgradeItem) {

    public static TowerLevel fromJsonObject(JsonObject jsonObject) {
        String name = jsonObject.get("name").getAsString();
        int level = jsonObject.get("level").getAsInt();
        int cost = jsonObject.get("cost").getAsInt();
        int fireDelay = jsonObject.get("fireDelay").getAsInt();
        double damage = jsonObject.get("damage").getAsDouble();
        double range = jsonObject.get("range").getAsDouble();

        List<String> description = StreamSupport.stream(jsonObject.get("description").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsString).toList();
        Set<RelativeBlock> relativeBlocks = StreamSupport.stream(jsonObject.get("relativeBlocks").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativeBlock::fromJson)
            .collect(Collectors.toUnmodifiableSet());

        ItemStack ownedUpgradeItem = ItemUtils.fromJsonObject(
            jsonObject.get("upgradeItem").getAsJsonObject(), Lists.newArrayList(
                Template.of("cost", String.valueOf(cost)),
                Template.of("damage", String.valueOf(damage)),
                Template.of("fireDelay", String.valueOf(fireDelay)),
                Template.of("range", String.valueOf(range)))
        );

        ownedUpgradeItem = ownedUpgradeItem.withDisplayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GREEN));
        ItemStack buyUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.ORANGE_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GOLD))
            .build();
        ItemStack cantAffordUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.RED_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.RED))
            .build();

        return new TowerLevel(name, level, cost,
            fireDelay, damage, range * range,
            description, relativeBlocks,
            ownedUpgradeItem, buyUpgradeItem, cantAffordUpgradeItem);
    }
}
