package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.mob.living.types.BeeLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.types.LlamaLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.types.ZombieLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.types.skeleton.SkeletonLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.modifier.SpeedModifier;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffect;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.NecromancerTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.NecromancerTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.util.Map;
import java.util.Set;

public interface LivingTDEnemyMob extends LivingTDMob {

    static LivingTDEnemyMob create(GameHandler gameHandler, EnemyMob enemyMob, int level, Instance instance, TowerMap map, GameUser gameUser) {
        EntityType entityType = enemyMob.getLevel(level).getEntityType();

        return switch (entityType.name()) {
            case "minecraft:llama" -> new LlamaLivingEnemyMob(gameHandler, enemyMob, instance, map, gameUser, level);
            case "minecraft:bee" -> new BeeLivingEnemyMob(gameHandler, enemyMob, instance, map, gameUser, level);
            case "minecraft:zombie" -> new ZombieLivingEnemyMob(gameHandler, enemyMob, instance, map, gameUser, level);
            case "minecraft:skeleton" -> SkeletonLivingEnemyMob.create(gameHandler, enemyMob, level, instance, map, gameUser);
            default -> new SingleEnemyTDMob(gameHandler, enemyMob, level, instance, map, gameUser);
        };
    }

    @Override
    default @NotNull Component createNameComponent(@NotNull Player player) {
        TDPlayer tdPlayer = (TDPlayer) player;
        String health = tdPlayer.getHealthMode().resolve(this);
        TextComponent.Builder builder = Component.text()
                .append(Component.text(this.getEnemyMob().getCommonName() + " " + StringUtils.integerToCardinal(this.getLevel()), NamedTextColor.DARK_GREEN))
                .append(Component.text(" | ", NamedTextColor.GREEN))
                .append(Component.text(health, NamedTextColor.DARK_GREEN))
                .style(Style.style(TextDecoration.BOLD));

        // add status effect icons
        if (!this.getStatusEffects().isEmpty()) {
            builder.append(Component.text(" | ", NamedTextColor.GREEN));
            for (StatusEffect<?> statusEffect : this.getStatusEffects().values())
                builder.append(statusEffect.getIcon());
        }
        return builder.build();
    }

    @NotNull Team getTDTeam();

    @NotNull EnemyMob getEnemyMob();

    @Override
    default float getMaxHealth() {
        return this.getEnemyMobLevel().getHealth();
    };

    @NotNull EnemyMobLevel getEnemyMobLevel();

    @Override
    default int getLevel() {
        return this.getEnemyMobLevel().getLevel();
    };

    double getTotalDistanceMoved();

    @Nullable Task getAttackTask();

    @NotNull Set<PlacedAttackingTower<?>> getAttackingTowers();

    @NotNull MobHandler getMobHandler();

    float damage(DamageSource damageSource, float damage);

    default void remove() {
        if (this.getAttackTask() != null)
            this.getAttackTask().cancel();

        for (PlacedAttackingTower<?> tower : this.getAttackingTowers())
            tower.getTargets().remove(this);

        if (this.getTDTeam() == Team.RED)
            this.getMobHandler().getRedSideMobs().remove(this);
        else
            this.getMobHandler().getBlueSideMobs().remove(this);

    }

    // status effects
    @NotNull Map<StatusEffectType, StatusEffect<?>> getStatusEffects();

    default void applyStatusEffect(@NotNull StatusEffect<?> statusEffect) {
        if (this.getEnemyMob().isEffectIgnored(statusEffect.type()))
            throw new IllegalArgumentException("Tried to apply ignored status effect to mob (%s)".formatted(statusEffect.type()));

        StatusEffect<?> removedEffect = this.getStatusEffects().put(statusEffect.type(), statusEffect);
        if (removedEffect != null) {
            removedEffect.remove();
        }

        this.updateCustomName();
    }

    default void removeStatusEffect(@NotNull StatusEffect<?> statusEffect) {
        if (this.getStatusEffects().remove(statusEffect.type(), statusEffect)) this.updateCustomName();
    }

    // modifiers

    @NotNull Set<SpeedModifier> getSpeedModifiers();

    default void applySpeedModifier(@NotNull SpeedModifier speedModifier) {
        this.getSpeedModifiers().add(speedModifier);
    }

    default void removeSpeedModifier(@NotNull SpeedModifier speedModifier) {
        this.getSpeedModifiers().remove(speedModifier);
    }

    default @NotNull NecromancerTower.NecromancedMob necromancedVersion(NecromancerTower tower, NecromancerTowerLevel towerLevel, GameUser towerOwner) {
        return new NecromancerTower.NecromancedMob(tower, towerLevel, this, towerOwner);
    }
}
