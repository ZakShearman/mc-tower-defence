package pink.zak.minestom.towerdefence.model.prediction;

import com.google.common.util.concurrent.AtomicDouble;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// NOTE: These preds will unnecessarily stack up. We need to remove them when the projectile hits.
public final class DamagePredictionHandler {
    private static final SchedulerManager SCHEDULER_MANAGER = MinecraftServer.getSchedulerManager();

    private final @NotNull Set<Task> cleanupTasks = ConcurrentHashMap.newKeySet();
    private final @NotNull Supplier<Float> healthSupplier;

    private final AtomicDouble counter = new AtomicDouble(0);

    public DamagePredictionHandler(@NotNull Supplier<Float> healthSupplier) {
        this.healthSupplier = healthSupplier;
    }

    public Prediction addPrediction(float damage) {
        this.counter.addAndGet(damage);
        return new Prediction(damage, null, this);
    }

    public Prediction addPrediction(long duration, float damage) {
        this.counter.addAndGet(damage);
        Task task = SCHEDULER_MANAGER.buildTask(() -> this.counter.addAndGet(-damage))
                .delay(duration, TimeUnit.MILLISECOND).schedule();
        this.cleanupTasks.add(task);

        return new Prediction(damage, task, this);
    }

    void removePrediction(@NotNull Prediction prediction) {
        Task task = prediction.task();
        if (task != null) {
            task.cancel();
            this.cleanupTasks.remove(task);
        }

        this.counter.addAndGet(-prediction.damage());
    }

    public boolean isPredictedDead() {
        return this.counter.get() >= this.healthSupplier.get();
    }

    public void destroy() {
        this.cleanupTasks.forEach(Task::cancel);
        this.cleanupTasks.clear();
    }
}
