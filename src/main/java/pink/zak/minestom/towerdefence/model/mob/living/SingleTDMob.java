package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SingleTDMob extends EntityCreature implements LivingTDMob {
    private final int level;

    public SingleTDMob(@NotNull EntityType entityType, int level) {
        super(entityType);
        this.level = level;

        this.setNoGravity(true);
        this.hasPhysics = false;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public float getMaxHealth() {
        return 0; // todo idk what this is meant to be??
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
