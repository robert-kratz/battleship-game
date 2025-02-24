package protocol;

import server.GameState;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Validiert die Platzierung von Schiffen auf einem Spielbrett.
 */
public class ShipPlacementValidator {
    private final int boardSize;

    // Bestehender Konstruktor, der den GameState verwendet
    public ShipPlacementValidator(GameState gameState) {
        this.boardSize = gameState.getBoardSize();
    }

    // Neuer Überladener Konstruktor, der direkt die Board-Größe verwendet
    public ShipPlacementValidator(int boardSize) {
        this.boardSize = boardSize;
    }

    /**
     * Prüft, ob beim Platzieren des übergebenen Schiffs an (startRow, startCol) eine Kollision (mit Rand oder
     * einem bereits platzierten Schiff) entsteht.
     *
     * @param ship        Das zu platzierende Schiff
     * @param startRow    Zielzeile
     * @param startCol    Zielspalte
     * @param placedShips Liste bereits platzierter Schiffe
     * @return true, wenn eine Kollision vorliegt, sonst false.
     */
    public boolean isCollision(Ship ship, int startRow, int startCol, List<Ship> placedShips) {
        if (ship == null) return false;
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
     * Erzeugt ein zufällig generiertes Spielbrett, indem die übergebenen Schiffe so platziert werden, dass
     * keine Überlappungen oder Out-of-Bounds auftreten. Diese Methode wird genutzt, wenn ein Spieler seine
     * Schiffe nicht submitted.
     *
     * @param boardSize      Die Größe des Spielbretts (Anzahl der Zellen pro Seite)
     * @param availableShips Liste der zu platzierenden Schiffe
     * @return Liste der zufällig platzierten Schiffe
     */
    public static ArrayList<Ship> createRandomizedGameBoard(int boardSize, ArrayList<Ship> availableShips) {
        ArrayList<Ship> placedShips = new ArrayList<>();
        ShipPlacementValidator validator = new ShipPlacementValidator(boardSize);
        Random random = new Random();

        // Für jedes Schiff aus der übergebenen Liste
        for (Ship ship : new ArrayList<>(availableShips)) {
            boolean placed = false;
            int attempts = 0;
            // Versuche, das Schiff in maximal 1000 Durchläufen zu platzieren
            while (!placed && attempts < 1000) {
                // Wähle zufällig eine Orientierung (angenommen, Orientation ist ein Enum in deiner Ship-Klasse)
                Ship.Orientation[] orientations = Ship.Orientation.values();
                int orientIndex = random.nextInt(orientations.length);
                ship.setOrientation(orientations[orientIndex]);

                // Wähle zufällige Startkoordinaten (x,y) innerhalb des Spielfeldes
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