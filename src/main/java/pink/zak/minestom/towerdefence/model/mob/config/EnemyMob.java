package pink.zak.minestom.towerdefence.model.mob.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EnemyMob {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final String BASE_ITEM_DISPLAY_NAME = "<i:false><yellow><mob_name> <gold>(<red>Not Owned<gold>)";
    private static final String NOT_OWNED_LORE_LINE = "<i:false><u><gold>Level 1 stats:";

    private final @NotNull String commonName;
    private final int slot;
    private final boolean flying;
    private final int sendTime; // in milliseconds
    private final @NotNull ItemStack baseItem;
    private final @NotNull Set<StatusEffectType> ignoredEffects;
    private final @NotNull Map<Integer, EnemyMobLevel> levels;

    public EnemyMob(@NotNull JsonObject jsonObject) {
        this.commonName = jsonObject.get("commonName").getAsString();
        this.slot = jsonObject.get("guiSlot").getAsInt();
        this.flying = jsonObject.get("flying").getAsBoolean();
        this.sendTime = jsonObject.get("sendTime").getAsInt();

        this.ignoredEffects = StreamSupport.stream(jsonObject.get("ignoredEffects").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .map(StatusEffectType::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        this.levels = StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(json -> new EnemyMobLevel(this.commonName, json))
                .collect(Collectors.toUnmodifiableMap(EnemyMobLevel::getLevel, enemyMobLevel -> enemyMobLevel));

        ItemStack item = jsonObject.has("item") ? ItemUtils.fromJsonObject(jsonObject.get("item").getAsJsonObject(), null) : null;
        if (item == null) {
            this.baseItem = ItemStack.builder(Material.BARRIER).displayName(Component.text("No item set")).build();
        } else {
            EnemyMobLevel levelOne = this.levels.get(1);
            List<Component> lore = new ArrayList<>();

            lore.add(Component.empty());
            lore.add(MINI_MESSAGE.deserialize(NOT_OWNED_LORE_LINE));
            lore.addAll(levelOne.generateDiff(levelOne).generateStatLines());

            this.baseItem = item
                    .withDisplayName(MINI_MESSAGE.deserialize(BASE_ITEM_DISPLAY_NAME, Placeholder.unparsed("mob_name", this.commonName)))
                    .withLore(lore);
        }
    }

    public @NotNull String getCommonName() {
        return this.commonName;
    }

    public int getSlot() {
        return this.slot;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public int getSendTime() {
        return this.sendTime;
    }

    public @NotNull ItemStack getBaseItem() {
        return this.baseItem;
    }

    public @NotNull Set<StatusEffectType> getIgnoredEffects() {
        return this.ignoredEffects;
    }

    public boolean isEffectIgnored(@NotNull StatusEffectType statusEffectType) {
        return this.ignoredEffects.contains(statusEffectType);
    }

    public @NotNull Map<Integer, EnemyMobLevel> getLevels() {
        return this.levels;
    }

    public EnemyMobLevel getLevel(int level) {
        return this.levels.get(level);
    }

    public int getMaxLevel() {
        return this.levels.keySet().stream().max(Integer::compareTo).orElseThrow();
    }
}
