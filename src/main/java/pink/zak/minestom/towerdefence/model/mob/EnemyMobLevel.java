package pink.zak.minestom.towerdefence.model.mob;

import com.google.gson.JsonObject;

public record EnemyMobLevel(int level, int price, int health, int damage, double movementSpeed) {

    public static EnemyMobLevel fromJsonObject(JsonObject jsonObject) {
        int level = jsonObject.get("level").getAsInt();
        int price = jsonObject.get("price").getAsInt();
        int health = jsonObject.get("health").getAsInt();
        int damage = jsonObject.get("damage").getAsInt();
        double movementSpeed = jsonObject.get("movementSpeed").getAsDouble() / 20;

        return new EnemyMobLevel(level, price, health, damage, movementSpeed);
    }
}
