package pink.zak.minestom.towerdefence.ui.spawner;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.upgrade.MobUpgradeFailureReason;
import pink.zak.minestom.towerdefence.upgrade.UpgradeHandler;
import pink.zak.minestom.towerdefence.utils.Result;

import java.util.Optional;

public final class TroopUpgradeUI extends Inventory {

    private static final @NotNull ItemStack BACK_ITEM = ItemStack.builder(Material.BARRIER)
            .set(ItemComponent.CUSTOM_NAME, Component.text("Back", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private final @NotNull Inventory parent;
    private final @NotNull GameUser gameUser;
    private final @NotNull EnemyMob mob;

    public TroopUpgradeUI(@NotNull Inventory parent, @NotNull GameUser gameUser, @NotNull EnemyMob mob) {
        super(InventoryType.CHEST_3_ROW, mob.getCommonName()); // this title isn't needed
        this.parent = parent;
        this.gameUser = gameUser;
        this.mob = mob;

        this.setItemStack(18, BACK_ITEM);
        this.refresh();

        this.addInventoryCondition((player, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!player.equals(this.gameUser.getPlayer())) return;

            // run the click handler
            this.onClick(slot);
        });
    }

    private void refresh() {
        UpgradeHandler upgradeHandler = this.gameUser.getUpgradeHandler();
        Optional<EnemyMobLevel> currentLevel = upgradeHandler.getLevel(this.mob);

        ItemStack preview = currentLevel
                .map(EnemyMobLevel::createSendItem)
                .orElse(this.mob.getBaseItem());
        this.setItemStack(0, preview);

        for (EnemyMobLevel level : this.mob.getLevels()) {
            boolean owned = upgradeHandler.has(this.mob, level);

            ItemStack item;
            if (!owned) {
                int cost = upgradeHandler.getCost(this.mob, level);
                boolean canAfford = this.gameUser.getCoins() >= cost;
                item = currentLevel
                        .map(l -> level.createBuyUpgradeItem(cost, canAfford, l))
                        .orElse(level.createStatUpgradeItem(cost, canAfford, false));
            } else {
                item = level.createStatUpgradeItem(level.getUnlockCost(), true, true);
            }

            this.setItemStack(10 + level.asInteger(), item);
        }
    }

    public void onClick(int slot) {
        if (slot == 18) {
            this.gameUser.getPlayer().openInventory(this.parent);
            return;
        }

        int clickedLevel = slot - 10;
        if (clickedLevel < 1) return;
        if (clickedLevel > this.mob.getMaxLevel()) return;

        EnemyMobLevel level = this.mob.getLevel(clickedLevel);
        if (level == null) return;

        if (this.gameUser.getUpgradeHandler().upgrade(this.mob, level) instanceof Result.Failure<MobUpgradeFailureReason> failure) {
            this.gameUser.getPlayer().sendMessage(switch (failure.reason()) {
                case ALREADY_AT_LEVEL -> "You already have this level!";
                case CANNOT_AFFORD -> "You cannot afford this upgrade!";
            });
        }
        this.refresh();
    }

}
