package pink.zak.minestom.towerdefence.model.tower.placed;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.targetting.Target;

public abstract class PlacedAttackingTower<T extends AttackingTowerLevel> extends PlacedTower<T> implements DamageSource {
    private final @NotNull MobHandler mobHandler;

    private int ticksSinceLastAttack = this.level.getFireDelay();

    // todo: give this tower a protected event node of some sort
    private final @NotNull EventListener<InstanceTickEvent> tickListener = EventListener.of(InstanceTickEvent.class, event -> {
        this.ticksSinceLastAttack++;
        if (this.ticksSinceLastAttack < this.level.getFireDelay()) return;
        if (this.attemptToFire()) this.ticksSinceLastAttack = 0;
    });

    protected PlacedAttackingTower(@NotNull MobHandler mobHandler, Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);
        this.mobHandler = mobHandler;

        this.instance.eventNode().addListener(this.tickListener);
    }

    /**
     * Attempts to fire the tower.
     *
     * @return true if the tower fired, false otherwise
     */
    protected abstract boolean attemptToFire();

    @Override
    public void destroy() {
        this.instance.eventNode().removeListener(this.tickListener);
        super.destroy();
    }

    public @NotNull Set<LivingTDEnemyMob> findPossibleTargets() {
        // get mobs spawned by the enemy team
        Set<LivingTDEnemyMob> mobs = this.mobHandler.getMobs(this.owner.getTeam().getOpposite());

        return mobs.stream()
                // filter out mobs that are already dead
                .filter(mob -> !mob.isDead())
                // filter out mobs that are predicted to be dead before the tower can fire
                .filter(mob -> !mob.isPredictedDead())
                // filter out mobs that are out of the tower's range
                .filter(mob -> {
                    // get the 4 points of the mob's bounding box
                    BoundingBox boundingBox = mob.getBoundingBox();
                    double minX = boundingBox.minX();
                    double maxX = boundingBox.maxX();
                    double minY = boundingBox.minY();
                    double maxY = boundingBox.maxY();
                    double minZ = boundingBox.minZ();
                    double maxZ = boundingBox.maxZ();

                    // get current mob position
                    Point position = mob.getPosition();

                    // convert the points to a set
                    Set<Point> points = Set.of(
                            position.add(minX, minY, minZ),
                            position.add(minX, minY, maxZ),
                            position.add(minX, maxY, minZ),
                            position.add(minX, maxY, maxZ),
                            position.add(maxX, minY, minZ),
                            position.add(maxX, minY, maxZ),
                            position.add(maxX, maxY, minZ),
                            position.add(maxX, maxY, maxZ)
                    );

                    // check if any of the points are within the tower's range
                    return points.stream().anyMatch(point -> point.distance(this.getBasePoint()) <= this.level.getRange());
                }).collect(Collectors.toUnmodifiableSet());
    }

    public @NotNull List<LivingTDEnemyMob> findPossibleTargets(@NotNull Target target) {
        return this.findPossibleTargets().stream()
                // sort mobs by target priority
                .sorted(target)
                .toList();
    }

    @Override
    public @NotNull GameUser getOwningUser() {
        return super.owner;
    }
}
