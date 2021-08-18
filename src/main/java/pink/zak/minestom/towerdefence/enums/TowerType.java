package pink.zak.minestom.towerdefence.enums;

public enum TowerType {
    ARCHER(Size.THREE, 10),
    BOMBER(Size.THREE, 11)
    /*CHARITY(Size.THREE, 11)*/;

    private final Size size;
    private final int guiSlot;

    TowerType(Size size, int guiSlot) {
        this.size = size;
        this.guiSlot = guiSlot;
    }

    public Size getSize() {
        return this.size;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public static TowerType valueOf(int guiSlot) {
        for (TowerType towerType : TowerType.values())
            if (towerType.getGuiSlot() == guiSlot)
                return towerType;
        return null;
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
