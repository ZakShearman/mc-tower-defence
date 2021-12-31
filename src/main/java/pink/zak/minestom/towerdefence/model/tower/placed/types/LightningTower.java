package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class LightningTower extends PlacedAttackingTower<LightningTowerLevel> {
    private final List<LivingEntity> guardians = new CopyOnWriteArrayList<>();
    private List<Point> castPoints;

    public LightningTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, basePoint, facing, level);
        this.castPoints = this.getLevel().getRelativeCastPoints().stream()
            .map(castPoint -> castPoint.apply(basePoint)).toList();
    }

    @Override
    public int getMaxTargets() {
        return this.castPoints.size();
    }

    @Override
    protected void fire() {
//        if (this.level.getLevel() > 2)
//            this.drawGuardianBeams();
//        else
        this.drawParticles();

        for (LivingEnemyMob enemyMob : this.targets)
            enemyMob.towerDamage(this, this.level.getDamage());
    }

    private void drawGuardianBeams() {
        for (int i = 0; i < this.targets.size(); i++) {
            LivingEnemyMob target = this.targets.get(i);
            LivingEntity guardian = this.guardians.get(i);
            ((GuardianMeta) guardian.getEntityMeta()).setTarget(target);
            Audiences.all().sendMessage(Component.text(System.nanoTime() + " set the target of a guardian beam"));
        }
    }

    private void drawParticles() { // todo do height by entity height
        Set<SendablePacket> packets = new HashSet<>();
        for (int i = 0; i < super.targets.size(); i++) {
            LivingEnemyMob enemyMob = super.targets.get(i);
            Point castPoint = this.castPoints.get(i);
            Point point = enemyMob.getPosition();

            double x = castPoint.x();
            double y = castPoint.y();
            double z = castPoint.z();

            double dx = point.x() - castPoint.x();
            double dy = point.y() - castPoint.y();
            double dz = point.z() - castPoint.z();
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double distanceBetween = 0.25;
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
        LightningTowerLevel oldLevel = this.getLevel();

        super.upgrade();
        this.castPoints = this.getLevel().getRelativeCastPoints().stream()
            .map(castPoint -> castPoint.apply(basePoint)).toList();
//        this.updateGuardianBeams(oldLevel);
    }

    private void updateGuardianBeams(LightningTowerLevel oldLevel) {
        if (this.level.getLevel() > 2) {
            int beamDifference = this.getMaxTargets() - this.guardians.size();
            if (beamDifference > 0) {
                for (int i = 0; i < beamDifference; i++) {
                    EntityCreature guardian = new EntityCreature(EntityType.GUARDIAN);
                    guardian.setInvisible(true);
                    this.guardians.add(guardian);
                }
            } else if (beamDifference < 0) { // maybe someone will configure it to do this, probably not but let's just handle it.
                LivingEntity removedGuardian = this.guardians.remove(oldLevel.getRelativeCastPoints().size() - 1);
                removedGuardian.kill();
            }
        }
    }
}
