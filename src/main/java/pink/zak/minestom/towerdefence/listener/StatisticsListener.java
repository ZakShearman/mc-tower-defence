package pink.zak.minestom.towerdefence.listener;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import pink.zak.minestom.towerdefence.api.event.game.GameEndEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerManaChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTowerPlaceEvent;
import pink.zak.minestom.towerdefence.api.event.tower.TowerDamageMobEvent;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDStatistic;
import pink.zak.minestom.towerdefence.model.user.TDUser;

public class StatisticsListener {

    public StatisticsListener(EventNode<Event> eventNode) {
        eventNode.addListener(PlayerTowerPlaceEvent.class, event -> {
                    event.gameUser().getUser().getStatistic(TDStatistic.TOWERS_PLACED).incrementAndGet();
                })
                .addListener(TowerDamageMobEvent.class, event -> {
                    event.source().getOwningUser().getUser().getStatistic(TDStatistic.DAMAGE_DEALT)
                            .updateAndGet(current -> (long) (current + event.damage()));
                })
                // todo death .addListener()
                .addListener(PlayerCoinChangeEvent.class, event -> {
                    if (event.coins() > 0)
                        event.gameUser().getUser().getStatistic(TDStatistic.MONEY_EARNED)
                                .addAndGet(event.coins());
                })
                .addListener(PlayerManaChangeEvent.class, event -> {
                    if (event.mana() > 0)
                        event.gameUser().getUser().getStatistic(TDStatistic.MANA_EARNED)
                                .addAndGet(event.mana());
                })
                .addListener(GameEndEvent.class, event -> {
                    for (GameUser gameUser : event.users()) {
                        TDUser tdUser = gameUser.getUser();
                        tdUser.getStatistic(TDStatistic.GAMES_PLAYED).incrementAndGet();
                        if (event.winningTeam() == gameUser.getTeam())
                            tdUser.getStatistic(TDStatistic.GAMES_WON).incrementAndGet();
                    }
                    event.users().stream().map(GameUser::getUser).forEach(user -> {
                    });
                });
    }
}
