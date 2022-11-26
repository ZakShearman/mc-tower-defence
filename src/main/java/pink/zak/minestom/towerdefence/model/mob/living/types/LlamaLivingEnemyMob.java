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
        Pos position = this.getPosition();
        Entity spit = new Entity(EntityType.LLAMA_SPIT);
        Direction direction = this.currentCorner.direction();
        spit.setNoGravity(true);
        spit.setInstance(this.instance, DirectionUtil.add(position.add(0, EntityType.LLAMA.height(), 0), direction, 1));
        spit.setVelocity(DirectionUtil.createVec(direction, 7));
        spit.scheduleRemove(10, TimeUnit.SERVER_TICK);
    }

    @Override
    public void remove() { // damage the castle when the projectile hits
        super.damageCastle();
        super.remove();
    }
}
