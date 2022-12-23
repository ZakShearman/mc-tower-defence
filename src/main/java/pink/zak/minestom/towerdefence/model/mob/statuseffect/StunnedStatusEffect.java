package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.color.Color;
import org.jetbrains.annotations.NotNull;

public class StunnedStatusEffect implements StatusEffect<StunnedStatusEffect> {

    @Override
    public void remove() {

    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public int remainingTicks() {
        return 0;
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
    public boolean isBetterThan(StunnedStatusEffect other) {
        return false;
    }

    @Override
    public void tick(long time) {

    }
}
