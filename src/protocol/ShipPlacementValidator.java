package protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Validiert die Platzierung von Schiffen auf einem Spielbrett.
 */
public class ShipPlacementValidator {
    private final int boardSize;

    // Konstruktor, der direkt die Board-Größe verwendet
    public ShipPlacementValidator(int boardSize) {
        this.boardSize = boardSize;
    }

    /**
     * Prüft, ob beim Platzieren des übergebenen Schiffs an (startRow, startCol) eine Kollision entsteht.
     */
    public boolean isCollision(Ship ship, int startRow, int startCol, List<Ship> placedShips) {
        if (ship == null) return false;
        List<java.awt.Point> candidateCells = ship.getOccupiedCellsAt(startCol, startRow);
        for (java.awt.Point p : candidateCells) {
            if (p.x < 0 || p.x >= boardSize || p.y < 0 || p.y >= boardSize) {
                return true;
            }
            for (Ship other : placedShips) {
                if (other == ship) continue;
                for (java.awt.Point sp : other.getOccupiedCells()) {
                    if (p.x == sp.x && p.y == sp.y) return true;
                }
            }
        }
        return false;
    }

    /**
     * Erzeugt ein zufällig generiertes Spielbrett, indem die übergebenen Schiffe deep copied und so platziert werden, dass
     * keine Überlappungen oder Out-of-Bounds auftreten.
     *
     * @param boardSize      Die Größe des Spielbretts (Anzahl der Zellen pro Seite)
     * @param availableShips Liste der zu platzierenden Schiffe (Prototypen)
     * @return Liste der zufällig platzierten Schiffe
     */
    public static ArrayList<Ship> createRandomizedGameBoard(int boardSize, ArrayList<Ship> availableShips) {
        ArrayList<Ship> placedShips = new ArrayList<>();
        ShipPlacementValidator validator = new ShipPlacementValidator(boardSize);
        Random random = new Random();

        // Für jedes Schiff in der Prototyp-Liste
        for (Ship originalShip : availableShips) {
            // Erzeuge eine deep copy des Schiffes
            Ship ship = new Ship(originalShip);
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
                if (!validator.isCollision(ship, y, x, placedShips)) {
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