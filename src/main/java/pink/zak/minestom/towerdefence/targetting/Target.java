package pink.zak.minestom.towerdefence.targetting;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.utils.StringUtils;

public sealed interface Target extends Comparator<LivingTDEnemyMob> {

    enum Type {
        CLOSEST(tower -> closest(tower.getBasePoint())),
        FURTHEST(tower -> furthest(tower.getBasePoint())),
        FIRST(ignored -> first()),
        WEAKEST(ignored -> weakest()),
        STRONGEST(ignored -> strongest());

        private final @NotNull Function<PlacedTower<?>, Target> constructor;

        Type(@NotNull Function<PlacedTower<?>, Target> constructor) {
            this.constructor = constructor;
        }

        public @NotNull Target create(@NotNull PlacedTower<?> tower) {
            return this.constructor.apply(tower);
        }

        public @NotNull Type next() {
            return this.relative(1);
        }

        public @NotNull Type previous() {
            return this.relative(-1);
        }

        private @NotNull Type relative(int offset) {
            return Type.values()[(this.ordinal()+offset)%Type.values().length];
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

    }

    /**
     * Target the closest mob to the given point.
     *
     * @param point The point to compare to.
     * @return targetter
     */
    static @NotNull Target closest(@NotNull Point point) {
        return new Closest(point);
    }

    /**
     * Target the furthest mob to the given point.
     *
     * @param point The point to compare to.
     * @return targetter
     */
    static @NotNull Target furthest(@NotNull Point point) {
        return new Furthest(point);
    }

    /**
     * Target the mob that has moved the furthest.
     *
     * @return targetter
     */
    static @NotNull Target first() {
        return new First();
    }

    /**
     * Target the mob with the least health.
     *
     * @return targetter
     */
    static @NotNull Target weakest() {
        return new Weakest();
    }

    /**
     * Target the mob with the most health.
     *
     * @return targetter
     */
    static @NotNull Target strongest() {
        return new Strongest();
    }

    final class Closest implements Target {
        private final @NotNull Point point;

        Closest(@NotNull Point point) {
            this.point = point;
        }

        @Override
        public int compare(LivingTDEnemyMob a, LivingTDEnemyMob b) {
            return Double.compare(
                b.getPosition().distanceSquared(point),
                a.getPosition().distanceSquared(point)
            );
        }
    }

    final class Furthest implements Target {
        private final @NotNull Point point;

        Furthest(@NotNull Point point) {
            this.point = point;
        }

        @Override
        public int compare(LivingTDEnemyMob a, LivingTDEnemyMob b) {
            return Double.compare(
                a.getPosition().distanceSquared(point),
                b.getPosition().distanceSquared(point)
            );
        }
    }

    final class First implements Target {
        First() {
        }

        @Override
        public int compare(LivingTDEnemyMob a, LivingTDEnemyMob b) {
            return Double.compare(
                b.getTotalDistanceMoved(),
                a.getTotalDistanceMoved()
            );
        }
    }
    
    final class Weakest implements Target {
        Weakest() {
        }

        @Override
        public int compare(LivingTDEnemyMob a, LivingTDEnemyMob b) {
            return Double.compare(
                a.getHealth(),
                b.getHealth()
            );
        }
    }

    final class Strongest implements Target {
        Strongest() {
        }

        @Override
        public int compare(LivingTDEnemyMob a, LivingTDEnemyMob b) {
            return Double.compare(
                b.getHealth(),
                a.getHealth()
            );
        }
    }

}
