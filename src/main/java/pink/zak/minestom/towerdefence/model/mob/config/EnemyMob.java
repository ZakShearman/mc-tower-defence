package pink.zak.minestom.towerdefence.model.mob.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.ToString;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.TDDamageType;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.utils.ItemUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ToString
public class EnemyMob {
    private final @NotNull String commonName;
    private final @NotNull TDDamageType damageType;
    private final int slot;
    private final boolean flying;
    private final int sendTime; // in milliseconds
    private final @NotNull ItemStack unownedItem;
    private final @NotNull Set<StatusEffectType> ignoredEffects;
    private final @NotNull Set<TDDamageType> ignoredDamageTypes;
    private final @NotNull Map<Integer, EnemyMobLevel> levels;

    public EnemyMob(@NotNull JsonObject jsonObject) {
        this.commonName = jsonObject.get("commonName").getAsString();
        this.damageType = TDDamageType.valueOf(jsonObject.get("damageType").getAsString());
        this.slot = jsonObject.get("guiSlot").getAsInt();
        this.flying = jsonObject.get("flying").getAsBoolean();
        this.sendTime = jsonObject.get("sendTime").getAsInt();

        this.unownedItem = jsonObject.has("unownedItem") ? ItemUtils.fromJsonObject(jsonObject.get("unownedItem").getAsJsonObject(), null) : null;

        this.ignoredEffects = StreamSupport.stream(jsonObject.get("ignoredEffects").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .map(StatusEffectType::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        this.ignoredDamageTypes = StreamSupport.stream(jsonObject.get("ignoredDamageTypes").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .map(TDDamageType::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        this.levels = StreamSupport.stream(jsonObject.get("levels").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(json -> new EnemyMobLevel(this.commonName, json))
                .collect(Collectors.toUnmodifiableMap(EnemyMobLevel::getLevel, enemyMobLevel -> enemyMobLevel));
    }

    public @NotNull String getCommonName() {
        return this.commonName;
    }

    public @NotNull TDDamageType getDamageType() {
        return this.damageType;
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

    public @NotNull ItemStack getUnownedItem() {
        return this.unownedItem;
    }

    public @NotNull Set<StatusEffectType> getIgnoredEffects() {
        return this.ignoredEffects;
    }

    public boolean isEffectIgnored(@NotNull StatusEffectType statusEffectType) {
        return this.ignoredEffects.contains(statusEffectType);
    }

    public @NotNull Set<TDDamageType> getIgnoredDamageTypes() {
        return this.ignoredDamageTypes;
    }

    public boolean isDamageTypeIgnored(TDDamageType damageType) {
        return this.ignoredDamageTypes.contains(damageType);
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
