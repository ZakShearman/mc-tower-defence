package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.modifier.SpeedModifier;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.concurrent.atomic.AtomicInteger;

public class FrozenStatusEffect implements StatusEffect<FrozenStatusEffect>, SpeedModifier, DamageSource {
    private final @NotNull LivingTDEnemyMob mob;
    private final @NotNull GameUser owningUser;

    private final double speedModifier;
    private final int maxTicks;

    private final float damage;
    private final int damageTickRate;

    private final AtomicInteger ticks = new AtomicInteger();

    private boolean removed = false;

    // TODO use this method
    public FrozenStatusEffect(@NotNull LivingTDEnemyMob mob, @NotNull GameUser owningUser,
                              double speedModifier, int maxTicks, float damage, int damageTickRate) {
        this.mob = mob;
        this.owningUser = owningUser;
        this.speedModifier = speedModifier;
        this.maxTicks = maxTicks;
        this.damage = damage;
        this.damageTickRate = damageTickRate;
    }

    public FrozenStatusEffect(@NotNull LivingTDEnemyMob mob, @NotNull GameUser owningUser, double speedModifier, int maxTicks) {
        this(mob, owningUser, speedModifier, maxTicks, 0, 0);
    }

    @Override
    public void tick(long time) {
        if (this.isRemoved()) return;
        if (this.remainingTicks() <= 0) {
            this.remove();
            return;
        }

        if (this.damageTickRate != 0 && this.ticks.get() % this.damageTickRate == 0) {
            this.mob.damage(this, this.damage);
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
        if (this.speedModifier < other.speedModifier) // smaller than 1 = slower
            return true;
        else if (this.speedModifier == other.speedModifier)
            return this.remainingTicks() > other.remainingTicks();

        return false;
    }

    @Override
    public @NotNull GameUser getOwningUser() {
        return this.owningUser;
    }
}
