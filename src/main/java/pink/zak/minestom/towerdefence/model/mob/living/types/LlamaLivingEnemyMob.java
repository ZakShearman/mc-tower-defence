package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.utils.DirectionUtils;

public class LlamaLivingEnemyMob extends LivingEnemyMob {

    public LlamaLivingEnemyMob(TowerHandler towerHandler, MobHandler mobHandler, @NotNull EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(towerHandler, mobHandler, enemyMob, instance, map, gameUser, level);
    }

    @Override
    protected void attackCastle() {
        Pos position = this.getPosition();
        for (int i = 1; i <= 3; i++) {
            Pos particlePosition = DirectionUtils.add(position, this.currentCorner.direction(), i);
            ParticlePacket particlePacket = ParticleCreator.createParticlePacket(
                Particle.SPIT,
                particlePosition.x(), particlePosition.y() + EntityType.LLAMA.height(), particlePosition.z(),
                0.1f, 0.1f, 0.1f,
                2
            );
            for (Player player : this.getViewers()) {
                player.sendPacketToViewersAndSelf(particlePacket);
            }
        }
    }
}
