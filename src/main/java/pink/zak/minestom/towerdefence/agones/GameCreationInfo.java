package pink.zak.minestom.towerdefence.agones;

import dev.emortal.api.model.matchmaker.Match;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/*
 * The map ID, game mode ID, and player IDs would usually come from the matchmaker, when running in the standard environment.
 * The allocation time comes from the implementation (the game SDK).
 */
public record GameCreationInfo(@NotNull Match match, @NotNull Set<UUID> playerIds) {

    public @NotNull String id() {
        return this.match.getId();
    }

    public @Nullable String mapId() {
        if (!this.match.hasMapId()) return null;
        return this.match.getMapId();
    }

    public @NotNull String gameModeId() {
        return this.match.getGameModeId();
    }
}
