package pink.zak.minestom.towerdefence.storage.dynamic.repository.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.TDUser;
import pink.zak.minestom.towerdefence.model.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.utils.storage.json.JsonRepository;

import java.nio.file.Path;
import java.util.UUID;

public class JsonUserRepository extends JsonRepository<UUID, TDUser> {

    public JsonUserRepository(Path folder) {
        super(folder);
    }

    @Override
    public String getIdAsString(UUID uuid) {
        return uuid.toString();
    }

    @Override
    protected TDUser deserialize(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        UUID uuid = UUID.fromString(json.get("id").getAsString());
        HealthDisplayMode healthMode = HealthDisplayMode.valueOf(json.get("healthMode").getAsString());
        boolean damageIndicators = json.get("damageIndicators").getAsBoolean();
        boolean thinParticles = json.get("thinParticles").getAsBoolean();
        FlySpeed flySpeed = FlySpeed.valueOf(json.get("flySpeed").getAsString());
        return new TDUser(uuid, healthMode, damageIndicators, thinParticles, flySpeed);
    }

    @Override
    protected JsonElement serialize(TDUser user) {
        JsonObject json = new JsonObject();
        json.addProperty("id", user.getUuid().toString());
        json.addProperty("healthMode", user.getHealthMode().toString());
        json.addProperty("damageIndicators", user.isDamageIndicators());
        json.addProperty("thinParticles", user.isThinParticles());
        json.addProperty("flySpeed", user.getFlySpeed().toString());
        return json;
    }
}
