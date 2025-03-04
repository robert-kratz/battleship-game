package protocol;

import java.util.ArrayList;

public class ItemManager {

    public static int getAmountOfShipsIn3x3Area(ArrayList<Ship> ships, int x, int y) {
        int amountOfShips = 0;
        System.out.println("Getting amount of ships in 3x3 area");
        for (Ship ship : ships) {
            if (ship.isIn3x3Area(x, y)) {
                amountOfShips++;
            }
        }
        return amountOfShips;
    }
}
