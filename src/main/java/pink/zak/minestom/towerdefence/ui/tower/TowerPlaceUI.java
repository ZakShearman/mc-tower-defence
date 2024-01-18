package pink.zak.minestom.towerdefence.ui.tower;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.tower.TowerPlaceFailureReason;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.utils.Result;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

public final class TowerPlaceUI extends Inventory {

    private static final @NotNull Component TITLE = Component.text("Place a Tower", NamedTextColor.DARK_GRAY);

    private final @NotNull GameUser user;
    private final @NotNull TowerStorage towerStorage;
    private final @NotNull TowerManager towerManager;

    public TowerPlaceUI(@NotNull GameUser user, @NotNull TowerStorage towerStorage, @NotNull TowerManager towerManager) {
        super(InventoryType.CHEST_3_ROW, TITLE);
        this.user = user;
        this.towerStorage = towerStorage;
        this.towerManager = towerManager;

        for (Tower tower : this.towerStorage.getTowers().values())
            this.setItemStack(tower.getGuiSlot(), tower.getBaseItem());

        this.addInventoryCondition((player, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!player.equals(this.user.getPlayer())) return;

            // run the click handler
            this.onClick(slot);
        });
    }

    private void onClick(int slot) {
        Tower tower = this.towerStorage.getTower(slot);
        if (tower == null) return;

        TDPlayer player = this.user.getPlayer();
        player.closeInventory();

        Point lastClickedTowerBlock = this.user.getLastClickedTowerBlock();
        if (lastClickedTowerBlock == null) throw new IllegalStateException("Last clicked tower block is null");

        Result<TowerPlaceFailureReason> result = this.towerManager.placeTower(tower, lastClickedTowerBlock, this.user);
        if (!(result instanceof Result.Failure<TowerPlaceFailureReason> failure)) return;

        player.sendMessage(Component.text(switch (failure.reason()) {
            case CAN_NOT_AFFORD -> "You can not afford this tower.";
            case AREA_NOT_CLEAR -> "The area is not clear.";
        }, NamedTextColor.RED));
    }

}
