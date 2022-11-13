package pink.zak.minestom.towerdefence.scoreboard;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.scoreboard.types.GameScoreboardManager;
import pink.zak.minestom.towerdefence.scoreboard.types.LobbyScoreboard;

import java.util.EnumMap;
import java.util.Map;

public class ScoreboardManager {
    private final Map<TowerScoreboard.Type, TowerScoreboard> scoreboardMap = new EnumMap<>(TowerScoreboard.Type.class);
    private final TowerDefenceModule plugin;

    private final GameScoreboardManager gameScoreboardManager = new GameScoreboardManager();
    private final LobbyScoreboard lobbyScoreboard;

    public ScoreboardManager(TowerDefenceModule plugin, LobbyManager lobbyManager) {
        this.plugin = plugin;

        this.lobbyScoreboard = new LobbyScoreboard(plugin, lobbyManager);
        this.scoreboardMap.put(TowerScoreboard.Type.LOBBY, this.lobbyScoreboard);
        this.scoreboardMap.put(TowerScoreboard.Type.IN_GAME, this.gameScoreboardManager);

        // remove player from scoreboards on disconnect
        plugin.getEventNode().addListener(PlayerDisconnectEvent.class, event -> {
            for (TowerScoreboard scoreboard : this.scoreboardMap.values()) {
                if (scoreboard.removeViewer(event.getPlayer()))
                    break;
            }
        });
    }

    public void startGame() {
        for (GameUser gameUser : this.plugin.getGameHandler().getUsers().values()) {
            this.lobbyScoreboard.removeViewer(gameUser.getPlayer());
            this.gameScoreboardManager.createScoreboard(this.plugin, gameUser);
        }
    }
}
