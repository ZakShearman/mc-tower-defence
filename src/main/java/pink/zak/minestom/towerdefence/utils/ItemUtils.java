package pink.zak.minestom.towerdefence.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PlayerHeadMeta;
import net.minestom.server.utils.mojang.MojangUtils;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemUtils {

    public static ItemStack fromJsonObject(JsonObject jsonObject) {
        Material material = Material.fromNamespaceId(jsonObject.get("material").getAsString());
        ItemStack.Builder builder = ItemStack.builder(material);

        if (material == Material.PLAYER_HEAD) {
            PlayerHeadMeta.Builder metaBuilder = new PlayerHeadMeta.Builder();

            if (jsonObject.has("owner-uuid")) {
                String uuid = jsonObject.get("owner-uuid").getAsString();
                metaBuilder.skullOwner(UUID.fromString(uuid));
                metaBuilder.playerSkin(PlayerSkin.fromUuid(uuid)); // is this necessary?
            } else if (jsonObject.has("owner-username")) {
                String uuid = MojangUtils.fromUsername(jsonObject.get("owner-username").getAsString()).get("id").getAsString();
                metaBuilder.skullOwner(UUID.fromString(uuid));
                metaBuilder.playerSkin(PlayerSkin.fromUuid(uuid)); // is this necessary?
            } else if (jsonObject.has("texture")) {
                metaBuilder.skullOwner(UUID.randomUUID());
                metaBuilder.playerSkin(new PlayerSkin(jsonObject.get("texture").getAsString(), null));
                builder.meta(metaBuilder.build());
            }
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

    public static ItemStack withMaterial(ItemStack itemStack, Material material) {
        return withMaterialBuilder(itemStack, material).build();
    }

    public static ItemStack.Builder withMaterialBuilder(ItemStack itemStack, Material material) {
        return ItemStack.builder(material)
                .displayName(itemStack.getDisplayName())
                .lore(itemStack.getLore())
                .amount(itemStack.amount())
                .meta(itemStack.meta());
    }
}
