package pink.zak.minestom.towerdefence.ui.spawner;

import java.util.Optional;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;

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
        Optional<EnemyMobLevel> optionalLevel = this.gameUser.getUpgradeHandler().getLevel(this.mob);
        ItemStack preview = optionalLevel
                .map(EnemyMobLevel::createSendItem)
                .orElse(this.mob.getBaseItem());
        this.setItemStack(0, preview);

        int maxLevel = this.mob.getMaxLevel();
        for (int i = 1; i <= maxLevel; i++) {
            EnemyMobLevel level = this.mob.getLevel(i);
            if (level == null) throw new IllegalStateException("Mob " + this.mob.getCommonName() + " does not have level " + i);

            boolean owned = i <= optionalLevel.map(EnemyMobLevel::getLevel).orElse(0);
            ItemStack item;
            if (owned) item = level.createStatUpgradeItem(level.getUnlockCost(), true, true);
            else {
                int cost = this.gameUser.getUpgradeHandler().getCost(this.mob, level);
                boolean canAfford = this.gameUser.getCoins() >= cost;
                item = optionalLevel
                        .map(l -> l.createStatUpgradeItem(cost, false, canAfford))
                        .orElse(level.createBuyUpgradeItem(canAfford, cost, level));
            }

            this.setItemStack(1 + i, item);
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
