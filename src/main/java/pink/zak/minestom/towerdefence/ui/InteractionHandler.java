package pink.zak.minestom.towerdefence.ui;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.ui.tower.TowerManagementUI;
import pink.zak.minestom.towerdefence.ui.tower.TowerOutliner;
import pink.zak.minestom.towerdefence.ui.tower.TowerPlaceUI;

public final class InteractionHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);
    private final @NotNull EventNode<? super PlayerEvent> parentNode;

    public InteractionHandler(@NotNull TowerDefenceModule module, @NotNull EventNode<? super PlayerEvent> node) {
        this.parentNode = node;
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
        });

        TowerOutliner outliner = new TowerOutliner(module.getInstance());
        this.eventNode.addListener(PlayerTickEvent.class, event -> {
            Player player = event.getPlayer();
            GameUser user = module.getGameHandler().getGameUser(player);
            if (user == null) throw new IllegalStateException("Player is not associated with a game user");

            ItemStack item = player.getItemInMainHand();
            if (!item.hasTag(TowerPlaceUI.UI_TAG)) return;

            TowerType towerType = item.getTag(TowerPlaceUI.TOWER_TYPE);
            if (towerType == null) return;

            Point targetBlockPos = player.getTargetBlockPosition(TowerPlaceUI.TOWER_PLACE_DISTANCE);
            if (targetBlockPos == null) return;

            player.sendPackets(outliner.calculateOutline(user, targetBlockPos, towerType));
        });
    }

    public void initialise() {
        this.parentNode.addChild(this.eventNode);
    }

    public void shutdown() {
        this.parentNode.removeChild(this.eventNode);
    }

}
