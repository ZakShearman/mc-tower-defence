package pink.zak.minestom.towerdefence.model.tower.placed.types;

import java.util.List;
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
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.LightningTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pink.zak.minestom.towerdefence.targetting.Target;

public final class LightningTower extends PlacedAttackingTower<LightningTowerLevel> {
    private Point castPoint;
    private Set<Point> spawnPoints;

    public LightningTower(@NotNull MobHandler mobHandler, Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(mobHandler, instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);
        this.castPoint = this.getLevel().getRelativeCastPoint().apply(basePoint);
        this.spawnPoints = this.getLevel().getRelativeSpawnPoints().stream()
                .map(castPoint -> castPoint.apply(basePoint))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void attemptToFire() {
        List<LivingTDEnemyMob> targets = this.findPossibleTargets(Target.first());
        if (targets.isEmpty()) return;

        LivingTDEnemyMob target = targets.getFirst();
        this.drawParticles(target);
        target.damage(this, this.level.getDamage());
    }

    private void drawParticles(@NotNull LivingTDEnemyMob target) {
        Map<ParticleThickness, Set<SendablePacket>> thicknessPackets = new HashMap<>();

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
            TDPlayer tdPlayer = (TDPlayer) player;
            player.sendPackets(thicknessPackets.get(tdPlayer.getParticleThickness()));
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
