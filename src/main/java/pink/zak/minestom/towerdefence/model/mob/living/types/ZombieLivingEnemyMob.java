package pink.zak.minestom.towerdefence.model.mob.living.types;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class ZombieLivingEnemyMob extends LivingEnemyMob {

    public ZombieLivingEnemyMob(GameHandler gameHandler, EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(gameHandler, enemyMob, instance, map, gameUser, level, true);

        ItemStack chestplateItem = ItemStack.of(switch (super.getLevel().getLevel()) {
            case 1 -> Material.LEATHER_CHESTPLATE;
            case 2 -> Material.CHAINMAIL_CHESTPLATE;
            case 3 -> Material.GOLDEN_CHESTPLATE;
            case 4 -> Material.IRON_CHESTPLATE;
            case 5 -> Material.DIAMOND_CHESTPLATE;
            default -> Material.AIR;
        });

        ItemStack heldItem = ItemStack.of(switch (super.getLevel().getLevel()) {
            case 1 -> Material.WOODEN_SWORD;
            case 2 -> Material.STONE_SWORD;
            case 3 -> Material.GOLDEN_SWORD;
            case 4 -> Material.IRON_SWORD;
            case 5 -> Material.DIAMOND_SWORD;
            default -> Material.AIR;
        });

        this.setEquipment(EquipmentSlot.CHESTPLATE, chestplateItem);
        this.setEquipment(EquipmentSlot.MAIN_HAND, heldItem);
    }
}
