package pink.zak.minestom.towerdefence.model.tower.placed;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.targetting.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public abstract class PlacedAttackingTower<T extends AttackingTowerLevel> extends PlacedTower<T> implements DamageSource {
    private int ticksSinceLastAttack = this.level.getFireDelay();

    // todo: give this tower a protected event node of some sort
    private final @NotNull EventListener<InstanceTickEvent> tickListener = EventListener.of(InstanceTickEvent.class, event -> {
        this.ticksSinceLastAttack++;
        if (this.ticksSinceLastAttack < this.level.getFireDelay()) return;
        if (this.attemptToFire()) this.ticksSinceLastAttack = 0;
    });

    // inRangeChunks is a list of chunks that are within the tower's range, split up into rings based on distance from the tower
    // Thunk of its structure as the following (number is the index of its list):
    // 2  2  2  2  2
    // 2  1  1  1  2
    // 2  1  0  1  2
    // 2  1  1  1  2
    // 2  2  2  2  2
    private @NotNull List<List<Chunk>> inRangeChunks = new ArrayList<>();

    protected PlacedAttackingTower(@NotNull GameHandler gameHandler, AttackingTower tower, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(gameHandler, tower, id, owner, basePoint, facing, level);

        this.gameHandler.getInstance().eventNode().addListener(this.tickListener);
        this.inRangeChunks = this.calculateInRangeChunks();
    }

    /**
     * Attempts to fire the tower.
     *
     * @return true if the tower fired, false otherwise
     */
    protected abstract boolean attemptToFire();

    @Override
    public void destroy() {
        this.gameHandler.getInstance().eventNode().removeListener(this.tickListener);
        super.destroy();
    }

    @Override
    public void upgrade(int level, @Nullable GameUser user) {
        super.upgrade(level, user);

        this.inRangeChunks = this.calculateInRangeChunks();
    }

    private List<List<Chunk>> calculateInRangeChunks() {
        Instance instance = this.gameHandler.getInstance();
        Chunk towerChunk = instance.getChunkAt(this.getBasePoint());
        if (towerChunk == null) throw new IllegalStateException("Tower chunk is null");

        List<List<Chunk>> chunks = new ArrayList<>();
        chunks.add(List.of(towerChunk));


        int baseChunkX = towerChunk.getChunkX();
        int baseChunkZ = towerChunk.getChunkZ();
        int chunkRange = Math.ceilDiv(this.level.getRange(), 16);

        for (int distance = 1; distance <= chunkRange; distance++) {
            List<Chunk> ring = new ArrayList<>();
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    double euclideanDistance = Math.sqrt(dx * dx + dy * dy);
                    if (euclideanDistance < distance || euclideanDistance > distance + 1) continue; // Ensure a "ring" effect

                    Chunk chunk = instance.getChunk(baseChunkX + dx, baseChunkZ + dy);
                    if (chunk == null) continue;
                    ring.add(chunk);
                }
            }

            chunks.add(ring);
        }

        StringJoiner joiner = new StringJoiner(" ");
        for (List<Chunk> chunkList : chunks) {
            StringJoiner innerJoiner = new StringJoiner(" ");
            for (Chunk chunk : chunkList) {
                innerJoiner.add("[%s, %s]".formatted(chunk.getChunkX(), chunk.getChunkZ()));
            }

            joiner.add("(%s)".formatted(innerJoiner.toString()));
        }

        Audiences.all().sendMessage(Component.text("Chunks: " + joiner));

        return chunks;
    }

    // todo could this take in a Set<Limitation>?
    // e.g. Limitation.count(1) so then we can massively reduce the amount of mobs we need to check
    public @NotNull List<LivingTDEnemyMob> findPossibleTargets() {
        // get mobs attacking the tower's team
        Set<LivingTDEnemyMob> mobs = this.gameHandler.getMobHandler().getMobs(this.owner.getTeam());

        List<LivingTDEnemyMob> targets = new ArrayList<>();
        for (LivingTDEnemyMob mob : mobs) {
            // filter out mobs that are predicted to be dead before the tower can fire
            if (mob.isPredictedDead()) continue;

            // filter out mobs that are out of the tower's range
            // get the 2 points of the mob's bounding box
            BoundingBox boundingBox = mob.getTDEntityType().registry().boundingBox();
            double minX = boundingBox.minX();
            double maxX = boundingBox.maxX();
            double minZ = boundingBox.minZ();
            double maxZ = boundingBox.maxZ();

            // find side lengths
            double xLength = maxX - minX;
            double adjustedRadius = this.level.getRange() + xLength * 2;
            if (mob.getPosition().withY(this.basePoint.y()).distanceSquared(super.basePoint) <= Math.pow(adjustedRadius, 2)) {
                targets.add(mob);
            }

            // todo: implement line of sight checking, this was broken so I've commented it out for now
            // draw a line between the centres of tower and the mob
//            Vec vector = new Vec(
//                    super.basePoint.x() - mob.getPosition().x(),
//                    super.basePoint.y() - mob.getPosition().y(),
//                    super.basePoint.z() - mob.getPosition().z()
//            ).normalize();
//            Point intersection = super.basePoint.add(vector.mul(this.level.getRange()));
//
//            // check if intersection is within the mob's bounding box
//            if (intersection.x() < minX || intersection.x() > maxX) continue;
//            if (intersection.z() < minZ || intersection.z() > maxZ) continue;
//
//            targets.add(mob);
        }

        return targets;
    }

    public @NotNull List<LivingTDEnemyMob> findPossibleTargets(@NotNull Target target) {
        List<LivingTDEnemyMob> targets = findPossibleTargets();
//        targets.sort(target);

        LivingTDEnemyMob first = null;
        double distance = 0;
        for (LivingTDEnemyMob mob : targets) {

            if (mob.getTotalDistanceMoved() > distance) {
                first = mob;
                distance = mob.getTotalDistanceMoved();
            }
        }

        if (first == null) return List.of();
        return List.of(first);
    }

    public @NotNull List<List<Chunk>> getInRangeChunks() {
        return this.inRangeChunks;
    }
}
