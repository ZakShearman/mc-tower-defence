package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.OwnedEntity;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;

import java.util.Set;

public class BomberTower extends PlacedAttackingTower<AttackingTowerLevel> implements OwnedEntity {
    private final Pos spawnPos;
    private final MobHandler mobHandler;
    private final GameUser owner;

    public BomberTower(GameHandler gameHandler, Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point baseBlock, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, level);
        this.mobHandler = gameHandler.getMobHandler();
        this.owner = owner;

        this.spawnPos = new Pos(baseBlock.add(0.5, 2.5, 0.5));
    }

    @Override
    public int getMaxTargets() {
        return 1;
    }

    @Override
    protected void fire() {
        new BombTnt(this);
    }

    private void damageTroops(BombTnt tnt) {
        Pos center = tnt.getPosition();
        Set<LivingEnemyMob> enemyMobs = super.team == Team.RED ? this.mobHandler.getRedSideMobs() : this.mobHandler.getBlueSideMobs();

        for (LivingEnemyMob enemyMob : enemyMobs) {
            if (enemyMob.getDistance(center) <= 4) {
                enemyMob.towerDamage(this, this.level.getDamage());
            }
        }
    }

    @Override
    public @NotNull GameUser getOwningUser() {
        return this.owner;
    }

    private static class BombTnt extends LivingEntity {
        private final BomberTower tower;
        private final LivingEntity target;
        private double xVel;
        private double zVel;
        private boolean set;

        BombTnt(BomberTower tower) {
            super(EntityType.TNT);
            this.tower = tower;

            // takes 50 ticks to land, -14 for the initial upwards velocity
            // When vertical maps are supported, this cannot be hardcoded or weird explosion behaviour will be experienced
            this.target = tower.getTargets().get(0);
            Pos targetPos = this.target.getPosition();
            this.xVel = (targetPos.x() - this.tower.spawnPos.x());
            this.zVel = (targetPos.z() - this.tower.spawnPos.z());

            super.hasPhysics = false;
            ((PrimedTntMeta) this.getEntityMeta()).setFuseTime(34);

            this.setInstance(tower.instance, tower.spawnPos);
            this.setVelocity(new Vec(0, 12, 0));
        }

        @Override
        public void tick(long time) {
            super.tick(time);

            long aliveTicks = super.getAliveTicks();
            if (aliveTicks == 34) {
                Pos pos = this.getPosition();
                this.instance.explode(pos.blockX(), pos.blockY(), pos.blockZ(), 2);
                this.tower.damageTroops(this);
                this.remove();
                return;
            }

            if (this.getVelocity().y() <= 0) {
                if (!this.set) {
                    this.setGravity(0, 0.0777777778);
                    // update the velocity if the target isn't dead yet
                    if (!this.target.isDead()) {
                        Entity target = this.tower.getTargets().get(0);
                        // takes 40 ticks to land, -14 for the initial upwards velocity.
                        // When vertical maps are supported, we'll have to do projectile motion calculations live or weird explosion behaviour will be experienced
                        this.xVel = (target.getPosition().x() - this.tower.spawnPos.x());
                        this.zVel = (target.getPosition().z() - this.tower.spawnPos.z());
                    }

                    this.setVelocity(new Vec(this.xVel, 10, this.zVel));
                    this.set = true;
                }
            }
            if (this.set)
                this.setVelocity(new Vec(this.xVel, this.getVelocity().y(), this.zVel));
        }
    }
}
