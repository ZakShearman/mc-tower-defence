package pink.zak.minestom.towerdefence.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TDUser {
    private final @NotNull UUID uuid;

    public TDUser(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public @NotNull UUID getUuid() {
        return this.uuid;
    }
}
