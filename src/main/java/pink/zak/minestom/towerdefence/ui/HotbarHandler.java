package pink.zak.minestom.towerdefence.ui;

import java.util.Set;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.ui.spawner.TroopSpawnerUI;

public final class HotbarHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);
    private final @NotNull EventNode<? super PlayerEvent> parentNode;

    public HotbarHandler(@NotNull TowerDefenceModule module, @NotNull EventNode<? super PlayerEvent> node) {
        this.parentNode = node;
        this.eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (module.getGameState() != GameState.GAME) return;

            Player p = event.getPlayer();
            if (!(p instanceof TDPlayer player)) throw new IllegalStateException("Player is not a TDPlayer");
            GameUser user = module.getGameHandler().getGameUser(player);
            if (user == null) throw new IllegalStateException("Player is not associated with a game user");

            ItemStack item = event.getItemStack();
            if (item.isSimilar(TroopSpawnerUI.HOTBAR_ITEM)) {
                player.openInventory(new TroopSpawnerUI(module.getMobStorage(), user));
            } else if (item.isSimilar(UserSettingsUI.HOTBAR_ITEM)) {
                player.openInventory(new UserSettingsUI(player));
            }
        });
    }

    public void initialise(@NotNull Set<Player> players) {
        this.parentNode.addChild(this.eventNode);
        for (Player player : players) {
            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setItemStack(4, TroopSpawnerUI.HOTBAR_ITEM);
            inventory.setItemStack(8, UserSettingsUI.HOTBAR_ITEM);
        }
    }

    public void shutdown() {
        this.parentNode.removeChild(this.eventNode);
    }

}
