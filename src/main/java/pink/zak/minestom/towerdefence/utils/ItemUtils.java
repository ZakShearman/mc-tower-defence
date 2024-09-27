package pink.zak.minestom.towerdefence.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.HeadProfile;
import net.minestom.server.utils.mojang.MojangUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemUtils {

    public static ItemStack fromJsonObject(JsonObject jsonObject, @Nullable TagResolver tagResolver) {
        Material material = Material.fromNamespaceId(jsonObject.get("material").getAsString());
        ItemStack.Builder builder = ItemStack.builder(material);

        if (material == Material.PLAYER_HEAD) {
            HeadProfile profile = null;

            if (jsonObject.has("owner-uuid")) {
                String uuid = jsonObject.get("owner-uuid").getAsString();
                profile = new HeadProfile(null, UUID.fromString(uuid), List.of());
            } else if (jsonObject.has("owner-username")) {
                String username = MojangUtils.fromUsername(jsonObject.get("owner-username").getAsString()).get("id").getAsString();
                profile = new HeadProfile(username, null, List.of());
            } else if (jsonObject.has("texture")) {
                profile = new HeadProfile(new PlayerSkin(jsonObject.get("texture").getAsString(), null));
            }

            if (profile != null) builder.set(ItemComponent.PROFILE, profile);
        }

        if (jsonObject.has("displayName"))
            builder.set(ItemComponent.CUSTOM_NAME, StringUtils.parseMessage(jsonObject.get("displayName").getAsString(), tagResolver));

        if (jsonObject.has("lore"))
            builder.lore(StringUtils.parseMessages(
                    StreamSupport.stream(jsonObject.get("lore").getAsJsonArray().spliterator(), false)
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toList()),
                    tagResolver
            ));

        if (jsonObject.has("amount"))
            builder.amount(jsonObject.get("amount").getAsInt());

        return builder.build();
    }
}
