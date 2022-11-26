package pink.zak.minestom.towerdefence.model.mob.living.types.skeleton;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.animal.AbstractHorseMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.SingleEnemyTDMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.HashMap;
import java.util.Map;

public class SkeletonLivingEnemyMob extends SingleEnemyTDMob {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkeletonLivingEnemyMob.class);

    private final MountedSkeletonMob mountedSkeletonMob;

    public SkeletonLivingEnemyMob(GameHandler gameHandler, EnemyMob enemyMob, int level, Instance instance, TowerMap map, GameUser gameUser) {
        super(gameHandler, enemyMob, level,
                instance, map, gameUser,
                level == 5 ? EntityType.ZOMBIE_HORSE : level > 1 ? EntityType.SKELETON_HORSE : EntityType.SKELETON
        );


        ItemStack chestplateItem = ItemStack.of(switch (level) {
            case 2 -> Material.CHAINMAIL_CHESTPLATE;
            case 3 -> Material.GOLDEN_CHESTPLATE;
            case 4 -> Material.IRON_CHESTPLATE;
            case 5 -> Material.DIAMOND_CHESTPLATE;
            default -> Material.AIR;
        });
        ItemStack heldItem = ItemStack.of(Material.BOW);

        AbstractHorseMeta horseMeta = (AbstractHorseMeta) this.getEntityMeta();

        this.mountedSkeletonMob = new MountedSkeletonMob(this);
        this.mountedSkeletonMob.setEquipment(EquipmentSlot.CHESTPLATE, chestplateItem);
        this.mountedSkeletonMob.setEquipment(EquipmentSlot.MAIN_HAND, heldItem);

        this.addPassenger(this.mountedSkeletonMob);
    }

    public static LivingTDEnemyMob create(@NotNull GameHandler gameHandler, @NotNull EnemyMob enemyMob, int level, @NotNull Instance instance, @NotNull TowerMap map, @NotNull GameUser gameUser) {
        if (level == 1) return new SingleEnemyTDMob(gameHandler, enemyMob, level, instance, map, gameUser);
        return new SkeletonLivingEnemyMob(gameHandler, enemyMob, level, instance, map, gameUser);
    }

    @Override
    public void kill() {
        super.kill();
        this.mountedSkeletonMob.remove();
    }

    @Override
    public void updateCustomName() {
        for (Player player : this.getViewers()) {
            Component value = this.createNameComponent(player);
            Metadata.Entry<?> nameEntry = Metadata.OptChat(value);
            player.sendPacket(new EntityMetaDataPacket(this.mountedSkeletonMob.getEntityId(), Map.of(2, nameEntry)));
        }
    }

    @Override
    public float damage(@NotNull DamageSource source, float value) {
        this.sendPacketToViewersAndSelf(new EntityAnimationPacket(this.mountedSkeletonMob.getEntityId(), EntityAnimationPacket.Animation.TAKE_DAMAGE));
        return super.damage(source, value);
    }

    @Override
    public void refreshPosition(@NotNull Pos newPosition, boolean ignoreView) {
        this.mountedSkeletonMob.teleport(this.mountedSkeletonMob.getPosition().withYaw(newPosition.yaw()));
        super.refreshPosition(newPosition, ignoreView);
    }

    private static class MountedSkeletonMob extends LivingEntity {
        private final SkeletonLivingEnemyMob parent;

        public MountedSkeletonMob(SkeletonLivingEnemyMob parent) {
            super(EntityType.SKELETON);
            this.parent = parent;

            this.setCustomNameVisible(true);
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            player.sendPacket(this.getEntityType().registry().spawnType().getSpawnPacket(this));
            if (this.hasVelocity()) player.sendPacket(this.getVelocityPacket());

            Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(this.metadata.getEntries());
            Metadata.Entry<?> nameEntry = Metadata.OptChat(this.parent.createNameComponent(player));
            entries.put(2, nameEntry);
            player.sendPacket(new LazyPacket(() -> new EntityMetaDataPacket(getEntityId(), entries)));
            // Passengers are removed here as I don't need them

            // Head position
            player.sendPacket(new EntityHeadLookPacket(getEntityId(), this.position.yaw()));
        }
    }

    @Override
    public void setCustomName(@Nullable Component customName) {
        LOGGER.warn("setCustomName called for a LivingEnemyMob. This action is not supported");
    }

    @Override
    public @NotNull EntityType getTDEntityType() {
        return EntityType.SKELETON;
    }
}
