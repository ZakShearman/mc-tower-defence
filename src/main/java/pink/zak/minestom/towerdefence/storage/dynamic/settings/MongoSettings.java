package pink.zak.minestom.towerdefence.storage.dynamic.settings;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.typesafe.config.Config;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Collections;

public record MongoSettings(String address,
                            int port,
                            String username,
                            String database,
                            String authDb,
                            char[] password) {

    public static MongoSettings parse(Config globalConfig) {
        Config config = globalConfig.getConfig("mongo");
        return new MongoSettings(
            config.getString("address"),
            config.getInt("port"),
            config.getString("database"),
            globalConfig.hasPath("auth.username") ? globalConfig.getString("auth.username") : null,
            globalConfig.hasPath("auth.db") ? globalConfig.getString("auth.db") : null,
            globalConfig.hasPath("auth.password") ? globalConfig.getString("auth.password").toCharArray() : null
        );
    }

    public MongoClientSettings asClientSettings() {
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD);

        ServerAddress serverAddress = new ServerAddress(this.address, this.port);
        builder.applyToClusterSettings(clusterBuilder -> clusterBuilder.hosts(Collections.singletonList(serverAddress))); // todo is there a better way to do this?

        if (this.authDb != null && !this.authDb.isEmpty()) {
            MongoCredential credentials = MongoCredential.createCredential(this.username, this.authDb, this.password);
            builder.credential(credentials);
        }
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        builder.codecRegistry(codecRegistry);

        return builder.build();
    }
}
