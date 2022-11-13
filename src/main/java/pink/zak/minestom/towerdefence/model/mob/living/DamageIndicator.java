package pink.zak.minestom.towerdefence.model.mob.living;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;

import java.time.Duration;

public class DamageIndicator extends Entity {
    private static final float OFFSET_Y = -0.5f;
    private static final Cache<Double, Component> NAME_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(Duration.of(30, TimeUnit.SECOND))
            .build();

    private final Vec[] vectors = MobHandler.DAMAGE_INDICATOR_CACHE.getPreCalculatedVelocity();

    private DamageIndicator(Instance instance, Pos spawnPosition, Component text) {
        super(EntityType.AREA_EFFECT_CLOUD);

        AreaEffectCloudMeta meta = (AreaEffectCloudMeta) this.getEntityMeta();
        meta.setRadius(0f);
        meta.setSilent(true);
        meta.setCustomName(text);
        meta.setCustomNameVisible(true);

        meta.setHasNoGravity(true);
        meta.setNotifyAboutChanges(false);
        this.setAutoViewable(false);

        this.setInstance(instance, spawnPosition.add(0, OFFSET_Y, 0));
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            TDPlayer tdPlayer = (TDPlayer) player;
            if (tdPlayer.isDamageIndicators() &&  tdPlayer.getDistance(this) < 20) // distance check probably isn't necessary but save some packets
                this.addViewer(tdPlayer);
        }

        this.position = spawnPosition;
    }

    public static void create(LivingEnemyMob enemyMob, double damage) {
        Component text = NAME_CACHE.get(damage, key -> Component.text(damage, NamedTextColor.RED));
        new DamageIndicator(enemyMob.getInstance(), enemyMob.getPosition().add(0, enemyMob.getEntityType().height(), 0), text);
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        int aliveTicks = (int) this.getAliveTicks();
        if (aliveTicks == 16) {
            this.remove();
            return;
        }
        this.velocity = this.vectors[aliveTicks];

        this.position = this.position.add(this.velocity.div(20));
    }
}
