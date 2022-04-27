package pink.zak.minestom.towerdefence.storage.dynamic.repository.user;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.TDStatistic;
import pink.zak.minestom.towerdefence.model.user.TDUser;
import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;
import pink.zak.minestom.towerdefence.utils.storage.mongo.MongoRepository;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MongoUserRepository extends MongoRepository<UUID, TDUser> {

    public MongoUserRepository(MongoClient mongoClient, String database) {
        super(mongoClient, database, "users");
    }

    @Override
    public @NotNull Document serialize(@NotNull TDUser user) {
        Document document = new Document();

        document.append("_id", user.getUuid())
            .append("healthMode", user.getHealthMode().toString())
            .append("particleThickness", user.getParticleThickness().toString())
            .append("damageIndicators", user.isDamageIndicators())
            .append("flySpeed", user.getFlySpeed().toString());

        Document statistics = new Document();
        for (Map.Entry<TDStatistic, AtomicLong> entry : user.getStatistics().entrySet()) {
            statistics.append(entry.getKey().toString(), entry.getValue().longValue());
        }
        document.append("statistics", statistics);

        return document;
    }

    @Override
    public @NotNull TDUser deserialize(@NotNull Document document) {
        UUID uuid = document.get("_id", UUID.class);
        HealthDisplayMode healthMode = HealthDisplayMode.valueOf(document.getString("healthMode"));
        ParticleThickness particleThickness = ParticleThickness.valueOf(document.getString("particleThickness"));
        boolean damageIndicators = document.getBoolean("damageIndicators");
        FlySpeed flySpeed = FlySpeed.valueOf(document.getString("flySpeed"));

        Map<TDStatistic, AtomicLong> statistics = Collections.synchronizedMap(new EnumMap<>(TDStatistic.class));
        Document statisticsDocument = document.get("statistics", Document.class);
        for (Map.Entry<String, Object> entry : statisticsDocument.entrySet()) {
            statistics.put(TDStatistic.valueOf(entry.getKey()), new AtomicLong((Long) entry.getValue()));
        }

        return new TDUser(uuid, statistics, healthMode, particleThickness, flySpeed, damageIndicators);
    }
}
