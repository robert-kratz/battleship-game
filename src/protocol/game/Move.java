package protocol.game;

import protocol.game.items.AirStrikeItem;
import protocol.game.items.RadarItem;
import protocol.game.items.SeaBombItem;

import java.io.Serializable;
import java.util.ArrayList;

public class Move implements Serializable {
    private int x, y;
    private int rowOrColumn = -1;

    private SeaBombItem seaBombItem;
    private AirStrikeItem airStrikeItem;
    private RadarItem radarItem;

    private int radarShipsIn3x3Area = -1;

    private ArrayList<Cell> affectedCells = new ArrayList<>();

    /**
     * Creates a new Move instance.
     * @param x The x coordinate of the move.
     * @param y The y coordinate of the move.
     */
    public Move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new Move instance for a sea bomb item.
     * @param seaBombItem The sea bomb item to be used.
     * @param seaBombX The x coordinate of the sea bomb item.
     * @param seaBombY The y coordinate of the sea bomb item.
     */
    public Move(SeaBombItem seaBombItem, int seaBombX, int seaBombY) {
        this.x = seaBombX;
        this.y = seaBombY;
        this.seaBombItem = seaBombItem;
    }

    /**
     * Creates a new Move instance for an air strike item.
     * @param airStrikeItem The air strike item to be used.
     * @param rowOrColumn The row or column to be affected by the air strike.
     */
    public Move(AirStrikeItem airStrikeItem, int rowOrColumn) {
        this.rowOrColumn = rowOrColumn;
        this.airStrikeItem = airStrikeItem;
    }

    /**
     * Creates a new Move instance for a radar item.
     * @param radarItem The radar item to be used.
     * @param radarX the x coordinate of the radar item.
     * @param radarY the y coordinate of the radar item.
     */
    public Move(RadarItem radarItem, int radarX, int radarY) {
        this.x = radarX;
        this.y = radarY;
        this.radarItem = radarItem;
    }

    /**
     * Computes the affected cells based on the selected item.
     * @param boardSize The size of the board (number of cells per side)
     */
    public void computeAffectedCells(int boardSize) {
        if (this.seaBombItem != null) {
            this.affectedCells = new ArrayList<>(this.seaBombItem.getAffectedFields(this.x, this.y, boardSize));
        } else if (this.airStrikeItem != null) {
            this.affectedCells = new ArrayList<>(this.airStrikeItem.getAffectedFields(
                    this.rowOrColumn,
                    boardSize));
        } else if (this.radarItem != null) {
            // Radar impacts no hits, so the list remains empty
            this.affectedCells = new ArrayList<>();
        } else {
            this.affectedCells = new ArrayList<>();
            this.affectedCells.add(new Cell(this.x, this.y));
        }
    }

    /**
     * Get selected item.
     * @return The selected item.
     */
    public Item getSelectedItem() {
        if (this.seaBombItem != null) {
            return this.seaBombItem;
        } else if (this.airStrikeItem != null) {
            return this.airStrikeItem;
        } else if (this.radarItem != null) {
            return this.radarItem;
        }
        return null;
    }

    /**
     * Checks if the move is a ship move.
     * @return True if the move is a ship move, false otherwise.
     */
    public boolean isItemMove() {
        return this.seaBombItem != null || this.airStrikeItem != null || this.radarItem != null;
    }

    public int getRowOrColumn() {
        return rowOrColumn;
    }

    /**
     * Gibt die Liste der betroffenen Zellen zurück.
     */
    public ArrayList<Cell> getAffectedCells() {
        return affectedCells;
    }

    public int getRadarShipsIn3x3Area() {
        return radarShipsIn3x3Area;
    }

    public void setRadarShipsIn3x3Area(int radarShipsIn3x3Area) {
        this.radarShipsIn3x3Area = radarShipsIn3x3Area;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public AirStrikeItem getAirStrikeItem() {
        return airStrikeItem;
    }

    public RadarItem getRadarItem() {
        return radarItem;
    }

    public SeaBombItem getSeaBombItem() {
        return seaBombItem;
    }

    @Override
    public String toString() {
        return "Move{" +
                "x=" + x +
                ", y=" + y +
                ", rowOrColumn=" + rowOrColumn +
                ", seaBombItem=" + seaBombItem +
                ", airStrikeItem=" + airStrikeItem +
                ", radarItem=" + radarItem +
                ", radarShipsIn3x3Area=" + radarShipsIn3x3Area +
                ", affectedCells=" + affectedCells +
                '}';
    }
}