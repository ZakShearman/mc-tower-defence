package pink.zak.minestom.towerdefence.model.tower.placed.types;

import com.google.common.collect.Lists;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.LightningTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LightningTower extends PlacedAttackingTower<LightningTowerLevel> {
    private Point castPoint;

    public LightningTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, basePoint, facing, level);
        this.castPoint = this.getLevel().getRelativeCastPoint().apply(basePoint);
    }

    @Override
    public int getMaxTargets() {
        return this.level.getMaxTargets();
    }

    @Override
    protected void fire() {
        this.drawParticles();
        for (LivingEnemyMob enemyMob : targets) {
            enemyMob.towerDamage(this, this.level.getDamage());
        }
    }

    private void drawParticles() { // todo do height by entity height
        Set<SendablePacket> packets = new HashSet<>();
        for (LivingEnemyMob enemyMob : super.targets) {
            Point point = enemyMob.getPosition();

            double x = this.castPoint.x();
            double y = this.castPoint.y();
            double z = this.castPoint.z();

            double dx = point.x() - this.castPoint.x();
            double dy = point.y() - this.castPoint.y();
            double dz = point.z() - this.castPoint.z();
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double distanceBetween = 0.2;
            int particleCount = (int) Math.round(length / distanceBetween);

            double changeX = dx / particleCount;
            double changeY = dy / particleCount;
            double changeZ = dz / particleCount;

            for (double thing = 0; thing <= length; thing += distanceBetween) {
                packets.add(
                    ParticleCreator.createParticlePacket(Particle.SOUL_FIRE_FLAME,
                        x, y + enemyMob.getEyeHeight(), z,
                        0, 0, 0,
                        1)
                );

                x += changeX;
                y += changeY;
                z += changeZ;
            }
        }

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.sendPackets(packets);
        }
    }

    @Override
    public void upgrade() {
        super.upgrade();
        this.castPoint = this.getLevel().getRelativeCastPoint().apply(this.getBasePoint());
    }
}
