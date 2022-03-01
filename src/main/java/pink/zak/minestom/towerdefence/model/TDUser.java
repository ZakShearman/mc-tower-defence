package pink.zak.minestom.towerdefence.model;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.model.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.settings.ParticleThickness;

import java.util.UUID;

public class TDUser {
    private final @NotNull UUID uuid;
    private Player player;

    private @NotNull HealthDisplayMode healthMode = HealthDisplayMode.PERCENTAGE;
    private @NotNull ParticleThickness particleThickness = ParticleThickness.STANDARD;
    private @NotNull FlySpeed flySpeed = FlySpeed.NORMAL;
    private boolean damageIndicators = true;

    public TDUser(@NotNull UUID uuid, @NotNull HealthDisplayMode healthMode, @NotNull ParticleThickness particleThickness, @NotNull FlySpeed flySpeed, boolean damageIndicators) {
        this.uuid = uuid;
        this.healthMode = healthMode;
        this.particleThickness = particleThickness;
        this.flySpeed = flySpeed;
        this.damageIndicators = damageIndicators;
    }

    public TDUser(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    public @Nullable Player getPlayer() {
        if (this.player == null)
            this.player = MinecraftServer.getConnectionManager().getPlayer(this.uuid);

        return this.player;
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

    public ParticleThickness getParticleThickness() {
        return this.particleThickness;
    }

    public void setParticleThickness(ParticleThickness particleThickness) {
        this.particleThickness = particleThickness;
    }

    public boolean isDamageIndicators() {
        return this.damageIndicators;
    }

    public void setDamageIndicators(boolean damageIndicators) {
        this.damageIndicators = damageIndicators;
    }
}
