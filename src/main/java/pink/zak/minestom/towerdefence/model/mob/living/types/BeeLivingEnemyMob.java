package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;

public class BeeLivingEnemyMob extends LivingEnemyMob {
    private final BeeMeta beeMeta;

    public BeeLivingEnemyMob(@NotNull EnemyMob enemyMob, Instance instance, TowerMap map, Team team, int level) {
        super(enemyMob, instance, map, team, level);
        this.beeMeta = (BeeMeta) this.entityMeta;
    }

    @Override
    protected void startAttackingCastle() {
        this.beeMeta.setAngerTicks(Integer.MAX_VALUE);
        super.startAttackingCastle();
    }
}
