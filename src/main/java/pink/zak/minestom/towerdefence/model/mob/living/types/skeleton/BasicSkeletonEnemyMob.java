package pink.zak.minestom.towerdefence.model.mob.living.types.skeleton;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.SingleEnemyTDMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class BasicSkeletonEnemyMob extends SingleEnemyTDMob {

    public BasicSkeletonEnemyMob(@NotNull GameHandler gameHandler, @NotNull EnemyMob enemyMob, int level, @NotNull Instance instance, @NotNull TowerMap map, @NotNull GameUser gameUser) {
        super(gameHandler, enemyMob, level, instance, map, gameUser);

        this.setChestplate(ItemStack.of(Material.LEATHER_CHESTPLATE));
        this.setItemInHand(Player.Hand.MAIN, ItemStack.of(Material.BOW));
    }
}
