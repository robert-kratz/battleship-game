package protocol.game.items;

import protocol.game.Cell;
import protocol.game.Item;
import protocol.game.ItemType;
import server.Parameters;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class AirStrikeItem extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private Orientation orientation;

    public AirStrikeItem() {
        super(ItemType.AIRSTRIKE, Parameters.ITEM_AIRSTRIKE_COST);

        this.orientation = Orientation.HORIZONTAL;
    }

    public AirStrikeItem(Orientation orientation) {
        super(ItemType.AIRSTRIKE, Parameters.ITEM_AIRSTRIKE_COST);

        this.orientation = orientation;
    }

    @Override
    public ArrayList<Cell> getAffectedFields(int rowOrColumn, int boardSize) {
        ArrayList<Cell> affectedFields = new ArrayList<>();

        if(orientation == Orientation.HORIZONTAL) {
            for(int i = 0; i < boardSize; i++) {
                affectedFields.add(new Cell(i, rowOrColumn));
            }
        } else {
            for(int i = 0; i < boardSize; i++) {
                affectedFields.add(new Cell(rowOrColumn, i));
            }
        }

        return affectedFields;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }
}
