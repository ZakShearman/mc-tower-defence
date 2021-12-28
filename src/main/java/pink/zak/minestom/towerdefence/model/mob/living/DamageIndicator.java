package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.game.MobHandler;

import java.util.List;

public class DamageIndicator extends Entity {
    private static final float OFFSET_Y = -0.5f;

    private final List<Vec> vectors = MobHandler.DAMAGE_INDICATOR_CACHE.getPreCalculatedVelocity();

    private DamageIndicator(Instance instance, Pos spawnPosition, Component text) {
        super(EntityType.AREA_EFFECT_CLOUD);

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) this.getEntityMeta();
        meta.setRadius(0f);
        meta.setNotifyAboutChanges(false);
        meta.setCustomName(text);
        meta.setCustomNameVisible(true);

        this.setInstance(instance, spawnPosition.add(0, OFFSET_Y, 0));

        this.position = spawnPosition;

        this.scheduleRemove(15, TimeUnit.CLIENT_TICK);
    }

    @Override
    protected void velocityTick() {
        this.velocity = this.vectors.get((int) this.getAliveTicks());

        Pos newPosition = this.position.add(this.velocity.div(20));

        this.refreshPosition(newPosition, true);
        this.sendPacketToViewers(this.getVelocityPacket());
    }

    public static void create(LivingEnemyMob enemyMob, double damage) {
        Component text = Component.text(damage, NamedTextColor.RED);

        new DamageIndicator(enemyMob.getInstance(), enemyMob.getPosition().add(0, enemyMob.getEntityType().height(), 0), text);
    }
}
