package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.AbstractHorseMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDMob;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffect;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.utils.StringUtils;

public class SkeletonLivingEnemyMob extends LivingEnemyMob {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkeletonLivingEnemyMob.class);

    private final SkeletonMob skeletonMob;

    public SkeletonLivingEnemyMob(GameHandler gameHandler, EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(gameHandler,
                level == 5 ? EntityType.ZOMBIE_HORSE : level > 1 ? EntityType.SKELETON_HORSE : EntityType.SKELETON,
                enemyMob, instance, map, gameUser, level, level == 1
        );

        ItemStack chestplateItem = ItemStack.of(switch (level) {
            case 1 -> Material.LEATHER_CHESTPLATE;
            case 2 -> Material.CHAINMAIL_CHESTPLATE;
            case 3 -> Material.GOLDEN_CHESTPLATE;
            case 4 -> Material.IRON_CHESTPLATE;
            case 5 -> Material.DIAMOND_CHESTPLATE;
            default -> Material.AIR;
        });
        ItemStack heldItem = ItemStack.of(Material.BOW);

        if (level == 1) {
            this.setEquipment(EquipmentSlot.CHESTPLATE, chestplateItem);
            this.setEquipment(EquipmentSlot.MAIN_HAND, heldItem);

            this.skeletonMob = null;
        } else if (level > 1) {
            AbstractHorseMeta horseMeta = (AbstractHorseMeta) this.getEntityMeta();

            this.skeletonMob = new SkeletonMob(this);
            this.skeletonMob.setEquipment(EquipmentSlot.CHESTPLATE, chestplateItem);
            this.skeletonMob.setEquipment(EquipmentSlot.MAIN_HAND, heldItem);

            this.addPassenger(this.skeletonMob);
        } else {
            this.skeletonMob = null;
        }
    }

    @Override
    public void updateCustomName() {
        super.updateCustomName();
        if (this.skeletonMob != null) {
            this.skeletonMob.updateCustomName();
        }
    }

    private class SkeletonMob extends LivingTDMob {
        private final SkeletonLivingEnemyMob parent;

        public SkeletonMob(SkeletonLivingEnemyMob parent) {
            super(EntityType.SKELETON, true);
            this.parent = parent;
        }

        @Override
        public Component createNameComponent(@NotNull Player player) {
            TDPlayer tdPlayer = (TDPlayer) player;
            String health = tdPlayer.getHealthMode().resolve(this.parent);
            TextComponent.Builder builder = Component.text()
                    .append(Component.text(StringUtils.namespaceToName(this.entityType.name()) + " " + StringUtils.integerToCardinal(this.parent.level.getLevel()), NamedTextColor.DARK_GREEN))
                    .append(Component.text(" | ", NamedTextColor.GREEN))
                    .append(Component.text(health, NamedTextColor.DARK_GREEN))
                    .style(Style.style(TextDecoration.BOLD));

            // add status effect icons
            if (!this.parent.statusEffects.isEmpty()) {
                builder.append(Component.text(" | ", NamedTextColor.GREEN));
                for (StatusEffect<?> statusEffect : this.parent.statusEffects.values())
                    builder.append(statusEffect.getIcon());
            }

            return builder.build();
        }
    }


    @Override
    public void setCustomName(@Nullable Component customName) {
        LOGGER.warn("setCustomName called for a LivingEnemyMob. This action is not supported");
    }
}
