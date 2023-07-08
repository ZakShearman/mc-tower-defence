package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class StatusEffect<T extends StatusEffect> implements Tickable, Comparable<T> {
    private final AtomicInteger ticksAlive = new AtomicInteger();

    private final int ticksToLive;

    private boolean removed = false;

    protected StatusEffect(int ticksToLive) {
        this.ticksToLive = ticksToLive;
    }

    @Override
    public void tick(long time) {
        if (this.isRemoved()) return;
        if (this.getRemainingTicks() <= 0) {
            this.remove();
            return;
        }

        this.ticksAlive.incrementAndGet();
    }

    public void remove() {
        this.removed = true;
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public int getTicksAlive() {
        return this.ticksAlive.get();
    }

    public int getRemainingTicks() {
        return this.ticksToLive - this.ticksAlive.get();
    }

    public abstract @NotNull StatusEffectType type();

    public abstract @NotNull Component getIcon();
}
