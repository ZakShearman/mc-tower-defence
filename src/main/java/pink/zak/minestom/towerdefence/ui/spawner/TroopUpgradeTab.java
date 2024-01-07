package pink.zak.minestom.towerdefence.ui.spawner;

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
        int levelInt = this.gameUser.getMobLevel(this.mob);
        EnemyMobLevel level = this.mob.getLevel(levelInt);
        this.setItemStack(0, level == null ? mob.getBaseItem() : level.createSendItem());

        int maxLevel = mob.getMaxLevel();
        for (int i = 1; i <= maxLevel; i++) {
            EnemyMobLevel targetLevel = mob.getLevel(i);
            if (targetLevel == null) throw new IllegalStateException("Mob " + mob.getCommonName() + " does not have level " + i);
            boolean owned = i <= levelInt;

            ItemStack item;
            if (owned) item = targetLevel.createStatUpgradeItem(targetLevel.getUnlockCost(), true, true);
            else {
                int cost = this.getCost(targetLevel.getLevel());
                boolean canAfford = gameUser.getCoins() >= cost;
                if (level == null) item = targetLevel.createStatUpgradeItem(cost, false, canAfford);
                else item = targetLevel.createBuyUpgradeItem(canAfford, cost, level);
            }

            this.setItemStack(1 + i, item);
        }

        this.parent.updateSubInventory();
    }

    public void onClick(int slot) {
        int clickedLevel = slot - 1;
        if (clickedLevel < 0) return;
        if (clickedLevel > this.mob.getMaxLevel()) return;

        int currentLevel = this.getCurrentLevel();
        if (currentLevel >= clickedLevel) return;

        int cost = getCost(clickedLevel);

        EnemyMobLevel targetLevel = this.mob.getLevel(clickedLevel);
        if (targetLevel == null) throw new IllegalStateException("Mob " + this.mob.getCommonName() + " does not have level " + clickedLevel);

        if (this.gameUser.getCoins() < cost) return;
        this.gameUser.updateCoins(current -> current - cost);
        this.gameUser.getMobLevels().put(this.mob, clickedLevel);

        refresh();
    }

    private int getCurrentLevel() {
        return this.gameUser.getMobLevel(this.mob);
    }

    private int getCost(int level) {
        int cost = 0;
        for (int i = this.getCurrentLevel() + 1; i <= level; i++) {
            EnemyMobLevel mobLevel = this.mob.getLevel(i);
            if (mobLevel == null) throw new IllegalStateException("Mob " + this.mob.getCommonName() + " does not have level " + i);
            cost += mobLevel.getUnlockCost();
        }
        return cost;
    }

}
