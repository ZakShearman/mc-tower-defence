package pink.zak.minestom.towerdefence.lobby.starting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.agones.AgonesManager;
import pink.zak.minestom.towerdefence.agones.GameCreationInfo;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for starting the game when there are enough players.
 * <p>
 * It is necessary to remember how the matchmaker works.
 * We wait for the expected amount of players for a certain amount of time.
 * If we hit the expected amount of players, we start the game. If the time expires, we start the game if there are
 * enough players.
 */
public class ProdLobbyStartingManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProdLobbyStartingManager.class);

    private static final int MAX_TIME = 20; // seconds
    private static final int MIN_PLAYERS = 2;

    private final AtomicInteger playerCount = new AtomicInteger();
    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicInteger timeLeft = new AtomicInteger(MAX_TIME);
    private final LobbyManager lobbyManager;
    private final TowerDefenceModule module;
    private Task countdownTask;

    // TODO start timer when allocated instead of when first player joins
    public ProdLobbyStartingManager(@NotNull LobbyManager lobbyManager, @NotNull TowerDefenceModule module,
                                    @NotNull AgonesManager agonesManager) {

        this.lobbyManager = lobbyManager;
        this.module = module;

        EventNode<Event> eventNode = EventNode.all("lobby-starter");
        lobbyManager.getEventNode().addChild(eventNode);

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            if (!event.isFirstSpawn()) return;
            this.lock.lock();

            int playerCount = this.playerCount.incrementAndGet();

            System.out.println("Player joined " + event.getPlayer().getUsername());

            Player player = event.getPlayer();

            if (agonesManager.getGameCreationInfo().isEmpty()) {
                player.kick(Component.text("Unexpected join (game not found)", NamedTextColor.RED));
                LOGGER.warn("Unexpected join, game not found, playerId: {}, username: {}", player.getUuid(), player.getUsername());
                return;
            }

            GameCreationInfo gameCreationInfo = agonesManager.getGameCreationInfo().get();
            if (!gameCreationInfo.playerIds().contains(player.getUuid())) {
                player.kick(Component.text("Unexpected join (not in game)", NamedTextColor.RED));
                LOGGER.warn("Unexpected join, player not in game, playerId: {}, username: {}", player.getUuid(), player.getUsername());
                return;
            }

            // Note that we run both playerCount == gameCount and playerCount == 1 because in testing there may only be one player.
            if (playerCount == gameCreationInfo.playerIds().size()) {
                this.startGameIfPossible(); // TODO maybe we don't start here and give players time to change team.
            }

            // Null check in case a player joins, leaves then another joins.
            if (playerCount == 1 && this.countdownTask == null) {
                this.startCountdown();
            }

            this.lock.unlock();
        });
    }

    private void startCountdown() {
        this.countdownTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
                    int timeLeft = this.timeLeft.decrementAndGet();
                    float progress = timeLeft / (float) MAX_TIME;

                    if (timeLeft == 0) {
                        this.startGameIfPossible();
                    }

                    for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        player.setLevel(timeLeft);
                        player.setExp(progress);
                    }
                })
                .repeat(1, ChronoUnit.SECONDS)
                .schedule();
    }

    private void startGameIfPossible() {
        if (this.countdownTask != null) {
            this.countdownTask.cancel();
            this.countdownTask = null;
        }

        if (this.playerCount.get() < MIN_PLAYERS) {
            this.cancelGame();
            return;
        }

        if (!this.areTeamsBalanced()) {
            Audiences.all().sendMessage(Component.text("The teams are not balanced so the game will not start.", NamedTextColor.RED)
                    .append(Component.newline())
                    .append(Component.text("Please change your team to ensure teams are balanced.", NamedTextColor.RED)));

            this.timeLeft.set(10);
            this.startCountdown();
            return;
        }

        this.startGame();
    }

    private boolean areTeamsBalanced() {
        int blueSize = this.lobbyManager.getTeamSize(Team.BLUE).get();
        int redSize = this.lobbyManager.getTeamSize(Team.RED).get();
        int differential = Math.abs(blueSize - redSize);

        if (blueSize + redSize <= 6) return differential <= 1; // Max differential of 1 for 6 or less players
        return differential <= 2; // Max differential of 2 for 7 or more players
    }

    private void startGame() {
        this.module.getGameHandler().start();
        this.destroy();

        Audiences.all().sendMessage(Component.text("Game starting!", NamedTextColor.GREEN));
    }

    /**
     * Cancels the game and sends players back to the lobby
     */
    private void cancelGame() {
        Audiences.all().sendMessage(Component.text("Not enough players to start the game.", NamedTextColor.RED)
                .append(Component.newline())
                .append(Component.text("You will be returned to the lobby.", NamedTextColor.RED)));

        // todo send players back to lobby
    }

    private void destroy() {
        this.lobbyManager.destroy();
        if (this.countdownTask != null)
            this.countdownTask.cancel();
    }
}