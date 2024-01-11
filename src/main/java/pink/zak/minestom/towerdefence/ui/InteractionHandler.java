package pink.zak.minestom.towerdefence.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.ui.tower.TowerManagementUI;
import pink.zak.minestom.towerdefence.ui.tower.TowerPlaceUI;

public final class InteractionHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);

    public InteractionHandler(@NotNull TowerDefenceModule module) {
        this.eventNode.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN) return;
            if (module.getGameState() != GameState.GAME) return;

            Player player = event.getPlayer();
            GameUser user = module.getGameHandler().getGameUser(player);
            if (user == null) throw new IllegalStateException("Player is not associated with a game user");

            Block block = event.getBlock();

            // check if clicked block is a tower. if so, open the tower management UI
            if (block.hasTag(PlacedTower.ID_TAG)) {
                int id = block.getTag(PlacedTower.ID_TAG);
                PlacedTower<?> tower = module.getGameHandler().getTowerManager().getTower(user.getTeam(), id);
                if (tower == null) throw new IllegalStateException("Player attempted to interact with a tower that does not exist");
                player.openInventory(new TowerManagementUI(tower, user, module.getGameHandler().getTowerManager()));
            }

            TowerMap map = module.getInstance().getTowerMap();
            if (block.registry().material() != map.getTowerBaseMaterial()) return;

            Point lastClickedTowerBlock = event.getBlockPosition().add(0.5, 0.5, 0.5);
            user.setLastClickedTowerBlock(lastClickedTowerBlock);

            // check if clicked block is on the player's side of the map
            if (!map.getArea(user.getTeam()).isWithin(lastClickedTowerBlock)) {
                player.sendMessage(Component.text("You can only place towers on your side of the map (" + user.getTeam().name().toLowerCase() + ").", NamedTextColor.RED));
                return;
            }

            // open tower place UI
            player.openInventory(new TowerPlaceUI(user, module.getTowerStorage(), module.getGameHandler().getTowerManager()));
        });
    }

    public void register(@NotNull EventNode<? super PlayerEvent> node) {
        node.addChild(this.eventNode);
    }

    public void unregister(@NotNull EventNode<? super PlayerEvent> node) {
        node.removeChild(this.eventNode);
    }

}