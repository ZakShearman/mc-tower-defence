package pink.zak.minestom.towerdefence.model.mob;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.Template;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

public record EnemyMobLevel(int level, int price, int health, int damage, double movementSpeed, int manaCost,
                            ItemStack sendItem,
                            ItemStack ownedUpgradeItem, ItemStack buyUpgradeItem, ItemStack cantAffordUpgradeItem) {

    public static EnemyMobLevel fromJsonObject(JsonObject jsonObject) {
        int level = jsonObject.get("level").getAsInt();
        int price = jsonObject.get("price").getAsInt();
        int health = jsonObject.get("health").getAsInt();
        int damage = jsonObject.get("damage").getAsInt();
        double movementSpeed = jsonObject.get("movementSpeed").getAsDouble() / 20;
        int manaCost = jsonObject.get("manaCost").getAsInt();
        ItemStack sendItem = ItemUtils.fromJsonObject(jsonObject.get("sendItem").getAsJsonObject());
        ItemStack ownedUpgradeItem = ItemUtils.fromJsonObject(
            jsonObject.get("upgradeItem").getAsJsonObject(),
            Template.of("price", String.valueOf(price)),
            Template.of("health", String.valueOf(health)),
            Template.of("damage", String.valueOf(damage)),
            Template.of("speed", String.valueOf(Math.ceil(movementSpeed * 20))),
            Template.of("manaCost", String.valueOf(manaCost))
        );

        ownedUpgradeItem = ownedUpgradeItem.withDisplayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GREEN));
        ItemStack buyUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.ORANGE_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GOLD))
            .build();
        ItemStack cantAffordUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.RED_STAINED_GLASS_PANE)
            .displayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.RED))
            .build();

        return new EnemyMobLevel(level, price, health, damage, movementSpeed, manaCost, sendItem, ownedUpgradeItem, buyUpgradeItem, cantAffordUpgradeItem);
    }
}
