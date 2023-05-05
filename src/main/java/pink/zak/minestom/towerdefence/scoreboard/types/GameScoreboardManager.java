package pink.zak.minestom.towerdefence.scoreboard.types;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.scoreboard.TowerScoreboard;
import pink.zak.minestom.towerdefence.scoreboard.types.game.GameScoreboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameScoreboardManager implements TowerScoreboard {
    private final Map<UUID, GameScoreboard> scoreboards = new ConcurrentHashMap<>();

    public GameScoreboardManager() {

    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        GameScoreboard scoreboard = this.scoreboards.get(player.getUuid());
        if (scoreboard != null) {
            scoreboard.destroy();
            return true;
        }
        return false;
    }

    public void createScoreboard(TowerDefenceModule plugin, @NotNull GameUser gameUser) {
        this.scoreboards.put(gameUser.getPlayer().getUuid(), new GameScoreboard(plugin, gameUser));
    }
}
