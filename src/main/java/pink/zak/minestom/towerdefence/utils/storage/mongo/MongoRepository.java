package pink.zak.minestom.towerdefence.utils.storage.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.utils.storage.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@ApiStatus.OverrideOnly
public abstract class MongoRepository<ID, T> implements MongoConverter<T>, Repository<ID, T> {
    private static final ReplaceOptions UPSERT_OPTION = new ReplaceOptions().upsert(true);
    private static List<String> CACHED_COLLECTIONS;

    private final String collectionName;

    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> collection;


    protected MongoRepository(MongoClient mongoClient, String database, String collectionName) {
        this.collectionName = collectionName;

        this.mongoDatabase = mongoClient.getDatabase(database);
        this.collection = this.getCollectionOrCreateIfNotExists();
    }

    private MongoCollection<Document> getCollectionOrCreateIfNotExists() {
        if (CACHED_COLLECTIONS == null)
            this.generateCachedCollections();

        MongoCollection<Document> collection;
        if (!CACHED_COLLECTIONS.contains(this.collectionName)) {
            this.mongoDatabase.createCollection(this.collectionName);
        }
        collection = this.mongoDatabase.getCollection(this.collectionName);
        return collection;
    }

    private void generateCachedCollections() {
        CACHED_COLLECTIONS = this.mongoDatabase.listCollectionNames().into(new ArrayList<>());
    }

    public <S extends T> @NotNull S insert(S entity) {
        this.collection.insertOne(this.serialize(entity));
        return entity;
    }

    @Override
    public <S extends T> @NotNull S save(@NotNull ID id, @NotNull S entity) {
        this.collection.replaceOne(Filters.eq("_id", id), this.serialize(entity), UPSERT_OPTION);
        return entity;
    }

    @Override
    public @NotNull <S extends T> Iterable<S> saveAll(@NotNull Iterable<ID> ids, @NotNull Iterable<S> entities) {
        List<ReplaceOneModel<Document>> updates = new ArrayList<>();
        Iterator<ID> idIterable = ids.iterator();
        Iterator<S> entityIterable = entities.iterator();
        while (idIterable.hasNext()) {
            ID id = idIterable.next();
            S entity = entityIterable.next();
            updates.add(new ReplaceOneModel<>(Filters.eq("_id", id), this.serialize(entity), UPSERT_OPTION));
        }
        this.collection.bulkWrite(updates);
        return entities;
    }

    @Override
    public @NotNull Optional<T> findById(@NotNull ID id) {
        Document result = this.collection.find(Filters.eq("_id", id)).first();
        if (result == null)
            return Optional.empty();

        return Optional.of(this.deserialize(result));
    }

    @Override
    public @NotNull Iterable<T> findAll() {
        FindIterable<Document> queryResults = this.collection.find();
        List<T> results = new ArrayList<>();

        for (Document document : queryResults) {
            results.add(this.deserialize(document));
        }

        return results;
    }

    @Override
    public boolean existsById(@NotNull ID id) {
        return this.collection.find(Filters.eq("_id", id)).first() != null;
    }

    @Override
    public long count() {
        Document result = this.mongoDatabase.runCommand(new Document("collStats", this.collectionName));
        return result.getLong("count");
    }

    @Override
    public void deleteById(@NotNull ID id) {
        this.collection.findOneAndDelete(Filters.eq("_id", id));
    }

    @Override
    public void deleteAllById(@NotNull Iterable<? extends ID> ids) {
        List<DeleteOneModel<Document>> updates = new ArrayList<>();
        for (ID id : ids) {
            updates.add(new DeleteOneModel<>(Filters.eq("_id", id)));
        }
        this.collection.bulkWrite(updates);
    }

    @Override
    public void deleteAll() {
        this.collection.deleteMany(MongoUtils.EMPTY_DOCUMENT);
    }
}
