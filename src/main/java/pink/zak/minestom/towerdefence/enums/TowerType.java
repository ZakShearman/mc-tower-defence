package pink.zak.minestom.towerdefence.enums;

public enum TowerType {
    ARCHER(Size.THREE, 10, true),
    BOMBER(Size.THREE, 11, false)
    /*CHARITY(Size.THREE, 11)*/;

    private final Size size;
    private final int guiSlot;
    private final boolean targetAir;

    TowerType(Size size, int guiSlot, boolean targetAir) {
        this.size = size;
        this.guiSlot = guiSlot;
        this.targetAir = targetAir;
    }

    public static TowerType valueOf(int guiSlot) {
        for (TowerType towerType : TowerType.values())
            if (towerType.getGuiSlot() == guiSlot)
                return towerType;
        return null;
    }

    public Size getSize() {
        return this.size;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public boolean isTargetAir() {
        return this.targetAir;
    }

    public enum Size {
        THREE(1),
        FIVE(2);

        private final int checkDistance;

        Size(int checkDistance) {
            this.checkDistance = checkDistance;
        }

        public int getCheckDistance() {
            return this.checkDistance;
        }
    }
}
