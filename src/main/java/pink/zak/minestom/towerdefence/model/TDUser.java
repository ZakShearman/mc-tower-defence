package pink.zak.minestom.towerdefence.model;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.model.settings.HealthDisplayMode;

import java.util.UUID;

public class TDUser {
    private final @NotNull UUID uuid;
    private Player player;
    private @NotNull HealthDisplayMode healthMode = HealthDisplayMode.PERCENTAGE;
    private boolean damageIndicators = true;
    private float flySpeed = 0.05f;

    public TDUser(@NotNull UUID uuid, @NotNull HealthDisplayMode healthMode, boolean damageIndicators, float flySpeed) {
        this.uuid = uuid;
        this.healthMode = healthMode;
        this.damageIndicators = damageIndicators;
        this.flySpeed = flySpeed;
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

    public boolean isDamageIndicators() {
        return this.damageIndicators;
    }

    public void setDamageIndicators(boolean damageIndicators) {
        this.damageIndicators = damageIndicators;
    }

    public float getFlySpeed() {
        return this.flySpeed;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }
}
