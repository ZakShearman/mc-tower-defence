package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class BurningDamageEffect extends StatusEffect<BurningDamageEffect> implements DamageSource {
    private final @NotNull LivingTDEnemyMob mob;
    private final @NotNull GameUser owningUser;

    private final float damage;
    private final int damageTickRate;

    public BurningDamageEffect(@NotNull LivingTDEnemyMob mob, @NotNull GameUser owningUser,
                               int maxTicks, float damage, int damageTickRate) {
        super(maxTicks);

        this.mob = mob;
        this.owningUser = owningUser;
        this.damage = damage;
        this.damageTickRate = damageTickRate;

        this.mob.setOnFire(true);
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
        this.mob.setOnFire(false);
    }

    @Override
    public @NotNull StatusEffectType type() {
        return StatusEffectType.BURNING;
    }

    @Override
    public @NotNull Component getIcon() {
        return Component.text("\uD83D\uDD25", NamedTextColor.AQUA);
    }

    @Override
    public int compareTo(@NotNull BurningDamageEffect o) {
        int damageComparison = Double.compare(this.damage, o.damage);
        if (damageComparison == -1 || damageComparison == 1) return damageComparison; // better or worse, not equal

        return Integer.compare(this.getRemainingTicks(), o.getRemainingTicks());
    }

    @Override
    public @NotNull GameUser getOwner() {
        return this.owningUser;
    }

    public float getDamage() {
        return this.damage;
    }
}
