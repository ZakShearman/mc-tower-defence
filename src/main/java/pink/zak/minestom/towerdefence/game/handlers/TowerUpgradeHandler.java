package pink.zak.minestom.towerdefence.game.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
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
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TowerUpgradeHandler {
    private static final @NotNull Function<PlacedTower<?>, ItemStack> RADIUS_MENU_ITEM_FUNCTION = tower -> ItemStack.builder(Material.REDSTONE)
            .displayName(StringUtils.parseMessage("<red>Preview Tower Radius", null))
            .lore(StringUtils.parseMessages(null,
                    "",
                    "<dark_red>Left-click</dark_red><red> shows the tower's range",
                    "<dark_red>Right-click</dark_red><red> shows all %s tower ranges"
                            .formatted(tower.getTower().getName())
            ))
            .build();

    private static final @NotNull ItemStack REMOVE_TOWER_ITEM = ItemStack.builder(Material.BARRIER)
            .displayName(StringUtils.parseMessage("<red>Remove Tower", null))
            .lore(StringUtils.parseMessages(null,
                    "",
                    "<red>Removes the tower"
            ))
            .build();

    private final @NotNull Map<Tower, Component> towerUpgradeTitles = new HashMap<>();
    private final @NotNull TowerDefenceModule plugin;
    private final @NotNull GameHandler gameHandler;
    private final @NotNull TowerHandler towerHandler;

    public TowerUpgradeHandler(@NotNull TowerDefenceModule plugin, @NotNull GameHandler gameHandler) {
        for (Tower tower : plugin.getTowerStorage().getTowers().values())
            this.towerUpgradeTitles.put(tower, Component.text(tower.getName()));

        this.plugin = plugin;
        this.gameHandler = gameHandler;
        this.towerHandler = gameHandler.getTowerHandler();

        plugin.getEventNode()
                .addListener(PlayerBlockInteractEvent.class, event -> {
                    Player player = event.getPlayer();
                    if (event.getHand() != Player.Hand.MAIN || plugin.getGameState() != GameState.GAME)
                        return;
                    GameUser gameUser = this.gameHandler.getGameUser(player);
                    Integer towerId = event.getBlock().getTag(PlacedTower.ID_TAG);
                    if (gameUser == null || towerId == null)
                        return;
                    PlacedTower<?> tower = this.towerHandler.getTower(gameUser, towerId);
                    if (tower == null) return; // Tower is null if it is from the other team

                    this.openUpgradeGui(gameUser, tower);
                });

        this.startTowerUpgradeGuiListener();
    }

    private void openUpgradeGui(@NotNull GameUser gameUser, @NotNull PlacedTower<?> placedTower) {
        TowerLevel currentLevel = placedTower.getLevel();
        Tower tower = placedTower.getTower();

        String towerOwnerName = placedTower.getOwner().getPlayer().getUsername();
        Component title = this.towerUpgradeTitles.get(tower).append(Component.text(" - " + towerOwnerName));

        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, title);
        inventory.setTag(PlacedTower.ID_TAG, placedTower.getId());
        inventory.setItemStack(0, tower.getBaseItem());

        inventory.setItemStack(11, tower.getLevel(1).getOwnedUpgradeItem());
        for (int i = 2; i <= tower.getMaxLevel(); i++) {
            TowerLevel targetLevel = tower.getLevel(i);
            boolean purchased = i <= currentLevel.getLevel();

            ItemStack itemStack;
            if (purchased) {
                itemStack = targetLevel.getOwnedUpgradeItem();
            } else {
                int cost = 0;
                for (int j = currentLevel.getLevel() + 1; j <= targetLevel.getLevel(); j++) {
                    cost += tower.getLevel(j).getCost();
                }

                boolean canAfford = gameUser.getCoins() >= cost;
                itemStack = targetLevel.createBuyUpgradeItem(canAfford, cost, currentLevel);
            }

            inventory.setItemStack(10 + i, itemStack);
        }

        inventory.setItemStack(17, REMOVE_TOWER_ITEM);
        inventory.setItemStack(26, RADIUS_MENU_ITEM_FUNCTION.apply(placedTower));

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
            PlacedTower<?> placedTower = this.towerHandler.getTower(gameUser, inventory.getTag(PlacedTower.ID_TAG));

            event.setCancelled(true);
            int slot = event.getSlot();

            if (slot == 26) {
                this.showTowerRadius(gameUser, player, placedTower, event.getClickType());
                return;
            }

            if (slot == 17) {
                this.removeTower(gameUser, placedTower);
                return;
            }

            int clickedLevelInt = slot - 10;

            Tower tower = placedTower.getTower();
            int currentLevel = placedTower.getLevelInt();

            if (clickedLevelInt < 0 || clickedLevelInt > tower.getMaxLevel() || currentLevel >= clickedLevelInt)
                return;

            int cost = 0;
            for (int i = currentLevel + 1; i <= clickedLevelInt; i++)
                cost += tower.getLevel(i).getCost();

            if (gameUser.getCoins() >= cost) {
                int finalCost = cost;
                gameUser.updateCoins(current -> current - finalCost);
                placedTower.upgrade(clickedLevelInt);
                TowerLevel towerLevel = placedTower.getLevel();
                // update inventory items
                inventory.setItemStack(0, tower.getBaseItem());
                for (int i = currentLevel + 1; i <= clickedLevelInt; i++)
                    inventory.setItemStack(10 + i, towerLevel.getOwnedUpgradeItem());
            }
        });
    }

    private void showTowerRadius(@NotNull GameUser gameUser, @NotNull Player player, @NotNull PlacedTower<?> tower, ClickType clickType) {
        Set<SendablePacket> packets = clickType == ClickType.LEFT_CLICK ? this.createRadiusPackets(tower, Set.of()) : new HashSet<>();

        if (clickType == ClickType.RIGHT_CLICK) {
            Set<PlacedTower<?>> towers = this.towerHandler.getTowers(gameUser.getTeam()).stream()
                    .filter(testTower -> testTower.getTower().getType() == tower.getTower().getType())
                    .collect(Collectors.toUnmodifiableSet());

            for (PlacedTower<?> placedTower : towers) {
                Set<PlacedTower<?>> otherTowers = new HashSet<>(towers);
                otherTowers.remove(placedTower);
                packets.addAll(this.createRadiusPackets(placedTower, otherTowers));
            }
        }

        player.sendPackets(packets);

        Task task = MinecraftServer.getSchedulerManager()
                .buildTask(() -> player.sendPackets(packets))
                .repeat(750, TimeUnit.MILLISECOND)
                .schedule();

        MinecraftServer.getSchedulerManager()
                .buildTask(task::cancel)
                .delay(5, TimeUnit.SECOND)
                .schedule();
    }

    private Set<SendablePacket> createRadiusPackets(@NotNull PlacedTower<?> tower, Set<PlacedTower<?>> otherTowers) {
        Point center = tower.getBasePoint();
        double radius = tower.getLevel().getRange();

        Set<SendablePacket> packets = new HashSet<>();
        for (int i = 0; i <= 360; i += 1) {
            double c1 = radius * Math.cos(i);
            double c2 = radius * Math.sin(i);

            Vec vec = new Vec(center.x() + c1, center.y() + 1.5, center.z() + c2);

            boolean isOverlapping = false;
            for (PlacedTower<?> otherTower : otherTowers) {
                if (vec.distance(otherTower.getBasePoint()) < otherTower.getLevel().getRange()) {
                    isOverlapping = true;
                    break;
                }
            }

            if (isOverlapping) continue;

            packets.add(ParticleCreator.createParticlePacket(Particle.DUST, true,
                    center.x() + c1, center.y() + 1.5, center.z() + c2,
                    0, 0, 0, 0f, 1,
                    binaryWriter -> {
                        binaryWriter.writeFloat(1);
                        binaryWriter.writeFloat(0);
                        binaryWriter.writeFloat(0);
                        binaryWriter.writeFloat(1.5f);
                    }));
        }

        return packets;
    }

    private void removeTower(GameUser gameUser, PlacedTower<?> placedTower) {
        Player player = gameUser.getPlayer();

        placedTower.destroy();
        this.towerHandler.removeTower(gameUser, placedTower);

        player.closeInventory();
        player.sendMessage(Component.text("Tower removed!", NamedTextColor.RED));
    }
}
