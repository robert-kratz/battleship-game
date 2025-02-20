package protocol;

import java.io.Serializable;

public class Ship implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private int id;
    private int x, y;
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

    public Ship(int id, Orientation orientation, int length, int width) {
        this.id = id;
        this.isPlaced = false;
        this.orientation = orientation;
        this.length = length;
        this.width = width;
    }

    public String[][] drawShip(String[][] ship) {
        //use the orientation to draw the ship on the board
        for (int i = 0; i < length; i++) {
            if (orientation == Orientation.HORIZONTAL) {
                ship[y][x + i] = "S";
            } else {
                ship[y + i][x] = "S";
            }
        }
        return ship;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Orientation getOrientation() {
        return orientation;
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
        if (hits == length) {
            isSunk = true;
        }
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return "Ship{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", orientation='" + orientation + '\'' +
                ", length=" + length +
                ", width=" + width +
                ", isSunk=" + isSunk +
                ", hits=" + hits +
                '}';
    }
}
