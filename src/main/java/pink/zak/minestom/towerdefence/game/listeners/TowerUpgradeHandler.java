package pink.zak.minestom.towerdefence.game.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

public class TowerUpgradeHandler {
    private final GameHandler gameHandler;
    private final TowerHandler towerHandler;

    public TowerUpgradeHandler(TowerDefencePlugin plugin, GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.towerHandler = gameHandler.getTowerHandler();

        plugin.getEventNode()
            .addListener(PlayerBlockInteractEvent.class, event -> {
                Player player = event.getPlayer();
                if (event.getHand() != Player.Hand.MAIN || plugin.getGameState() != GameState.IN_PROGRESS)
                    return;
                GameUser gameUser = this.gameHandler.getGameUser(player);
                Short towerId = event.getBlock().getTag(PlacedTower.ID_TAG);
                if (gameUser == null || towerId == null)
                    return;
                PlacedTower tower = this.towerHandler.getTower(gameUser, towerId);
                this.openUpgradeGui(gameUser, tower);
            });
    }

    private void openUpgradeGui(GameUser gameUser, PlacedTower placeTower) {
        TowerLevel currentLevel = placeTower.getLevel();
        Tower tower = placeTower.getTower();

        TextComponent title = Component.text("Upgrade " + tower.name());
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, title);

        inventory.setItemStack(0, currentLevel.menuItem());

        inventory.setItemStack(11, tower.level(1).ownedUpgradeItem());
        for (int i = 2; i <= tower.maxLevel(); i++) {
            TowerLevel towerLevel = tower.level(i);
            boolean purchased = i <= currentLevel.level();
            boolean canAfford = gameUser.getCoins().get() >= towerLevel.cost();

            ItemStack itemStack = purchased ? towerLevel.ownedUpgradeItem() : canAfford ? towerLevel.buyUpgradeItem() : towerLevel.cantAffordUpgradeItem();
            inventory.setItemStack(10 + i, itemStack);
        }

        gameUser.getPlayer().openInventory(inventory);
    }
}
