package pink.zak.minestom.towerdefence.statdiff;

import org.jetbrains.annotations.NotNull;

public interface Diffable<T> {

    @NotNull StatDiffCollection generateDiff(@NotNull T other);
}
