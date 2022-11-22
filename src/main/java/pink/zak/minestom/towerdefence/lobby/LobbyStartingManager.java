package pink.zak.minestom.towerdefence.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.timer.Task;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.Team;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyStartingManager {
    private static final EventNode<Event> EVENT_NODE = EventNode.all("lobby-starter");
    private static final int MAX_TIME = 45; // seconds

    private final AtomicInteger timeLeft = new AtomicInteger();
    private final LobbyManager lobbyManager;
    private final TowerDefenceModule module;
    private Task countdownTask;

    public LobbyStartingManager(LobbyManager lobbyManager, TowerDefenceModule module) {
        LobbyManager.getEventNode().addChild(EVENT_NODE);
        this.lobbyManager = lobbyManager;
        this.module = module;

        EVENT_NODE.addListener(PlayerSpawnEvent.class, event -> {
            int playerCount = MinecraftServer.getConnectionManager().getOnlinePlayers().size();

            if (playerCount >= 2) {
                boolean startingTimer = this.countdownTask == null;

                int newMinTime = this.skipTimeSeconds(playerCount);
                if (startingTimer)
                    this.timeLeft.set(newMinTime);
                else
                    this.timeLeft.updateAndGet(current -> Math.min(current, newMinTime));

                if (startingTimer) this.startCountdown();
            }
        }).addListener(PlayerDisconnectEvent.class, event -> {
            int playerCount = MinecraftServer.getConnectionManager().getOnlinePlayers().size();

            if (playerCount < 2) {
                if (this.countdownTask != null) {
                    this.countdownTask.cancel();
                    this.countdownTask = null;
                }
                this.timeLeft.set(0);

                for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    player.setLevel(0);
                    player.setExp(0);
                }
            }
        });
    }

    private void startCountdown() {
        this.countdownTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
                    int timeLeft = this.timeLeft.decrementAndGet();
                    float progress = timeLeft / (float) MAX_TIME;

                    if (timeLeft <= 0) {
                        this.countdownTask.cancel();
                        this.countdownTask = null;

                        if (this.areTeamsBalanced()) {
                            Audiences.all().sendMessage(Component.text("Game starting!", NamedTextColor.GREEN));
                            this.module.getGameHandler().start();
                            this.destroy();
                        } else {
                            Audiences.all().sendMessage(Component.text("The teams are not balanced so the game will not start.", NamedTextColor.RED)
                                    .append(Component.newline())
                                    .append(Component.text("Please wait for more players or balance the teams yourself.", NamedTextColor.RED)));

                            this.timeLeft.set(this.skipTimeSeconds(MinecraftServer.getConnectionManager().getOnlinePlayers().size()));
                            this.startCountdown();
                        }
                    }

                    for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        player.setLevel(timeLeft);
                        player.setExp(progress);
                    }
                })
                .repeat(1, ChronoUnit.SECONDS)
                .schedule();
    }

    private void destroy() {
        this.lobbyManager.destroy();
        this.countdownTask.cancel();
    }

    private boolean areTeamsBalanced() {
        int blueSize = this.lobbyManager.getTeamSize(Team.BLUE).get();
        int redSize = this.lobbyManager.getTeamSize(Team.RED).get();
        int differential = Math.abs(blueSize - redSize);

        if (blueSize + redSize <= 6) return differential < 2;
        return differential < 3;
    }

    private int skipTimeSeconds(int playerCount) {
        if (playerCount < 2) throw new IllegalStateException("skipTimeSeconds should not be called for < 2 players");
        if (playerCount < 4) return 45;
        if (playerCount < 6) return 30;
        return 15;
    }
}
