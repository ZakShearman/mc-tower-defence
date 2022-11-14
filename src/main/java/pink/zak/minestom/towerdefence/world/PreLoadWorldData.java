package pink.zak.minestom.towerdefence.world;

import com.google.gson.*;

import java.lang.reflect.Type;

public record PreLoadWorldData(int minX, int maxX, int minZ, int maxZ) {

    public static class GsonConverter implements JsonSerializer<PreLoadWorldData>, JsonDeserializer<PreLoadWorldData> {
        public static GsonConverter INSTANCE = new GsonConverter();

        @Override
        public PreLoadWorldData deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject json = jsonElement.getAsJsonObject();

            return new PreLoadWorldData(json.get("minX").getAsInt(), json.get("maxX").getAsInt(), json.get("minZ").getAsInt(), json.get("maxZ").getAsInt());
        }

        @Override
        public JsonElement serialize(PreLoadWorldData data, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("minX", data.minX());
            json.addProperty("maxX", data.maxX());
            json.addProperty("minZ", data.minZ());
            json.addProperty("maxZ", data.maxZ());

            return json;
        }
    }
}