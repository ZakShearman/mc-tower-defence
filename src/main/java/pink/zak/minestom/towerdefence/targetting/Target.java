package pink.zak.minestom.towerdefence.targetting;

import java.util.Comparator;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;

public sealed interface Target extends Comparator<LivingTDEnemyMob> permits Target.Closest, Target.First, Target.Furthest, Target.Strongest, Target.Weakest {

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
