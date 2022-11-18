package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class LivingTDMob extends EntityCreature {
    private static final Logger LOGGER = LoggerFactory.getLogger(LivingTDMob.class);

    public LivingTDMob(@NotNull EntityType entityType, boolean useCustomName) {
        super(entityType);

        this.setCustomNameVisible(useCustomName);
    }

    @Override
    public void setCustomName(@Nullable Component customName) {
        LOGGER.warn("setCustomName called for a LivingEnemyMob. This action is not supported");
    }

    // todo handle health and updating here?

    public void updateCustomName() {
        if (!this.isCustomNameVisible()) return;
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
        // Passengers are removed here as I don't need them

        // Head position
        player.sendPacket(new EntityHeadLookPacket(getEntityId(), this.position.yaw()));
    }

    protected abstract Component createNameComponent(Player player);
}
