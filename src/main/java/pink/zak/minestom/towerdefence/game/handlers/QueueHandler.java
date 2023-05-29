package pink.zak.minestom.towerdefence.game.handlers;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class QueueHandler {
    private final @NotNull GameHandler gameHandler;
    private final @NotNull MobHandler mobHandler;
    private final @NotNull MobMenuHandler mobMenuHandler;

    public QueueHandler(@NotNull GameHandler gameHandler, @NotNull MobHandler mobHandler, @NotNull MobMenuHandler mobMenuHandler) {
        this.gameHandler = gameHandler;
        this.mobHandler = mobHandler;
        this.mobMenuHandler = mobMenuHandler;

        MinecraftServer.getSchedulerManager().buildTask(this::tick)
                .repeat(TaskSchedule.nextTick())
                .schedule();
    }

    private void tick() {
        for (GameUser user : this.gameHandler.getUsers().values()) {
            this.tick(user, user.getQueue());
        }
    }

    private void tick(@NotNull GameUser user, @NotNull GameUser.SendQueue queue) {
        if (queue.getQueuedMobs().isEmpty()) return;

        int timeToCurrentSend = queue.tickTime();
        if (timeToCurrentSend != 0) return;

        QueuedEnemyMob queuedMob = queue.poll();
        if (queuedMob == null) return;

        this.mobHandler.spawnMob(queuedMob, user);
        user.updateIncomeRate(current -> current + queuedMob.level().getSendIncomeIncrease());

        Inventory inventory = user.getPlayer().getOpenInventory();
        if (inventory != null && inventory.hasTag(MobMenuHandler.SEND_GUI_TAG)) {
            this.mobMenuHandler.updateSendMobGui(user, inventory);
        }
    }
}
