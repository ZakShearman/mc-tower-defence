package pink.zak.minestom.towerdefence.ui.hotbar;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.ui.spawner.TroopSpawnerUI;

public final class HotbarHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);

    public HotbarHandler(@NotNull TowerDefenceModule module, @NotNull GameHandler gameHandler) {
        this.eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (module.getGameState() != GameState.GAME) return;

            if (event.getItemStack().material().equals(Material.CHEST)) {
                GameUser gameUser = gameHandler.getGameUser(event.getPlayer());
                if (gameUser == null) return; // todo: should we throw here?
                event.getPlayer().openInventory(new TroopSpawnerUI(module.getMobStorage(), gameUser));
            }
        });
    }

    public void register(@NotNull EventNode<? super PlayerEvent> node) {
        node.addChild(this.eventNode);
    }

    public void unregister(@NotNull EventNode<? super PlayerEvent> node) {
        node.removeChild(this.eventNode);
    }

}
