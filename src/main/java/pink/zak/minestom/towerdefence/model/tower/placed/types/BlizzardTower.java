package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.FrozenStatusEffect;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.BlizzardTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.function.Function;

public class BlizzardTower extends PlacedAttackingTower<BlizzardTowerLevel> {
    private static final Function<Point, ParticlePacket> LEVEL_4_PACKET = point -> ParticleCreator.createParticlePacket(
            Particle.SNOWFLAKE, point.x(), point.y(), point.z(), 0.8f, 0.9f, 0.8f, 100
    );

    private static final Function<Point, ParticlePacket> LEVEL_5_PACKET = point -> ParticleCreator.createParticlePacket(
            Particle.SNOWFLAKE, point.x(), point.y(), point.z(), 0.9f, 1.3f, 0.9f, 150
    );

    private final Pos restSnowmanPos;
    private Entity snowman;
    private Task snowmanTask;

    public BlizzardTower(TowerDefenceInstance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);

        // todo faces the wrong direction
        Pos mobSpawnPos = instance.getTowerMap().getMobSpawn(this.team);
        this.restSnowmanPos = new Pos(basePoint.add(0, 1.5, 0)).withDirection(mobSpawnPos).withPitch(0);

        this.snowman = new Entity(EntityType.SNOW_GOLEM);
        ((SnowGolemMeta) this.snowman.getEntityMeta()).setHasPumpkinHat(false);

        this.snowman.setInstance(instance, this.restSnowmanPos);
        this.startUpdatingSnowmanYaw();
    }

    @Override
    protected void fire() {
        double speedModifier = this.level.getSpeedModifier();
        int tickDuration = this.level.getTickDuration();

        for (LivingTDEnemyMob target : this.getTargetsNotImmune(StatusEffectType.FROZEN)) {
            FrozenStatusEffect currentEffect = (FrozenStatusEffect) target.getStatusEffects().get(StatusEffectType.FROZEN);

            // if it: A) has no effect, B) current effect is worse than this one, or C) current effect is the same but has less time left
            if (currentEffect == null
                    || currentEffect.getModifier() < speedModifier
                    || (currentEffect.getModifier() == speedModifier && currentEffect.remainingTicks() < tickDuration)) {
                FrozenStatusEffect effect = new FrozenStatusEffect(target, this.owner, speedModifier, tickDuration);
                target.applyStatusEffect(effect);
                target.applySpeedModifier(effect);
            }
        }

        // The below uses all targets because they may be immune to being frozen but not cold damage.
        if (this.targets.size() > 0) {
            if (this.levelInt >= 4) {
                Point point = this.basePoint.add(0, this.levelInt == 4 ? 3.5 : 4.5, 0);
                ParticlePacket particlePacket = (this.levelInt == 4 ? LEVEL_4_PACKET : LEVEL_5_PACKET).apply(point);

                MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendPacket(particlePacket));
            }
        }

        if (this.level.getDamage() > 0) {
            for (LivingTDEnemyMob target : this.targets) { //
                target.damage(this, this.level.getDamage());
            }
        }
    }

    @Override
    public void upgrade() {
        if (this.snowman != null) {
            this.snowman.remove();
            this.snowmanTask.cancel();
            this.snowman = null;
        }
        super.upgrade();
    }

    @Override
    public void destroy() {
        if (this.snowman != null) {
            this.snowman.remove();
            this.snowmanTask.cancel();
            this.snowman = null;
        }

        super.destroy();
    }

    private void startUpdatingSnowmanYaw() {
        this.snowmanTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
                    if (this.snowman != null) {
                        if (this.targets.size() > 0) {
                            this.snowman.lookAt(this.targets.get(0).getPosition());
                        } else if (this.snowman.getPosition() != this.restSnowmanPos) {
                            this.snowman.setView(this.restSnowmanPos.yaw(), 0);
                        }
                    }
                }).repeat(5, TimeUnit.SERVER_TICK)
                .schedule();
    }

    @Override
    public int getMaxTargets() {
        return Integer.MAX_VALUE;
    }
}
