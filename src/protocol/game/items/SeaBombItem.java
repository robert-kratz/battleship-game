package protocol.game.items;

import protocol.game.Cell;
import protocol.game.Item;
import protocol.game.ItemType;
import server.Parameters;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

public class SeaBombItem extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    public SeaBombItem() {
        super(ItemType.SEA_BOMB, Parameters.ITEM_BOMB_COST);
    }

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
