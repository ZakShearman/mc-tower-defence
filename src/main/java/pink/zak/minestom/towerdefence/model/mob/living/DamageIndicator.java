package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.time.TimeUnit;

public class DamageIndicator extends Entity {

    private DamageIndicator(Instance instance, Pos spawnPosition, Component text, Vec velocity) {
        super(EntityType.ARMOR_STAND);

        ArmorStandMeta meta = (ArmorStandMeta) this.getEntityMeta();

        meta.setSmall(true);
        meta.setCustomName(text);
        meta.setCustomNameVisible(true);
        meta.setInvisible(true);

        this.setInstance(instance, spawnPosition.add(0, -0.9875f, 0));

        this.position = spawnPosition;
        this.setVelocity(new Vec(0, 3, 0));
        this.setGravity(0.4, 0.05);

        this.scheduleRemove(15, TimeUnit.CLIENT_TICK);
    }

    public static void create(LivingEnemyMob enemyMob, double damage) {
        Component text = Component.text(damage, NamedTextColor.RED);

        new DamageIndicator(enemyMob.getInstance(), enemyMob.getPosition().add(0, enemyMob.getEntityType().height(), 0), text, null);
    }
}
