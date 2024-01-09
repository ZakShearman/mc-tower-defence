package pink.zak.minestom.towerdefence.queue;

import org.jetbrains.annotations.NotNull;

public sealed interface QueueResult {
    
    static @NotNull QueueResult success() {
        return new Success();
    }
    
    static @NotNull QueueResult failure(@NotNull Reason reason) {
        return new Failure(reason);
    }

    final class Success implements QueueResult {
        private Success() {
        }
    }

    final class Failure implements QueueResult {
        private final @NotNull Reason reason;

        private Failure(@NotNull Reason reason) {
            this.reason = reason;
        }

        public @NotNull Reason reason() {
            return this.reason;
        }

    }

    enum Reason {
        NOT_UNLOCKED,
        CAN_NOT_AFFORD,
        QUEUE_FULL,
    }

}
