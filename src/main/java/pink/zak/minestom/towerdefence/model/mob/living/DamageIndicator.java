package pink.zak.minestom.towerdefence.model.mob.living;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.cache.TDUserCache;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.TDUser;

import java.time.Duration;
import java.util.function.Predicate;

public class DamageIndicator extends Entity {
    private static final float OFFSET_Y = -0.5f;
    private static final Cache<Double, Component> NAME_CACHE = Caffeine.newBuilder()
        .expireAfterAccess(Duration.of(30, TimeUnit.SECOND))
        .build();

    private final Vec[] vectors = MobHandler.DAMAGE_INDICATOR_CACHE.getPreCalculatedVelocity();

    private DamageIndicator(TDUserCache userCache, Instance instance, Pos spawnPosition, Component text) {
        super(EntityType.AREA_EFFECT_CLOUD);

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) this.getEntityMeta();
        meta.setRadius(0f);
        meta.setNotifyAboutChanges(false);
        meta.setCustomName(text);
        meta.setCustomNameVisible(true);

        this.setAutoViewable(false);

        for (TDUser user : userCache.getAllUsers())
            if (user.isDamageIndicators() && user.getPlayer() != null)
                this.addViewer(user.getPlayer());

        this.setInstance(instance, spawnPosition.add(0, OFFSET_Y, 0));

        this.position = spawnPosition;

        this.scheduleRemove(15, TimeUnit.CLIENT_TICK);

    }

    @Override
    protected void velocityTick() {
        this.velocity = this.vectors[(int) this.getAliveTicks()];

        Pos newPosition = this.position.add(this.velocity.div(20));

        this.refreshPosition(newPosition, true);
        this.sendPacketToViewers(this.getVelocityPacket());
    }

    public static void create(TDUserCache userCache, LivingEnemyMob enemyMob, double damage) {
        Component text = NAME_CACHE.get(damage, key -> Component.text(damage, NamedTextColor.RED));
        new DamageIndicator(userCache, enemyMob.getInstance(), enemyMob.getPosition().add(0, enemyMob.getEntityType().height(), 0), text);
    }
}
