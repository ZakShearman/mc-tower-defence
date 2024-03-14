package pink.zak.minestom.towerdefence.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

import java.util.List;

public class TowerPlaceUI extends Inventory {

    public static final @NotNull Tag<Boolean> UI_TAG = Tag.Boolean("tower-place-ui");

    public static final @NotNull ItemStack HOTBAR_ITEM = ItemStack.builder(Material.ENDER_CHEST)
            .displayName(Component.text("Place Tower", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .lore(createItemLore(null))
            .set(UI_TAG, true)
            .build();

    private static final @NotNull Component TITLE = Component.text("Place a Tower", NamedTextColor.DARK_GRAY);

    private final @NotNull GameUser user;
    private final @NotNull TowerStorage towerStorage;

    public TowerPlaceUI(@NotNull GameUser user, @NotNull TowerStorage towerStorage) {
        super(InventoryType.CHEST_3_ROW, TITLE);
        this.user = user;
        this.towerStorage = towerStorage;

        for (Tower tower : this.towerStorage.getTowers().values()) {
            this.setItemStack(tower.getGuiSlot(), tower.getBaseItem());
        }

        this.addInventoryCondition((player, slot, clickType, result) -> {
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!player.equals(this.user.getPlayer())) return;

            this.onClick(slot);
        });
    }

    private void onClick(int slot) {
        Tower tower = this.towerStorage.getTower(slot);
        if (tower == null) return;

        TDPlayer player = this.user.getPlayer();
        player.closeInventory();

        player.getInventory().setItemStack(1, createItemStack(tower));
    }

    private static ItemStack createItemStack(@NotNull Tower tower) {
        return ItemStack.builder(tower.getBaseItem().material())
                .displayName(Component.text("Place Tower: " + tower.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .lore(createItemLore(tower))
                .set(UI_TAG, true)
                .build();
    }

    private static List<Component> createItemLore(@Nullable Tower tower) {
        String towerName = tower == null ? "None" : tower.getName();
        return List.of(
                Component.text("Right-click block:", NamedTextColor.GOLD).append(Component.text(" Place selected tower", NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false),
                Component.text("Right-click air:", NamedTextColor.GOLD).append(Component.text(" Change tower", NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Selected Tower:", NamedTextColor.GOLD).append(Component.text(" " + towerName, NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false)
        );
    }
}
