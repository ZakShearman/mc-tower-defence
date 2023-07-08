package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.ArcherTowerConfig;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.projectile.ArcGuidedProjectile;

import java.util.Set;
import java.util.stream.Collectors;

public class ArcherTower extends PlacedAttackingTower<AttackingTowerLevel> {
    private final @NotNull Set<Point> firingPoints;
    private final @NotNull EventNode<InstanceEvent> eventNode;

    private final @NotNull Entity fakeShooter = new LivingEntity(EntityType.ITEM_DISPLAY);

    public ArcherTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);

        this.eventNode = EventNode.type("archer-tower-%s".formatted(this.id), EventFilter.INSTANCE);
        instance.eventNode().addChild(this.eventNode);

        ArcherTowerConfig config = (ArcherTowerConfig) this.tower;
        this.firingPoints = config.getRelativeFiringPoints().stream()
                .map(relativePoint -> relativePoint.apply(this.getBasePoint()))
                .collect(Collectors.toUnmodifiableSet());

        this.fakeShooter.setAutoViewable(false);
        this.fakeShooter.setInstance(this.instance, this.basePoint.add(0, 4, 0));
    }

    // todo adjust arrow mid-flight
    // todo entity location prediction?
    // todo stick arrow in entity
    // todo predict that we will kill the entity and don't target that entity any more.
    private void betterFire() {
        LivingTDEnemyMob target = this.targets.get(0);
        Pos targetPos = target.getPosition().add(0, target.getEyeHeight() / 2, 0);

        Point fPoint = this.getFiringPoint(targetPos);
        double distanceToTarget = fPoint.distance(targetPos);

        double speed = (distanceToTarget / 3.5);

        // NOTE: We can't expand the bounding box of the arrow MORE, or it will collide with the tower.
        this.fakeShooter.lookAt(targetPos);
        ArcGuidedProjectile projectile = new ArcGuidedProjectile(this.fakeShooter, EntityType.ARROW, speed, 1.10);
        projectile.setBoundingBox(projectile.getBoundingBox().expand(0.2, 0.2, 0.2));
        projectile.shoot(this.instance, fPoint, targetPos);

        this.eventNode.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            Entity eventEntity = event.getEntity();
            if (eventEntity != projectile) return;

            Audiences.all().sendMessage(Component.text("arrow " + event.getEntity().getEntityId()
                    + " collided with entity id " + event.getTarget().getEntityId() + " " + event.getTarget().getEntityType()));

            Entity collidedEntity = event.getTarget();
            if (!(collidedEntity instanceof LivingEntity livingEntity)) {
                return;
            }
            if (!(collidedEntity instanceof LivingTDEnemyMob tdEnemyMob)) {
                return;
            }

//            livingEntity.setArrowCount(livingEntity.getArrowCount() + 1); // todo
            eventEntity.remove();
            tdEnemyMob.damage(this, this.getLevel().getDamage());
        });
    }

    @Override
    protected void fire() {
        this.betterFire();
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

    @Override
    public void destroy() {
        super.destroy();

        this.eventNode.getParent().removeChild(this.eventNode);
    }
}
