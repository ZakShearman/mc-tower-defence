package pink.zak.minestom.towerdefence.agones;

import dev.emortal.api.kurushimi.AllocationData;
import dev.emortal.api.kurushimi.Match;
import dev.emortal.api.kurushimi.Ticket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record GameCreationInfo(@NotNull Instant allocationTime, // Provided by implementation, below is provided by allocation data
                               @Nullable String mapId, @NotNull String gameModeId,
                               @NotNull Set<UUID> playerIds, @NotNull AllocationData rawData) {

    public static @NotNull GameCreationInfo fromAllocationData(@NotNull Instant allocationTime, @NotNull AllocationData data) {
        Match match = data.getMatch();

        Set<UUID> playerIds = new HashSet<>();
        for (Ticket ticket : match.getTicketsList()) {
            for (String playerId : ticket.getPlayerIdsList()) {
                playerIds.add(UUID.fromString(playerId));
            }
        }

        return new GameCreationInfo(
                allocationTime,
                match.hasMapId() ? match.getMapId() : null,
                match.getGameModeId(),
                playerIds,
                data
        );
    }
}