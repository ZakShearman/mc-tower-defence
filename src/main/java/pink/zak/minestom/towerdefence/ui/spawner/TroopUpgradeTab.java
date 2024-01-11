package pink.zak.minestom.towerdefence.ui.spawner;

import java.util.Optional;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.upgrade.UpgradeHandler;

public final class TroopUpgradeTab extends Inventory {

    private final @NotNull TroopSpawnerUI parent;
    private final @NotNull GameUser gameUser;
    private final @NotNull EnemyMob mob;

    public TroopUpgradeTab(@NotNull TroopSpawnerUI parent, @NotNull GameUser gameUser, @NotNull EnemyMob mob) {
        super(InventoryType.CHEST_2_ROW, mob.getCommonName()); // this title isn't needed
        this.parent = parent;
        this.gameUser = gameUser;
        this.mob = mob;

        refresh();
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
                        .map(l -> l.createStatUpgradeItem(cost, false, canAfford))
                        .orElse(level.createBuyUpgradeItem(canAfford, cost, level));
            } else item = level.createStatUpgradeItem(level.getUnlockCost(), true, true);

            this.setItemStack(1 + level.asInteger(), item);
        }

        this.parent.updateSubInventory();
    }

    public void onClick(int slot) {
        int clickedLevel = slot - 1;
        if (clickedLevel < 0) return;
        if (clickedLevel > this.mob.getMaxLevel()) return;

        EnemyMobLevel level = this.mob.getLevel(clickedLevel);
        if (level == null) return;

        this.gameUser.getUpgradeHandler().upgrade(this.mob, level);
        refresh();
    }

}
