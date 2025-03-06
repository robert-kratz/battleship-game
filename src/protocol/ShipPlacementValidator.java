package protocol;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Validiert die Platzierung von Schiffen auf einem Spielbrett.
 */
public class ShipPlacementValidator {

    /**
     * Prüft, ob beim Platzieren des übergebenen Schiffs an (startRow, startCol) eine Kollision entsteht.
     */
    public static boolean isCollision(Ship ship, int boardSize, int startRow, int startCol, List<Ship> placedShips) {
        if (ship == null) return false;
        // Beachte: Der erste Parameter ist die X-Koordinate (Spalte) und der zweite die Y-Koordinate (Zeile)
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
     * Überprüft, ob alle Schiffe in der Liste platziert sind und ob sie die gleiche Größe, ID und Breite haben.
     * @param availableShips Liste der verfügbaren Schiffe
     * @param placedShips Liste der platzierten Schiffe, es müssen alle Schiffe platziert sein!
     * @param boardSize Größe des Spielbretts
     * @return true, wenn alle Schiffe platziert sind und die gleiche Größe, ID und Breite haben
     */
    public static boolean shipsAreAllPlacedAndTheSame(ArrayList<Ship> availableShips, ArrayList<Ship> placedShips, int boardSize) {

        if (availableShips.size() != placedShips.size()) {
            return false;
        }

        return shipsAreShipsTheSame(availableShips, placedShips, boardSize);
    }

    /**
     * Überprüft, ob alle Schiffe in der Liste die gleiche Größe, ID und Breite haben.
     * @param availableShips Liste der verfügbaren Schiffe
     * @param placedShips Liste der platzierten Schiffe, es müssen nicht alle Schiffe platziert sein!
     * @param boardSize Größe des Spielbretts
     * @return true, wenn alle Schiffe die gleiche Größe, ID und Breite haben
     */
    public static boolean shipsAreShipsTheSame(ArrayList<Ship> availableShips, ArrayList<Ship> placedShips, int boardSize) {

        boolean areShipsTheSame = true;

        System.out.println("-------------------------------");
        System.out.println("Available ships: " + availableShips.size());
        System.out.println("Placed ships: " + placedShips.size());
        System.out.println("Board size: " + boardSize);
        System.out.println("-------------------------------");

        if (placedShips.size() > availableShips.size()) {
            System.out.println("placedShips.size() > availableShips.size()");
            return false;
        }

        for (Ship ship : placedShips) {
            boolean found = false;
            for (Ship availableShip : availableShips) {
                // Vergleiche ID, Breite und Länge
                if (ship.getId() == availableShip.getId() &&
                        ship.getWidth() == availableShip.getWidth() &&
                        ship.getLength() == availableShip.getLength()) {
                    found = true;
                }

                // Erstelle eine Kopie der platzierten Schiffe und entferne das aktuelle Schiff
                ArrayList<Ship> remainingShips = new ArrayList<>(placedShips);
                remainingShips.removeIf(ship1 -> ship1.getId() == ship.getId());

                // Achtung: Hier müssen die eigenen Koordinaten des Schiffes verwendet werden!
                if (isCollision(ship, boardSize, ship.getY(), ship.getX(), remainingShips)) {
                    System.out.println("#Collision detected");
                    // Je nach Logik könntest du hier auch false zurückgeben
                }
            }
            if (!found) {
                areShipsTheSame = false;
                System.out.println("areShipsTheSame = false für Schiff ID " + ship.getId());
                break;
            }
        }

        return areShipsTheSame;
    }

    /**
     * Erzeugt ein zufällig generiertes Spielbrett, indem die übergebenen Schiffe deep copied und so platziert werden, dass
     * keine Überlappungen oder Out-of-Bounds auftreten.
     *
     * @param boardSize      Die Größe des Spielbretts (Anzahl der Zellen pro Seite)
     * @param availableShips Liste der zu platzierenden Schiffe (Prototypen)
     * @param alreadyPlacedShips Liste bereits platzierter Schiffe
     * @return Liste der zufällig platzierten Schiffe
     */
    public static ArrayList<Ship> createRandomizedGameBoard(int boardSize, ArrayList<Ship> availableShips, ArrayList<Ship> alreadyPlacedShips) {
        ArrayList<Ship> placedShips = new ArrayList<>();
        Random random = new Random();

        // Für jedes Schiff in der Prototyp-Liste
        for (Ship originalShip : availableShips) {
            // Erzeuge eine deep copy des Schiffes
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
            // Versuche, das Schiff in maximal 1000 Durchläufen zu platzieren
            while (!placed && attempts < 1000) {
                // Wähle zufällig eine Orientierung
                Ship.Orientation[] orientations = Ship.Orientation.values();
                int orientIndex = random.nextInt(orientations.length);
                ship.setOrientation(orientations[orientIndex]);

                // Wähle zufällige Startkoordinaten innerhalb des Spielfeldes
                int x = random.nextInt(boardSize);
                int y = random.nextInt(boardSize);
                ship.setX(x);
                ship.setY(y);

                // Überprüfe, ob das Schiff an dieser Position gültig platziert werden kann
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