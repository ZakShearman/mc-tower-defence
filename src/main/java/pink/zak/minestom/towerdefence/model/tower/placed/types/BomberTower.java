package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.NamedSoundEffectPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.BomberTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BomberTower extends PlacedAttackingTower<BomberTowerLevel> {
    private static final double GRAVITY_PER_TICK = 0.04;
    private static final double DRAG_PER_TICK = 0;

    private static final int RAISE_TICKS = 6;
    private static final double RAISE_BLOCKS = 1.5;

    private static final int FLYING_TICKS = 10;
    private static final @NotNull Vec RAISE_VELOCITY;

    static {
        RAISE_VELOCITY = new Vec(0, findVelocity(RAISE_BLOCKS, -GRAVITY_PER_TICK, RAISE_TICKS), 0);
    }

    private final MobHandler mobHandler;
    private final GameUser owner;

    private Point spawnPoint;

    public BomberTower(GameHandler gameHandler, Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);
        this.mobHandler = gameHandler.getMobHandler();
        this.owner = owner;

        this.updateSpawnPoint();
    }

    @Override
    public int getMaxTargets() {
        return 1;
    }

    private void updateSpawnPoint() {
        this.spawnPoint = this.getLevel().getRelativeTntSpawnPoint().apply(this.getBasePoint());
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.updateSpawnPoint();
    }

    @Override
    protected void fire() {
        new BombTnt(this);
    }

    private void damageTroops(@NotNull BombTnt tnt) {
        Pos center = tnt.getPosition();
        Set<LivingTDEnemyMob> enemyMobs = this.mobHandler.getMobs(super.team);

        for (LivingTDEnemyMob enemyMob : enemyMobs) {
            if (enemyMob.getPosition().distance(center) <= this.level.getExplosionRadius()) {
                enemyMob.damage(this, this.level.getDamage());
            }
        }
    }

    @Override
    public @NotNull GameUser getOwningUser() {
        return this.owner;
    }

    private static class BombTnt extends LivingEntity {
        private final BomberTower tower;
        private final Pos fallbackPos;

        BombTnt(BomberTower tower) {
            super(EntityType.TNT);
            this.tower = tower;

            super.hasPhysics = false;
            ((PrimedTntMeta) this.getEntityMeta()).setFuseTime(RAISE_TICKS + FLYING_TICKS);

            this.setInstance(tower.instance, tower.spawnPoint);
            this.setGravity(DRAG_PER_TICK, GRAVITY_PER_TICK);
            this.setVelocity(RAISE_VELOCITY);

            LivingTDEnemyMob target = tower.getTargets().get(0);
            this.fallbackPos = target.getPosition();
        }

        @Override
        public void tick(long time) {
            super.tick(time);
            long aliveTicks = super.getAliveTicks();

            if (aliveTicks == RAISE_TICKS) {
                Pos targetPos = this.tower.getTargets().isEmpty() ? this.fallbackPos : this.tower.getTargets().get(0).getPosition();
                targetPos = targetPos.add(0, 1.5, 0);
                this.setVelocity(this.tower.calculateLaunchVec(this.position, targetPos));
            } else if (aliveTicks == RAISE_TICKS + FLYING_TICKS) {
                Pos pos = this.getPosition();

                ServerPacket soundPacket =  new NamedSoundEffectPacket(SoundEvent.ENTITY_GENERIC_EXPLODE.name(), Sound.Source.PLAYER,
                        pos.blockX(), pos.blockY(), pos.blockZ(), 1f, 1f, ThreadLocalRandom.current().nextLong());

                ServerPacket particlePacket = ParticleCreator.createParticlePacket(
                        Particle.EXPLOSION, pos.x(), pos.y(), pos.z(), 0, 0, 0, 1
                );
                this.instance.sendGroupedPacket(particlePacket);
                this.instance.sendGroupedPacket(soundPacket);

                this.tower.damageTroops(this);
                this.remove();
            }
        }
    }

    public Vec calculateLaunchVec(Point current, Point target) {
        double x = target.x() - current.x();
        double z = target.z() - current.z();
        double y = target.y() - current.y();

        double xVel = findVelocity(x, -DRAG_PER_TICK, FLYING_TICKS);
        double zVel = findVelocity(z, -DRAG_PER_TICK, FLYING_TICKS);
        double yVel = findVelocity(y, -GRAVITY_PER_TICK, FLYING_TICKS);
        return new Vec(xVel, yVel, zVel);
    }

    private static double findVelocity(double s, double a, int t) {
        return (s / t - 0.5 * a * t) * MinecraftServer.TICK_PER_SECOND; // a Vec is blocks/s, not blocks/tick
    }
}
