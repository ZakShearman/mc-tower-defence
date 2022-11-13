package pink.zak.minestom.towerdefence.model.mob.modifier;

public interface SpeedModifier {

    /**
     * Speed will be calculated as {@code distance * modifier}
     *
     * @return The modifier 0-1 where 0 is no movement and 1 is normal movement.
     */
    double getModifier();

    default ModifierType modifierType() {
        return ModifierType.SPEED;
    }
}
