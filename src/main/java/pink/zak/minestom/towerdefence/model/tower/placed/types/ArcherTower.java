package pink.zak.minestom.towerdefence.model.tower.placed.types;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDespawnEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.prediction.DamagePrediction;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.ArcherTowerConfig;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.targetting.Target;
import pink.zak.minestom.towerdefence.utils.projectile.ArrowProjectile;
import pink.zak.minestom.towerdefence.utils.projectile.Projectile;

public final class ArcherTower extends PlacedAttackingTower<AttackingTowerLevel> {

    private final @NotNull Set<Point> firingPoints;

    public ArcherTower(@NotNull MobHandler mobHandler, Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(mobHandler, instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);

        ArcherTowerConfig config = (ArcherTowerConfig) this.tower;
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
        projectile.setInstance(this.instance, start);
    }

    @Override
    protected boolean attemptToFire() {
        List<LivingTDEnemyMob> targets = this.findPossibleTargets(Target.first());
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
