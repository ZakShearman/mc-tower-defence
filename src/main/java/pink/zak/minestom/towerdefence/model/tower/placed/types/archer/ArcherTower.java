package pink.zak.minestom.towerdefence.model.tower.placed.types.archer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.prediction.DamagePrediction;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.ArcherTowerConfig;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.targetting.Target;

public final class ArcherTower extends PlacedAttackingTower<AttackingTowerLevel> {

    private final @NotNull Set<Point> firingPoints;

    public ArcherTower(@NotNull GameHandler gameHandler, AttackingTower tower, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(gameHandler, tower, id, owner, basePoint, facing, level);

        ArcherTowerConfig config = (ArcherTowerConfig) this.configuration;
        this.firingPoints = config.getRelativeFiringPoints().stream()
                .map(relativePoint -> relativePoint.apply(this.getBasePoint()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private void fireAt(@NotNull LivingTDEnemyMob target) {
        // register damage prediction
        float damage = this.level.getDamage();
        DamagePrediction prediction = target.applyDamagePrediction(damage);

        ArrowProjectile projectile = new ArrowProjectile(this, target, prediction);

        Point start = this.getFiringPoint(target.getPosition());
        projectile.setInstance(this.gameHandler.getInstance(), start);
    }

    @Override
    protected boolean attemptToFire() {
        List<LivingTDEnemyMob> targets = this.findPossibleTargets();
        if (targets.isEmpty()) return false;
        this.fireAt(targets.getFirst());
        return true;
    }

    private @NotNull Point getFiringPoint(@NotNull Point target) {
        return this.firingPoints.stream().min((point1, point2) -> {
            double distance1 = point1.distanceSquared(target);
            double distance2 = point2.distanceSquared(target);
            return Double.compare(distance1, distance2);
        }).orElseThrow(() -> new IllegalStateException("No firing points found"));
    }
}
