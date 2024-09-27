package pink.zak.minestom.towerdefence.ui;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.ui.tower.TowerOutliner;
import pink.zak.minestom.towerdefence.ui.tower.TowerPlaceUI;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public final class InteractionHandler {

    private final @NotNull EventNode<PlayerEvent> eventNode = EventNode.type("hotbar-handler", EventFilter.PLAYER);
    private final @NotNull EventNode<? super PlayerEvent> parentNode;
    private final @NotNull GameHandler gameHandler;
    private final @NotNull TowerDefenceInstance instance;

    public InteractionHandler(@NotNull TowerDefenceModule module, @NotNull GameHandler gameHandler, @NotNull EventNode<? super PlayerEvent> node) {
        this.parentNode = node;
        this.gameHandler = gameHandler;
        this.instance = module.getInstance();
    }

    public void initialise() {
        this.parentNode.addChild(this.eventNode);

        TowerOutliner outliner = new TowerOutliner(this.instance);
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                GameUser user = this.gameHandler.getGameUser(player);
                if (user == null) throw new IllegalStateException("Player is not associated with a game user");

                ItemStack item = player.getItemInMainHand();
                if (!item.hasTag(TowerPlaceUI.UI_TAG)) continue;

                TowerType towerType = item.getTag(TowerPlaceUI.TOWER_TYPE);
                if (towerType == null) continue;

                Point targetBlockPos = player.getTargetBlockPosition(TowerPlaceUI.TOWER_PLACE_REACH);
                if (targetBlockPos == null) continue;

                if (player.getInstance().getBlock(targetBlockPos).hasTag(PlacedTower.ID_TAG)) continue;

                player.sendPackets(outliner.calculateOutline(user, targetBlockPos, towerType));
            }
        }).repeat(TaskSchedule.tick(2)).schedule();
    }

    public void shutdown() {
        this.parentNode.removeChild(this.eventNode);
    }

}
