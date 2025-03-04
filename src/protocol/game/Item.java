package protocol.game;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private ItemType type;
    private int energy;

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
}
