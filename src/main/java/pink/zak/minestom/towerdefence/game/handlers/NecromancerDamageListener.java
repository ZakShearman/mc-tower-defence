package pink.zak.minestom.towerdefence.game.handlers;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.placed.types.NecromancerTower;

public class NecromancerDamageListener {

    public NecromancerDamageListener(EventNode<Event> eventNode) {
        eventNode.addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof NecromancerTower.NecromancedMob necromancedMob)) return;
            if (!(event.getTarget() instanceof LivingTDEnemyMob livingEnemyMob)) return;

            float damage = (float) (necromancedMob.getEnemyMobLevel().getDamage() * necromancedMob.getOriginTower().getLevel().getDamageMultiplier());
            livingEnemyMob.damage(necromancedMob, damage);
        });
    }
}
