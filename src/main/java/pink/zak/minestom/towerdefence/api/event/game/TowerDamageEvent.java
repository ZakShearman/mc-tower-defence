package pink.zak.minestom.towerdefence.api.event.game;

import net.minestom.server.event.Event;
import pink.zak.minestom.towerdefence.enums.Team;

public class TowerDamageEvent implements Event {
    private final Team team;
    private final int damage;
    private final int health;

    public TowerDamageEvent(Team team, int damage, int health) {
        this.team = team;
        this.damage = damage;
        this.health = health;
    }

    public Team getTeam() {
        return this.team;
    }

    public int getDamage() {
        return this.damage;
    }

    public int getHealth() {
        return this.health;
    }
}
