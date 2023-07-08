package pink.zak.minestom.towerdefence.utils.projectile;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.ShapeImpl;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.ProjectileMeta;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityRotationPacket;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;

import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="https://github.com/iam4722202468">iam4722202468</a>
 */
public abstract class ProjectileParent extends Entity {
    protected final Entity shooter;

    public ProjectileParent(Entity shooter, EntityType type) {
        super(type);
        this.shooter = shooter;

        setup();
    }

    private void setup() {
        this.hasPhysics = false;
        if (getEntityMeta() instanceof ProjectileMeta) {
            ((ProjectileMeta) getEntityMeta()).setShooter(this.shooter);
        }

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (!this.isRemoved()) {
                this.remove();
            }
        }, TaskSchedule.seconds(15), TaskSchedule.stop(), ExecutionType.ASYNC);
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        var res = super.setInstance(instance, spawnPosition);

        Pos insideBlock = checkInsideBlock(instance);
        // Check if we're inside of a block
        if (insideBlock != null) {
            var e = new ProjectileCollideWithBlockEvent(this, Pos.fromPoint(spawnPosition), instance.getBlock(spawnPosition));
            MinecraftServer.getGlobalEventHandler().call(e);
        }

        return res;
    }

    private Pos checkInsideBlock(@NotNull Instance instance) {
        var iterator = this.getBoundingBox().getBlocks(this.getPosition());

        while (iterator.hasNext()) {
            var block = iterator.next();
            Block b = instance.getBlock(block);
            var hit = b.registry().collisionShape().intersectBox(this.getPosition().sub(block), this.getBoundingBox());
            if (hit) return Pos.fromPoint(block);
        }

        return null;
    }

    @Override
    public void refreshPosition(@NotNull Pos newPosition) {
    }

    @Override
    public @NotNull Pos getPosition() {
        return super.getPosition().withView(-this.position.yaw(), this.position.pitch());
    }

    protected abstract void onHitTick(Pos currentPos, Vec stepped);

    protected abstract void onTick(Pos currentPos, Vec stepped);

    protected abstract CompletableFuture<Pos> updatePosition(long time);

    @Override
    public void tick(long time) {
        if (isRemoved()) return;

        final Pos posBefore = getPosition();
        updatePosition(time).whenCompleteAsync((posNow, t) -> {
            if (posBefore.equals(posNow)) return;
            if (!instance.isChunkLoaded(posNow)) {
                this.remove();
                return;
            }

            Vec diff = Vec.fromPoint(posNow.sub(posBefore));
            PhysicsResult result = CollisionUtils.handlePhysics(
                    instance, this.getChunk(),
                    this.getBoundingBox(),
                    posBefore, diff,
                    null, true
            );

            onTick(posNow, diff);

            PhysicsResult collided = CollisionUtils.checkEntityCollisions(
                    this.instance, this.getBoundingBox(), posBefore, diff, 3,
                    entity -> entity != this && entity != this.shooter && entity instanceof LivingTDEnemyMob,
                    result
            );

            if (collided != null && collided.collisionShapes()[0] != shooter) {
                if (collided.collisionShapes()[0] instanceof Entity entity) {
                    var e = new ProjectileCollideWithEntityEvent(this, collided.newPosition().withView(-this.position.yaw(), this.position.pitch()), entity);
                    MinecraftServer.getGlobalEventHandler().call(e);
                    return;
                }
            }

            if (result.hasCollision()) {
                Block hitBlock = null;
                Point hitPoint = null;
                if (result.collisionShapes()[0] instanceof ShapeImpl block) {
                    hitBlock = block.block();
                    hitPoint = result.collisionPoints()[0];
                }
                if (result.collisionShapes()[1] instanceof ShapeImpl block) {
                    hitBlock = block.block();
                    hitPoint = result.collisionPoints()[1];
                }
                if (result.collisionShapes()[2] instanceof ShapeImpl block) {
                    hitBlock = block.block();
                    hitPoint = result.collisionPoints()[2];
                }

                if (hitBlock == null) return;
                setNoGravity(true);
                this.onGround = true;

                Point finalHitPoint = hitPoint;
                Block finalHitBlock = hitBlock;

                this.teleport(Pos.fromPoint(finalHitPoint));

                float blockYaw = 0;
                float blockPitch = 0;

                Point blockPos = result.newPosition();

                onHitTick(posNow, diff);

                this.velocity = Vec.ZERO;
                sendPacketToViewers(getVelocityPacket());
                sendPacketToViewersAndSelf(new EntityRotationPacket(getEntityId(), this.getPosition().yaw(), this.getPosition().pitch(), true));
                var e = new ProjectileCollideWithBlockEvent(this, Pos.fromPoint(blockPos).withView(blockYaw, blockPitch), finalHitBlock);
                MinecraftServer.getGlobalEventHandler().call(e);
            }
        });
    }
}