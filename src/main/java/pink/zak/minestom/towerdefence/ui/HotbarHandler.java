package pink.zak.minestom.towerdefence.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.tower.TowerPlaceFailureReason;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;
import pink.zak.minestom.towerdefence.ui.spawner.TroopSpawnerUI;
import pink.zak.minestom.towerdefence.ui.tower.TowerManagementUI;
import pink.zak.minestom.towerdefence.ui.tower.TowerPlaceUI;
import pink.zak.minestom.towerdefence.utils.Result;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.Set;

public final class HotbarHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);

    private final @NotNull EventNode<? super PlayerEvent> parentNode;
    private final @NotNull TowerDefenceModule module;
    private final @NotNull GameHandler gameHandler;
    private final @NotNull TowerManager towerManager;
    private final @NotNull TowerStorage towerStorage;
    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull MobStorage mobStorage;

    public HotbarHandler(@NotNull TowerDefenceModule module, @NotNull TowerManager towerManager, @NotNull GameHandler gameHandler,
                         @NotNull EventNode<? super PlayerEvent> node) {
        this.module = module;
        this.gameHandler = gameHandler;
        this.parentNode = node;
        this.towerManager = towerManager;
        this.towerStorage = module.getTowerStorage();
        this.instance = module.getInstance();
        this.mobStorage = module.getMobStorage();

        this.eventNode.addListener(PlayerUseItemEvent.class, event -> {
            this.handleItemUse(event.getPlayer(), event.getItemStack());
        });

        this.eventNode.addListener(EventListener.builder(PlayerBlockPlaceEvent.class).ignoreCancelled(false)
                .handler(event -> {
                    // assumes it's never in off-hand, it shouldn't be
                    this.handleItemUse(event.getPlayer(), event.getPlayer().getItemInMainHand());
                }).build());
    }

    private void handleItemUse(@NotNull Player p, @NotNull ItemStack usedItem) {
        if (this.module.getGameState() != GameState.GAME) return;

        if (!(p instanceof TDPlayer player)) throw new IllegalStateException("Player is not a TDPlayer");

        GameUser user = this.gameHandler.getGameUser(player);
        if (user == null) throw new IllegalStateException("Player is not associated with a game user");

        if (usedItem.isSimilar(TroopSpawnerUI.HOTBAR_ITEM)) {
            player.openInventory(new TroopSpawnerUI(this.mobStorage, user));
            return;
        }

        if (usedItem.isSimilar(UserSettingsUI.HOTBAR_ITEM)) {
            player.openInventory(new UserSettingsUI(player));
            return;
        }

        Point targetBlockPos = player.getTargetBlockPosition(TowerPlaceUI.TOWER_PLACE_DISTANCE);

        if (targetBlockPos != null) {
            Block targetBlock = targetBlock = player.getInstance().getBlock(targetBlockPos);
            if (targetBlock.hasTag(PlacedTower.ID_TAG)) {
                // todo check team, maybe we don't need to do this because tower IDs are globally unique?
                PlacedTower<?> tower = this.towerManager.getTower(user.getTeam(), targetBlock.getTag(PlacedTower.ID_TAG));
                if (tower != null) {
                    player.openInventory(new TowerManagementUI(tower, user, this.towerManager));
                }

                return;
            }
        }

        if (usedItem.hasTag(TowerPlaceUI.UI_TAG)) {
            TowerType towerType = usedItem.getTag(TowerPlaceUI.TOWER_TYPE);

            if (targetBlockPos == null || towerType == null) { // classed as the player clicking air or tower not set
                player.openInventory(new TowerPlaceUI(user, this.towerStorage));
                return;
            }

            if (!this.instance.getTowerMap().getArea(user.getTeam()).isWithin(targetBlockPos)) {
                player.sendMessage(Component.text("You can only place towers on your side of the map (" + user.getTeam().name().toLowerCase() + ").", NamedTextColor.RED));
                return;
            }

            this.handleTowerPlaceClick(towerType, player, user, targetBlockPos);
        }
    }

    private void handleTowerPlaceClick(@NotNull TowerType towerType, @NotNull Player player, @NotNull GameUser gameUser, @NotNull Point clickedBlock) {
        Tower tower = this.towerStorage.getTower(towerType);

        Result<TowerPlaceFailureReason> result = (this.towerManager.placeTower(tower, clickedBlock.add(0.5, 0.5, 0.5), gameUser));
        if (!(result instanceof Result.Failure<TowerPlaceFailureReason> failure)) return;

        player.sendMessage(Component.text(switch (failure.reason()) {
            case CAN_NOT_AFFORD -> "You can not afford this tower.";
            case AREA_NOT_CLEAR -> "The area is not clear.";
        }, NamedTextColor.RED));
    }

    public void initialise(@NotNull Set<Player> players) {
        this.parentNode.addChild(this.eventNode);
        for (Player player : players) {
            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setItemStack(0, TroopSpawnerUI.HOTBAR_ITEM);
            inventory.setItemStack(1, TowerPlaceUI.HOTBAR_ITEM);
            inventory.setItemStack(8, UserSettingsUI.HOTBAR_ITEM);
        }
    }

    public void shutdown() {
        this.parentNode.removeChild(this.eventNode);
    }

}
