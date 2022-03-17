package pink.zak.minestom.towerdefence.storage.dynamic.repository.user;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.TDUser;
import pink.zak.minestom.towerdefence.model.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.settings.ParticleThickness;
import pink.zak.minestom.towerdefence.utils.storage.mongo.MongoRepository;

import java.util.UUID;

public class MongoUserRepository extends MongoRepository<UUID, TDUser> {

    public MongoUserRepository(MongoClient mongoClient, String database) {
        super(mongoClient, database, "users");
    }

    @Override
    public @NotNull Document serialize(@NotNull TDUser user) {
        Document document = new Document();

        document.put("_id", user.getUuid());
        document.put("healthMode", user.getHealthMode().toString());
        document.put("particleThickness", user.getParticleThickness().toString());
        document.put("damageIndicators", user.isDamageIndicators());
        document.put("flySpeed", user.getFlySpeed().toString());
        return document;
    }

    @Override
    public @NotNull TDUser deserialize(@NotNull Document document) {
        UUID uuid = document.get("_id", UUID.class);
        HealthDisplayMode healthMode = HealthDisplayMode.valueOf(document.getString("healthMode"));
        ParticleThickness particleThickness = ParticleThickness.valueOf(document.getString("particleThickness"));
        boolean damageIndicators = document.getBoolean("damageIndicators");
        FlySpeed flySpeed = FlySpeed.valueOf(document.getString("flySpeed"));
        return new TDUser(uuid, healthMode, particleThickness, flySpeed, damageIndicators);
    }
}
