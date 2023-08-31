package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.prediction.Prediction;
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

    // TODO it regularly misses on short distances when firing completely straight from the tower as the mob moves a lot in relation.
    // todo adjust arrow mid-flight
    // todo entity location prediction?
    // todo stick arrow in entity
    private void betterFire() {
        LivingTDEnemyMob target = this.targets.get(0);
        float damage = this.level.getDamage();
        Prediction prediction = target.addDamagePrediction(damage);

        Pos targetPos = target.getPosition().add(0, target.getEyeHeight() / 2, 0);

        Point fPoint = this.getFiringPoint(targetPos);
        double distanceToTarget = fPoint.distance(targetPos);

        double speed = 0.75 + (distanceToTarget / 3.55);
        double expansionSpeed = 10 + (distanceToTarget / 4.5);

        // NOTE: We can't expand the bounding box of the arrow MORE, or it will collide with the tower.
        this.fakeShooter.lookAt(targetPos);
        ArcGuidedProjectile projectile = new ArcGuidedProjectile(EntityType.ARROW, this.fakeShooter, speed, 1.25);
        projectile.setBoundingBox(projectile.getBoundingBox().expand(0.25, 0.25, 0.25));
        projectile.shoot(this.instance, fPoint, targetPos);

        double increment = 0.0125 * expansionSpeed;
        Audiences.all().sendMessage(Component.text("Increment: %s Distance: %s Speed: %s".formatted(increment, distanceToTarget, speed)));

        Task task = projectile.scheduler().buildTask(() -> {
            Audiences.all().sendMessage(Component.text("Running arrow task for %s".formatted(projectile.getEntityId())));
            projectile.setBoundingBox(projectile.getBoundingBox().expand(increment, increment, increment));
        }).schedule();

        projectile.scheduler().buildTask(task::cancel)
                .delay(20L * MinecraftServer.TICK_PER_SECOND, TimeUnit.SERVER_TICK)
                .schedule();

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
            prediction.destroy();
            eventEntity.remove();
            tdEnemyMob.damage(this, this.getLevel().getDamage());
        }).addListener(ProjectileCollideWithBlockEvent.class, event -> {
            Entity eventEntity = event.getEntity();
            if (eventEntity != projectile) return;

            prediction.destroy();
            eventEntity.remove();
            Audiences.all().sendMessage(Component.text("Arrow %s missed".formatted(eventEntity.getEntityId())));
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
