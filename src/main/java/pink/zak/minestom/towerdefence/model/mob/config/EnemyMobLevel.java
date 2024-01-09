package pink.zak.minestom.towerdefence.model.mob.config;

import com.google.gson.JsonObject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.statdiff.Diffable;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.DoubleStatDiff;
import pink.zak.minestom.towerdefence.statdiff.types.IntStatDiff;
import pink.zak.minestom.towerdefence.utils.ItemUtils;
import pink.zak.minestom.towerdefence.utils.NumberUtils;

public class EnemyMobLevel implements Diffable<EnemyMobLevel> {
    private static final String SEND_ITEM_NAME = "<i:false><mob_name> <level_numeral>";
    private static final String UPGRADE_ITEM_NAME = "<i:false><%s><level_numeral> - <yellow>$<cost></yellow>";

    private static final DecimalFormat MOVEMENT_SPEED_FORMAT = new DecimalFormat("#.##");

    private final @NotNull String name;

    private final int level;
    private final @NotNull EntityType entityType;
    private final int unlockCost;
    private final int sendIncomeIncrease;

    private final int sendCost;
    private final int killReward;

    private final int health;
    private final int damage;
    private final double movementSpeed;
    private final ItemStack sendItem;

    public EnemyMobLevel(@NotNull String name, @NotNull JsonObject jsonObject) {
        this.name = name;

        this.level = jsonObject.get("level").getAsInt();
        this.entityType = EntityType.fromNamespaceId(jsonObject.get("entityType").getAsString());
        this.unlockCost = jsonObject.get("unlockCost").getAsInt(); // Cost to upgrade to this level (in coins)
        this.sendIncomeIncrease = jsonObject.get("sendIncomeIncrease").getAsInt(); // Income increase from sending this level

        this.sendCost = jsonObject.get("sendCost").getAsInt();
        this.killReward = this.sendCost / 4; // Coins gained from killing this level mob // todo balance

        this.health = jsonObject.get("health").getAsInt();
        this.damage = jsonObject.get("damage").getAsInt();
        this.movementSpeed = jsonObject.get("movementSpeed").getAsDouble() / MinecraftServer.TICK_PER_SECOND;

        // todo can this be removed? we just autogen the lore
        TagResolver tagResolver = TagResolver.resolver(
                Placeholder.unparsed("send_cost", String.valueOf(this.sendCost)),
                Placeholder.unparsed("income_increase", String.valueOf(this.sendIncomeIncrease)),
                Placeholder.unparsed("health", String.valueOf(this.health)),
                Placeholder.unparsed("damage", String.valueOf(this.damage)),
                Placeholder.unparsed("movement_speed", MOVEMENT_SPEED_FORMAT.format(this.movementSpeed * MinecraftServer.TICK_PER_SECOND))
        );

        this.sendItem = ItemUtils.fromJsonObject(jsonObject.get("sendItem").getAsJsonObject(), tagResolver);
    }

    public ItemStack createSendItem() {
        return ItemStack.builder(this.sendItem.material())
                .meta(this.sendItem.meta())
                .displayName(MiniMessage.miniMessage().deserialize(SEND_ITEM_NAME,
                        Placeholder.unparsed("mob_name", this.name),
                        Placeholder.unparsed("level_numeral", NumberUtils.toRomanNumerals(this.level)),
                        Placeholder.unparsed("cost", String.valueOf(this.sendCost))))
                .lore(this.createStatLore())
                .build();
    }

    public @NotNull ItemStack createPreviewItem() {
        return ItemStack.builder(this.sendItem.material())
                .meta(this.sendItem.meta())
                .build();
    }

    public ItemStack createStatUpgradeItem(int cost, boolean owned, boolean canAfford) {
        return ItemStack.builder(owned ? Material.GREEN_STAINED_GLASS_PANE : canAfford ? Material.ORANGE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                .displayName(MiniMessage.miniMessage().deserialize(
                        UPGRADE_ITEM_NAME.formatted(owned ? "green" : canAfford ? "gold" : "red"),
                        Placeholder.unparsed("level_numeral", NumberUtils.toRomanNumerals(this.level)),
                        Placeholder.unparsed("cost", String.valueOf(cost))))
                .lore(this.createStatLore())
                .build();
    }

    public @NotNull ItemStack createBuyUpgradeItem(boolean canAfford, int cost, @NotNull EnemyMobLevel currentLevel) {
        String itemName = UPGRADE_ITEM_NAME.formatted(canAfford ? "gold" : "red");

        return ItemStack.builder(canAfford ? Material.ORANGE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE)
                .displayName(MiniMessage.miniMessage().deserialize(itemName,
                        Placeholder.unparsed("level_numeral", NumberUtils.toRomanNumerals(this.level)),
                        Placeholder.unparsed("cost", String.valueOf(cost))))
                .lore(this.createUpgradeLore(currentLevel))
                .build();
    }

    private @NotNull List<Component> createUpgradeLore(@NotNull EnemyMobLevel currentLevel) {
        List<Component> components = new ArrayList<>();
        components.add(Component.empty());
        components.addAll(currentLevel.generateDiff(this).generateComparisonLines());
        return components;
    }

    /**
     * Creates the same lore as upgrades but without comparison to another level
     *
     * @return the lore
     */
    private @NotNull List<Component> createStatLore() {
        List<Component> components = new ArrayList<>();
        components.add(Component.empty());
        components.addAll(this.generateDiff(this).generateStatLines());
        return components;
    }

    public int getLevel() {
        return this.level;
    }

    public int getSendCost() {
        return this.sendCost;
    }

    public int getKillReward() {
        return this.killReward;
    }

    /**
     * @return Income increase when this mob is sent.
     */
    public int getSendIncomeIncrease() {
        return this.sendIncomeIncrease;
    }

    public int getHealth() {
        return this.health;
    }

    public int getDamage() {
        return this.damage;
    }

    /**
     * @return Distance moved per tick
     */
    public double getMovementSpeed() {
        return this.movementSpeed;
    }

    public int getUnlockCost() {
        return this.unlockCost;
    }

    public @NotNull EntityType getEntityType() {
        return this.entityType;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull EnemyMobLevel other) {
        return new StatDiffCollection()
                .addDiff("Send Cost", new IntStatDiff(this.sendCost, other.sendCost, "$", null))
                .addDiff("Send Income", new IntStatDiff(this.sendIncomeIncrease, other.sendIncomeIncrease, "$", "/6s"))
                .addDiff("Health", new IntStatDiff(this.health, other.health))
                .addDiff("Damage", new IntStatDiff(this.damage, other.damage))
                .addDiff("Movement Speed", new DoubleStatDiff(
                        this.movementSpeed * MinecraftServer.TICK_PER_SECOND,
                        other.movementSpeed * MinecraftServer.TICK_PER_SECOND,
                        null, "b/s"
                ));
    }
}
