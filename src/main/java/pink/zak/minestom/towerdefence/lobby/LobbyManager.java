package pink.zak.minestom.towerdefence.lobby;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.agones.AgonesManager;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTeamSwitchEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.lobby.starting.DevLobbyStartingManager;
import pink.zak.minestom.towerdefence.lobby.starting.ProdLobbyStartingManager;
import pink.zak.minestom.towerdefence.model.user.LobbyPlayer;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyManager {
    private static final GlobalEventHandler GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler();

    private static final Boolean DEV_MODE = Boolean.valueOf(System.getenv("DEV_MODE"));

    private final @NotNull EventNode<Event> eventNode = EventNode.all("lobby-manager");

    private final Map<Team, AtomicInteger> teamPlayerCount = new EnumMap<>(Team.class);
    private final Map<TDPlayer, LobbyPlayer> lobbyPlayers = new WeakHashMap<>();

    private final TowerDefenceModule module;

    public LobbyManager(TowerDefenceModule module, AgonesManager agonesManager) {
        this.teamPlayerCount.put(Team.RED, new AtomicInteger(0));
        this.teamPlayerCount.put(Team.BLUE, new AtomicInteger(0));
        this.module = module;

        module.getEventNode().addChild(this.eventNode);

        this.eventNode.addListener(PlayerSpawnEvent.class, event -> {
            TDPlayer player = (TDPlayer) event.getPlayer();

            LobbyPlayer lobbyPlayer = new LobbyPlayer(player, this.pickTeam(player));
            player.setDisplayName(Component.text(player.getUsername(), lobbyPlayer.getTeam().getColor()));

            this.lobbyPlayers.put(player, lobbyPlayer);
        }).addListener(PlayerDisconnectEvent.class, event -> {
            TDPlayer player = (TDPlayer) event.getPlayer();
            LobbyPlayer lobbyPlayer = this.lobbyPlayers.remove(player);
            if (lobbyPlayer != null) {
                this.teamPlayerCount.get(lobbyPlayer.getTeam()).decrementAndGet();
                GLOBAL_EVENT_HANDLER.call(new PlayerTeamSwitchEvent(null, lobbyPlayer.getTeam(), player));
            }
        });

        new SpawnItemHandler(this);
        if (DEV_MODE) {
            new DevLobbyStartingManager(this, module);
        } else {
            new ProdLobbyStartingManager(this, module, agonesManager);
        }
    }

    private synchronized @NotNull Team pickTeam(TDPlayer player) {
        if (this.teamPlayerCount.get(Team.RED).get() < this.teamPlayerCount.get(Team.BLUE).get()) {
            this.teamPlayerCount.get(Team.RED).incrementAndGet();
            GLOBAL_EVENT_HANDLER.call(new PlayerTeamSwitchEvent(Team.RED, null, player));
            return Team.RED;
        } else {
            this.teamPlayerCount.get(Team.BLUE).incrementAndGet();
            GLOBAL_EVENT_HANDLER.call(new PlayerTeamSwitchEvent(Team.BLUE, null, player));
            return Team.BLUE;
        }
    }

    public void destroy() {
        this.module.getEventNode().removeChild(this.eventNode);
    }

    public LobbyPlayer getLobbyPlayer(TDPlayer player) {
        return this.lobbyPlayers.get(player);
    }

    public Set<LobbyPlayer> getLobbyPlayers() {
        return Set.copyOf(this.lobbyPlayers.values());
    }

    public void setPlayerTeam(@NotNull LobbyPlayer player, @NotNull Team team) {
        Team oldTeam = player.getTeam();
        if (oldTeam == team) return;

        this.teamPlayerCount.get(oldTeam).decrementAndGet();
        this.teamPlayerCount.get(team).incrementAndGet();
        player.setTeam(team);
    }

    public AtomicInteger getTeamSize(@NotNull Team team) {
        return this.teamPlayerCount.get(team);
    }

    public @NotNull EventNode<Event> getEventNode() {
        return this.eventNode;
    }
}
