package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.instance.Instance;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class BeeLivingEnemyMob extends LivingEnemyMob {
    private final BeeMeta beeMeta;

    public BeeLivingEnemyMob(GameHandler gameHandler, EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(gameHandler, enemyMob, instance, map, gameUser, level, true);
        this.beeMeta = (BeeMeta) this.entityMeta;
    }

    @Override
    protected void startAttackingCastle() {
        this.beeMeta.setAngerTicks(Integer.MAX_VALUE);
        super.startAttackingCastle();
    }
}
