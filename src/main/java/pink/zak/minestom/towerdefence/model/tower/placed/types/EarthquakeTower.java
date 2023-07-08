package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.BlockActionPacket;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StunnedStatusEffect;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.EarthquakeTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.List;

public class EarthquakeTower extends PlacedAttackingTower<EarthquakeTowerLevel> {

    public EarthquakeTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);
    }

    @Override
    protected void fire() {
        this.doFireAnimation();

        List<LivingTDEnemyMob> targets = this.getTargets();
        for (LivingTDEnemyMob target : targets) {
            target.damage(this, this.level.getDamage());
        }

        this.launchTargets(targets);
    }

    private void launchTargets(@NotNull List<LivingTDEnemyMob> targets) {
        for (LivingTDEnemyMob target : targets) {
            // Create stunned effect to stop target from moving
            new StunnedStatusEffect(this.level.getStunTicks(), target);
        }
    }

    private void doFireAnimation() {
        this.setShulkerBoxState(true);
        MinecraftServer.getSchedulerManager().buildTask(() -> this.setShulkerBoxState(false))
                .delay(this.level.getShulkerAnimationTicks(), TimeUnit.SERVER_TICK).schedule();
    }

    @Override
    public int getMaxTargets() {
        return 500;
    }

    private void setShulkerBoxState(boolean open) {
        byte playerCount = (byte) (open ? 1 : 0);

        for (RelativePoint relativePoint : this.level.getRelativeShulkerBoxes()) {
            Point point = relativePoint.apply(this.basePoint);
            BlockActionPacket packet = new BlockActionPacket(point, (byte) 1, playerCount, Block.SHULKER_BOX);
            PacketUtils.sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), packet);
        }
    }
}
