package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.ArcherTowerConfig;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Set;
import java.util.stream.Collectors;

public class ArcherTower extends PlacedAttackingTower<AttackingTowerLevel> {
    private static final int ARROW_BLOCKS_PER_SECOND = 60;

    private final @NotNull Set<Point> firingPoints;

    public ArcherTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);

        ArcherTowerConfig config = (ArcherTowerConfig) this.tower;
        this.firingPoints = config.getRelativeFiringPoints().stream()
                .map(relativePoint -> relativePoint.apply(this.getBasePoint()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void fire() {
        LivingTDEnemyMob target = this.targets.get(0);
        Pos targetPoint = target.getPosition().add(0, target.getEyeHeight(), 0);
        Point fPoint = this.getFiringPoint(this.targets.get(0).getPosition());

        double scheduleDistance = fPoint.distance(targetPoint) - 1;
        int tickLifespan = (int) Math.ceil(scheduleDistance / (double) ARROW_BLOCKS_PER_SECOND * MinecraftServer.TICK_MS);

        Entity entity = new Entity(EntityType.ARROW);
        entity.setVelocity(targetPoint.sub(fPoint).asVec().normalize().mul(0.5 * ARROW_BLOCKS_PER_SECOND));
        entity.lookAt(targetPoint);
        entity.setGravity(0, 0);
        entity.setNoGravity(true);

        entity.setInstance(this.instance, fPoint);

        MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    if (entity.isRemoved()) return;

                    entity.remove();
                    target.damage(this, this.getLevel().getDamage());
                })
                .delay(tickLifespan, TimeUnit.SERVER_TICK)
                .schedule();
    }

    @Override
    public int getMaxTargets() {
        return 1;
    }

    private @NotNull Point getFiringPoint(@NotNull Point target) {
        return this.firingPoints.stream()
                .min((point1, point2) -> {
                    double distance1 = point1.distance(target);
                    double distance2 = point2.distance(target);
                    return Double.compare(distance1, distance2);
                })
                .orElseThrow(() -> new IllegalStateException("No firing points found"));
    }
}
