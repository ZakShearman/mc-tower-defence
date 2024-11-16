package pink.zak.minestom.towerdefence.actionbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.api.event.game.CastleDamageEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerIncomeChangeEvent;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class ActionBarHandler {
    private final @NotNull GameHandler gameHandler;

    public ActionBarHandler(@NotNull GameHandler gameHandler, @NotNull EventNode<Event> eventNode) {
        this.gameHandler = gameHandler;

        eventNode.addListener(PlayerCoinChangeEvent.class, event -> event.user()
                        .getPlayer().sendActionBar(this.createActionBar(event.user())))

                .addListener(PlayerIncomeChangeEvent.class, event -> event.user()
                        .getPlayer().sendActionBar(this.createActionBar(event.user())))

                .addListener(CastleDamageEvent.class, event -> {
                    this.gameHandler.getUsers().values().stream()
                            .filter(user -> user.getTeam() == event.team())
                            .forEach(user -> user.getPlayer().sendActionBar(this.createActionBar(user)));
                });

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            this.gameHandler.getUsers().values().forEach(user -> user.getPlayer().sendActionBar(this.createActionBar(user)));
        }).repeat(TaskSchedule.tick(10)).schedule();
    }

    private @NotNull Component createActionBar(@NotNull GameUser user) {
        int castleHealth = this.gameHandler.getCastleHealth(user.getTeam());

        return Component.text()
                .append(Component.text("⛃ ", NamedTextColor.GOLD))
                .append(Component.text(user.getCoins(), NamedTextColor.GOLD))
                .append(Component.text(" (", NamedTextColor.GOLD))
                .append(Component.text(user.getIncomeRate(), NamedTextColor.GOLD))
                .append(Component.text("/6s) ", NamedTextColor.GOLD))
                .append(Component.text("| ", NamedTextColor.GRAY))
                .append(Component.text("❤ ", user.getTeam().getColor()))
                .append(Component.text(castleHealth, user.getTeam().getColor()))
                .build();
    }
}
