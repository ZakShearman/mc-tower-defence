package pink.zak.minestom.towerdefence.enums;

import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.DustParticleData;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public enum TowerSize {

    THREE(3, 1),
    FIVE(5, 2);

    private final int numericalValue;
    private final int checkDistance;

    TowerSize(int numericalValue, int checkDistance) {
        this.numericalValue = numericalValue;
        this.checkDistance = checkDistance;
    }

    public int getCheckDistance() {
        return this.checkDistance;
    }

    public @NotNull String getFormattedName() {
        return this.numericalValue + "x" + this.numericalValue;
    }

}