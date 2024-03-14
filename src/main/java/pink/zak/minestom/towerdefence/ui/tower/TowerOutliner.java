package pink.zak.minestom.towerdefence.ui.tower;

import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.DustParticleData;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.TowerSize;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public final class TowerOutliner {

    private final @NotNull TowerDefenceInstance instance;

    public TowerOutliner(@NotNull TowerDefenceInstance instance) {
        this.instance = instance;
    }

    public @NotNull Set<SendablePacket> calculateOutline(@NotNull Point point, @NotNull TowerSize size) {
        Set<SendablePacket> packets = new HashSet<>();
        Block block = instance.getBlock(point);
        Material material = block.registry().material();
        if (material != null && material.name().contains("slab")) point = point.sub(0, 1, 0);

        Point position1 = point.add(-size.getCheckDistance(), 0, -size.getCheckDistance());
        Point position2 = point.add(size.getCheckDistance() + 1, 0, size.getCheckDistance() + 1);
        for (double x = position1.x(); x < position2.x(); x += 0.1) {
            packets.add(this.createParticle(instance, new Pos(x, point.y(), position1.z() + 0.05)));
            packets.add(this.createParticle(instance, new Pos(x, point.y(), position2.z() - 0.05)));
        }
        for (double z = position1.z(); z < position2.z(); z += 0.1) {
            packets.add(this.createParticle(instance, new Pos(position1.x() + 0.05, point.y(), z)));
            packets.add(this.createParticle(instance, new Pos(position2.x() - 0.05, point.y(), z)));
        }
        return packets;
    }

    public @NotNull Set<SendablePacket> calculateOutline(@NotNull Point point, @NotNull TowerType tower) {
        return this.calculateOutline(point, tower.getSize());
    }

    private @NotNull ParticlePacket createParticle(@NotNull TowerDefenceInstance instance, @NotNull Point point) {
        TextColor color = this.isValidPosition(instance, point) ? TextColor.color(0, 255, 0) : TextColor.color(255, 0, 0);
        double offset = 1;

        Block block = instance.getBlock(point.add(0, 1, 0));
        Material material = block.registry().material();
        if (material != null && material.name().contains("slab")) offset += 0.5;

        return new ParticlePacket(
                Particle.DUST.withData(new DustParticleData(color, 1)),
                point.x(), point.y() + offset, point.z(),
                0, 0, 0,
                0, 1
        );
    }

    private boolean isValidPosition(@NotNull TowerDefenceInstance instance, @NotNull Point point) {
        Block block = instance.getBlock(point);

        // only allow placement on the base material
        if (block.registry().material() != instance.getTowerMap().getTowerBaseMaterial()) return false;

        // do not allow placement if a tower is already there
        if (block.hasTag(PlacedTower.ID_TAG)) return false;

        // only allow if the block above is air
        Block blockAbove = instance.getBlock(point.add(0, 1, 0));
        return blockAbove.isAir();
    }

}
