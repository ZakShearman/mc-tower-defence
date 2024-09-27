package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.BurningDamageEffect;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.ScorcherTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.List;

public class ScorcherTower extends PlacedAttackingTower<ScorcherTowerLevel> {

    public ScorcherTower(@NotNull GameHandler gameHandler, AttackingTower tower, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(gameHandler, tower, id, owner, basePoint, facing, level);
    }

    @Override
    protected boolean attemptToFire() {
        int tickDuration = this.level.getTickDuration();
        float damage = this.level.getDamage();

        List<LivingTDEnemyMob> targets = this.findPossibleTargets().stream()
                // filter out targets that are immune to being frozen
                .filter(target -> !target.getEnemyMob().isEffectIgnored(StatusEffectType.BURNING))
                .toList();

        for (LivingTDEnemyMob target : targets) {
            BurningDamageEffect currentEffect = (BurningDamageEffect) target.getStatusEffects().get(StatusEffectType.BURNING);

            // if it: A) has no effect, B) current effect is worse than this one, or C) current effect is the same but has less time left
            if (currentEffect == null
                    || currentEffect.getDamage() < damage
                    || (currentEffect.getDamage() == damage && currentEffect.getRemainingTicks() < tickDuration)) {
                BurningDamageEffect effect = new BurningDamageEffect(target, this.owner, tickDuration, damage, 20);
                target.applyStatusEffect(effect);
            }
        }

        return !targets.isEmpty();
    }
}
