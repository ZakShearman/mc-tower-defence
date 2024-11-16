package pink.zak.minestom.towerdefence.queue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.api.event.player.PlayerQueueUpdateEvent;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.Result;
import pink.zak.minestom.towerdefence.utils.TDEnvUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public final class MobQueue {
    private static final @NotNull ItemStack BASE_QUEUE_ITEM = ItemStack.builder(Material.BUNDLE)
            .set(ItemComponent.CUSTOM_NAME, Component.text("Current Queue", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private final @NotNull Queue<QueuedEnemyMob> queue = new ConcurrentLinkedQueue<>();

    private final @NotNull MobHandler mobHandler;
    private final @NotNull GameUser user;

    private final @NotNull Task task;
    private long nextSpawnTime = 0;
    private @Nullable QueuedEnemyMob currentlySpawning;

    public MobQueue(@NotNull MobHandler mobHandler, @NotNull GameUser user) {
        this.mobHandler = mobHandler;
        this.user = user;

        this.task = MinecraftServer.getSchedulerManager().buildTask(() -> {
                    for (int i = 0; i < TDEnvUtils.QUEUE_MOB_TICKS_PER_TICK; i++) {
                        this.tick();
                    }
                })
                .repeat(TaskSchedule.nextTick())
                .schedule();
    }

    public void unregister() {
        this.task.cancel();
    }

    private void tick() {
        // check if we have a mob to send
        if (this.currentlySpawning == null) {
            this.currentlySpawning = this.queue.poll();
            if (this.currentlySpawning == null) return;
            this.nextSpawnTime = System.currentTimeMillis() + this.currentlySpawning.mob().getSendTime();
        }

        // check if we can send the mob
//        if (System.currentTimeMillis() < this.nextSpawnTime) return;

        // send the mob
        this.mobHandler.spawnMob(this.currentlySpawning, this.user);
        this.currentlySpawning = null;

        // broadcast the queue event
        this.callUpdateEvent();
    }

    private long currentQueueTime() {
        return this.queue.stream()
                .mapToLong(mob -> mob.mob().getSendTime())
                .sum();
    }

    public boolean canQueue(@NotNull EnemyMob mob, long count) {
        return this.currentQueueTime() + (mob.getSendTime() * count) <= TDEnvUtils.QUEUE_MAX_TIME;
    }

    public @NotNull Result<QueueFailureReason> queue(@NotNull EnemyMob mob, long count) {
        return this.user.getUpgradeHandler().getLevel(mob)
                .map(level -> this.queue(new QueuedEnemyMob(mob, level), count))
                .orElse(Result.failure(QueueFailureReason.NOT_UNLOCKED));
    }

    public @NotNull Result<QueueFailureReason> queue(@NotNull QueuedEnemyMob mob, long count) {
        // check if user has access to mob at specified level
        if (!this.user.getUpgradeHandler().has(mob.mob(), mob.level()))
            return Result.failure(QueueFailureReason.NOT_UNLOCKED);

        // check if user can afford
        long cost = mob.level().getSendCost() * count;
        if (this.user.getCoins() < cost) return Result.failure(QueueFailureReason.CAN_NOT_AFFORD);

        // check if possible to queue
        if (!this.canQueue(mob.mob(), count)) return Result.failure(QueueFailureReason.QUEUE_FULL);

        // charge the user and queue
        this.user.updateCoins(current -> current - cost);
        if (count == 1) {
            this.queue.add(mob);
        } else {
            this.queue.addAll(Collections.nCopies((int) count, mob));
        }

        // broadcast the queue event
        this.callUpdateEvent();

        return Result.success();
    }

    private void callUpdateEvent() {
        MinecraftServer.getGlobalEventHandler().call(new PlayerQueueUpdateEvent(this.user, this));
    }

    public @NotNull ItemStack createItem() {
        Map<EnemyMob, Long> count = this.queue.stream()
                .collect(Collectors.groupingBy(
                        QueuedEnemyMob::mob,
                        Collectors.counting()
                ));
        List<ItemStack> items = count.entrySet().stream()
                .map(entry -> {
                    EnemyMob mob = entry.getKey();
                    long amount = entry.getValue();
                    return mob.getBaseItem().withAmount((int) amount);
                }).toList();

        return BASE_QUEUE_ITEM.with(ItemComponent.BUNDLE_CONTENTS, items);
    }
}
