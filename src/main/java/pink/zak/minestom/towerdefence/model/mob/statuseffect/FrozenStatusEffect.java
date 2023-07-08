package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.modifier.SpeedModifier;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class FrozenStatusEffect extends StatusEffect<FrozenStatusEffect> implements SpeedModifier, DamageSource {
    private final @NotNull LivingTDEnemyMob mob;
    private final @NotNull GameUser owningUser;

    private final double speedModifier;

    private final float damage;
    private final int damageTickRate;

    // TODO use this method
    public FrozenStatusEffect(@NotNull LivingTDEnemyMob mob, @NotNull GameUser owningUser,
                              double speedModifier, int maxTicks, float damage, int damageTickRate) {
        super(maxTicks);

        this.mob = mob;
        this.owningUser = owningUser;
        this.speedModifier = speedModifier;
        this.damage = damage;
        this.damageTickRate = damageTickRate;
    }

    public FrozenStatusEffect(@NotNull LivingTDEnemyMob mob, @NotNull GameUser owningUser, double speedModifier, int maxTicks) {
        this(mob, owningUser, speedModifier, maxTicks, 0, 0);
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (this.damageTickRate != 0 && this.getTicksAlive() % this.damageTickRate == 0) {
            this.mob.damage(this, this.damage);
        }

    }

    @Override
    public void remove() {
        super.remove();

        this.mob.removeStatusEffect(this);
        this.mob.removeSpeedModifier(this);
    }

    @Override
    public double getSpeedModifier() {
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
    public int compareTo(@NotNull FrozenStatusEffect o) {
        int speedComparison = Double.compare(this.speedModifier, o.speedModifier);
        if (speedComparison == -1 || speedComparison == 1) return speedComparison; // better or worse, not equal

        return Integer.compare(this.getRemainingTicks(), o.getRemainingTicks());
    }

    @Override
    public @NotNull GameUser getOwningUser() {
        return this.owningUser;
    }
}
