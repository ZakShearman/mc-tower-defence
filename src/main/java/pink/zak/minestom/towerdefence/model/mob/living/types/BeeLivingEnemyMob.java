package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.instance.Instance;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;

public class BeeLivingEnemyMob extends LivingEnemyMob {
    private final BeeMeta beeMeta;

    public BeeLivingEnemyMob(TowerHandler towerHandler, MobHandler mobHandler, EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(towerHandler, mobHandler, enemyMob, instance, map, gameUser, level);
        this.beeMeta = (BeeMeta) this.entityMeta;
    }

    @Override
    protected void startAttackingCastle() {
        this.beeMeta.setAngerTicks(Integer.MAX_VALUE);
        super.startAttackingCastle();
    }
}
