package pink.zak.minestom.towerdefence.game.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerManaChangeEvent;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.storage.MobStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MobMenuHandler {
    private static final Component SEND_TITLE = Component.text("Send Troops", NamedTextColor.DARK_GRAY);
    private static final Component UPGRADE_TITLE = Component.text("Upgrade Troops", NamedTextColor.DARK_GRAY);
    private static final Map<EnemyMob, Component> MOB_UPGRADE_TITLES = Maps.newHashMap();
    private final TowerDefencePlugin plugin;
    private final GameHandler gameHandler;
    private final MobHandler mobHandler;
    private final MobStorage mobStorage;
    private final ItemStack chestItem;
    private final ItemStack upgradeItem;

    public MobMenuHandler(TowerDefencePlugin plugin, GameHandler gameHandler) {
        for (EnemyMob enemyMob : plugin.getMobStorage().getEnemyMobs().values())
            MOB_UPGRADE_TITLES.put(enemyMob, Component.text("Upgrade " + enemyMob.commonName()));

        this.plugin = plugin;
        this.gameHandler = gameHandler;
        this.mobHandler = gameHandler.getMobHandler();
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
        this.startQueueProcessor();
    }

    public void startGame() {
        for (Player player : this.gameHandler.getUsers().keySet())
            player.getInventory().setItemStack(4, this.chestItem);

        this.plugin.getEventNode().addListener(PlayerUseItemEvent.class, event -> {
            if (this.plugin.getGameState() == GameState.IN_PROGRESS && event.getItemStack().getMaterial() == Material.CHEST) {
                this.createGui(this.gameHandler, event.getPlayer());
            }
        });
    }

    private void createGui(GameHandler gameHandler, Player player) {
        GameUser gameUser = gameHandler.getGameUser(player);
        if (gameUser == null)
            return;

        Inventory inventory = new Inventory(InventoryType.CHEST_4_ROW, SEND_TITLE);
        Map<EnemyMob, Integer> levels = gameUser.getMobLevels();
        for (EnemyMob enemyMob : this.mobStorage.getEnemyMobs().values()) {
            Integer userLevel = levels.get(enemyMob);
            ItemStack itemStack = userLevel == null ? enemyMob.unownedItem() : enemyMob.level(userLevel).sendItem().withAmount(userLevel);
            inventory.setItemStack(enemyMob.slot(), itemStack);
        }
        inventory.setItemStack(31, this.upgradeItem);
        inventory.setItemStack(35, this.createQueueItem(gameUser));

        player.openInventory(inventory);
    }

    private ItemStack createQueueItem(GameUser gameUser) {
        List<Component> loreLines = Lists.newArrayList(Component.empty());

        EnemyMob currentTrackedMob = null;
        int count = 0;
        int iterations = 0;
        for (QueuedEnemyMob queuedMob : gameUser.getQueuedMobs()) {
            EnemyMob enemyMob = queuedMob.mob();
            if (currentTrackedMob != enemyMob) {
                if (currentTrackedMob != null) {
                    loreLines.add(Component.text(count + "x " + currentTrackedMob.commonName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    count = 0;
                }

                if (loreLines.size() >= 6) {
                    loreLines.add(Component.text("And " + (gameUser.getQueuedMobs().size() - iterations) + " more...", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                    break;
                }
                currentTrackedMob = enemyMob;
            }
            count++;
            iterations++;
        }
        if (currentTrackedMob != null && loreLines.size() < 6)
            loreLines.add(Component.text(count + "x " + currentTrackedMob.commonName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        return ItemStack.builder(Material.PAPER)
            .displayName(Component.text("Current Queue (" + iterations + "/" + gameUser.getMaxQueueSize() + ")", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .lore(loreLines)
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

            Optional<EnemyMob> optionalClickedMob = this.mobStorage.getEnemyMobs().values()
                .stream()
                .filter(enemyMob -> enemyMob.slot() == slot)
                .findFirst();
            optionalClickedMob.ifPresent(clickedMob -> {
                GameUser gameUser = this.gameHandler.getGameUser(event.getPlayer());

                int mobLevelInt = gameUser.getMobLevel(clickedMob);
                if (mobLevelInt == 0)
                    return;
                EnemyMobLevel mobLevel = clickedMob.level(mobLevelInt);
                if (gameUser.getCoins() >= mobLevel.cost()) {
                    gameUser.updateAndGetCoins(current -> current - mobLevel.cost());

                    this.sendTroops(gameUser, clickedMob, mobLevel);
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

            Optional<EnemyMob> optionalClickedMob = this.mobStorage.getEnemyMobs().values()
                .stream()
                .filter(enemyMob -> enemyMob.slot() == slot)
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

            if (clickedLevelInt < 0 || clickedLevelInt > mob.maxLevel())
                return;

            GameUser gameUser = this.gameHandler.getGameUser(event.getPlayer());
            int currentLevel = gameUser.getMobLevel(mob);

            if (currentLevel >= clickedLevelInt)
                return;

            int manaCost = 0;
            for (int i = currentLevel + 1; i <= clickedLevelInt; i++) {
                manaCost += mob.level(i).manaCost();
            }

            EnemyMobLevel clickedLevel = mob.level(clickedLevelInt);

            if (gameUser.getMana() >= manaCost) {
                int finalManaCost = manaCost;
                gameUser.updateAndGetMana(current -> current - finalManaCost);
                gameUser.getMobLevels().put(mob, clickedLevelInt);

                // update inventory items
                inventory.setItemStack(0, clickedLevel.sendItem());
                for (int i = currentLevel + 1; i <= clickedLevelInt; i++)
                    inventory.setItemStack(10 + i, clickedLevel.ownedUpgradeItem());
            }
        });
    }

    private void convertToUpgradeGui(Inventory inventory) {
        inventory.setTitle(UPGRADE_TITLE);
        inventory.setItemStack(31, ItemStack.AIR); // todo Make upgrade just a right click on the mob
        inventory.setItemStack(35, ItemStack.AIR);
    }

    private void convertToMobUpgradeGui(GameUser gameUser, EnemyMob clickedMob, int currentLevel) {
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, MOB_UPGRADE_TITLES.get(clickedMob));

        inventory.setItemStack(0, currentLevel <= 0 ? clickedMob.unownedItem() : clickedMob.level(currentLevel).sendItem());

        int maxLevel = clickedMob.maxLevel();
        for (int i = 1; i <= maxLevel; i++) {
            EnemyMobLevel enemyMobLevel = clickedMob.level(i);
            boolean purchased = i <= currentLevel;
            boolean canAfford = gameUser.getMana() >= enemyMobLevel.manaCost();
            ItemStack itemStack = purchased ? enemyMobLevel.ownedUpgradeItem() : canAfford ? enemyMobLevel.buyUpgradeItem() : enemyMobLevel.cantAffordUpgradeItem();
            inventory.setItemStack(10 + i, itemStack);
        }

        gameUser.getPlayer().openInventory(inventory);
    }

    private void sendTroops(GameUser gameUser, EnemyMob enemyMob, EnemyMobLevel enemyMobLevel) {
        gameUser.getQueuedMobs().add(new QueuedEnemyMob(enemyMob, enemyMobLevel));
    }

    private void startQueueProcessor() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
                for (GameUser gameUser : this.gameHandler.getUsers().values()) {
                    QueuedEnemyMob enemyMob = gameUser.getQueuedMobs().poll();
                    if (enemyMob != null) {
                        for (int i = 0; i < 5; i++) {
                            this.mobHandler.spawnMob(enemyMob, gameUser);
                            Inventory inventory = gameUser.getPlayer().getOpenInventory();
                            if (inventory != null && inventory.getTitle() == SEND_TITLE)
                                inventory.setItemStack(35, this.createQueueItem(gameUser));
                        }
                    }
                }
            })
            .repeat(50, TimeUnit.MILLISECOND)
            .schedule();
    }
}
