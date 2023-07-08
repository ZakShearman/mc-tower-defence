package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.color.Color;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.modifier.SpeedModifier;

public final class StunnedStatusEffect extends StatusEffect<StunnedStatusEffect> implements SpeedModifier {
    private final @NotNull LivingTDEnemyMob mob;

    public StunnedStatusEffect(int ticksToLive, @NotNull LivingTDEnemyMob mob) {
        super(ticksToLive);
        this.mob = mob;

        this.mob.applyStatusEffect(this);
        this.mob.applySpeedModifier(this);
    }

    @Override
    public void remove() {
        this.mob.removeSpeedModifier(this);
        this.mob.removeStatusEffect(this);
    }

    @Override
    public @NotNull StatusEffectType type() {
        return StatusEffectType.STUNNED;
    }

    @Override
    public @NotNull Component getIcon() {
        return Component.text("☄", TextColor.color(new Color(150, 75, 0)));
    }

    @Override
    public double getSpeedModifier() {
        return 0;
    }

    @Override
    public int compareTo(@NotNull StunnedStatusEffect o) {
        return Integer.compare(super.getRemainingTicks(), o.getRemainingTicks());
    }
}
