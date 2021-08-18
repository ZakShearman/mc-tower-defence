package pink.zak.minestom.towerdefence.enums;

import net.minestom.server.entity.EntityType;

public enum EnemyMobType {

    SLIME(15, 40, 1, 0.5, false, 7, EntityType.SLIME),
    MAGMA_CUBE(15, 40, 1, 0.5, false, 7, EntityType.MAGMA_CUBE),
    VILLAGER(15, 40, 1, 0.5, false, 7, EntityType.VILLAGER),
    //SHULKER(15, 40, 1, 0.5, false, 7, EntityType.SHULKER_BULLET), // great for testing the pathing, trust me :)
    ZOMBIE(15, 40, 1, 0.5, false, 7, EntityType.ZOMBIE),
    SPIDER(25, 80, 1, 0.5, false, 15, EntityType.SPIDER),
    SKELETON(25, 80, 1, 0.5, false, 15, EntityType.SKELETON),
    CREEPER(25, 80, 1, 0.5, false, 15, EntityType.CREEPER),
    DROWNED(25, 80, 1, 0.5, false, 15, EntityType.DROWNED),
    ENDERMAN(25, 80, 1, 0.5, false, 15, EntityType.ENDERMAN),
    PIGLIN(25, 80, 1, 0.5, false, 15, EntityType.PIGLIN),
    PIGLIN_BRUTE(25, 80, 1, 0.5, false, 15, EntityType.PIGLIN_BRUTE),
    ZOMBIFIED_PIGLIN(25, 80, 1, 0.5, false, 15, EntityType.ZOMBIFIED_PIGLIN),
    HOGLIN(25, 80, 1, 0.5, false, 15, EntityType.HOGLIN); // big buff thing

    private final int cost;
    private final int health;
    private final int damage;
    private final double speed;
    private final boolean flying;
    private final int xpReward;
    private final EntityType entityType;

    EnemyMobType(int cost, int health, int damage, double speed, boolean flying, int xpReward, EntityType entityType) {
        this.cost = cost;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.flying = flying;
        this.xpReward = xpReward;
        this.entityType = entityType;
    }

    public int getCost() {
        return this.cost;
    }

    public int getHealth() {
        return this.health;
    }

    public int getDamage() {
        return this.damage;
    }

    public double getSpeed() {
        return this.speed;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public int getXpReward() {
        return this.xpReward;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }
}
