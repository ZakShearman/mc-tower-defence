package pink.zak.minestom.towerdefence.ui.tower;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.TowerSize;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

public final class TowerOutliner {

    private static final @NotNull TextColor VALID_COLOR = TextColor.color(0, 255, 0);
    private static final @NotNull TextColor INVALID_COLOR = TextColor.color(255, 0, 0);

    private final @NotNull TowerDefenceInstance instance;

    public TowerOutliner(@NotNull TowerDefenceInstance instance) {
        this.instance = instance;
    }

    public @NotNull Set<SendablePacket> calculateOutline(@NotNull GameUser user, @NotNull Point point, @NotNull TowerSize size) {
        // special case for slabs
        Block block = instance.getBlock(point);
        Material material = block.registry().material();
        if (material != null && material.name().contains("slab")) point = point.sub(0, 1, 0);

        Point position1 = point.add(-size.getCheckDistance(), 0, -size.getCheckDistance());
        Point position2 = point.add(size.getCheckDistance() + 1, 0, size.getCheckDistance() + 1);

        Set<Point> invalidPoints = new HashSet<>();
        for (int x = position1.blockX(); x < position2.blockX(); x++) {
            for (int z = position1.blockZ(); z < position2.blockZ(); z++) {
                Point checkPoint = new Pos(x + 0.5, point.y(), z + 0.5);
                if (!this.isValidPosition(user, instance, checkPoint)) invalidPoints.add(checkPoint);
            }
        }

        Set<SendablePacket> packets = new HashSet<>();
        for (double x = position1.x(); x < position2.x(); x += 0.3) {
            packets.add(this.createParticle(instance, new Pos(x, point.y(), position1.z() + 0.05), invalidPoints, size));
            packets.add(this.createParticle(instance, new Pos(x, point.y(), position2.z() - 0.05), invalidPoints, size));
        }
        for (double z = position1.z(); z < position2.z(); z += 0.3) {
            packets.add(this.createParticle(instance, new Pos(position1.x() + 0.05, point.y(), z), invalidPoints, size));
            packets.add(this.createParticle(instance, new Pos(position2.x() - 0.05, point.y(), z), invalidPoints, size));
        }
        return packets;
    }

    private @NotNull TextColor linearInterpolate(double t) {
        int red = (int) (TowerOutliner.INVALID_COLOR.red() + (TowerOutliner.VALID_COLOR.red() - TowerOutliner.INVALID_COLOR.red()) * t);
        int green = (int) (TowerOutliner.INVALID_COLOR.green() + (TowerOutliner.VALID_COLOR.green() - TowerOutliner.INVALID_COLOR.green()) * t);
        int blue = (int) (TowerOutliner.INVALID_COLOR.blue() + (TowerOutliner.VALID_COLOR.blue() - TowerOutliner.INVALID_COLOR.blue()) * t);
        return TextColor.color(red, green, blue);
    }

    private @NotNull TextColor easeOutCircularInterpolate(double t) {
        return this.linearInterpolate(Math.sqrt(1 - (t - 1) * (t - 1)));
    }

    public @NotNull Set<SendablePacket> calculateOutline(@NotNull GameUser user, @NotNull Point point, @NotNull TowerType tower) {
        return this.calculateOutline(user, point, tower.getSize());
    }

    private @NotNull ParticlePacket createParticle(@NotNull TowerDefenceInstance instance, @NotNull Point point, @NotNull Set<Point> invalidPoints, @NotNull TowerSize size) {
        OptionalDouble nearestInvalid = invalidPoints.stream()
                .mapToDouble(point::distance)
                .min();
        double transformation = size == TowerSize.FIVE ? 0.1 : 0.25;
        TextColor color = nearestInvalid.isPresent() ? this.easeOutCircularInterpolate(Math.max(0, (nearestInvalid.getAsDouble() / size.getNumericalValue()) - transformation)) : VALID_COLOR;

        double offset = 1;

        // special case for slabs
        Block block = instance.getBlock(point.add(0, 1, 0));
        Material material = block.registry().material();
        if (material != null && material.name().contains("slab")) offset += 0.5;

        return new ParticlePacket(
                Particle.DUST.withColor(color).withScale(1),
                point.x(), point.y() + offset, point.z(),
                0, 0, 0,
                1f, 1
        );
    }

    /**
     * This method must mirror {@link Tower#isSpaceClear(TowerDefenceInstance, Point)}. This method technically does
     * the same thing, but also includes team-based checks.
     */
    // todo: we shouldn't be mirroring methods like this, we should somehow combine the two.
    private boolean isValidPosition(@NotNull GameUser user, @NotNull TowerDefenceInstance instance, @NotNull Point point) {
        Block block = instance.getBlock(point);

        // only allow placement on the player's side of the map
        if (!instance.getTowerMap().getArea(user.getTeam()).isWithin(point)) return false;

        // only allow placement on the base material
        if (block.registry().material() != instance.getTowerMap().getTowerBaseMaterial()) return false;

        // do not allow placement if a tower is already there
        if (block.hasTag(PlacedTower.ID_TAG)) return false;

        // only allow if the block above is air
        Block blockAbove = instance.getBlock(point.add(0, 1, 0));
        return blockAbove.isAir();
    }

}
