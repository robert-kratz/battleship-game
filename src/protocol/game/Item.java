package protocol.game;

import java.io.Serializable;
import java.util.ArrayList;

public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ItemType type;
    private final int energy;

    /**
     * Default constructor for Item.
     * @param type the type of the item
     * @param energy the energy cost of the item
     */
    public Item(ItemType type, int energy) {
        this.type = type;
        this.energy = energy;
    }

    public ArrayList<Cell> getAffectedFields(int rowOrColumn, int boardSize) {
        return new ArrayList<>();
    }

    public ArrayList<Cell> getAffectedFields(int x, int y, int boardSize) {
        return new ArrayList<>();
    }

    public int getEnergyCost() {
        return energy;
    }

    public ItemType getType() {
        return type;
    }
}
