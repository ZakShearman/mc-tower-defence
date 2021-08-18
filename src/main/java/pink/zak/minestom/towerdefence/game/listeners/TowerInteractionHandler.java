package pink.zak.minestom.towerdefence.game.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

import java.util.concurrent.atomic.AtomicReference;

public class TowerInteractionHandler {
    private final TowerDefencePlugin plugin;
    private final TowerStorage towerStorage;
    private final Inventory towerPlaceGui;

    private final GameHandler gameHandler;
    private final TowerMap towerMap;

    private final AtomicReference<Short> towerIdCounter = new AtomicReference<>(Short.MIN_VALUE);

    public TowerInteractionHandler(TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.towerStorage = plugin.getTowerStorage();
        this.towerPlaceGui = this.createTowerPlaceGui();

        this.gameHandler = plugin.getGameHandler();
        this.towerMap = plugin.getMapStorage().getMap();

        plugin.getEventNode()
            .addListener(PlayerBlockInteractEvent.class, event -> {
                Player player = event.getPlayer();
                if (event.getHand() != Player.Hand.MAIN || plugin.getGameState() != GameState.IN_PROGRESS)
                    return;
                GameUser gameUser = this.gameHandler.getGameUser(player);
                if (gameUser == null || event.getBlock().registry().material() != this.towerMap.getTowerPlaceMaterial())
                    return;
                if (!this.towerMap.getArea(gameUser.getTeam()).isWithin(player)) {
                    player.sendMessage(Component.text("You can only place towers on your side of the map (" + gameUser.getTeam().name().toLowerCase() + ").", NamedTextColor.RED));
                    return;
                }
                gameUser.setLastClickedTowerBlock(event.getBlockPosition());
                player.openInventory(this.towerPlaceGui);
                // todo check if they clicked on a tower
            });
    }

    private Inventory createTowerPlaceGui() {
        TextComponent title = Component.text("Place a Tower", NamedTextColor.DARK_GRAY);
        Inventory inventory = new Inventory(InventoryType.CHEST_3_ROW, title);

        for (Tower tower : this.towerStorage.getTowers().values()) {
            inventory.setItemStack(tower.getType().getGuiSlot(), tower.getMenuItem());
        }

        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory checkInventory = event.getInventory();
            if (event.getClickType() == ClickType.START_DOUBLE_CLICK || checkInventory == null || checkInventory.getTitle() != title)
                return;
            event.setCancelled(true);
            TowerType clickedTower = TowerType.valueOf(event.getSlot());
            if (clickedTower == null)
                return;
            Player player = event.getPlayer();
            player.closeInventory();
            this.requestTowerBuy(player, clickedTower);
        });

        return inventory;
    }

    private void requestTowerBuy(Player player, TowerType towerType) {
        GameUser gameUser = this.gameHandler.getGameUser(player);
        Tower tower = this.towerStorage.getTower(towerType);
        TowerLevel level = tower.getLevel(1);

        int coins = gameUser.getCoins().get();
        if (level.cost() > coins) {
            player.sendMessage(Component.text("You do not have enough money to buy this tower", NamedTextColor.RED));
            return;
        }
        Instance instance = player.getInstance();
        Point basePoint = gameUser.getLastClickedTowerBlock();
        Material placeMaterial = this.towerMap.getTowerPlaceMaterial();
        if (!tower.isSpaceClear(player.getInstance(), basePoint, placeMaterial)) {
            Audiences.all().sendMessage(Component.text("Cannot place a tower as the area is not clear", NamedTextColor.RED));
            // todo handle - show an outline or something
            return;
        }
        new PlacedTower(instance, tower, this.towerMap.getTowerPlaceMaterial(), this.generateTowerId(), basePoint, Direction.NORTH, 1);
    }

    private short generateTowerId() {
        return this.towerIdCounter.getAndUpdate(aShort -> aShort++);
    }
}
