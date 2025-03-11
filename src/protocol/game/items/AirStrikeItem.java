package protocol.game.items;

import protocol.game.Cell;
import protocol.game.Item;
import protocol.game.ItemType;
import server.Parameters;

import java.io.Serializable;
import java.util.ArrayList;

public class AirStrikeItem extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    private Orientation orientation;

    /**
     * Default constructor for AirStrikeItem.
     */
    public AirStrikeItem() {
        super(ItemType.AIRSTRIKE, Parameters.ITEM_AIRSTRIKE_COST);

        this.orientation = Orientation.HORIZONTAL;
    }

    /**
     * Constructor for AirStrikeItem with specified orientation.
     * @param orientation the orientation of the airstrike (horizontal or vertical)
     */
    public AirStrikeItem(Orientation orientation) {
        super(ItemType.AIRSTRIKE, Parameters.ITEM_AIRSTRIKE_COST);

        this.orientation = orientation;
    }

    /**
     * Returns the affected fields for an AirStrikeItem.
     * @param rowOrColumn The row or column index of the cell.
     * @param boardSize The size of the board.
     * @return An ArrayList of affected cells.
     */
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
