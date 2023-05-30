package pink.zak.minestom.towerdefence.game.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.BundleMeta;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.storage.MobStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MobMenuHandler {
    public static final Tag<Boolean> SEND_GUI_TAG = Tag.Boolean("send_gui");

    private static final @NotNull Component SEND_TITLE = Component.text("Send Troops", NamedTextColor.DARK_GRAY);
    private static final @NotNull Component UPGRADE_TITLE = Component.text("Upgrade Troops", NamedTextColor.DARK_GRAY);
    private static final @NotNull Map<EnemyMob, Component> MOB_UPGRADE_TITLES = new HashMap<>();
    private final @NotNull TowerDefenceModule plugin;
    private final @NotNull GameHandler gameHandler;
    private final @NotNull MobStorage mobStorage;
    private final @NotNull ItemStack chestItem;
    private final @NotNull ItemStack upgradeItem;

    public MobMenuHandler(@NotNull TowerDefenceModule plugin, @NotNull GameHandler gameHandler) {
        for (EnemyMob enemyMob : plugin.getMobStorage().getEnemyMobs())
            MOB_UPGRADE_TITLES.put(enemyMob, Component.text("Upgrade " + enemyMob.getCommonName()));

        this.plugin = plugin;
        this.gameHandler = gameHandler;
        @NotNull MobHandler mobHandler = gameHandler.getMobHandler();
        this.mobStorage = plugin.getMobStorage();
        this.chestItem = ItemStack.builder(Material.CHEST)
                .displayName(Component.text("Send Troops", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .build();

        this.upgradeItem = ItemStack.builder(Material.ENDER_PEARL)
                .displayName(Component.text("Upgrade Troops", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .build();

        this.startSendGuiListener();
        this.startUpgradeGuiListener();
        this.startMobUpgradeGuiListener();

        new QueueHandler(gameHandler, mobHandler, this);
    }

    public void onGameStart() {
        this.plugin.getEventNode().addListener(PlayerUseItemEvent.class, event -> {
            if (this.plugin.getGameState() == GameState.GAME && event.getItemStack().material() == Material.CHEST) {
                this.createGui(this.gameHandler, event.getPlayer());
            }
        });
    }

    private void createGui(@NotNull GameHandler gameHandler, @NotNull Player player) {
        GameUser gameUser = gameHandler.getGameUser(player);
        if (gameUser == null)
            return;

        Inventory inventory = new Inventory(InventoryType.CHEST_4_ROW, SEND_TITLE);
        inventory.setTag(SEND_GUI_TAG, true);

        Map<EnemyMob, Integer> levels = gameUser.getMobLevels();
        for (EnemyMob enemyMob : this.mobStorage.getEnemyMobs()) {
            Integer userLevel = levels.get(enemyMob);
            ItemStack itemStack = userLevel == null ? enemyMob.getUnownedItem() : enemyMob.getLevel(userLevel).createSendItem().withAmount(userLevel);
            inventory.setItemStack(enemyMob.getSlot(), itemStack);
        }
        inventory.setItemStack(31, this.upgradeItem);
        inventory.setItemStack(35, this.createQueueItem(gameUser));

        player.openInventory(inventory);
    }

    private @NotNull ItemStack createQueueItem(@NotNull GameUser gameUser) {
        List<ItemStack> bundleItems = new ArrayList<>();

        EnemyMob currentTrackedMob = null;
        int count = 0; // count of the current iterated mob
        for (QueuedEnemyMob queuedMob : gameUser.getQueue().getQueuedMobs()) {
            EnemyMob enemyMob = queuedMob.mob();
            if (currentTrackedMob != enemyMob) {
                if (currentTrackedMob != null) {
                    bundleItems.add(currentTrackedMob.getLevel(gameUser.getMobLevels().get(currentTrackedMob)).createSendItem().withAmount(count));
                    count = 0;
                }

                // we +1 at the end, so it gets to 150 :)
                // also this is an arbitrary limit, not Minecraft's.
                if (bundleItems.size() == 149) break;

                currentTrackedMob = enemyMob;
            }
            count++;
        }
        if (currentTrackedMob != null) {
            bundleItems.add(currentTrackedMob.getLevel(gameUser.getMobLevels().get(currentTrackedMob)).createSendItem().withAmount(count));
        }

        return ItemStack.builder(Material.BUNDLE)
                .displayName(Component.text("Current Queue", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false))
                .meta(BundleMeta.class, meta -> {
                    meta.items(bundleItems);
                })
                .build();
    }

    private void startSendGuiListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (event.getClickType() == ClickType.START_DOUBLE_CLICK || inventory == null || inventory.getTitle() != SEND_TITLE)
                return;
            event.setCancelled(true);
            int slot = event.getSlot();

            if (slot == 31) {
                this.convertToUpgradeGui(inventory);
                return;
            }

            Optional<EnemyMob> optionalClickedMob = this.mobStorage.getEnemyMobs()
                    .stream()
                    .filter(enemyMob -> enemyMob.getSlot() == slot)
                    .findFirst();
            optionalClickedMob.ifPresent(clickedMob -> {
                GameUser gameUser = this.gameHandler.getGameUser(event.getPlayer());

                int mobLevelInt = gameUser.getMobLevel(clickedMob);
                if (mobLevelInt == 0)
                    return;
                EnemyMobLevel mobLevel = clickedMob.getLevel(mobLevelInt);
                if (gameUser.getCoins() >= mobLevel.getSendCost() && gameUser.getQueue().canQueue(clickedMob)) {
                    gameUser.updateCoins(current -> current - mobLevel.getSendCost());

                    gameUser.getQueue().queue(new QueuedEnemyMob(clickedMob, mobLevel));
                    inventory.setItemStack(35, this.createQueueItem(gameUser));
                }
            });
        });
    }

    private void startUpgradeGuiListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (event.getClickType() == ClickType.START_DOUBLE_CLICK || inventory == null || inventory.getTitle() != UPGRADE_TITLE)
                return;
            event.setCancelled(true);
            int slot = event.getSlot();

            Optional<EnemyMob> optionalClickedMob = this.mobStorage.getEnemyMobs()
                    .stream()
                    .filter(enemyMob -> enemyMob.getSlot() == slot)
                    .findFirst();
            if (optionalClickedMob.isEmpty())
                return;

            EnemyMob clickedMob = optionalClickedMob.get();
            GameUser gameUser = this.gameHandler.getGameUser(event.getPlayer());
            int currentLevel = gameUser.getMobLevel(clickedMob);
            this.convertToMobUpgradeGui(gameUser, clickedMob, currentLevel);
        });
    }

    private void startMobUpgradeGuiListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (event.getClickType() == ClickType.START_DOUBLE_CLICK || inventory == null)
                return;

            Optional<EnemyMob> optionalMob = MOB_UPGRADE_TITLES.entrySet().stream()
                    .filter(entry -> entry.getValue() == inventory.getTitle())
                    .map(Map.Entry::getKey).findFirst();
            if (optionalMob.isEmpty())
                return;
            EnemyMob mob = optionalMob.get();

            event.setCancelled(true);
            int slot = event.getSlot();
            int clickedLevelInt = slot - 10;

            if (clickedLevelInt < 0 || clickedLevelInt > mob.getMaxLevel())
                return;

            GameUser gameUser = this.gameHandler.getGameUser(event.getPlayer());
            int currentLevel = gameUser.getMobLevel(mob);

            if (currentLevel >= clickedLevelInt)
                return;

            int incomeCost = 0;
            for (int i = currentLevel + 1; i <= clickedLevelInt; i++) {
                incomeCost += mob.getLevel(i).getUpgradeCost();
            }

            EnemyMobLevel clickedLevel = mob.getLevel(clickedLevelInt);

            if (gameUser.canAffordWithIncome(incomeCost)) {
                int finalIncomeCost = incomeCost;
                gameUser.updateIncomeRate(current -> current - finalIncomeCost); // reduce user income rate

                gameUser.getMobLevels().put(mob, clickedLevelInt);

                // re-render the gui with new level + subsequent changes to other level items
                this.renderMobUpgradeGui(inventory, gameUser, mob, clickedLevel);
            }
        });
    }

    private void convertToUpgradeGui(@NotNull Inventory inventory) {
        inventory.setTitle(UPGRADE_TITLE);
        inventory.setItemStack(31, ItemStack.AIR); // todo Make upgrade just a right click on the mob
        inventory.setItemStack(35, ItemStack.AIR);
    }

    private void convertToMobUpgradeGui(GameUser gameUser, EnemyMob clickedMob, int currentLevelInt) {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, MOB_UPGRADE_TITLES.get(clickedMob));
        EnemyMobLevel currentLevel = clickedMob.getLevel(currentLevelInt); // Nullable if level is 0

        this.renderMobUpgradeGui(inventory, gameUser, clickedMob, currentLevel);
        gameUser.getPlayer().openInventory(inventory);
    }

    private void renderMobUpgradeGui(@NotNull Inventory inventory, @NotNull GameUser gameUser, @NotNull EnemyMob enemyMob,
                                     @Nullable EnemyMobLevel currentLevel) {
        int currentLevelInt = currentLevel == null ? 0 : currentLevel.getLevel();

        inventory.setItemStack(0, currentLevel == null ? enemyMob.getUnownedItem() : currentLevel.createSendItem());

        int maxLevel = enemyMob.getMaxLevel();
        for (int i = 1; i <= maxLevel; i++) {
            EnemyMobLevel targetLevel = enemyMob.getLevel(i);
            boolean owned = i <= currentLevelInt;

            ItemStack itemStack;
            if (owned) {
                itemStack = targetLevel.createStatUpgradeItem(true, true); // canAfford doesn't matter
            } else {
                int cost = 0;
                for (int j = currentLevelInt + 1; j <= targetLevel.getLevel(); j++) {
                    cost += enemyMob.getLevel(j).getUpgradeCost();
                }

                boolean canAfford = gameUser.canAffordWithIncome(cost);
                if (currentLevel == null) {
                    itemStack = targetLevel.createStatUpgradeItem(false, canAfford);
                } else {
                    itemStack = targetLevel.createBuyUpgradeItem(canAfford, cost, currentLevel);
                }
            }

            inventory.setItemStack(10 + i, itemStack);
        }
    }

    public void updateSendMobGui(@NotNull GameUser user, @NotNull Inventory inventory) {
        inventory.setItemStack(35, this.createQueueItem(user));

    }

    public @NotNull ItemStack getChestItem() {
        return this.chestItem;
    }
}
