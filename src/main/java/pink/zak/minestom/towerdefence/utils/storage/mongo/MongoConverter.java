package pink.zak.minestom.towerdefence.utils.storage.mongo;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public interface MongoConverter<T> {

    @NotNull Document serialize(@NotNull T entity);

    @NotNull T deserialize(@NotNull Document document);
}
