package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.modifier.SpeedModifier;

import java.util.concurrent.atomic.AtomicInteger;

public class FrozenStatusEffect implements StatusEffect<FrozenStatusEffect>, SpeedModifier {
    private final LivingTDEnemyMob mob;
    private final double speedModifier;
    private final int maxTicks;

    private final AtomicInteger ticks = new AtomicInteger();

    private boolean removed = false;

    public FrozenStatusEffect(LivingTDEnemyMob mob, double speedModifier, int maxTicks) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.maxTicks = maxTicks;
    }

    @Override
    public void tick(long time) {
        if (this.isRemoved()) return;
        if (this.remainingTicks() <= 0) {
            this.remove();
            return;
        }
        this.ticks.incrementAndGet();
    }

    @Override
    public void remove() {
        this.removed = true;

        this.mob.removeStatusEffect(this);
        this.mob.removeSpeedModifier(this);
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }

    @Override
    public int remainingTicks() {
        return this.maxTicks - this.ticks.get();
    }

    @Override
    public double getModifier() {
        return this.speedModifier;
    }

    @Override
    public @NotNull StatusEffectType type() {
        return StatusEffectType.FROZEN;
    }

    @Override
    public @NotNull Component getIcon() {
        return Component.text("❄", NamedTextColor.AQUA);
    }

    @Override
    public boolean isBetterThan(FrozenStatusEffect other) {
        return false;
    }
}
