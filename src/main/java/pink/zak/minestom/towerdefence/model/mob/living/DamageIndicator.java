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
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;

public class DamageIndicator extends Entity {
    private static final float OFFSET_Y = -0.5f;
    private static final Cache<Double, Component> NAME_CACHE = Caffeine.newBuilder()
            .maximumSize(10_000)
            .build();

    private static final int VIEW_DISTANCE = 10;
    private static final int VIEW_DISTANCE_SQUARED = VIEW_DISTANCE * VIEW_DISTANCE;

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
        this.updateViewableRule(player -> {
            TDPlayer tdPlayer = (TDPlayer) player;
            return tdPlayer.isDamageIndicators() && tdPlayer.getDistanceSquared(this) < VIEW_DISTANCE_SQUARED;
        });

        this.setInstance(instance, spawnPosition.add(0, OFFSET_Y, 0));

        this.position = spawnPosition;
    }

    /**
     * Creates a damage indicator for the given mob with the given damage
     * NOTE: A damage indicator will not be created if it will not be visible to any players.
     *
     * @param enemyMob The mob to create the damage indicator for
     * @param damage   The damage to display
     */
    public static void create(@NotNull LivingTDEnemyMob enemyMob, double damage) {
        Component text = NAME_CACHE.get(damage, key -> Component.text(damage, NamedTextColor.RED));

        boolean visible = false;
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (player.getPosition().distanceSquared(enemyMob.getPosition()) < VIEW_DISTANCE_SQUARED) {
                visible = true;
                break;
            }
        }
        if (visible)
            new DamageIndicator(enemyMob.getInstance(), enemyMob.getPosition().add(0, enemyMob.getTDEntityType().height(), 0), text);
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
