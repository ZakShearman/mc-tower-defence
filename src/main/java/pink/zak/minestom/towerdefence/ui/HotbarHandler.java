package pink.zak.minestom.towerdefence.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.tower.TowerPlaceFailureReason;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.storage.TowerStorage;
import pink.zak.minestom.towerdefence.ui.spawner.TroopSpawnerUI;
import pink.zak.minestom.towerdefence.ui.tower.TowerPlaceUI;
import pink.zak.minestom.towerdefence.utils.Result;

import java.util.Set;

public final class HotbarHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);

    private final @NotNull EventNode<? super PlayerEvent> parentNode;
    private final @NotNull TowerManager towerManager;
    private final @NotNull TowerStorage towerStorage;
    private final @NotNull TowerMap map;

    public HotbarHandler(@NotNull TowerDefenceModule module, @NotNull TowerManager towerManager, @NotNull EventNode<? super PlayerEvent> node) {
        this.parentNode = node;
        this.towerManager = towerManager;
        this.towerStorage = module.getTowerStorage();
        this.map = module.getInstance().getTowerMap();

        this.eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (module.getGameState() != GameState.GAME) return;

            Player p = event.getPlayer();
            if (!(p instanceof TDPlayer player)) throw new IllegalStateException("Player is not a TDPlayer");

            GameUser user = module.getGameHandler().getGameUser(player);
            if (user == null) throw new IllegalStateException("Player is not associated with a game user");

            ItemStack item = event.getItemStack();
            if (item.isSimilar(TroopSpawnerUI.HOTBAR_ITEM)) {
                player.openInventory(new TroopSpawnerUI(module.getMobStorage(), user));
            } else if (item.isSimilar(UserSettingsUI.HOTBAR_ITEM)) {
                player.openInventory(new UserSettingsUI(player));
            } else if (item.hasTag(TowerPlaceUI.UI_TAG)) {
                Point targetBlockPos = player.getTargetBlockPosition(TowerPlaceUI.TOWER_PLACE_DISTANCE);
                TowerType towerType = item.getTag(TowerPlaceUI.TOWER_TYPE);

                if (targetBlockPos == null || towerType == null) { // classed as the player clicking air or tower not set
                    player.openInventory(new TowerPlaceUI(user, module.getTowerStorage()));
                    return;
                }

                if (!this.map.getArea(user.getTeam()).isWithin(targetBlockPos)) {
                    player.sendMessage(Component.text("You can only place towers on your side of the map (" + user.getTeam().name().toLowerCase() + ").", NamedTextColor.RED));
                    return;
                }

                this.handleTowerPlaceClick(towerType, player, user, targetBlockPos);
            }
        });
    }

    private void handleTowerPlaceClick(@NotNull TowerType towerType, @NotNull Player player, @NotNull GameUser gameUser, @NotNull Point clickedBlock) {
        Tower tower = this.towerStorage.getTower(towerType);

        Result<TowerPlaceFailureReason> result = (this.towerManager.placeTower(tower, clickedBlock.add(0, 0.5, 0), gameUser));
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
