package protocol;

import protocol.game.Cell;

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
    private int x, y; // Ankerpunkt (abhängig von der Orientierung)
    private boolean isPlaced;
    private Orientation orientation;
    private int length, width;
    private boolean isSunk;
    private int hits;
    private String icon;

    // Bestehender Konstruktor (nicht platziert)
    public Ship(int id, Orientation orientation, int length, int width) {
        this.id = id;
        this.isPlaced = false;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
        this.x = -1;
        this.y = -1;
    }

    // Anderer Konstruktor
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

    // Neuer Copy-Konstruktor für Deep Copy
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
        this.icon = other.icon;
    }

    public boolean isIn3x3Area(int x, int y) {

        System.out.println("isIn3x3Area x: " + x + " y: " + y);

        for (Point cells : getOccupiedCells()) {
            System.out.println("cells.x: " + cells.x + " cells.y: " + cells.y);

            if (cells.x == x && cells.y == y) {
                return true;
            }
        }
        return false;
    }

    // Getter und Setter
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
     * Gibt die Liste der Zellen zurück, die von diesem Schiff belegt werden.
     */
    public ArrayList<Point> getOccupiedCells() {
        return getOccupiedCellsAt(this.x, this.y);
    }

    /**
     * Gibt die Liste der Zellen zurück, die belegt würden, wenn das Schiff am Ankerpunkt (anchorX, anchorY) platziert wird.
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