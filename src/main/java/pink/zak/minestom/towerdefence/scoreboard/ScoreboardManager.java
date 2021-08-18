package pink.zak.minestom.towerdefence.scoreboard;

import com.google.common.collect.Maps;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.scoreboard.types.LobbyScoreboard;

import java.util.Map;

public class ScoreboardManager {
    private final Map<TowerScoreboard.Type, TowerScoreboard> scoreboardMap = Maps.newEnumMap(TowerScoreboard.Type.class);

    public ScoreboardManager(TowerDefencePlugin plugin) {
        this.scoreboardMap.put(TowerScoreboard.Type.LOBBY, new LobbyScoreboard(plugin));

        // remove player from scoreboards on disconnect
        plugin.getEventNode().addListener(PlayerDisconnectEvent.class, event -> {
            for (TowerScoreboard scoreboard : this.scoreboardMap.values()) {
                if (scoreboard.removeViewer(event.getPlayer()))
                    break;
            }
        });
    }
}
