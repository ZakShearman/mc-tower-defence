package pink.zak.minestom.towerdefence.model.mob.config;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

public class EnemyMobLevel {
    private final int level;
    private final int cost;
    private final int killReward;
    private final int manaKillReward;
    private final int health;
    private final int damage;
    private final double movementSpeed;
    private final int manaCost;
    private final ItemStack sendItem;
    private final ItemStack ownedUpgradeItem;
    private final ItemStack buyUpgradeItem;
    private final ItemStack cantAffordUpgradeItem;

    public EnemyMobLevel(JsonObject jsonObject) {
        this.level = jsonObject.get("level").getAsInt();
        this.cost = jsonObject.get("cost").getAsInt();
        this.killReward = this.cost / 4; // todo balance
        this.manaKillReward = this.cost / 8; // todo balance
        this.health = jsonObject.get("health").getAsInt();
        this.damage = jsonObject.get("damage").getAsInt();
        this.movementSpeed = jsonObject.get("movementSpeed").getAsDouble() / MinecraftServer.TICK_PER_SECOND;
        this.manaCost = jsonObject.get("manaCost").getAsInt();
        this.sendItem = ItemUtils.fromJsonObject(jsonObject.get("sendItem").getAsJsonObject());
        ItemStack ownedUpgradeItem = ItemUtils.fromJsonObject(jsonObject.get("upgradeItem").getAsJsonObject());

        this.ownedUpgradeItem = ownedUpgradeItem.withDisplayName(ownedUpgradeItem.getDisplayName().color(NamedTextColor.GREEN));
        this.buyUpgradeItem = ItemUtils.withMaterialBuilder(this.ownedUpgradeItem, Material.ORANGE_STAINED_GLASS_PANE)
                .displayName(this.ownedUpgradeItem.getDisplayName().color(NamedTextColor.GOLD))
                .build();
        this.cantAffordUpgradeItem = ItemUtils.withMaterialBuilder(ownedUpgradeItem, Material.RED_STAINED_GLASS_PANE)
                .displayName(this.ownedUpgradeItem.getDisplayName().color(NamedTextColor.RED))
                .build();

    }

    public int getLevel() {
        return this.level;
    }

    public int getCost() {
        return this.cost;
    }

    public int getKillReward() {
        return this.killReward;
    }

    public int getManaKillReward() {
        return manaKillReward;
    }

    public int getHealth() {
        return this.health;
    }

    public int getDamage() {
        return this.damage;
    }

    /**
     * @return Distance moved per tick
     */
    public double getMovementSpeed() {
        return this.movementSpeed;
    }

    public int getManaCost() {
        return this.manaCost;
    }

    public ItemStack getSendItem() {
        return this.sendItem;
    }

    public ItemStack getOwnedUpgradeItem() {
        return this.ownedUpgradeItem;
    }

    public ItemStack getBuyUpgradeItem() {
        return this.buyUpgradeItem;
    }

    public ItemStack getCantAffordUpgradeItem() {
        return this.cantAffordUpgradeItem;
    }
}
