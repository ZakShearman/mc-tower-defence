package pink.zak.minestom.towerdefence.storage.dynamic;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.model.user.TDUser;
import pink.zak.minestom.towerdefence.storage.dynamic.repository.user.JsonUserRepository;
import pink.zak.minestom.towerdefence.storage.dynamic.repository.user.MongoUserRepository;
import pink.zak.minestom.towerdefence.utils.FileUtils;
import pink.zak.minestom.towerdefence.utils.storage.Repository;
import pink.zak.minestom.towerdefence.utils.storage.mongo.MongoSettings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class RepositoryCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryCreator.class);

    private final TowerDefencePlugin plugin;
    private final RepositoryType repositoryType;

    private Path jsonPath;

    private MongoClient mongoClient;
    private String mongoDatabase;

    public RepositoryCreator(TowerDefencePlugin plugin) {
        this.plugin = plugin;

        Path configPath = plugin.getDataDirectory().resolve("storage.conf");
        File configFile = configPath.toFile();
        if (Files.notExists(configPath))
            FileUtils.saveResource(configPath, "storage.conf");
        Config config = ConfigFactory.parseFile(configFile);

        this.repositoryType = config.getEnum(RepositoryType.class, "repository");

        if (!this.checkEligibility(config)) {
            LOGGER.error("Eligibility failed to use repository type {}", this.repositoryType);
            System.exit(1);
        } else
            LOGGER.info("Started storage using repository type {}", this.repositoryType);
    }

    // todo rest api
    private boolean checkEligibility(Config config) {
        switch (this.repositoryType) {
            case JSON -> this.jsonPath = this.plugin.getDataDirectory().resolve("data");
            case MONGO_DB -> {
                if (!config.hasPath("mongodb"))
                    return false;

                MongoSettings mongoSettings = MongoSettings.parse(config.getConfig("mongodb"));
                this.mongoDatabase = mongoSettings.database();
                this.mongoClient = MongoClients.create(mongoSettings.asClientSettings());
            }
            default -> {
            }
        }
        return true;
    }

    public Repository<UUID, TDUser> createUserRepository() {
        return switch (this.repositoryType) {
            case JSON -> new JsonUserRepository(this.jsonPath.resolve("users"));
            case MONGO_DB -> new MongoUserRepository(this.mongoClient, this.mongoDatabase);
            default -> null;
        };
    }
}
