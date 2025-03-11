package utils;

import protocol.Ship;

import java.awt.*;
import java.util.ArrayList;

public class ItemManager {

    /**
     * Check if the ship collides with other ships or goes out of bounds.
     * @param ships the list of ships to check
     * @param x the x-coordinate of the ship
     * @param y the y-coordinate of the ship
     * @return true if there is a collision or out of bounds, false otherwise
     */
    public static int getAmountOfShipsIn3x3Area(ArrayList<Ship> ships, int x, int y) {
        int amountOfShips = 0;

        ArrayList<Point> pointArrayList = new ArrayList<>(); // will include all 9 points in the 3x3 area

        // x,y, in center of the 3x3 area
        pointArrayList.add(new Point(x, y));
        pointArrayList.add(new Point(x - 1, y));
        pointArrayList.add(new Point(x + 1, y));
        pointArrayList.add(new Point(x, y - 1));
        pointArrayList.add(new Point(x - 1, y - 1));
        pointArrayList.add(new Point(x + 1, y - 1));
        pointArrayList.add(new Point(x, y + 1));
        pointArrayList.add(new Point(x - 1, y + 1));
        pointArrayList.add(new Point(x + 1, y + 1));

        // check if the points are in the 3x3 area
        for (Point point : pointArrayList) {
            for (Ship ship : ships) {
                for (Point cell : ship.getOccupiedCells()) {
                    if (cell.getX() == point.x && cell.getY() == point.y) {
                        amountOfShips++;
                        break;
                    }
                }
            }
        }


        return amountOfShips;
    }
}
