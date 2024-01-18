package pink.zak.minestom.towerdefence.utils;

import org.jetbrains.annotations.NotNull;

// intellij is convinced that `<R>` is not needed here, but it is - don't believe its lies
public sealed interface Result<R> permits Result.Success, Result.Failure {
    
    static <R> @NotNull Result<R> success() {
        return new Success<>();
    }
    
    static <R> @NotNull Result<R> failure(@NotNull R reason) {
        return new Failure<>(reason);
    }

    final class Success<R> implements Result<R> {
        private Success() {
        }
    }

    final class Failure<R> implements Result<R> {
        private final @NotNull R reason;

        private Failure(@NotNull R reason) {
            this.reason = reason;
        }

        public @NotNull R reason() {
            return this.reason;
        }

    }

}
