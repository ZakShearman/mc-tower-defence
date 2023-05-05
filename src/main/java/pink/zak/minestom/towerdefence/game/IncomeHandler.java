package pink.zak.minestom.towerdefence.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.concurrent.atomic.AtomicInteger;

public class IncomeHandler {
    private final @NotNull GameHandler gameHandler;

    private final @NotNull AtomicInteger tick = new AtomicInteger(0);

    public IncomeHandler(@NotNull GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        MinecraftServer.getSchedulerManager().buildTask(this::tick)
                .repeat(TaskSchedule.nextTick())
                .schedule();
    }

    private void tick() {
        int tick = this.tick.getAndIncrement();
        this.updateIncome(tick);

        if (this.tick.incrementAndGet() % 200 == 0) {
            this.grantIncome();
        }
    }

    private void updateIncome(int tick) {
        int ticksFromLastIncome = tick % 200;
        float progress = ticksFromLastIncome / 200f;

        for (GameUser user : this.gameHandler.getUsers().values()) {
            Player player = user.getPlayer();

            player.setLevel(user.getIncomeRate());
            player.setExp(progress);
        }
    }

    private void grantIncome() {
        for (GameUser user : this.gameHandler.getUsers().values()) {
            int income = user.getIncomeRate();
            user.updateCoins(current -> current + income);
        }
    }
}
