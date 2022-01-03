package pink.zak.minestom.towerdefence.storage.dynamic.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.TDUser;
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
        return new TDUser(uuid);
    }

    @Override
    protected JsonElement serialize(TDUser entity) {
        JsonObject json = new JsonObject();
        json.addProperty("id", entity.getUuid().toString());
        return json;
    }
}
