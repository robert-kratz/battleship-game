package protocol.game;

public class Item {

    private ItemType type;
    private int energy;

    public Item(ItemType type, int energy) {
        this.type = type;
        this.energy = energy;
    }

    public int getEnergyCost() {
        return energy;
    }
}
