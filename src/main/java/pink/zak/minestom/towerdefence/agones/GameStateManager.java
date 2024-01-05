package pink.zak.minestom.towerdefence.agones;

import dev.emortal.api.message.gamesdk.GameReadyMessage;
import dev.emortal.api.message.matchmaker.MatchCreatedMessage;
import dev.emortal.api.model.matchmaker.Match;
import dev.emortal.api.model.matchmaker.Ticket;
import dev.emortal.api.utils.kafka.FriendlyKafkaProducer;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the overall state of the server - when it gets allocated, when it finishes, etc...
 */
public class GameStateManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameStateManager.class);
    private final FriendlyKafkaProducer kafkaProducer;

    private GameCreationInfo gameCreationInfo;

    public GameStateManager(@NotNull MessagingModule messaging) {
        messaging.addListener(MatchCreatedMessage.class, message -> this.onMatchCreated(message.getMatch()));

        this.kafkaProducer = messaging.getKafkaProducer();
    }

    private void onMatchCreated(@NotNull Match match) {
        if (!this.isGameForThisServer(match)) return;

        this.gameCreationInfo = this.createInfo(match);
        LOGGER.info("Match created for this server (id: {}), creating game...", this.gameCreationInfo.id());

        this.notifyGameReady(match);
    }

    private boolean isGameForThisServer(@NotNull Match match) {
        return match.getAssignment().getServerId().equals(Environment.getHostname());
    }

    private @NotNull GameCreationInfo createInfo(@NotNull Match match) {
        Set<UUID> playerIds = new HashSet<>();

        for (Ticket ticket : match.getTicketsList()) {
            for (String playerId : ticket.getPlayerIdsList()) {
                playerIds.add(UUID.fromString(playerId));
            }
        }

        return new GameCreationInfo(match, playerIds);
    }

    private void notifyGameReady(@NotNull Match match) {
        this.kafkaProducer.produceAndForget(GameReadyMessage.newBuilder().setMatch(match).build());
    }

    public @UnknownNullability GameCreationInfo getGameCreationInfo() {
        return gameCreationInfo;
    }
}
