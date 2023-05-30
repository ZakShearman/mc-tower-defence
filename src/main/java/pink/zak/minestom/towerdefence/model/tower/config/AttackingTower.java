package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;

import java.util.Map;

public class AttackingTower extends Tower {

    public AttackingTower(JsonObject jsonObject, Map<Integer, JsonObject> levelJson) {
        super(jsonObject, levelJson);
    }
}
