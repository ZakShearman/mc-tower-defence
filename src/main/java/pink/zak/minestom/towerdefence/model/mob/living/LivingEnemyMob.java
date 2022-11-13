package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.LazyPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.api.event.tower.TowerDamageMobEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.game.TowerHandler;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.map.PathCorner;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.mob.living.types.BeeLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.types.LlamaLivingEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.modifier.SpeedModifier;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffect;
import pink.zak.minestom.towerdefence.model.mob.statuseffect.StatusEffectType;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.CharityTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.utils.DirectionUtils;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ThreadLocalRandom;

public class LivingEnemyMob extends EntityCreature {
    private static final Logger LOGGER = LoggerFactory.getLogger(LivingEnemyMob.class);
    protected final TowerHandler towerHandler;
    protected final MobHandler mobHandler;
    protected final EnemyMob enemyMob;
    protected final EnemyMobLevel level;
    protected final Team team;
    protected final GameUser sender;

    protected final int positionModifier;
    protected final List<PathCorner> corners;

    protected final Map<StatusEffectType, StatusEffect> statusEffects = Collections.synchronizedMap(new EnumMap<>(StatusEffectType.class));
    protected final Set<SpeedModifier> speedModifiers = new CopyOnWriteArraySet<>();

    private final GameHandler gameHandler;

    protected int currentCornerIndex;
    protected PathCorner currentCorner;
    protected PathCorner nextCorner;
    protected int currentCornerLengthModifier;
    protected double moveDistance;
    protected double totalDistanceMoved;

    protected Set<PlacedAttackingTower<?>> attackingTowers = ConcurrentHashMap.newKeySet();
    protected float health;

    private Task attackTask;

    protected LivingEnemyMob(TowerDefenceModule plugin, GameHandler gameHandler, EnemyMob enemyMob, Instance instance, TowerMap map, GameUser gameUser, int level) {
        super(enemyMob.getEntityType());

        this.gameHandler = gameHandler;

        this.towerHandler = gameHandler.getTowerHandler();
        this.mobHandler = gameHandler.getMobHandler();
        this.enemyMob = enemyMob;
        this.level = enemyMob.getLevel(level);
        this.team = gameUser.getTeam() /*gameUser.getTeam() == Team.RED ? Team.BLUE : Team.RED*/;

        this.sender = gameUser;

        this.positionModifier = ThreadLocalRandom.current().nextInt(-map.getRandomValue(), map.getRandomValue() + 1);
        this.corners = map.getCorners(this.team);
        this.currentCornerIndex = 0;
        this.currentCorner = this.corners.get(0);
        this.nextCorner = this.corners.get(1);

        this.currentCornerLengthModifier = this.getRandomLengthModifier();

        this.getAttribute(Attribute.MAX_HEALTH).setBaseValue(this.level.getHealth());
        this.health = this.level.getHealth();

        if (enemyMob.isFlying())
            this.setNoGravity(true);

        this.setCustomNameVisible(true);

        Pos spawnPos = this.team == Team.RED ? map.getRedMobSpawn() : map.getBlueMobSpawn();
        // todo the issue might be here? Adding the position modifier has a very different effect based on direction
        this.setInstance(instance, spawnPos.add(this.positionModifier, enemyMob.isFlying() ? 5 : 0, this.positionModifier));
    }

    public static LivingEnemyMob create(TowerDefenceModule plugin, GameHandler gameHandler, EnemyMob enemyMob, int level, Instance instance, TowerMap map, GameUser gameUser) {
        if (enemyMob.getEntityType() == EntityType.LLAMA)
            return new LlamaLivingEnemyMob(plugin, gameHandler, enemyMob, instance, map, gameUser, level);
        else if (enemyMob.getEntityType() == EntityType.BEE)
            return new BeeLivingEnemyMob(plugin, gameHandler, enemyMob, instance, map, gameUser, level);
        else
            return new LivingEnemyMob(plugin, gameHandler, enemyMob, instance, map, gameUser, level);
    }

    private Component createNameComponent(@NotNull Player player) {
        TDPlayer tdPlayer = (TDPlayer) player;
        String health = tdPlayer.getHealthMode().resolve(this);
        TextComponent.Builder builder = Component.text()
                .append(Component.text(StringUtils.namespaceToName(this.entityType.name()) + " " + StringUtils.integerToCardinal(this.level.getLevel()), NamedTextColor.DARK_GREEN))
                .append(Component.text(" | ", NamedTextColor.GREEN))
                .append(Component.text(health, NamedTextColor.DARK_GREEN));

        // add status effect icons
        if (!this.statusEffects.isEmpty()) {
            builder.append(Component.text(" | ", NamedTextColor.GREEN));
            for (StatusEffect<?> statusEffect : this.statusEffects.values())
                builder.append(statusEffect.getIcon());
        }

        return builder.build();
    }

    @Override
    public void tick(long time) {
        for (StatusEffect statusEffect : this.statusEffects.values()) statusEffect.tick(time);

        if (this.attackTask == null && !this.isDead())
            this.updatePos();
        super.tick(time);
    }

    private void updatePos() {
        double movement = this.level.getMovementSpeed();
        for (SpeedModifier speedModifier : this.speedModifiers) movement *= speedModifier.getModifier();

        this.refreshPosition(this.modifyPosition(movement));
        this.moveDistance += movement;
        this.totalDistanceMoved += movement;
        if (this.nextCorner == null) {
            // detect if we've reached the end
            int modifier = this.positionModifier;
            if (this.currentCorner.modify()) {
                if (this.currentCorner.negativeModifier()) modifier = -modifier;
            }
            if (this.moveDistance >= this.currentCorner.distance() - modifier) {
                this.nextCorner();
            }
        } else if (this.moveDistance >= this.currentCorner.distance() + this.currentCornerLengthModifier) {
            this.nextCorner();
        }
    }

    private Pos modifyPosition(double movement) {
        Pos currentPos = this.getPosition();

        Pos newPos = switch (this.currentCorner.direction()) {
            case EAST -> currentPos.add(movement, 0, 0);
            case SOUTH -> currentPos.add(0, 0, movement);
            case WEST -> currentPos.sub(movement, 0, 0);
            case NORTH -> currentPos.sub(0, 0, movement);
            default ->
                    throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was %s".formatted(this.currentCorner.direction()));
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

        this.currentCornerLengthModifier = this.getRandomLengthModifier();
        this.moveDistance = 0;
    }

    protected void attackCastle() {
        this.swingMainHand();
        this.swingOffHand();
        this.damageCastle();
    }

    protected void damageCastle() {
        this.gameHandler.damageTower(this.team, this.level.getDamage());
    }

    protected void startAttackingCastle() {
        this.attackTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::attackCastle)
                .repeat(2, ChronoUnit.SECONDS)
                .schedule();
    }

    @Override
    public void kill() {
        this.refreshIsDead(true);

        if (this.attackTask != null)
            this.attackTask.cancel();

        for (PlacedAttackingTower<?> tower : this.attackingTowers)
            tower.getTargets().remove(this);

        if (this.team == Team.RED)
            this.mobHandler.getRedSideMobs().remove(this);
        else
            this.mobHandler.getBlueSideMobs().remove(this);

        super.kill();
    }

    // todo all of this needs to be fixed up. Metadata/Metadata.Entry is no longer accessible
    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);

        player.sendPacket(this.getEntityType().registry().spawnType().getSpawnPacket(this));
        if (this.hasVelocity()) player.sendPacket(this.getVelocityPacket());

        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(this.metadata.getEntries());
        Metadata.Entry<?> nameEntry = Metadata.OptChat(this.createNameComponent(player));
        entries.put(2, nameEntry);
        player.sendPacket(new LazyPacket(() -> new EntityMetaDataPacket(getEntityId(), entries)));
        // Passengers are removed here as i don't need them

        // Head position
        player.sendPacket(new EntityHeadLookPacket(getEntityId(), this.position.yaw()));
    }

    public void updateCustomName() {
        for (Player player : this.getViewers()) {
            Component value = this.createNameComponent(player);
            Metadata.Entry<?> nameEntry = Metadata.OptChat(value);
            player.sendPacket(new EntityMetaDataPacket(this.getEntityId(), Map.of(2, nameEntry)));
        }
    }

    @Override
    public void setCustomName(@Nullable Component customName) {
        LOGGER.warn("setCustomName called for a LivingEnemyMob. This action is not supported");
    }

    /**
     * @param source
     * @param value
     * @return The amount of damage dealt
     */
    public float towerDamage(@NotNull DamageSource source, float value) {
        if (this.isDead) return 0;
        if (this.enemyMob.isDamageTypeIgnored(source.getDamageType())) return 0;

        DamageIndicator.create(this, value);


        this.sendPacketToViewersAndSelf(new EntityAnimationPacket(this.getEntityId(), EntityAnimationPacket.Animation.TAKE_DAMAGE));
        this.setHealth(this.health - value);
        float damageDealt = this.isDead ? value : value - Math.abs(this.health);

        if (this.isDead) {
            Set<PlacedTower<?>> towers = this.team == Team.RED ? this.towerHandler.getRedTowers() : this.towerHandler.getBlueTowers();
            double multiplier = 1;
            for (PlacedTower<?> tower : towers) {
                if (tower instanceof CharityTower charityTower && tower.getBasePoint().distance(this.position) <= tower.getLevel().getRange()) {
                    multiplier = charityTower.getLevel().getMultiplier();
                }
            }
            double finalMultiplier = multiplier;
            source.getOwningUser().updateAndGetCoins(current -> (int) Math.floor(current + (this.level.getKillReward() * finalMultiplier)));
            source.getOwningUser().updateAndGetMana(current -> current + this.level.getManaKillReward());
        }

        final SoundEvent sound = DamageType.VOID.getSound(this);
        if (sound != null) {
            Sound.Source soundCategory = Sound.Source.PLAYER;

            SoundEffectPacket damageSoundPacket = new SoundEffectPacket(sound, soundCategory, this.getPosition(), 1.0f, 1.0f);
            this.sendPacketToViewersAndSelf(damageSoundPacket);
        }

        MinecraftServer.getGlobalEventHandler().call(new TowerDamageMobEvent(source.getSourceTower(), this, damageDealt));
        return damageDealt;
    }

    public void applyStatusEffect(@NotNull StatusEffect statusEffect) {
        if (this.enemyMob.isEffectIgnored(statusEffect.type()))
            throw new IllegalArgumentException("Tried to apply ignored status effect to mob (%s)".formatted(statusEffect.type()));

        StatusEffect<?> removedEffect = this.statusEffects.put(statusEffect.type(), statusEffect);
        if (removedEffect != null) {
            removedEffect.remove();
        }

        this.updateCustomName();
    }

    public void removeStatusEffect(@NotNull StatusEffect<?> statusEffect) {
        if (this.statusEffects.remove(statusEffect.type(), statusEffect)) this.updateCustomName();
    }

    public void applySpeedModifier(@NotNull SpeedModifier speedModifier) {
        this.speedModifiers.add(speedModifier);
    }

    public void removeSpeedModifier(@NotNull SpeedModifier speedModifier) {
        this.speedModifiers.remove(speedModifier);
    }

    @Override
    public boolean damage(@NotNull DamageType type, float value) {
        return false;
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(float health) {
        this.health = health;
        if (this.health <= 0 && !isDead)
            this.kill();

        if (this.level != null)
            this.updateCustomName();
    }

    private int getRandomLengthModifier() {
        if (this.positionModifier == 0 || !this.currentCorner.modify())
            return 0;
        int value = this.positionModifier;
        if (this.currentCorner.multiplyModifier())
            value *= 2;
        if (this.currentCorner.negativeModifier())
            value = -value;
        return value;
    }

    public Map<StatusEffectType, StatusEffect> getStatusEffects() {
        return this.statusEffects;
    }

    public double getTotalDistanceMoved() {
        return this.totalDistanceMoved;
    }

    public EnemyMob getEnemyMob() {
        return this.enemyMob;
    }

    public Team getGameTeam() {
        return this.team;
    }
}
