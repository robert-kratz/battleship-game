package protocol;

import java.awt.*;
import java.util.ArrayList;

public class ItemManager {

    public static int getAmountOfShipsIn3x3Area(ArrayList<Ship> ships, int x, int y) {
        int amountOfShips = 0;
        System.out.println("Getting amount of ships in 3x3 area");

        ArrayList<Point> pointArrayList = new ArrayList<>(); //will include all 9 points in the 3x3 area

        //x,y, in center of the 3x3 area
        pointArrayList.add(new Point(x, y));
        pointArrayList.add(new Point(x - 1, y));
        pointArrayList.add(new Point(x + 1, y));
        pointArrayList.add(new Point(x, y - 1));
        pointArrayList.add(new Point(x - 1, y - 1));
        pointArrayList.add(new Point(x + 1, y - 1));
        pointArrayList.add(new Point(x, y + 1));
        pointArrayList.add(new Point(x - 1, y + 1));
        pointArrayList.add(new Point(x + 1, y + 1));

        //check if the points are in the 3x3 area
        for (Point point : pointArrayList) {
            for (Ship ship : ships) {
                for (Point cell : ship.getOccupiedCells()) {
                    if (cell.getX() == point.x && cell.getY() == point.y) {
                        amountOfShips++;
                        System.out.println("Ship found in 3x3 area");
                        break;
                    }
                }
            }
        }


        return amountOfShips;
    }
}
