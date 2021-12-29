package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;

import java.util.ArrayList;
import java.util.List;

public abstract class PlacedAttackingTower<T extends AttackingTowerLevel> extends PlacedTower<T> {
    protected List<LivingEnemyMob> targets = new ArrayList<>();
    protected Task attackTask;

    protected PlacedAttackingTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point baseBlock, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, level);
        this.startFiring();
    }

    private void startFiring() {
        this.attackTask = MinecraftServer.getSchedulerManager()
            .buildTask(() -> {
                if (!this.targets.isEmpty())
                    this.fire();
            })
            .repeat(this.level.getFireDelay(), TimeUnit.CLIENT_TICK)
            .schedule();
    }

    protected abstract void fire();

    public abstract int getMaxTargets();

    @Override
    public void upgrade() {
        AttackingTowerLevel oldLevel = this.level;
        super.upgrade();
        if (oldLevel.getFireDelay() != this.level.getFireDelay()) {
            this.attackTask.cancel();
            this.startFiring();
        }
    }

    public List<LivingEnemyMob> getTargets() {
        return this.targets;
    }

    public void setTargets(List<LivingEnemyMob> targets) {
        this.targets = targets;
    }
}
