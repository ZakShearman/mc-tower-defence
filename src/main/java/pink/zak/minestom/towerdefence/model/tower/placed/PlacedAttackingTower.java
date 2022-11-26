package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.TDDamageType;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.ArrayList;
import java.util.List;

public abstract class PlacedAttackingTower<T extends AttackingTowerLevel> extends PlacedTower<T> implements DamageSource {
    protected List<LivingTDEnemyMob> targets = new ArrayList<>();
    protected Task attackTask;

    protected PlacedAttackingTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);
        this.startFiring();
    }

    private void startFiring() {
        this.attackTask = MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    if (!this.targets.isEmpty())
                        this.fire();
                })
                .repeat(this.level.getFireDelay(), TimeUnit.SERVER_TICK)
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

    @Override
    public void destroy() {
        this.attackTask.cancel();
        super.destroy();
    }

    public List<LivingTDEnemyMob> getTargets() {
        return this.targets;
    }

    public void setTargets(List<LivingTDEnemyMob> targets) {
        this.targets = targets;
    }

    @Override
    public @NotNull GameUser getOwningUser() {
        return super.owner;
    }

    @Override
    public @NotNull TDDamageType getDamageType() {
        return ((AttackingTower) this.tower).getDamageType();
    }
}
