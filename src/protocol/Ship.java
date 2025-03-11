package protocol;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Orientation {
        NORTH, EAST, SOUTH, WEST;

        public Orientation next() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };
        }
    }

    private final int id;
    private int x, y; // Initial position of the ship
    private boolean isPlaced;
    private Orientation orientation;
    private final int length, width;
    private boolean isSunk;
    private int hits;

    /**
     * Constructor for creating a ship with the given parameters.
     * @param id the ID of the ship @unique
     * @param orientation the orientation of the ship
     * @param length the length of the ship
     * @param width the width of the ship
     */
    public Ship(int id, Orientation orientation, int length, int width) {
        this.id = id;
        this.isPlaced = false;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
        this.x = -1;
        this.y = -1;
    }

    /**
     * Constructor for creating a ship with the given parameters.
     * @param id the ID of the ship @unique
     * @param x the x-coordinate of the ship
     * @param y the y-coordinate of the ship
     * @param orientation the orientation of the ship
     * @param length the length of the ship
     * @param width the width of the ship
     * @param icon the icon of the ship
     */
    public Ship(int id, int x, int y, Orientation orientation, int length, int width, String icon) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isPlaced = true;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
    }

    /**
     * Copy constructor for creating a ship with the same properties as another ship.
     * @param other the ship to copy
     */
    public Ship(Ship other) {
        this.id = other.id;
        this.x = other.x;
        this.y = other.y;
        this.isPlaced = other.isPlaced;
        this.orientation = other.orientation;
        this.length = other.length;
        this.width = other.width;
        this.isSunk = other.isSunk;
        this.hits = other.hits;
    }

    /**
     * Returns the number of cells occupied by the ship.
     */
    public ArrayList<Point> getOccupiedCells() {
        return getOccupiedCellsAt(this.x, this.y);
    }

    /**
     * Provides the list of cells that would be occupied if the ship is placed at the anchor point (anchorX, anchorY).
     */
    public ArrayList<Point> getOccupiedCellsAt(int anchorX, int anchorY) {
        ArrayList<Point> cells = new ArrayList<>();
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

    public Orientation getOrientation() {
        return orientation;
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

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void setX(int x) {
        this.x = x;
        this.isPlaced = true;
    }

    public void setY(int y) {
        this.y = y;
        this.isPlaced = true;
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