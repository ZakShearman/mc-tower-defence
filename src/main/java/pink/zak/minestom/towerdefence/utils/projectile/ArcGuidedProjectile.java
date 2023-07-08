package pink.zak.minestom.towerdefence.utils.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="https://github.com/iam4722202468">iam4722202468</a>
 */
public class ArcGuidedProjectile extends ProjectileParent {
    final double xStep;
    final double zStep;

    private double currentStep = 0;
    private ParameticEquation movement;
    private int percent = 21;

    private double cx;
    private double cz;
    private double stepX;
    private double stepZ;
    private double stepSize;

    protected final double speed;
    protected final double power;

    private Vec nextPos;

    public ArcGuidedProjectile(Entity shooter, EntityType type, double speed, double power) {
        super(shooter, type);

        this.setNoGravity(true);

        this.speed = Math.max(0.1, speed);
        this.power = Math.max(0.1, power);

        float yaw = shooter.getPosition().yaw();
        xStep = Math.sin(Math.toRadians(yaw));
        zStep = Math.cos(Math.toRadians(yaw));
    }

    public CompletableFuture<Entity> shoot(Instance instance, Point from, Point to) {
        final EntityShootEvent shootEvent = new EntityShootEvent(this.shooter, this, to, power, 0);
        EventDispatcher.call(shootEvent);
        if (shootEvent.isCancelled()) {
            remove();
            return CompletableFuture.failedFuture(new IllegalStateException("EntityShootEvent was cancelled"));
        }

        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();

        if (Math.abs(dx) < 0.001) {
            dx = 0.001;
        }
        if (Math.abs(dz) < 0.001) {
            dz = 0.001;
        }
        if (Math.abs(dy) < 0.001) {
            dy = 0.001;
        }

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        Point max = new Vec(dx / 2 + from.x(), dy / 2 + from.y() + 1.0 / power * (distance / 8), dz / 2 + from.z());

        // Calculate x movement
        this.movement = ParameticEquation.calculateMovement(from.x(), from.y(), max.x(), max.y(), to.x(), to.y());

        double stepSize = speed / distance * 0.5;

        double stepX = dx * stepSize;
        double stepZ = dz * stepSize;

        this.cx = from.x();
        this.cz = from.z();

        this.stepX = stepX;
        this.stepZ = stepZ;

        this.stepSize = stepSize;

        this.nextPos = new Vec(cx, from.y(), cz);
        this.currentStep = stepSize;

        Vec diff = new Vec(stepX, this.movement.solve(cx + stepX) - from.y(), stepZ);

        float yaw = -(float) Math.toDegrees(Math.atan2(diff.x(), diff.z()));
        float pitch = -(float) Math.toDegrees(Math.atan2(diff.y(), Math.sqrt(diff.x() * diff.x() + diff.z() * diff.z())));

        return this.setInstance(instance, new Pos(from.x(), from.y(), from.z(), yaw, pitch))
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                } else {
                    this.setVelocity(diff.div(40));
                }
            }).thenApply(entity -> this);
    }

    @Override
    public void refreshPosition(@NotNull Pos newPosition) { }

    public CompletableFuture<Pos> updatePosition(long time) {
        if (movement == null) return CompletableFuture.completedFuture(null);
        Vec lastPos = nextPos;

        double yx = movement.solve(cx);
        if (Double.isNaN(yx)) yx = lastPos.y();

        nextPos = new Vec(cx, yx, cz);

        cx += stepX;
        cz += stepZ;

        this.currentStep += stepSize;

        if (currentStep > 10) {
            remove();
            return CompletableFuture.completedFuture(null);
        }

        this.position = Pos.fromPoint(nextPos);
        return CompletableFuture.completedFuture(this.position);
    }


    @Override
    protected void onHitTick(Pos currentPos, Vec stepped) {
        this.movement = null;
    }

    @Override
    protected void onTick(Pos currentPos, Vec stepped) {
        this.setVelocity(new Vec(stepped.x(), stepped.y(), stepped.z()).mul(20));

        if ((currentStep * 100) > percent) {
            float yaw = (float) Math.toDegrees(Math.atan2(stepped.x(), stepped.z()));
            float pitch = (float) Math.toDegrees(Math.atan2(stepped.y(), Math.sqrt(stepped.x() * stepped.x() + stepped.z() * stepped.z())));
            var correctPos = new Pos(currentPos.x(), currentPos.y(), currentPos.z(), yaw, pitch);
            teleport(correctPos);
            percent += 20;
        }
    }
}