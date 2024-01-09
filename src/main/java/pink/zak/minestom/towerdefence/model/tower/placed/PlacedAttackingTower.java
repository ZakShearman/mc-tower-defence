package pink.zak.minestom.towerdefence.model.tower.placed;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.targetting.Target;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public abstract class PlacedAttackingTower<T extends AttackingTowerLevel> extends PlacedTower<T> implements DamageSource {
    private final @NotNull MobHandler mobHandler;

    private int ticksSinceLastAttack = this.level.getFireDelay();

    // todo: give this tower a protected event node of some sort
    private final @NotNull EventListener<InstanceTickEvent> tickListener = EventListener.of(InstanceTickEvent.class, event -> {
        this.ticksSinceLastAttack++;
        if (this.ticksSinceLastAttack < this.level.getFireDelay()) return;
        if (this.attemptToFire()) this.ticksSinceLastAttack = 0;
    });

    protected PlacedAttackingTower(@NotNull MobHandler mobHandler, TowerDefenceInstance instance, AttackingTower tower, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, id, owner, basePoint, facing, level);
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

    public @NotNull List<LivingTDEnemyMob> findPossibleTargets() {
        // get mobs attacking the tower's team
        Set<LivingTDEnemyMob> mobs = this.mobHandler.getMobs(this.owner.getTeam());

        List<LivingTDEnemyMob> targets = new ArrayList<>(mobs.size());
        for (LivingTDEnemyMob mob : mobs) {
            // filter out mobs that are already dead
            if (mob.isDead()) continue;

            // filter out mobs that are predicted to be dead before the tower can fire
            if (mob.isPredictedDead()) continue;

            // filter out mobs that are CLEARLY out of the tower's range
            // the value `2` here is to account for the biggest bounding box theoretically possible
            if (mob.getPosition().distanceSquared(this.getBasePoint()) > Math.pow(this.level.getRange() + 2, 2)) continue;

            // filter out mobs that are out of the tower's range
            // get the 2 points of the mob's bounding box
            BoundingBox boundingBox = mob.getTDEntityType().registry().boundingBox();
            double minX = boundingBox.minX();
            double maxX = boundingBox.maxX();
            double minZ = boundingBox.minZ();
            double maxZ = boundingBox.maxZ();

            // get current mob position
            Point basePoint = this.getBasePoint();
            double y = basePoint.y();
            Point position = mob.getPosition().withY(y);

            // find side lengths
            double xLength = maxX - minX;
            double adjustedRadius = this.level.getRange() + xLength * 2;
            if (position.distanceSquared(basePoint) <= Math.pow(adjustedRadius, 2)) {
                targets.add(mob);
                continue;
            }

            // draw a line between the centres of tower and the mob
            Vec vector = Vec.fromPoint(basePoint.sub(position)).normalize();
            Point intersection = basePoint.add(vector.mul(this.level.getRange()));

            // check if intersection is within the mob's bounding box
            if (intersection.x() < minX || intersection.x() > maxX) continue;
            if (intersection.z() < minZ || intersection.z() > maxZ) continue;

            targets.add(mob);
        }

        return targets;
    }

    public @NotNull List<LivingTDEnemyMob> findPossibleTargets(@NotNull Target target) {
        List<LivingTDEnemyMob> targets = findPossibleTargets();
        targets.sort(target);
        return targets;
    }

}
