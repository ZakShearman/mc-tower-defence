package pink.zak.minestom.towerdefence.scoreboard.types.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.api.event.game.CastleDamageEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.scoreboard.TowerScoreboard;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.text.DecimalFormat;

public class GameScoreboard {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.#");

    private final Sidebar sidebar = new Sidebar(TowerScoreboard.TITLE);
    private final GameUser gameUser;
    private final double maxHealth = 10_000;

    public GameScoreboard(TowerDefenceModule plugin, GameUser gameUser) {
        this.gameUser = gameUser;

        this.sidebar.createLine(new Sidebar.ScoreboardLine("empty-6", Component.empty(), 6));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("coins", this.createCoins(), 5));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("empty-4", Component.empty(), 4));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("red-health", this.createRedHealth(new CastleDamageEvent(Team.RED, 0, (int) this.maxHealth)), 3));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("blue-health", this.createBlueHealth(new CastleDamageEvent(Team.BLUE, 0, (int) this.maxHealth)), 2));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("empty-1", Component.empty(), 1));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("website", TowerScoreboard.DOMAIN, 0));

        this.sidebar.addViewer(this.gameUser.getPlayer());

        plugin.getEventNode()
                .addListener(CastleDamageEvent.class, event -> {
                    if (event.team() == Team.RED)
                        this.sidebar.updateLineContent("red-health", this.createRedHealth(event));
                    else
                        this.sidebar.updateLineContent("blue-health", this.createBlueHealth(event));
                })
                .addListener(PlayerCoinChangeEvent.class, event -> this.sidebar.updateLineContent("coins", this.createCoins()));
    }

    public void destroy() {
        this.sidebar.removeViewer(this.gameUser.getPlayer());
        // todo cleanup events
    }

    private Component createCoins() {
        return Component.text("Coins: ", NamedTextColor.WHITE)
                .append(Component.text(StringUtils.commaSeparateNumber(this.gameUser.getCoins()), NamedTextColor.YELLOW));
    }

    private @NotNull Component createRedHealth(@NotNull CastleDamageEvent event) {
        return Component.text("Health: " + DECIMAL_FORMAT.format((event.health() / this.maxHealth) * 100) + "%", NamedTextColor.RED);
    }

    private @NotNull Component createBlueHealth(@NotNull CastleDamageEvent event) {
        return Component.text("Health: " + DECIMAL_FORMAT.format((event.health() / this.maxHealth) * 100) + "%", NamedTextColor.AQUA);
    }
}
