package pink.zak.minestom.towerdefence;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;

public class Tags {
    // Real time in ms of when a mob can be stunned again
    public static final Tag<Long> NEXT_STUNNABLE_TIME = Tag.Long("towerdefence:NEXT_STUNNABLE_TIME");

    public static <T> T getOrDefault(@NotNull Taggable taggable, Tag<T> tag, T defaultValue) {
        T value = taggable.getTag(tag);
        return value == null ? defaultValue : value;
    }
}
