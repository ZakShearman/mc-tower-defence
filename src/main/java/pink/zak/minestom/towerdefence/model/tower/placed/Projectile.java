package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

public class Projectile extends Entity {

    public Projectile(@NotNull EntityType entityType) {
        super(entityType);

        // remove physics
        this.hasPhysics = false;
        this.setNoGravity(true);

        // remove arrows after 15 seconds
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (!this.isRemoved()) this.remove();
        }, TaskSchedule.seconds(15), TaskSchedule.stop(), ExecutionType.ASYNC);
    }

    @Override
    public void setVelocity(@NotNull Vec velocity) {
        super.setVelocity(velocity);

        // the minecraft client interprets the yaw and pitch of projectiles differently to other entities.
        // instead, some trigonometry is used to calculate the yaw and pitch from the velocity.
        float yaw = (float) (Math.toDegrees(Math.atan2(velocity.x(), velocity.z())));
        double horizontalDistance = Math.sqrt(Math.pow(velocity.x(), 2) + Math.pow(velocity.z(), 2));
        float pitch = (float) (Math.toDegrees(Math.atan2(velocity.y(), horizontalDistance)));
        this.setView(yaw, pitch);
    }

}
