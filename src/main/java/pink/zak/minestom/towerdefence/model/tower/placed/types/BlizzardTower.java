package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.FrozenStatusEffect;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.BlizzardTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class BlizzardTower extends PlacedAttackingTower<BlizzardTowerLevel> {

    private final Pos restSnowmanPos;
    private Entity snowman;
    private Task snowmanTask;

    public BlizzardTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level, TowerMap map) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);

        Pos spawnPos = map.getMobSpawn(this.team);
        this.restSnowmanPos = new Pos(basePoint.add(0, 1.5, 0)).withDirection(spawnPos).withPitch(0);

        this.snowman = new Entity(EntityType.SNOW_GOLEM);
        ((SnowGolemMeta) this.snowman.getEntityMeta()).setHasPumpkinHat(false);

        this.snowman.setInstance(instance, this.restSnowmanPos);
        this.startUpdatingSnowmanYaw();
    }

    @Override
    protected void fire() {
        double speedModifier = this.level.getSpeedModifier();
        int tickDuration = this.level.getTickDuration();

        for (LivingEnemyMob target : this.targets) {
            FrozenStatusEffect currentEffect = (FrozenStatusEffect) target.getStatusEffects().get(StatusEffectType.FROZEN);

            // if it: A) has no effect, B) current effect is worse than this one, or C) current effect is the same but has less time left
            if (currentEffect == null
                    || currentEffect.getModifier() < speedModifier
                    || (currentEffect.getModifier() == speedModifier && currentEffect.remainingTicks() < tickDuration)) {
                target.applyStatusEffect(new FrozenStatusEffect(target, speedModifier, tickDuration));
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
                            this.snowman.lookAt(this.targets.get(0));
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
