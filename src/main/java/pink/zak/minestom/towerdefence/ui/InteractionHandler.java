package pink.zak.minestom.towerdefence.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public final class InteractionHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);

    public InteractionHandler(@NotNull TowerDefenceModule module) {
        this.eventNode.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN) return;
            if (module.getGameState() != GameState.GAME) return;

            Player player = event.getPlayer();
            GameUser user = module.getGameHandler().getGameUser(player);
            if (user == null) throw new IllegalStateException("Player is not associated with a game user");

            TowerMap map = module.getInstance().getTowerMap();
            if (event.getBlock().registry().material() != map.getTowerBaseMaterial()) return;

            Point lastClickedTowerBlock = event.getBlockPosition().add(0.5, 0.5, 0.5);
            user.setLastClickedTowerBlock(lastClickedTowerBlock);

            if (!map.getArea(user.getTeam()).isWithin(lastClickedTowerBlock)) {
                player.sendMessage(Component.text("You can only place towers on your side of the map (" + user.getTeam().name().toLowerCase() + ").", NamedTextColor.RED));
                return;
            }

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
