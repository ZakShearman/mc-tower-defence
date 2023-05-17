package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.IntStatDiff;

public class BomberTowerLevel extends AttackingTowerLevel {
    private final @NotNull RelativePoint relativeTntSpawnPoint;
    private final int explosionRadius;

    public BomberTowerLevel(@NotNull JsonObject jsonObject) {
        super("Bomber", jsonObject);

        this.relativeTntSpawnPoint = new RelativePoint(jsonObject.get("relativeTntSpawnPoint").getAsJsonObject());
        this.explosionRadius = jsonObject.get("explosionRadius").getAsInt();
    }

    public @NotNull RelativePoint getRelativeTntSpawnPoint() {
        return this.relativeTntSpawnPoint;
    }

    public int getExplosionRadius() {
        return this.explosionRadius;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel uncastOther) {
        if (!(uncastOther instanceof BomberTowerLevel other))
            throw new IllegalArgumentException("Cannot compare BomberTowerLevel to " + uncastOther.getClass().getSimpleName());

        return super.generateDiff(uncastOther)
                .addDiff("Explosion Radius", new IntStatDiff(this.getExplosionRadius(), other.getExplosionRadius()));
    }
}
