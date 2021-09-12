package pink.zak.minestom.towerdefence.model.mob.living;

import com.google.common.collect.Sets;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.PathCorner;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.mob.living.types.BeeLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.types.LlamaLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.utils.DirectionUtils;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LivingEnemyMob extends EntityCreature {
    protected final TowerHandler towerHandler;
    protected final MobHandler mobHandler;
    protected final EnemyMob enemyMob;
    protected final EnemyMobLevel level;
    protected final Team team;

    protected final int positionModifier;
    protected final List<PathCorner> corners;
    protected int currentCornerIndex;
    protected PathCorner currentCorner;
    protected PathCorner nextCorner;
    protected double moveDistance;
    protected double totalDistanceMoved;

    protected Set<PlacedTower> attackingTowers = Sets.newConcurrentHashSet();
    protected float health;

    private Task attackTask;

    protected LivingEnemyMob(TowerHandler towerHandler, MobHandler mobHandler, EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(enemyMob.entityType());

        this.towerHandler = towerHandler;
        this.mobHandler = mobHandler;
        this.enemyMob = enemyMob;
        this.team = gameUser.getTeam(); // todo invert the team. Only the same for testing
        this.level = enemyMob.level(level);
        this.positionModifier = ThreadLocalRandom.current().nextInt(-map.getRandomValue(), map.getRandomValue() + 1);
        this.corners = map.getCorners(gameUser.getTeam());
        this.currentCornerIndex = 0;
        this.currentCorner = this.corners.get(0);
        this.nextCorner = this.corners.get(1);

        this.getAttribute(Attribute.MAX_HEALTH).setBaseValue(this.level.health());
        this.health = this.level.health();

        if (enemyMob.flying())
            this.setNoGravity(true);

        this.setCustomName(this.createCustomName());
        this.setCustomNameVisible(true);
        this.setInstance(instance, (team == Team.RED ? map.getRedMobSpawn() : map.getBlueMobSpawn()).add(this.positionModifier, enemyMob.flying() ? 5 : 0, this.positionModifier));
    }

    public static LivingEnemyMob create(TowerHandler towerHandler, MobHandler mobHandler, EnemyMob enemyMob, int level, Instance instance, TowerMap map, GameUser gameUser) {
        if (enemyMob.entityType() == EntityType.LLAMA)
            return new LlamaLivingEnemyMob(towerHandler, mobHandler, enemyMob, instance, map, gameUser, level);
        else if (enemyMob.entityType() == EntityType.BEE)
            return new BeeLivingEnemyMob(towerHandler, mobHandler, enemyMob, instance, map, gameUser, level);
        else
            return new LivingEnemyMob(towerHandler, mobHandler, enemyMob, instance, map, gameUser, level);
    }

    private Component createCustomName() {
        return Component.text(StringUtils.namespaceToName(this.entityType.name()) + " " + StringUtils.integerToCardinal(this.level.level()), NamedTextColor.DARK_GREEN)
            .append(Component.text(" (", NamedTextColor.GREEN))
            .append(Component.text((this.health / this.level.health()) * 100 + "%", NamedTextColor.DARK_GREEN))
            .append(Component.text(")", NamedTextColor.GREEN));
    }


    @Override
    public void tick(long time) {
        if (this.attackTask == null && !this.isDead())
            this.updatePos();
        super.tick(time);
    }

    private void updatePos() {
        this.refreshPosition(this.modifyPosition());
        this.moveDistance += this.level.movementSpeed();
        this.totalDistanceMoved += this.level.movementSpeed();
        if (this.nextCorner == null) {
            if (this.moveDistance >= this.currentCorner.distance() - this.positionModifier) {
                this.nextCorner();
            }
        } else if (this.moveDistance >= this.currentCorner.distance() + this.getLengthIncrease()) {
            this.nextCorner();
        }
    }

    private Pos modifyPosition() {
        Pos currentPos = this.getPosition();
        Pos newPos = switch (this.currentCorner.direction()) {
            case EAST -> currentPos.add(this.level.movementSpeed(), 0, 0);
            case SOUTH -> currentPos.add(0, 0, this.level.movementSpeed());
            case WEST -> currentPos.sub(this.level.movementSpeed(), 0, 0);
            case NORTH -> currentPos.sub(0, 0, this.level.movementSpeed());
            default -> throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + this.currentCorner.direction());
        };
        return newPos.withView(DirectionUtils.getYaw(this.currentCorner.direction()), 0); // todo this can be removed in the majority of cases to reduce pos creations
    }

    private void nextCorner() {
        int newCornerIndex = ++this.currentCornerIndex;
        int cornerSize = this.corners.size();
        if (this.nextCorner == null) {
            this.currentCornerIndex = -1;
            this.startAttackingCastle();
            return;
        }
        this.currentCorner = this.nextCorner;
        if (++newCornerIndex == cornerSize)
            this.nextCorner = null;
        else
            this.nextCorner = this.corners.get(newCornerIndex);

        this.moveDistance = 0;
    }

    protected void attackCastle() {
        this.swingMainHand();
        this.swingOffHand();
    }

    protected void startAttackingCastle() {
        this.attackTask = MinecraftServer.getSchedulerManager()
            .buildTask(this::attackCastle)
            .repeat(2, ChronoUnit.SECONDS)
            .schedule();
    }

    @Override
    public void kill() {
        if (this.attackTask != null)
            this.attackTask.cancel();

        for (PlacedTower tower : this.attackingTowers)
            tower.setTarget(null);

        if (this.team == Team.RED)
            this.mobHandler.getRedSideMobs().remove(this);
        else
            this.mobHandler.getBlueSideMobs().remove(this);

        super.kill();
    }

    @Override
    public void setHealth(float health) {
        this.health = health;
        if (this.health <= 0 && !isDead)
            this.kill();

        if (this.level != null)
            this.setCustomName(this.createCustomName());
    }

    @Override
    public boolean damage(@NotNull DamageType type, float value) {
        if (type != DamageType.VOID || this.isImmune(type) || this.isInvulnerable() || this.isDead()) // all damage will be labelled as void
            return false;

        DamageIndicator.create(this, value);

        // Set the last damage type
        this.lastDamageSource = type;

        this.sendPacketToViewersAndSelf(new EntityAnimationPacket(this.getEntityId(), EntityAnimationPacket.Animation.TAKE_DAMAGE));

        // Set the final entity health
        this.setHealth(this.health - value);

        // play damage sound
        final SoundEvent sound = type.getSound(this);
        if (sound != null) {
            Sound.Source soundCategory = Sound.Source.PLAYER;

            SoundEffectPacket damageSoundPacket =
                SoundEffectPacket.create(soundCategory, sound,
                    this.getPosition(),
                    1.0f, 1.0f);
            this.sendPacketToViewersAndSelf(damageSoundPacket);
        }
        return true;
    }

    private int getLengthIncrease() {
        if (this.positionModifier == 0)
            return 0;
        if (this.nextCorner.direction() == Direction.SOUTH) {
            return -this.positionModifier * 2;
        } else if (this.nextCorner.direction() == Direction.EAST) {
            return 0;
        } else {
            return this.positionModifier * 2;
        }
    }

    public double getTotalDistanceMoved() {
        return this.totalDistanceMoved;
    }

    public Set<PlacedTower> getAttackingTowers() {
        return this.attackingTowers;
    }

    public void setAttackingTowers(Set<PlacedTower> attackingTowers) {
        this.attackingTowers = attackingTowers;
    }

    public EnemyMob getEnemyMob() {
        return this.enemyMob;
    }

    public Team getGameTeam() {
        return this.team;
    }
}
