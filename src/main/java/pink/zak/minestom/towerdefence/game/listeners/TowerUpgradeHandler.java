package pink.zak.minestom.towerdefence.game.listeners;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TowerUpgradeHandler {
    private static final Map<Tower, Component> TOWER_UPGRADE_TITLES = Maps.newHashMap();
    private static final ItemStack RADIUS_MENU_ITEM;
    private final TowerDefencePlugin plugin;
    private final GameHandler gameHandler;
    private final TowerHandler towerHandler;

    static {
        RADIUS_MENU_ITEM = ItemStack.builder(Material.REDSTONE)
            .displayName(StringUtils.parseMessage("<red>Preview Tower Radius"))
            .lore(StringUtils.parseMessages(List.of(
                "",
                "<red>Shows a particle outline of the tower's radius"
            )))
            .build();
    }

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
            boolean canAfford = gameUser.getCoins() >= towerLevel.cost();

            ItemStack itemStack = purchased ? towerLevel.ownedUpgradeItem() : canAfford ? towerLevel.buyUpgradeItem() : towerLevel.cantAffordUpgradeItem();
            inventory.setItemStack(10 + i, itemStack);
        }

        inventory.setItemStack(26, RADIUS_MENU_ITEM);

        gameUser.getPlayer().openInventory(inventory);
    }

    private void startTowerUpgradeGuiListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (event.getClickType() == ClickType.START_DOUBLE_CLICK || inventory == null)
                return;

            if (!inventory.hasTag(PlacedTower.ID_TAG))
                return;
            Player player = event.getPlayer();
            GameUser gameUser = this.gameHandler.getGameUser(player);
            PlacedTower placedTower = this.towerHandler.getTower(gameUser, inventory.getTag(PlacedTower.ID_TAG));

            event.setCancelled(true);
            int slot = event.getSlot();

            if (slot == 26) {
                this.showTowerRadius(player, placedTower);
                return;
            }

            int clickedLevelInt = slot - 10;

            Tower tower = placedTower.getTower();
            int level = placedTower.getLevelInt();

            if (clickedLevelInt < 0 || clickedLevelInt > tower.maxLevel() || level + 1 != clickedLevelInt)
                return;

            int cost = tower.level(clickedLevelInt).cost();

            if (gameUser.getMana() >= cost) {
                gameUser.updateAndGetCoins(current -> current - cost);
                placedTower.upgrade();
                TowerLevel towerLevel = placedTower.getLevel();
                // update inventory items
                inventory.setItemStack(0, towerLevel.menuItem());
                inventory.setItemStack(10 + clickedLevelInt, towerLevel.ownedUpgradeItem());
            }
        });
    }

    private void showTowerRadius(Player player, PlacedTower tower) {
        Point center = tower.getBasePoint();
        double radius = tower.getLevel().range();

        Set<SendablePacket> packets = new HashSet<>();
        for (double i = 1; i <= 360; i += 1.5) {
            double c1 = radius * Math.cos(i);
            double c2 = radius * Math.sin(i);

            packets.add(ParticleCreator.createParticlePacket(Particle.DUST, true,
                center.x() + c1, center.y() + 1.5, center.z() + c2,
                0, 0, 0, 0f, 10, binaryWriter -> {
                    binaryWriter.writeFloat(1);
                    binaryWriter.writeFloat(0);
                    binaryWriter.writeFloat(0);
                    binaryWriter.writeFloat(1.5f);
                }));
        }
        player.sendPackets(packets);
        //circle.iterator(ShapeOptions.builder(Particle.DUST).build()).draw(player);

        MinecraftServer.getSchedulerManager() // todo cancel
            .buildTask(() -> {
                player.sendPackets(packets);
            })
            .repeat(750, TimeUnit.MILLISECOND)
            .schedule();
    }
}
