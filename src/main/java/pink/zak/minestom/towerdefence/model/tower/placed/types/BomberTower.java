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
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

import java.util.Set;

public class BomberTower extends PlacedTower {
    private final Pos spawnPos;
    private final MobHandler mobHandler;

    public BomberTower(GameHandler gameHandler, Instance instance, Tower tower, Material towerPlaceMaterial, short id, Team team, Point baseBlock, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, team, baseBlock, facing, level);
        this.mobHandler = gameHandler.getMobHandler();

        this.spawnPos = new Pos(baseBlock.add(0.5, 2.5, 0.5));
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
                enemyMob.damage(DamageType.VOID, this.level.damage());
            }
        }
    }

    private static class BombTnt extends LivingEntity {
        private final BomberTower tower;
        private double xVel;
        private double zVel;
        private boolean set;

        BombTnt(BomberTower tower) {
            super(EntityType.TNT);
            this.tower = tower;

            // takes 50 ticks to land, -14 for the initial upwards velocity
            // When vertical maps are supported, this cannot be hardcoded or weird explosion behaviour will be experienced
            Entity target = tower.target;
            this.xVel = (target.getPosition().x() - this.tower.spawnPos.x()) * (20.0 / 36);
            this.zVel = (target.getPosition().z() - this.tower.spawnPos.z()) * (20.0 / 36);

            super.hasPhysics = false;
            ((PrimedTntMeta) this.getEntityMeta()).setFuseTime(49);

            this.setInstance(tower.instance, tower.spawnPos);
            this.setVelocity(new Vec(0, 12, 0));
        }

        @Override
        public void tick(long time) {
            super.tick(time);

            long ticks = super.getAliveTicks();
            if (ticks == 50) {
                Pos pos = this.getPosition();
                this.instance.explode(pos.blockX(), pos.blockY(), pos.blockZ(), 2);
                this.tower.damageTroops(this);
                return;
            }

            if (this.getVelocity().y() <= 0) {
                if (!this.set) {
                    System.out.println("WOO IT TOOK THIS MANY TICKS: " + ticks);
                    Entity target = this.tower.target;
                    if (target != null) {
                        // takes 50 ticks to land, -14 for the initial upwards velocity.
                        // When vertical maps are supported, this cannot be hardcoded or weird explosion behaviour will be experienced
                        this.xVel = (target.getPosition().x() - this.tower.spawnPos.x()) * (20.0 / 36);
                        this.zVel = (target.getPosition().z() - this.tower.spawnPos.z()) * (20.0 / 36);
                    }

                    this.setVelocity(new Vec(this.xVel, 10, this.zVel));
                    this.set = true;
                }
            }
            if (this.set) {
                this.setVelocity(new Vec(this.xVel, this.getVelocity().y(), this.zVel));
            }

            if (this.getPosition().y() < 65.5 && this.getPosition().y() > 64.5)
                System.out.println("LESS THAN 60 " + ticks + "  " + this.getPosition().y());
        }
    }
}
