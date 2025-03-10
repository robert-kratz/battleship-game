package protocol;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShipPlacementHelper {

    /**
     * Check if the ship collides with other ships or goes out of bounds.
     * @param ship the ship to check
     * @param boardSize the size of the board
     * @param startRow the starting row
     * @param startCol the starting column
     * @param placedShips the list of already placed ships
     * @return true if there is a collision or out of bounds, false otherwise
     */
    public static boolean isCollision(Ship ship, int boardSize, int startRow, int startCol, List<Ship> placedShips) {
        if (ship == null) return false;
        // x: col, y: row
        List<Point> candidateCells = ship.getOccupiedCellsAt(startCol, startRow);
        for (Point p : candidateCells) {
            if (p.x < 0 || p.x >= boardSize || p.y < 0 || p.y >= boardSize) {
                return true;
            }
            for (Ship other : placedShips) {
                if (other == ship) continue;
                for (Point sp : other.getOccupiedCells()) {
                    if (p.x == sp.x && p.y == sp.y) return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if all ships are placed and have the same size, ID, and width.
     * @param availableShips the list of available ships
     * @param placedShips the list of placed ships
     * @param boardSize the size of the board
     * @return true if all ships are placed and have the same size, ID, and width
     */
    public static boolean shipsAreAllPlacedAndTheSame(ArrayList<Ship> availableShips, ArrayList<Ship> placedShips, int boardSize) {
        if (availableShips.size() != placedShips.size()) {
            return false;
        }

        return shipsAreShipsTheSame(availableShips, placedShips, boardSize);
    }

    /**
     * Checks if the ships are the same by comparing their IDs, widths, and lengths.
     * @param availableShips the list of available ships
     * @param placedShips the list of placed ships
     * @param boardSize the size of the board
     * @return true if the ships are the same, false otherwise
     */
    public static boolean shipsAreShipsTheSame(ArrayList<Ship> availableShips, ArrayList<Ship> placedShips, int boardSize) {

        boolean areShipsTheSame = true;

        if (placedShips.size() > availableShips.size()) {
            System.out.println("Invalid number of ships");
            return false;
        }

        for (Ship ship : placedShips) {
            boolean found = false;
            for (Ship availableShip : availableShips) {
                // Check if the ship IDs, widths, and lengths match
                if (ship.getId() == availableShip.getId() &&
                        ship.getWidth() == availableShip.getWidth() &&
                        ship.getLength() == availableShip.getLength()) {
                    found = true;
                }

                // Create a copy of the placed ships and remove the current ship
                ArrayList<Ship> remainingShips = new ArrayList<>(placedShips);
                remainingShips.removeIf(ship1 -> ship1.getId() == ship.getId());

                // Update the ship's position
                if (isCollision(ship, boardSize, ship.getY(), ship.getX(), remainingShips)) {
                    System.out.println("Collision detected");
                }
            }
            if (!found) {
                areShipsTheSame = false;
                break;
            }
        }

        return areShipsTheSame;
    }

    /**
     * Creates a randomized game board with ships placed randomly.
     * @param boardSize the size of the board
     * @param availableShips the list of available ships
     * @param alreadyPlacedShips the list of already placed ships
     * @return the list of placed ships
     */
    public static ArrayList<Ship> createRandomizedGameBoard(int boardSize, ArrayList<Ship> availableShips, ArrayList<Ship> alreadyPlacedShips) {
        ArrayList<Ship> placedShips = new ArrayList<>();
        Random random = new Random();

        // For each ship in the available ships
        for (Ship originalShip : availableShips) {
            // Create a copy of the ship
            Ship ship = new Ship(originalShip);
            if (alreadyPlacedShips.stream().anyMatch(s -> {
                boolean found = s.getId() == originalShip.getId();
                if (found) placedShips.add(s);
                return found;
            })) {
                continue;
            }

            boolean placed = false;
            int attempts = 0;
            // Try to place the ship randomly within the board size and 1000 attempts
            while (!placed && attempts < 1000) {
                // Select a random orientation
                Ship.Orientation[] orientations = Ship.Orientation.values();
                int orientIndex = random.nextInt(orientations.length);
                ship.setOrientation(orientations[orientIndex]);

                // Select a random position
                int x = random.nextInt(boardSize);
                int y = random.nextInt(boardSize);
                ship.setX(x);
                ship.setY(y);

                // Check if the ship collides with other ships or goes out of bounds
                if (!isCollision(ship, boardSize, y, x, placedShips)) {
                    placedShips.add(ship);
                    placed = true;
                }
                attempts++;
            }
            if (!placed) {
                throw new IllegalStateException("Could not place ship after 1000 attempts.");
            }
        }
        return placedShips;
    }
}