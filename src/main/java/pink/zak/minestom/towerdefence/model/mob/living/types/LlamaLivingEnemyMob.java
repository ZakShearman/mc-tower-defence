package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.SingleEnemyTDMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.DirectionUtil;

public class LlamaLivingEnemyMob extends SingleEnemyTDMob {

    public LlamaLivingEnemyMob(GameHandler gameHandler, @NotNull EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(gameHandler, enemyMob, level, instance, map, gameUser);
    }

    @Override
    protected void attackCastle() {
        new LlamaSpit(this, this.currentCorner.direction());
    }

    private static class LlamaSpit extends Entity {
        private final @NotNull LlamaLivingEnemyMob mob;

        public LlamaSpit(@NotNull LlamaLivingEnemyMob mob, @NotNull Direction direction) {
            super(EntityType.LLAMA_SPIT);

            this.mob = mob;

            Pos spawnPos = mob.getPosition();
            spawnPos = spawnPos.add(0, EntityType.LLAMA.height(), 0); // Adjust for the llama's height
            spawnPos = DirectionUtil.add(spawnPos, direction, 1); // Adjust to the front of the llama (mouth)

            this.setNoGravity(true);
            this.setInstance(mob.getInstance(), spawnPos);

            this.setVelocity(DirectionUtil.createVec(direction, 7));
            this.scheduleRemove(10, TimeUnit.SERVER_TICK);
        }

        @Override
        public void remove() { // damage the castle when the projectile hits
            this.mob.damageCastle();
            super.remove();
        }
    }
}
