//package pink.zak.minestom.towerdefence.storage.dynamic.repository.user;
//
//import com.mongodb.client.MongoClient;
//import org.bson.Document;
//import org.jetbrains.annotations.NotNull;
//import pink.zak.minestom.towerdefence.model.user.TDPlayer;
//import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
//import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
//import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;
//import pink.zak.minestom.towerdefence.utils.storage.mongo.MongoRepository;
//
//import java.util.UUID;
//
//public class MongoUserRepository extends MongoRepository<UUID, TDPlayer> {
//
//    public MongoUserRepository(MongoClient mongoClient, String database) {
//        super(mongoClient, database, "users");
//    }
//
//    @Override
//    public @NotNull Document serialize(@NotNull TDPlayer user) {
//        Document document = new Document();
//
//        document.append("_id", user.getUuid())
//                .append("healthMode", user.getHealthMode().toString())
//                .append("particleThickness", user.getParticleThickness().toString())
//                .append("damageIndicators", user.isDamageIndicators())
//                .append("flySpeed", user.getFlySpeed().toString());
//
//        return document;
//    }
//
//    @Override
//    public @NotNull TDPlayer deserialize(@NotNull Document document) {
//        UUID uuid = document.get("_id", UUID.class);
//        HealthDisplayMode healthMode = HealthDisplayMode.valueOf(document.getString("healthMode"));
//        ParticleThickness particleThickness = ParticleThickness.valueOf(document.getString("particleThickness"));
//        boolean damageIndicators = document.getBoolean("damageIndicators");
//        FlySpeed flySpeed = FlySpeed.valueOf(document.getString("flySpeed"));
//
//        return new TDPlayer(uuid, healthMode, particleThickness, flySpeed, damageIndicators);
//    }
//}
