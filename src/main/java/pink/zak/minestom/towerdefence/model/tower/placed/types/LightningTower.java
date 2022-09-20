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
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.cache.TDUserCache;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.LightningTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDUser;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LightningTower extends PlacedAttackingTower<LightningTowerLevel> {
    private final @NotNull TDUserCache userCache;
    private Point castPoint;
    private Set<Point> spawnPoints;

    public LightningTower(TowerDefencePlugin plugin, Instance instance, AttackingTower tower, Material towerPlaceMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, basePoint, facing, level);
        this.userCache = plugin.getUserCache();
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
        Map<ParticleThickness, Set<SendablePacket>> thicknessPackets = new HashMap<>();

        LivingEnemyMob target = this.targets.get(0);

        for (ParticleThickness thickness : ParticleThickness.values()) {
            Set<SendablePacket> packets = new HashSet<>();
            for (Point spawnPoint : this.spawnPoints) {
                packets.addAll(this.drawParticleLine(thickness, spawnPoint, this.castPoint, 0)); // no modifier as this is going to the central point
            }
            packets.addAll(this.drawParticleLine(thickness, this.castPoint, target.getPosition(), target.getEyeHeight()));
            thicknessPackets.put(thickness, packets);
        }

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            // todo only send to applicable users
            TDUser tdUser = this.userCache.getUser(player.getUuid());
            player.sendPackets(thicknessPackets.get(tdUser.getParticleThickness()));
        }
    }

    private Set<SendablePacket> drawParticleLine(ParticleThickness thickness, Point origin, Point destination, double yModifier) {
        Set<SendablePacket> packets = new HashSet<>();
        double x = origin.x();
        double y = origin.y();
        double z = origin.z();

        double dx = destination.x() - origin.x();
        double dy = destination.y() + yModifier - origin.y();
        double dz = destination.z() - origin.z();
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        int particleCount = (int) Math.round(length / thickness.getSpacing());

        double xIncrement = dx / particleCount;
        double yIncrement = dy / particleCount;
        double zIncrement = dz / particleCount;

        for (double i = 0; i <= length; i += thickness.getSpacing()) {
            packets.add(
                    ParticleCreator.createParticlePacket(Particle.SOUL_FIRE_FLAME,
                            x, y, z,
                            0, 0, 0,
                            1)
            );

            x += xIncrement;
            y += yIncrement;
            z += zIncrement;
        }
        return packets;
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
