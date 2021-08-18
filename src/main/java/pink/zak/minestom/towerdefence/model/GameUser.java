package pink.zak.minestom.towerdefence.model;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import pink.zak.minestom.towerdefence.enums.Team;

import java.util.concurrent.atomic.AtomicInteger;

public class GameUser {
    private final Player player;
    private final Team team;
    private final AtomicInteger coins = new AtomicInteger(10000);

    private Point lastClickedTowerBlock;

    public GameUser(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    public AtomicInteger getCoins() {
        return this.coins;
    }

    public Point getLastClickedTowerBlock() {
        return this.lastClickedTowerBlock;
    }

    public void setLastClickedTowerBlock(Point lastClickedTowerBlock) {
        this.lastClickedTowerBlock = lastClickedTowerBlock;
    }
}
