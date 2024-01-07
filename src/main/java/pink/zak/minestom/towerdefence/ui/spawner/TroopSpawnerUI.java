package pink.zak.minestom.towerdefence.ui.spawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.BundleMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.storage.MobStorage;

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
    private static final @NotNull ItemStack BASE_QUEUE_ITEM = ItemStack.builder(Material.BUNDLE)
            .displayName(Component.text("Current Queue", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
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

        Map<EnemyMob, Integer> levels = this.gameUser.getMobLevels();
        for (EnemyMob enemyMob : this.mobStorage.getEnemyMobs()) {
            Integer userLevel = levels.get(enemyMob);
            ItemStack itemStack = userLevel == null ? enemyMob.getBaseItem() : enemyMob.getLevel(userLevel).createSendItem().withAmount(userLevel);
            this.setItemStack(enemyMob.getSlot(), itemStack);
        }

        this.setItemStack(27, SHORTCUTS_ITEM);
        this.setItemStack(31, UPGRADE_ITEM);
        this.updateQueue(this.gameUser.getQueue().getQueuedMobs());

        addInventoryCondition((p, slot, clickType, result) -> {
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
        int levelInt = this.gameUser.getMobLevel(enemyMob);
        EnemyMobLevel level = enemyMob.getLevel(levelInt);

        if (this.mode == Mode.SEND && level != null) attemptToSendMob(enemyMob);
        if (this.mode == Mode.UPGRADE) setTab(new TroopUpgradeTab(this, this.gameUser, enemyMob));
    }

    private void attemptToSendMob(@NotNull EnemyMob mob) {
        GameUser user = this.gameUser;

        EnemyMobLevel level = mob.getLevel(user.getMobLevel(mob));
        if (level == null) return; // the player attempted to send a mob that they don't own

        // check if the player can afford to send the mob
        if (user.getCoins() < level.getSendCost()) return;
        if (!user.getQueue().canQueue(mob)) return;

        // charge player for sending the mob
        user.updateCoins(current -> current - level.getSendCost());

        // add the mob to the queue
        user.getQueue().queue(new QueuedEnemyMob(mob, level));
        this.updateQueue(user.getQueue().getQueuedMobs());
    }

    private void enterUpgradeMode() {
        this.mode = Mode.UPGRADE;
        this.setTitle(UPGRADE_TITLE);
        this.setItemStack(31, ItemStack.AIR);
        this.setItemStack(35, ItemStack.AIR);
    }

    private void setTab(@NotNull TroopUpgradeTab tab) {
        this.tab = tab;
        updateSubInventory();
    }

    void updateSubInventory() {
        if (this.tab == null) return;
        for (int i = 9; i < 27; i++) setItemStack(i, this.tab.getItemStack(i - 9));
    }

    private @NotNull ItemStack createQueueItem(@NotNull Queue<QueuedEnemyMob> queue) {
        List<ItemStack> items = new ArrayList<>();

        EnemyMob currentlyTrackedMob = null;
        int count = 0; // count of the current iterated mob
        for (QueuedEnemyMob queuedMob : queue) {
            EnemyMob mob = queuedMob.mob();
            if (currentlyTrackedMob != mob) {
                if (currentlyTrackedMob != null) {
                    items.add(currentlyTrackedMob.getLevel(this.gameUser.getMobLevels().get(currentlyTrackedMob)).createPreviewItem().withAmount(count));
                    count = 0;
                }

                // we +1 at the end, so it gets to 150 :)
                // also this is an arbitrary limit, not Minecraft's.
                if (items.size() == 149) break;

                currentlyTrackedMob = mob;
            }
            count++;
        }

        if (currentlyTrackedMob != null) items.add(currentlyTrackedMob.getLevel(this.gameUser.getMobLevels().get(currentlyTrackedMob)).createPreviewItem().withAmount(count));

        return BASE_QUEUE_ITEM.withMeta(BundleMeta.class, meta -> meta.items(items));
    }

    public void updateQueue(@NotNull Queue<QueuedEnemyMob> queue) {
        setItemStack(35, createQueueItem(queue));
    }

    enum Mode {
        SEND,
        UPGRADE
    }

}
