package pink.zak.minestom.towerdefence.game.listeners;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

import java.util.Map;

public class TowerUpgradeHandler {
    private static final Map<Tower, Component> TOWER_UPGRADE_TITLES = Maps.newHashMap();
    private final TowerDefencePlugin plugin;
    private final GameHandler gameHandler;
    private final TowerHandler towerHandler;

    public TowerUpgradeHandler(TowerDefencePlugin plugin, GameHandler gameHandler) {
        for (Tower tower : plugin.getTowerStorage().getTowers().values())
            TOWER_UPGRADE_TITLES.put(tower, Component.text("Upgrade " + tower.name()));


        this.plugin = plugin;
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

        this.startTowerUpgradeGuiListener();
    }

    private void openUpgradeGui(GameUser gameUser, PlacedTower placedTower) {
        TowerLevel currentLevel = placedTower.getLevel();
        Tower tower = placedTower.getTower();

        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, TOWER_UPGRADE_TITLES.get(tower));
        inventory.setTag(PlacedTower.ID_TAG, placedTower.getId());
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

    private void startTowerUpgradeGuiListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (event.getClickType() == ClickType.START_DOUBLE_CLICK || inventory == null)
                return;

            if (!inventory.hasTag(PlacedTower.ID_TAG))
                return;

            event.setCancelled(true);
            int slot = event.getSlot();
            int clickedLevelInt = slot - 10;

            GameUser gameUser = this.gameHandler.getGameUser(event.getPlayer());
            PlacedTower placedTower = this.towerHandler.getTower(gameUser, inventory.getTag(PlacedTower.ID_TAG));
            Tower tower = placedTower.getTower();
            int level = placedTower.getLevelInt();

            if (clickedLevelInt < 0 || clickedLevelInt > tower.maxLevel() || level + 1 != clickedLevelInt)
                return;

            int cost = tower.level(clickedLevelInt).cost();

            if (gameUser.getMana().get() >= cost) {
                int newCoins = gameUser.getCoins().updateAndGet(current -> current - cost);
                placedTower.upgrade();
                TowerLevel towerLevel = placedTower.getLevel();
                // update inventory items
                inventory.setItemStack(0, towerLevel.menuItem());
                inventory.setItemStack(10 + clickedLevelInt, towerLevel.ownedUpgradeItem());

                MinecraftServer.getGlobalEventHandler().call(new PlayerCoinChangeEvent(gameUser, newCoins));
            }
        });
    }
}
