package pink.zak.minestom.towerdefence.model.mob.living;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.cache.TDUserCache;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.user.TDUser;

import java.time.Duration;

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

        this.setInstance(instance, spawnPosition.add(0, OFFSET_Y, 0));
        for (TDUser user : userCache.getAllLoadedUsers())
            if (user.isDamageIndicators() && user.getPlayer() != null && user.getPlayer().getDistance(this) < 10) // distance check probably isnt necessary but save some packets
                this.addViewer(user.getPlayer());

        this.position = spawnPosition;
    }

    @Override
    protected void velocityTick() {
        int aliveTicks = (int) this.getAliveTicks();
        if (aliveTicks == 16) {
            this.remove();
            return;
        }
        this.velocity = this.vectors[aliveTicks];

        Pos newPosition = this.position.add(this.velocity.div(20));

        this.refreshPosition(newPosition, true);
        this.sendPacketToViewers(this.getVelocityPacket());
    }

    public static void create(TDUserCache userCache, LivingEnemyMob enemyMob, double damage) {
        Component text = NAME_CACHE.get(damage, key -> Component.text(damage, NamedTextColor.RED));
        new DamageIndicator(userCache, enemyMob.getInstance(), enemyMob.getPosition().add(0, enemyMob.getEntityType().height(), 0), text);
    }
}
