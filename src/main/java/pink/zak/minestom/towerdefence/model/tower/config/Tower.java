package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.utils.ItemUtils;
import pink.zak.minestom.towerdefence.utils.StringUtils;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public class Tower {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private static final String BASE_ITEM_DISPLAY_NAME = "<i:false><gold><tower_name> - <yellow>$<tower_cost>";

    private final TowerType type;
    private final String name;
    private final int guiSlot;

    private final @NotNull ItemStack item;

    private final @NotNull List<Component> description;

    private final List<? extends TowerLevel> levels;

    private final ItemStack baseItem;

    public Tower(@NotNull JsonObject jsonObject, @NotNull Map<Integer, JsonObject> levelJsonMap) {
        this.type = TowerType.valueOf(jsonObject.get("type").getAsString());
        this.name = StringUtils.capitaliseSentenceWords(jsonObject.get("name").getAsString());
        this.guiSlot = jsonObject.get("guiSlot").getAsInt();

        this.item = ItemUtils.fromJsonObject(jsonObject.get("item").getAsJsonObject(), null);

        this.description = jsonObject.get("description").getAsJsonArray().asList().stream()
                .map(JsonElement::getAsString)
                .map(line -> MiniMessage.miniMessage().deserialize(line))
                .toList();

        this.levels = levelJsonMap.values().stream()
                .sorted(Comparator.comparingInt(json -> json.get("level").getAsInt()))
                .map(json -> this.type.getTowerLevelFunction().apply(json))
                .toList();

        this.baseItem = this.item.with(builder -> {
            builder.displayName(MINI_MESSAGE.deserialize(BASE_ITEM_DISPLAY_NAME,
                    Placeholder.unparsed("tower_name", this.name),
                    Placeholder.unparsed("tower_cost", String.valueOf(this.levels.get(1).getCost())))
            );

            List<Component> description = new ArrayList<>();
            description.add(Component.empty());
            description.addAll(this.description);
            description.add(Component.empty());
            description.add(Component.text("Size: ", NamedTextColor.GOLD)
                    .append(Component.text(this.type.getSize().getFormattedName(), NamedTextColor.YELLOW))
                    .decoration(TextDecoration.ITALIC, false));

            builder.lore(description);
        });
    }

    public boolean isSpaceClear(@NotNull TowerDefenceInstance instance, @NotNull Point basePoint) {
        int checkDistance = this.type.getSize().getCheckDistance();
        for (int x = basePoint.blockX() - checkDistance; x <= basePoint.blockX() + checkDistance; x++) {
            for (int z = basePoint.blockZ() - checkDistance; z <= basePoint.blockZ() + checkDistance; z++) {
                Block first = instance.getBlock(x, basePoint.blockY(), z);
                if (first.registry().material() != instance.getTowerMap().getTowerBaseMaterial() || first.properties().containsKey("towerId"))
                    return false;
                Material second = instance.getBlock(x, basePoint.blockY() + 1, z).registry().material();
                if (second != null && second != Material.AIR)
                    return false;
            }
        }
        return true;
    }

    public TowerType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public @NotNull ItemStack getItem() {
        return this.item;
    }

    public @NotNull ItemStack getBaseItem() {
        return this.baseItem;
    }

    public List<? extends TowerLevel> getLevels() {
        return this.levels;
    }

    public @Nullable TowerLevel getLevel(int level) {
        return this.levels.get(level - 1);
    }

    public @NotNull TowerLevel getMaxLevel() {
        return this.levels.getLast();
    }
}
