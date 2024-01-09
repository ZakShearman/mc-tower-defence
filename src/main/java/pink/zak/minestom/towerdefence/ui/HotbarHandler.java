package pink.zak.minestom.towerdefence.ui;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.ui.spawner.TroopSpawnerUI;

public final class HotbarHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);

    public HotbarHandler(@NotNull TowerDefenceModule module, @NotNull GameHandler gameHandler) {
        this.eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (module.getGameState() != GameState.GAME) return;

            Player p = event.getPlayer();
            if (!(p instanceof TDPlayer player)) throw new IllegalStateException("Player is not a TDPlayer");
            GameUser user = gameHandler.getGameUser(player);
            if (user == null) throw new IllegalStateException("Player is not associated with a game user");

            ItemStack item = event.getItemStack();
            if (item.isSimilar(TroopSpawnerUI.HOTBAR_ITEM)) {
                player.openInventory(new TroopSpawnerUI(module.getMobStorage(), user));
            } else if (item.isSimilar(UserSettingsUI.HOTBAR_ITEM)) {
                player.openInventory(new UserSettingsUI(player));
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
