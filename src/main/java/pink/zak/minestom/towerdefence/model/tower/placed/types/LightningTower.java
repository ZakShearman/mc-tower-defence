package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.LightningTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LightningTower extends PlacedAttackingTower<LightningTowerLevel> {
    private Point castPoint;
    private Set<Point> spawnPoints;

    public LightningTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, basePoint, facing, level);
        this.castPoint = this.getLevel().getRelativeCastPoint().apply(basePoint);
        this.spawnPoints = this.getLevel().getRelativeSpawnPoints().stream()
            .map(castPoint -> castPoint.apply(basePoint))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int getMaxTargets() {
        return 1;
    }

    @Override
    protected void fire() {
        this.drawParticles();

        for (LivingEnemyMob enemyMob : this.targets)
            enemyMob.towerDamage(this, this.level.getDamage());
    }

    private void drawParticles() {
        Set<SendablePacket> packets = new HashSet<>();

        LivingEnemyMob target = this.targets.get(0);
        for (Point spawnPoint : this.spawnPoints) {
            this.drawParticleLine(packets, spawnPoint, this.castPoint, 0); // no modifier as this is going to the central point
        }
        this.drawParticleLine(packets, this.castPoint, target.getPosition(), target.getEyeHeight());

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers())
            player.sendPackets(packets);
    }

    private void drawParticleLine(Set<SendablePacket> packets, Point origin, Point destination, double yModifier) {
        double x = origin.x();
        double y = origin.y();
        double z = origin.z();

        double dx = destination.x() - origin.x();
        double dy = destination.y() + yModifier - origin.y();
        double dz = destination.z() - origin.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double distanceBetween = 0.25;
        int particleCount = (int) Math.round(length / distanceBetween);

        double changeX = dx / particleCount;
        double changeY = dy / particleCount;
        double changeZ = dz / particleCount;

        for (double thing = 0; thing <= length; thing += distanceBetween) {
            packets.add(
                ParticleCreator.createParticlePacket(Particle.SOUL_FIRE_FLAME,
                    x, y, z,
                    0, 0, 0,
                    1)
            );

            x += changeX;
            y += changeY;
            z += changeZ;
        }
    }

    @Override
    public void upgrade() {
        super.upgrade();

        this.castPoint = this.getLevel().getRelativeCastPoint().apply(this.basePoint);
        this.spawnPoints = this.getLevel().getRelativeSpawnPoints().stream()
            .map(castPoint -> castPoint.apply(this.basePoint))
            .collect(Collectors.toUnmodifiableSet());
    }
}
