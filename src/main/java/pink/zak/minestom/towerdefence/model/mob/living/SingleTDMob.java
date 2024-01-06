package pink.zak.minestom.towerdefence.model.mob.living;

import java.util.HashSet;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.*;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import pink.zak.minestom.towerdefence.model.prediction.DamagePrediction;

public class SingleTDMob extends EntityCreature implements LivingTDMob {
    private final int level;

    public SingleTDMob(@NotNull EntityType entityType, int level) {
        super(entityType);
        this.level = level;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void updateCustomName() {
        for (Player player : this.getViewers()) {
            Component value = this.createNameComponent(player);
            Metadata.Entry<?> nameEntry = Metadata.OptChat(value);
            player.sendPacket(new EntityMetaDataPacket(this.getEntityId(), Map.of(2, nameEntry)));
        }
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        if (!this.isCustomNameVisible()) {
            super.updateNewViewer(player);
            return;
        }

        player.sendPacket(this.getEntityType().registry().spawnType().getSpawnPacket(this));
        if (this.hasVelocity()) player.sendPacket(this.getVelocityPacket());

        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(this.metadata.getEntries());
        Metadata.Entry<?> nameEntry = Metadata.OptChat(this.createNameComponent(player));
        entries.put(2, nameEntry);
        player.sendPacket(new LazyPacket(() -> new EntityMetaDataPacket(getEntityId(), entries)));

        // Head position
        player.sendPacket(new EntityHeadLookPacket(getEntityId(), this.position.yaw()));

        // Passengers
        final Set<Entity> passengers = this.getPassengers();
        if (!passengers.isEmpty()) {
            for (Entity passenger : passengers) {
                if (passenger != player) passenger.updateNewViewer(player);
            }
            player.sendPacket(getPassengersPacket());
        }

        // from LivingEntity
        player.sendPacket(new LazyPacket(this::getEquipmentsPacket));
        player.sendPacket(new LazyPacket(this::getPropertiesPacket));
        if (getTeam() != null) player.sendPacket(getTeam().createTeamsCreationPacket());
    }

    @Override
    public @NotNull EntityType getTDEntityType() {
        return this.getEntityType();
    }
}
