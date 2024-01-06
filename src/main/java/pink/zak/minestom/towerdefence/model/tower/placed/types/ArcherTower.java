package pink.zak.minestom.towerdefence.model.tower.placed.types;

import java.util.Set;
import java.util.function.Function;
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
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.prediction.Prediction;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.ArcherTowerConfig;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.projectile.Projectile;

public final class ArcherTower extends PlacedAttackingTower<AttackingTowerLevel> {
    private static final double ARROW_SPEED = 25;

    private final @NotNull Set<Point> firingPoints;
    private final @NotNull EventNode<InstanceEvent> eventNode;

    public ArcherTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);

        this.eventNode = EventNode.type("archer-tower-%s".formatted(this.id), EventFilter.INSTANCE);
        instance.eventNode().addChild(this.eventNode);

        ArcherTowerConfig config = (ArcherTowerConfig) this.tower;
        this.firingPoints = config.getRelativeFiringPoints().stream()
                .map(relativePoint -> relativePoint.apply(this.getBasePoint()))
                .collect(Collectors.toUnmodifiableSet());
    }

    // todo: stick the arrow into the target once it hits
    private void fire(@NotNull LivingTDEnemyMob target) {
        Point start = this.getFiringPoint(target.getPosition());

        // register damage prediction
        float damage = this.level.getDamage();
        Prediction prediction = target.addDamagePrediction(damage);

        Entity projectile = new Projectile(EntityType.ARROW);
        projectile.setInstance(this.instance, start);

        Supplier<Vec> velocity = () -> Vec.fromPoint(target.getPosition().sub(projectile.getPosition()))
                .normalize()
                .mul(ARROW_SPEED);
        projectile.setVelocity(velocity.get());

        EventNode<EntityEvent> projectileNode = projectile.eventNode();
        EventListener<EntityTickEvent> collisionListener = EventListener.builder(EntityTickEvent.class)
                .filter(event -> event.getEntity().equals(projectile))
                .expireWhen(event -> event.getEntity().isRemoved())
                .handler(event -> {
                    // set velocity to match target's new position
                    projectile.setVelocity(velocity.get());

                    // check if the arrow is within the target's bounding box
                    if (target.getBoundingBox().expand(0.5, 0.5, 0.5).intersectEntity(target.getPosition(), projectile)) {
                        projectile.remove();
                        target.damage(this, damage);
                    }
                }).build();
        projectileNode.addListener(collisionListener);

        EventListener<EntityDespawnEvent> removalListener = EventListener.builder(EntityDespawnEvent.class)
                .filter(event -> event.getEntity().equals(projectile))
                .expireCount(1)
                // destroy the prediction when the arrow is removed
                .handler(event -> prediction.destroy())
                .build();
        projectileNode.addListener(removalListener);
    }

    @Override
    protected void fire() {
        this.fire(this.targets.getFirst());
    }

    @Override
    public int getMaxTargets() {
        return 1;
    }

    private @NotNull Point getFiringPoint(@NotNull Point target) {
        return this.firingPoints.stream().min((point1, point2) -> {
            double distance1 = point1.distanceSquared(target);
            double distance2 = point2.distanceSquared(target);
            return Double.compare(distance1, distance2);
        }).orElseThrow(() -> new IllegalStateException("No firing points found"));
    }

    @Override
    public void destroy() {
        super.destroy();

        this.eventNode.getParent().removeChild(this.eventNode);
    }
}
