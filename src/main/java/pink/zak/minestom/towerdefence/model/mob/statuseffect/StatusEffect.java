package pink.zak.minestom.towerdefence.model.mob.statuseffect;

import net.kyori.adventure.text.Component;
import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

public interface StatusEffect<T extends StatusEffect> extends Tickable {

    void remove();

    boolean isRemoved();

    int remainingTicks();

    @NotNull StatusEffectType type();

    @NotNull Component getIcon();

    /**
     * @return true if this is better than the passed in effect
     */
    boolean isBetterThan(T other);

}
