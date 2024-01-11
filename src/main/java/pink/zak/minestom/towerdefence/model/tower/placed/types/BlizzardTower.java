package pink.zak.minestom.towerdefence.model.tower.placed.types;

import java.util.List;
import java.util.function.Function;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.FrozenStatusEffect;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.BlizzardTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public final class BlizzardTower extends PlacedAttackingTower<BlizzardTowerLevel> {
    private static final Function<Point, ParticlePacket> LEVEL_4_PACKET = point -> ParticleCreator.createParticlePacket(
            Particle.SNOWFLAKE, point.x(), point.y(), point.z(), 0.8f, 0.9f, 0.8f, 100
    );

    private static final Function<Point, ParticlePacket> LEVEL_5_PACKET = point -> ParticleCreator.createParticlePacket(
            Particle.SNOWFLAKE, point.x(), point.y(), point.z(), 0.9f, 1.3f, 0.9f, 150
    );

    private final @NotNull Pos restSnowmanPos;
    private @Nullable Task snowmanTask;
    private @Nullable Entity snowman;
    private @Nullable LivingTDEnemyMob target;

    public BlizzardTower(@NotNull GameHandler gameHandler, AttackingTower tower, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(gameHandler, tower, id, owner, basePoint, facing, level);

        // todo faces the wrong direction
        TowerDefenceInstance instance = this.gameHandler.getInstance();
        Pos mobSpawnPos = instance.getTowerMap().getMobSpawn(this.owner.getTeam());
        this.restSnowmanPos = new Pos(basePoint.add(0, 1.5, 0)).withDirection(mobSpawnPos).withPitch(0);

        this.snowman = new Entity(EntityType.SNOW_GOLEM);
        ((SnowGolemMeta) this.snowman.getEntityMeta()).setHasPumpkinHat(false);

        this.snowman.setInstance(instance, this.restSnowmanPos);
        this.snowmanTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (this.snowman == null) return;
            if (this.target != null) this.snowman.lookAt(this.target.getPosition().add(0, this.target.getEyeHeight(), 0));
            else this.snowman.setView(this.restSnowmanPos.yaw(), 0);
        }).repeat(5, TimeUnit.SERVER_TICK).schedule();
    }

    @Override
    protected boolean attemptToFire() {
        double speedModifier = this.level.getSpeedModifier();
        int tickDuration = this.level.getTickDuration();

        List<LivingTDEnemyMob> targets = this.findPossibleTargets().stream()
                // filter out targets that are immune to being frozen
                .filter(target -> !target.getEnemyMob().isEffectIgnored(StatusEffectType.FROZEN))
                .toList();

        this.target = targets.isEmpty() ? null : targets.getFirst();

        for (LivingTDEnemyMob target : targets) {
            FrozenStatusEffect currentEffect = (FrozenStatusEffect) target.getStatusEffects().get(StatusEffectType.FROZEN);

            // if it: A) has no effect, B) current effect is worse than this one, or C) current effect is the same but has less time left
            if (currentEffect == null
                    || currentEffect.getSpeedModifier() < speedModifier
                    || (currentEffect.getSpeedModifier() == speedModifier && currentEffect.getRemainingTicks() < tickDuration)) {
                FrozenStatusEffect effect = new FrozenStatusEffect(target, this.owner, speedModifier, tickDuration);
                target.applyStatusEffect(effect);
                target.applySpeedModifier(effect);
            }
        }

        // The below uses all targets because they may be immune to being frozen but not cold damage.
        if (!targets.isEmpty()) {
            int level = this.level.asInteger();
            if (level >= 4) {
                Point point = this.basePoint.add(0, level == 4 ? 3.5 : 4.5, 0);
                ParticlePacket particlePacket = (level == 4 ? LEVEL_4_PACKET : LEVEL_5_PACKET).apply(point);

                MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendPacket(particlePacket));
            }
        }

        if (this.level.getDamage() > 0) {
            for (LivingTDEnemyMob target : targets) {
                target.damage(this, this.level.getDamage());
            }
        }

        return !targets.isEmpty();
    }

    @Override
    public void upgrade(int level, @Nullable GameUser user) {
        if (this.snowman != null && this.snowmanTask != null) {
            this.snowman.remove();
            this.snowmanTask.cancel();
            this.snowman = null;
            this.snowmanTask = null;
        }
        super.upgrade(level, user);
    }

    @Override
    public void destroy() {
        if (this.snowman != null && this.snowmanTask != null) {
            this.snowman.remove();
            this.snowmanTask.cancel();
            this.snowman = null;
        }

        super.destroy();
    }
}
