package pink.zak.minestom.towerdefence.storage.dynamic.repository.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.TDStatistic;
import pink.zak.minestom.towerdefence.model.user.TDUser;
import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;
import pink.zak.minestom.towerdefence.utils.storage.json.JsonRepository;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
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
    protected JsonElement serialize(@NotNull TDUser user) {
        JsonObject json = new JsonObject();
        json.addProperty("id", user.getUuid().toString());
        json.addProperty("healthMode", user.getHealthMode().toString());
        json.addProperty("particleThickness", user.getParticleThickness().toString());
        json.addProperty("damageIndicators", user.isDamageIndicators());
        json.addProperty("flySpeed", user.getFlySpeed().toString());

        JsonObject statistics = new JsonObject();
        for (Map.Entry<TDStatistic, Long> entry : user.getStatistics().entrySet()) {
            statistics.addProperty(entry.getKey().toString(), entry.getValue());
        }
        json.add("statistics", statistics);

        return json;
    }

    @Override
    protected TDUser deserialize(@NotNull JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        UUID uuid = UUID.fromString(json.get("id").getAsString());
        HealthDisplayMode healthMode = HealthDisplayMode.valueOf(json.get("healthMode").getAsString());
        ParticleThickness particleThickness = ParticleThickness.valueOf(json.get("particleThickness").getAsString());
        FlySpeed flySpeed = FlySpeed.valueOf(json.get("flySpeed").getAsString());
        boolean damageIndicators = json.get("damageIndicators").getAsBoolean();

        Map<TDStatistic, Long> statistics = Collections.synchronizedMap(new EnumMap<>(TDStatistic.class));
        JsonObject statisticsJson = json.get("statistics").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : statisticsJson.entrySet()) {
            statistics.put(TDStatistic.valueOf(entry.getKey()), entry.getValue().getAsLong());
        }

        return new TDUser(uuid, statistics, healthMode, particleThickness, flySpeed, damageIndicators);
    }
}
