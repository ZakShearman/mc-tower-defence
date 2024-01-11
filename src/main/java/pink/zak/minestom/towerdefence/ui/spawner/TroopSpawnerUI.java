package pink.zak.minestom.towerdefence.ui.spawner;

import java.util.Optional;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.event.EventListener;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.api.event.player.PlayerQueueUpdateEvent;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.queue.QueueFailureReason;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.utils.Result;

public final class TroopSpawnerUI extends Inventory {

    public static final @NotNull ItemStack HOTBAR_ITEM = ItemStack.builder(Material.CHEST)
            .displayName(Component.text("Send Troops", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private static final @NotNull MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final @NotNull Component SEND_TITLE = Component.text("Send Troops", NamedTextColor.DARK_GRAY);
    private static final @NotNull Component UPGRADE_TITLE = Component.text("Upgrade Troops", NamedTextColor.DARK_GRAY);
    private static final @NotNull ItemStack SHORTCUTS_ITEM = ItemStack.builder(Material.PAPER)
            .displayName(Component.text("Shortcuts", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .lore(Stream.of(
                    "",
                    "<i:false><yellow>Quick Send Mobs <gold>(</gold> HOLD 1 <gold>)</gold>",
                    "<i:false><yellow>Quick Unlock Mob <gold>(</gold> RIGHT CLICK <gold>)</gold>" // todo: change to keybind tags
            ).map(MINI_MESSAGE::deserialize).toList())
            .build();
    private static final @NotNull ItemStack UPGRADE_ITEM = ItemStack.builder(Material.ENDER_PEARL)
            .displayName(Component.text("Upgrade Troops", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private final @NotNull GameUser gameUser;
    private final @NotNull MobStorage mobStorage;

    private @NotNull Mode mode = Mode.SEND;
    private @Nullable TroopUpgradeTab tab;

    public TroopSpawnerUI(@NotNull MobStorage mobStorage, @NotNull GameUser gameUser) {
        super(InventoryType.CHEST_4_ROW, SEND_TITLE);
        this.mobStorage = mobStorage;
        this.gameUser = gameUser;

        for (EnemyMob enemyMob : this.mobStorage.getEnemyMobs()) {
            ItemStack item = this.gameUser.getUpgradeHandler().getLevel(enemyMob)
                    .map(level -> level.createSendItem().withAmount(level.asInteger()))
                    .orElse(enemyMob.getBaseItem());
            this.setItemStack(enemyMob.getSlot(), item);
        }

        this.setItemStack(27, SHORTCUTS_ITEM);
        this.setItemStack(31, UPGRADE_ITEM);
        this.updateQueue();

        this.addInventoryCondition((p, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!p.equals(gameUser.getPlayer())) return;

            // check if slots are within the tab range
            if (this.tab != null && slot >= 9 && slot < 27) {
                this.tab.onClick(slot - 9);
                return;
            }

            // run the click handler
            this.onClick(slot);
        });

        EventListener<PlayerQueueUpdateEvent> queueUpdateListener = EventListener.builder(PlayerQueueUpdateEvent.class)
                .filter(event -> event.user().equals(this.gameUser))
                .expireWhen(event -> this.getViewers().isEmpty())
                .handler(event -> this.updateQueue())
                .build();
        this.gameUser.getPlayer().eventNode().addListener(queueUpdateListener);
    }

    private void onClick(int slot) {
        if (slot == 31 && this.mode == Mode.SEND) {
            this.enterUpgradeMode();
            return;
        }

        Optional<EnemyMob> optionalEnemyMob = this.mobStorage.getEnemyMobs().stream()
                .filter(mob -> mob.getSlot() == slot)
                .findFirst();
        if (optionalEnemyMob.isEmpty()) return; // clicked on an empty slot
        EnemyMob enemyMob = optionalEnemyMob.get();

        if (this.mode == Mode.SEND) this.attemptToSendMob(enemyMob);
        if (this.mode == Mode.UPGRADE) this.setTab(new TroopUpgradeTab(this, this.gameUser, enemyMob));
    }

    private void attemptToSendMob(@NotNull EnemyMob mob) {
        Result<QueueFailureReason> result = this.gameUser.getQueue().queue(mob);
        if (!(result instanceof Result.Failure<QueueFailureReason> failure)) return;
        TDPlayer player = this.gameUser.getPlayer();
        player.sendMessage(switch (failure.reason()) { // todo: better feedback
            case NOT_UNLOCKED -> "You have not unlocked this mob.";
            case CAN_NOT_AFFORD -> "You can not afford to send this mob.";
            case QUEUE_FULL -> "Your queue is full.";
        });
    }

    private void enterUpgradeMode() {
        this.mode = Mode.UPGRADE;
        this.setTitle(UPGRADE_TITLE);
        this.setItemStack(31, ItemStack.AIR);
        this.setItemStack(35, ItemStack.AIR);
    }

    private void setTab(@NotNull TroopUpgradeTab tab) {
        this.tab = tab;
        this.updateSubInventory();
    }

    void updateSubInventory() {
        if (this.tab == null) return;
        for (int i = 9; i < 27; i++) this.setItemStack(i, this.tab.getItemStack(i - 9));
    }

    private void updateQueue() {
        this.setItemStack(35, this.gameUser.getQueue().createItem());
    }

    enum Mode {
        SEND,
        UPGRADE
    }

}
