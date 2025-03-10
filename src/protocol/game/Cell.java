package protocol.game;

import java.awt.*;
import java.io.Serializable;

public class Cell implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean isHit = false;
    private boolean isShip = false;

    private final int x;
    private final int y;

    /**
     * Creates a new Cell instance.
     * @param x The x coordinate of the cell.
     * @param y The y coordinate of the cell.
     */
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * This constructor is used to create a cell with a ship.
     * @param x The x coordinate of the cell.
     * @param y The y coordinate of the cell.
     * @param hit The hit status of the cell.
     */
    public Cell(int x, int y, boolean hit) {
        this.x = x;
        this.y = y;
        this.isHit = hit;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

    public boolean isShip() {
        return isShip;
    }

    public void setShip(boolean ship) {
        isShip = ship;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
