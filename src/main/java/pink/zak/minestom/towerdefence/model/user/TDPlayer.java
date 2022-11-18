package pink.zak.minestom.towerdefence.model.user;

import net.minestom.server.entity.Player;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;

import java.util.UUID;

public class TDPlayer extends Player {
    private @NotNull HealthDisplayMode healthMode = HealthDisplayMode.RAW;
    private @NotNull ParticleThickness particleThickness = ParticleThickness.THIN;
    private @NotNull FlySpeed flySpeed = FlySpeed.NORMAL;
    private boolean damageIndicators = true;

    public TDPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection connection,
                    @NotNull HealthDisplayMode healthMode, @NotNull ParticleThickness particleThickness,
                    @NotNull FlySpeed flySpeed, boolean damageIndicators) {
        super(uuid, username, connection);
        this.healthMode = healthMode;
        this.particleThickness = particleThickness;
        this.flySpeed = flySpeed;
        this.damageIndicators = damageIndicators;
    }

    public TDPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection connection) {
        super(uuid, username, connection);
        this.uuid = uuid;
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
