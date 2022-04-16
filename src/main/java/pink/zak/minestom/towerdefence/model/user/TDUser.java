package pink.zak.minestom.towerdefence.model.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class TDUser {
    private final @NotNull UUID uuid;
    private WeakReference<Player> player;

    private final @NotNull Map<TDStatistic, Long> statistics;

    private @NotNull HealthDisplayMode healthMode = HealthDisplayMode.PERCENTAGE;
    private @NotNull ParticleThickness particleThickness = ParticleThickness.STANDARD;
    private @NotNull FlySpeed flySpeed = FlySpeed.NORMAL;
    private boolean damageIndicators = true;

    public TDUser(@NotNull UUID uuid, @NotNull Map<TDStatistic, Long> statistics, @NotNull HealthDisplayMode healthMode, @NotNull ParticleThickness particleThickness, @NotNull FlySpeed flySpeed, boolean damageIndicators) {
        this.uuid = uuid;
        this.statistics = statistics;
        this.healthMode = healthMode;
        this.particleThickness = particleThickness;
        this.flySpeed = flySpeed;
        this.damageIndicators = damageIndicators;
    }

    public TDUser(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.statistics = Collections.synchronizedMap(new EnumMap<>(TDStatistic.class));
    }

    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    public @Nullable Player getPlayer() {
        if (this.player == null)
            this.player = new WeakReference<>(MinecraftServer.getConnectionManager().getPlayer(this.uuid));

        return this.player.get();
    }

    public @NotNull Map<TDStatistic, Long> getStatistics() {
        return this.statistics;
    }

    public long getStatistic(@NotNull TDStatistic statistic) {
        return this.statistics.getOrDefault(statistic, 0L);
    }

    public @NotNull HealthDisplayMode getHealthMode() {
        return this.healthMode;
    }

    public void setHealthMode(@NotNull HealthDisplayMode healthMode) {
        this.healthMode = healthMode;
    }

    public @NotNull FlySpeed getFlySpeed() {
        return this.flySpeed;
    }

    public void setFlySpeed(@NotNull FlySpeed flySpeed) {
        this.flySpeed = flySpeed;
    }

    public @NotNull ParticleThickness getParticleThickness() {
        return this.particleThickness;
    }

    public void setParticleThickness(@NotNull ParticleThickness particleThickness) {
        this.particleThickness = particleThickness;
    }

    public boolean isDamageIndicators() {
        return this.damageIndicators;
    }

    public void setDamageIndicators(boolean damageIndicators) {
        this.damageIndicators = damageIndicators;
    }
}
