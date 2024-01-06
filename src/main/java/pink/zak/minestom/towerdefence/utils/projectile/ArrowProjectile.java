package pink.zak.minestom.towerdefence.utils.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.prediction.DamagePrediction;

public class ArrowProjectile extends Projectile {
    private static final double ARROW_SPEED = 40;

    private final @NotNull DamageSource damageSource;
    private final @NotNull LivingTDEnemyMob target;
    private final @NotNull DamagePrediction prediction;

    public ArrowProjectile(@NotNull DamageSource damageSource, @NotNull LivingTDEnemyMob target, @NotNull DamagePrediction prediction) {
        super(EntityType.ARROW);

        this.target = target;
        this.prediction = prediction;
        this.damageSource = damageSource;

        this.setVelocity(this.calculateVelocity());
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        // if the target is dead, remove the arrow
        // in theory it shouldn't reach here anyway but just in case, removing the arrow is fine.
        if (target.isDead()) {
            this.remove();
            return;
        }

        // set velocity to match target's new position
        this.setVelocity(this.calculateVelocity());

        // check if the arrow is within the target's bounding box
        if (target.getBoundingBox().expand(0.5, 0.5, 0.5).intersectEntity(target.getPosition(), this)) {
            this.remove();
            target.damage(this.damageSource, this.prediction.damage());
        }
    }

    @Override
    public void remove() {
        super.remove();

        this.prediction.complete();
    }

    private Vec calculateVelocity() {
        Point targetPosition = target.getPosition().add(0, target.getEyeHeight() / 2, 0);
        return Vec.fromPoint(targetPosition.sub(this.getPosition()))
                .normalize()
                .mul(ARROW_SPEED);
    }
}
