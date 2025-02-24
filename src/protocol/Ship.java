package protocol;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Orientation {
        NORTH, EAST, SOUTH, WEST;

        public Orientation next() {
            switch (this) {
                case NORTH: return EAST;
                case EAST: return SOUTH;
                case SOUTH: return WEST;
                case WEST: return NORTH;
            }
            return NORTH;
        }
    }

    private int id;
    private int x, y; // Anchor point (interpreted according to orientation)
    private boolean isPlaced;
    private Orientation orientation;
    private int length, width;
    private boolean isSunk;
    private int hits;
    private String icon;

    public Ship(int id, int x, int y, Orientation orientation, int length, int width, String icon) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isPlaced = true;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
        this.icon = icon;
    }

    // Default constructor: not placed, therefore x and y are -1.
    public Ship(int id, Orientation orientation, int length, int width) {
        this.id = id;
        this.isPlaced = false;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
        this.x = -1;
        this.y = -1;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setX(int x) {
        this.x = x;
        this.isPlaced = true;
    }

    public void setY(int y) {
        this.y = y;
        this.isPlaced = true;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public int getHits() {
        return hits;
    }

    public void hit() {
        hits++;
        if (hits >= length) {
            isSunk = true;
        }
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Returns the list of grid cells occupied by this ship (using the current anchor x,y).
     * The ship’s cells are computed based on its orientation:
     * - EAST: extends rightwards: cells (x, y) to (x+length-1, y+width-1)
     * - WEST: extends leftwards: cells (x, y) to (x-length+1, y+width-1)
     * - SOUTH: extends downwards: cells (x, y) to (x+width-1, y+length-1)
     * - NORTH: extends upwards: cells (x, y) to (x+width-1, y-length+1)
     */
    public List<Point> getOccupiedCells() {
        return getOccupiedCellsAt(this.x, this.y);
    }

    /**
     * Returns the list of grid cells that would be occupied if the ship were placed with its anchor at (anchorX, anchorY).
     */
    public List<Point> getOccupiedCellsAt(int anchorX, int anchorY) {
        List<Point> cells = new ArrayList<>();
        switch (orientation) {
            case EAST:
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < width; j++) {
                        cells.add(new Point(anchorX + i, anchorY + j));
                    }
                }
                break;
            case WEST:
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < width; j++) {
                        cells.add(new Point(anchorX - i, anchorY + j));
                    }
                }
                break;
            case SOUTH:
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < width; j++) {
                        cells.add(new Point(anchorX + j, anchorY + i));
                    }
                }
                break;
            case NORTH:
                for (int i = 0; i < length; i++) {
                    for (int j = 0; j < width; j++) {
                        cells.add(new Point(anchorX + j, anchorY - i));
                    }
                }
                break;
        }
        return cells;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", orientation=" + orientation +
                ", length=" + length +
                ", width=" + width +
                ", isSunk=" + isSunk +
                ", hits=" + hits +
                '}';
    }
}