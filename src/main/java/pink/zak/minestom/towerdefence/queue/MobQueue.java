package pink.zak.minestom.towerdefence.queue;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.BundleMeta;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.ui.spawner.TroopSpawnerUI;

public final class MobQueue {

    private static final int MAX_QUEUE_TIME = 45_000; // 45 seconds
    private static final @NotNull ItemStack BASE_QUEUE_ITEM = ItemStack.builder(Material.BUNDLE)
            .displayName(Component.text("Current Queue", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
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

        this.task = MinecraftServer.getSchedulerManager().buildTask(this::tick)
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
        if (System.currentTimeMillis() < this.nextSpawnTime) return;

        // send the mob
        this.mobHandler.spawnMob(this.currentlySpawning, this.user);
        this.currentlySpawning = null;

        // update the player's troop ui
        Inventory inventory = this.user.getPlayer().getOpenInventory();
        if (inventory instanceof TroopSpawnerUI ui) ui.updateQueue(this);
    }

    private int currentQueueTime() {
        return this.queue.stream()
                .mapToInt(mob -> mob.mob().getSendTime())
                .sum();
    }

    public boolean canQueue(@NotNull EnemyMob mob) {
        return this.currentQueueTime() + mob.getSendTime() <= MAX_QUEUE_TIME;
    }

    public @NotNull QueueResult queue(@NotNull EnemyMob mob) {
        return this.user.getUpgradeHandler().getLevel(mob).map(level -> {
            this.queue(new QueuedEnemyMob(mob, level));
            return QueueResult.success();
        }).orElse(QueueResult.failure(QueueResult.Reason.NOT_UNLOCKED));
    }

    public @NotNull QueueResult queue(@NotNull QueuedEnemyMob mob) {
        // check if user has access to mob at specified level
        if (!this.user.getUpgradeHandler().has(mob.mob(), mob.level())) return QueueResult.failure(QueueResult.Reason.NOT_UNLOCKED);

        // check if user can afford
        int cost = mob.level().getSendCost();
        if (this.user.getCoins() < cost) return QueueResult.failure(QueueResult.Reason.CAN_NOT_AFFORD);

        // check if possible to queue
        if (!this.canQueue(mob.mob())) return QueueResult.failure(QueueResult.Reason.QUEUE_FULL);

        // charge the user and queue
        this.user.updateCoins(current -> current - cost);
        this.queue.add(mob);

        return QueueResult.success();
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
        return BASE_QUEUE_ITEM.withMeta(BundleMeta.class, meta -> meta.items(items));
    }

}
