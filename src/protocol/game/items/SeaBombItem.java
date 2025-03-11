package protocol.game.items;

import protocol.game.Cell;
import protocol.game.Item;
import protocol.game.ItemType;
import server.Parameters;

import java.io.Serializable;
import java.util.ArrayList;

public class SeaBombItem extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor for SeaBombItem.
     */
    public SeaBombItem() {
        super(ItemType.SEA_BOMB, Parameters.ITEM_BOMB_COST);
    }

    /**
     * Returns the affected fields for a SeaBombItem.
     * @param x The x coordinate of the cell.
     * @param y The y coordinate of the cell.
     * @param boardSize The size of the board.
     * @return An ArrayList of affected cells.
     */
    @Override
    public ArrayList<Cell> getAffectedFields(int x, int y, int boardSize) {
        ArrayList<Cell> affectedFields = new ArrayList<>();

        for(int i = x; i < x + 2; i++) {
            for(int j = y; j < y + 2; j++) {
                if(i >= 0 && i < boardSize && j >= 0 && j < boardSize) {
                    affectedFields.add(new Cell(i, j));
                }
            }
        }

        return affectedFields;
    }
}
