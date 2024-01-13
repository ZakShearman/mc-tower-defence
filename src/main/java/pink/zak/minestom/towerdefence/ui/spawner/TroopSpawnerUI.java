package pink.zak.minestom.towerdefence.ui.spawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.api.event.player.PlayerQueueUpdateEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerUpgradeMobEvent;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.queue.QueueFailureReason;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.ui.ConfirmationUI;
import pink.zak.minestom.towerdefence.upgrade.UpgradeHandler;
import pink.zak.minestom.towerdefence.utils.Result;

public final class TroopSpawnerUI extends Inventory {

    public static final @NotNull ItemStack HOTBAR_ITEM = ItemStack.builder(Material.CHEST)
            .displayName(Component.text("Send Troops", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private static final @NotNull Component SEND_TITLE = Component.text("Send Troops", NamedTextColor.DARK_GRAY);
    private static final @NotNull ItemStack SHORTCUTS_ITEM = ItemStack.builder(Material.PAPER)
            .displayName(Component.text("Shortcuts", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .lore(Stream.of(
                    "",
                    "<i:false><yellow>Quick Send Mobs <gold>(</gold> HOLD 1 <gold>)</gold>",
                    "<i:false><yellow>Send Mob <gold>(</gold> LEFT CLICK <gold>)</gold>",
                    "<i:false><yellow>Unlock/Upgrade Mob <gold>(</gold> RIGHT CLICK <gold>)</gold>" // todo: change to keybind tags
            ).map(MiniMessage.miniMessage()::deserialize).toList())
            .build();

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("troop-spawner-ui", EventFilter.PLAYER);

    private final @NotNull GameUser gameUser;
    private final @NotNull MobStorage mobStorage;

    public TroopSpawnerUI(@NotNull MobStorage mobStorage, @NotNull GameUser gameUser) {
        super(InventoryType.CHEST_4_ROW, SEND_TITLE);
        this.mobStorage = mobStorage;
        this.gameUser = gameUser;

        this.setItemStack(27, SHORTCUTS_ITEM);
        this.refresh();

        this.addInventoryCondition((player, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!player.equals(this.gameUser.getPlayer())) return;

            // run the click handler
            this.onClick(slot, clickType);
        });

        EventListener<PlayerQueueUpdateEvent> queueListener = EventListener.builder(PlayerQueueUpdateEvent.class)
                .filter(event -> event.user().equals(this.gameUser))
                .handler(event -> this.updateQueue())
                .build();
        this.eventNode.addListener(queueListener);

        EventListener<PlayerUpgradeMobEvent> upgradeListener = EventListener.builder(PlayerUpgradeMobEvent.class)
                .filter(event -> event.user().equals(this.gameUser))
                .handler(event -> this.refresh())
                .build();
        this.eventNode.addListener(upgradeListener);
    }

    private void refresh() {
        for (EnemyMob enemyMob : this.mobStorage.getEnemyMobs()) {
            Optional<EnemyMobLevel> optionalLevel = this.gameUser.getUpgradeHandler().getLevel(enemyMob);
            boolean unlocked = optionalLevel.isPresent();
            ItemStack item = optionalLevel
                    .map(level -> level.createSendItem().withAmount(level.asInteger()))
                    .orElse(enemyMob.getBaseItem());

            List<@NotNull Component> lore = new ArrayList<>(item.getLore());
            JoinConfiguration separator = JoinConfiguration.separator(Component.space());

            lore.add(Component.empty());

            if (unlocked) {
                lore.add(Component.join(
                        separator,
                        Component.text("LEFT CLICK", NamedTextColor.DARK_RED, TextDecoration.BOLD),
                        Component.text("to send", NamedTextColor.RED)
                ).decoration(TextDecoration.ITALIC, false));
            }

            String action;
            if (!unlocked) {
                int cost = this.gameUser.getUpgradeHandler().getCost(enemyMob, enemyMob.getLevel(1));
                action = "to unlock ($" + cost + ")";
            } else {
                action = "to upgrade";
            }

            lore.add(Component.join(
                    separator,
                    Component.text("RIGHT CLICK", NamedTextColor.DARK_RED, TextDecoration.BOLD),
                    Component.text(action, NamedTextColor.RED)
            ).decoration(TextDecoration.ITALIC, false));

            this.setItemStack(enemyMob.getSlot(), item.withLore(lore));
        }
        this.updateQueue();
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        this.refresh();
        boolean added = super.addViewer(player);
        this.updateListener();
        return added;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        boolean removed = super.removeViewer(player);
        this.updateListener();
        return removed;
    }

    private void updateListener() {
        EventNode<EntityEvent> eventNode = this.gameUser.getPlayer().eventNode();
        if (this.getViewers().isEmpty()) eventNode.removeChild(this.eventNode);
        else eventNode.addChild(this.eventNode);
    }

    private void onClick(int slot, @NotNull ClickType clickType) {
        Optional<EnemyMob> optionalEnemyMob = this.mobStorage.getEnemyMobs().stream()
                .filter(mob -> mob.getSlot() == slot)
                .findFirst();
        if (optionalEnemyMob.isEmpty()) return; // clicked on an empty slot
        EnemyMob enemyMob = optionalEnemyMob.get();

        if (clickType == ClickType.RIGHT_CLICK) {
            UpgradeHandler upgradeHandler = this.gameUser.getUpgradeHandler();
            TDPlayer player = this.gameUser.getPlayer();

            // if the mob is unlocked, open the upgrade UI. otherwise, attempt to unlock the mob
            if (upgradeHandler.has(enemyMob)) player.openInventory(new TroopUpgradeUI(this, this.gameUser, enemyMob));
            else this.attemptToUnlockMob(player, upgradeHandler, enemyMob);
        } else if (clickType == ClickType.LEFT_CLICK || clickType == ClickType.CHANGE_HELD) this.attemptToSendMob(enemyMob);
    }

    private void attemptToUnlockMob(@NotNull Player player, @NotNull UpgradeHandler upgradeHandler, @NotNull EnemyMob mob) {
        if (upgradeHandler.has(mob)) return;

        int cost = upgradeHandler.getCost(mob, mob.getLevel(1));
        if (this.gameUser.getCoins() < cost) {
            player.sendMessage(Component.text("You can not afford to unlock this mob.")); // todo: better feedback
            return;
        }

        player.openInventory(new ConfirmationUI(player, Component.text("This action will cost you $" + cost + ".", NamedTextColor.RED), result -> {
            if (result) upgradeHandler.unlock(mob);
            player.openInventory(this);
        }, false));
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

    private void updateQueue() {
        this.setItemStack(35, this.gameUser.getQueue().createItem());
    }

}
