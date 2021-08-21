package pink.zak.minestom.towerdefence.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.ItemStackBuilder;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PlayerHeadMeta;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemUtils {

    public static ItemStack fromJsonObject(JsonObject jsonObject) {
        Material material = Material.fromNamespaceId(jsonObject.get("material").getAsString());
        ItemStackBuilder builder = ItemStack.builder(material);

        if (material == Material.PLAYER_HEAD) {
            PlayerHeadMeta.Builder metaBuilder = new PlayerHeadMeta.Builder();
            if (jsonObject.has("owner-uuid"))
                metaBuilder.playerSkin(PlayerSkin.fromUuid(jsonObject.get("owner-uuid").getAsString()));
            else if (jsonObject.has("owner-username"))
                metaBuilder.playerSkin(PlayerSkin.fromUsername(jsonObject.get("owner-username").getAsString()));
            else if (jsonObject.has("texture"))
                metaBuilder.playerSkin(new PlayerSkin(jsonObject.get("texture").getAsString(), "")); //todo do
        }

        if (jsonObject.has("displayName"))
            builder.displayName(StringUtils.parseMessage(jsonObject.get("displayName").getAsString()));

        if (jsonObject.has("lore"))
            builder.lore(StringUtils.parseMessages(
                StreamSupport.stream(jsonObject.get("lore").getAsJsonArray().spliterator(), false)
                    .map(JsonElement::getAsString)
                    .collect(Collectors.toList())
            ));

        if (jsonObject.has("amount"))
            builder.amount(jsonObject.get("amount").getAsInt());

        return builder.build();
    }
}
