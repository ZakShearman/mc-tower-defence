package pink.zak.minestom.towerdefence.actionbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
import pink.zak.minestom.towerdefence.utils.StringUtils;

public class ActionBarHandler {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final String ACTION_BAR_CONTENT = "<gold>⛃ <coins> (+<income>/6s) <gray>| <team_color>❤ <castle_health>";

    private final @NotNull GameHandler gameHandler;

    public ActionBarHandler(@NotNull GameHandler gameHandler, @NotNull EventNode<Event> eventNode) {
        this.gameHandler = gameHandler;

        eventNode.addListener(PlayerCoinChangeEvent.class, event -> event.gameUser()
                        .getPlayer().sendActionBar(this.createActionBar(event.gameUser())))

                .addListener(PlayerIncomeChangeEvent.class, event -> event.gameUser()
                        .getPlayer().sendActionBar(this.createActionBar(event.gameUser())))

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

        return MINI_MESSAGE.deserialize(ACTION_BAR_CONTENT,
                Placeholder.unparsed("coins", StringUtils.commaSeparateNumber(user.getCoins())),
                Placeholder.unparsed("income", StringUtils.commaSeparateNumber(user.getIncomeRate())),
                Placeholder.parsed("team_color", "<%s>".formatted(user.getTeam().getMiniMessageColor())),
                Placeholder.unparsed("castle_health", StringUtils.commaSeparateNumber(castleHealth))
        );
    }
}
