package pink.zak.minestom.towerdefence.statdiff;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// TODO can we handle when there is no change?
public record StatDiffCollection(@NotNull Map<String, StatDiff<?>> statDiffMap) {
    private static final String STAT_LINE_TEMPLATE = "<i:false><gold><stat_name>: <yellow><value></yellow>";
    private static final String COMP_LINE_TEMPLATE = "<i:false><gold><stat_name>: <yellow><original_value></yellow> » <yellow><new_value></yellow> (<yellow><value_diff></yellow>)";

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public StatDiffCollection() {
        this(new LinkedHashMap<>());
    }

    public @NotNull List<Component> generateComparisonLines() {
        List<Component> lines = new ArrayList<>();

        for (Map.Entry<String, StatDiff<?>> entry : this.statDiffMap.entrySet()) {
            StatDiff<?> diff = entry.getValue();

            TagResolver tags = TagResolver.resolver(
                    Placeholder.unparsed("stat_name", entry.getKey()),
                    Placeholder.unparsed("original_value", diff.getOriginal()),
                    Placeholder.unparsed("new_value", diff.getNew()),
                    Placeholder.unparsed("value_diff", diff.getDiffText())
            );

            lines.add(MINI_MESSAGE.deserialize(COMP_LINE_TEMPLATE, tags));
        }

        return lines;
    }

    public @NotNull List<Component> generateStatLines() {
        List<Component> lines = new ArrayList<>();

        for (Map.Entry<String, StatDiff<?>> entry : this.statDiffMap.entrySet()) {
            StatDiff<?> diff = entry.getValue();

            TagResolver tags = TagResolver.resolver(
                    Placeholder.unparsed("stat_name", entry.getKey()),
                    Placeholder.unparsed("value", diff.getOriginal())
            );

            lines.add(MINI_MESSAGE.deserialize(STAT_LINE_TEMPLATE, tags));
        }

        return lines;
    }

    public @NotNull StatDiffCollection addDiff(@NotNull String key, @NotNull StatDiff<?> statDiff) {
        this.statDiffMap.put(key, statDiff);
        return this;
    }
}
