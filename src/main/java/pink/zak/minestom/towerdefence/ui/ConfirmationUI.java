package pink.zak.minestom.towerdefence.ui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfirmationUI extends Inventory {

    private static final @NotNull Component TITLE = Component.text("Are you sure?", NamedTextColor.DARK_GRAY);
    private static final @NotNull ItemStack CONFIRM_ITEM = ItemStack.builder(Material.GREEN_STAINED_GLASS_PANE)
            .displayName(Component.text("Confirm", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
            .build();
    private static final @NotNull ItemStack CANCEL_ITEM = ItemStack.builder(Material.RED_STAINED_GLASS_PANE)
            .displayName(Component.text("Cancel", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            .build();

    private final @NotNull Player player;
    private final @NotNull BooleanConsumer consumer;
    private final boolean closeOnCompletion;

    public ConfirmationUI(@NotNull Player player, @Nullable Component warning, @NotNull BooleanConsumer consumer, boolean closeOnCompletion) {
        super(InventoryType.CHEST_1_ROW, TITLE);
        this.player = player;
        this.consumer = consumer;
        this.closeOnCompletion = closeOnCompletion;

        ItemStack confirmItem = warning != null ? CONFIRM_ITEM.withLore(List.of(warning.decoration(TextDecoration.ITALIC, false))) : CONFIRM_ITEM;

        this.setItemStack(2, confirmItem);
        this.setItemStack(6, CANCEL_ITEM);

        this.addInventoryCondition((p, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!p.equals(player)) return;

            // check if the player clicked confirm or cancel
            if (slot == 2) this.onClick(true);
            else if (slot == 6) this.onClick(false);
        });
    }

    public ConfirmationUI(@NotNull Player player, @Nullable Component warning, @NotNull BooleanConsumer consumer) {
        this(player, warning, consumer, true);
    }

    private void onClick(boolean result) {
        if (closeOnCompletion) this.player.closeInventory();
        this.consumer.accept(result);
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        boolean removed = super.removeViewer(player);
        if (removed) MinecraftServer.getSchedulerManager().scheduleNextTick(() -> this.onClick(false));
        return removed;
    }

}
