package pink.zak.minestom.towerdefence.model.tower.placed.types;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import com.extollit.gaming.ai.path.SchedulingPriority;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ai.EntityAIGroupBuilder;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.PFPathingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.TDDamageType;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.SingleTDMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.NecromancerTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class NecromancerTower extends PlacedTower<NecromancerTowerLevel> {
    private final AtomicInteger necromancedMobCount = new AtomicInteger(0);

    public NecromancerTower(Instance instance, AttackingTower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, level);
    }

    public void createNecromancedMob(LivingTDEnemyMob livingEnemyMob) {
        if (this.necromancedMobCount.get() >= this.level.getMaxNecromancedMobs()) return;
        this.necromancedMobCount.incrementAndGet();

        NecromancedMob necromancedMob = livingEnemyMob.necromancedVersion(this, this.getLevel(), super.owner);
    }

    public static class NecromancedMob extends SingleTDMob implements DamageSource {
        private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
        private static final String CUSTOM_NAME = "<dark_purple><mob_type> <level> <light_purple>| <dark_purple><health>";

        private final @NotNull NecromancerTower originTower;
        private final @NotNull EnemyMob enemyMob;
        private final @NotNull EnemyMobLevel enemyMobLevel;
        private final @NotNull GameUser owner;

        public NecromancedMob(@NotNull NecromancerTower originTower, @NotNull NecromancerTowerLevel towerLevel,
                              @NotNull LivingTDEnemyMob originalMob, @NotNull GameUser owner) {

            super(originalMob.getTDEntityType(), originalMob.getLevel());

            this.originTower = originTower;
            this.enemyMob = originalMob.getEnemyMob();
            this.enemyMobLevel = originalMob.getEnemyMobLevel();
            this.owner = owner;

            this.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue((float) this.enemyMobLevel.getMovementSpeed() + 0.050f);
            this.getAttribute(Attribute.MAX_HEALTH).setBaseValue(towerLevel.getNecromancedHealth());

            Navigator navigator = this.getNavigator();
            PFPathingEntity pathingEntity = navigator.getPathingEntity();

            this.setNoGravity(originalMob.getEnemyMob().isFlying());
            pathingEntity.setAvian(this.hasNoGravity());

            this.setHealth(towerLevel.getNecromancedHealth());
            this.setCustomNameVisible(true);

            HydrazinePathFinder pathFinder = new HydrazinePathFinder(this.getNavigator().getPathingEntity(), originalMob.getInstance().getInstanceSpace());
            pathFinder.schedulingPriority(SchedulingPriority.high);

            navigator.setPathFinder(pathFinder);

            this.addAIGroup(
                    new EntityAIGroupBuilder()
                            .addGoalSelector(new MeleeAttackGoal(this, 1.2, 1000, TimeUnit.MILLISECOND))
                            .addTargetSelector(new ClosestEntityTarget(this, 15, entity -> {
                                return entity instanceof LivingTDEnemyMob livingEnemyMob && !livingEnemyMob.isDead();
                            }))
                            .build()
            );

            this.setInstance(originalMob.getInstance(), originalMob.getPosition());
        }

        @Override
        public void setHealth(float health) {
            super.setHealth(health);
            this.updateCustomName();
        }

        @Override
        public void kill() {
            this.originTower.necromancedMobCount.decrementAndGet();
            super.kill();
        }

        @Override
        public void tick(long time) {
            if (!this.isDead) {
                if (this.getAliveTicks() % 5 == 0) this.setHealth(this.getHealth() - 1);
            }

            super.tick(time);
        }

        @Override
        public @NotNull Component createNameComponent(@NotNull Player player) {
            TDPlayer tdPlayer = (TDPlayer) player;
            String health = tdPlayer.getHealthMode().resolve(this);

            return MINI_MESSAGE.deserialize(CUSTOM_NAME,
                    Placeholder.unparsed("mob_type", StringUtils.namespaceToName(this.entityType.name())),
                    Placeholder.unparsed("level", StringUtils.integerToCardinal(this.enemyMobLevel.getLevel())),
                    Placeholder.unparsed("health", health));
        }

        public @NotNull NecromancerTower getOriginTower() {
            return originTower;
        }

        public @NotNull EnemyMobLevel getEnemyMobLevel() {
            return enemyMobLevel;
        }

        @Override
        public @NotNull GameUser getOwningUser() {
            return this.owner;
        }

        @Override
        public @NotNull TDDamageType getDamageType() {
            return this.enemyMob.getDamageType();
        }
    }

}
