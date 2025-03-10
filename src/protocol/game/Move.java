package protocol.game;

import protocol.game.items.AirStrikeItem;
import protocol.game.items.RadarItem;
import protocol.game.items.SeaBombItem;

import java.io.Serializable;
import java.util.ArrayList;

public class Move implements Serializable {
    private int x, y;
    private int rowOrColumn = -1;

    private SeaBombItem seaBombItem;
    private AirStrikeItem airStrikeItem;
    private RadarItem radarItem;

    private int radarShipsIn3x3Area = -1;

    // Neues Feld: Liste der von diesem Move betroffenen Zellen
    private ArrayList<Cell> affectedCells = new ArrayList<>();

    public Move(int x, int y) {
        this.x = x;
        this.y = y;
        // affectedCells wird später durch computeAffectedCells(boardSize) gesetzt
    }

    public Move(SeaBombItem seaBombItem, int seaBombX, int seaBombY) {
        this.x = seaBombX;
        this.y = seaBombY;
        this.seaBombItem = seaBombItem;
        // affectedCells wird später gesetzt
    }

    public Move(AirStrikeItem airStrikeItem, int rowOrColumn) {
        this.rowOrColumn = rowOrColumn;
        this.airStrikeItem = airStrikeItem;
        // affectedCells wird später gesetzt
    }

    public Move(RadarItem radarItem, int radarX, int radarY) {
        this.x = radarX;
        this.y = radarY;
        this.radarItem = radarItem;
        // affectedCells wird später gesetzt
    }

    /**
     * Berechnet und speichert die betroffenen Zellen (affectedCells) dieses Moves,
     * basierend auf der aktuellen Boardgröße.
     *
     * @param boardSize Die Größe des Spielbretts (Anzahl der Zellen pro Seite)
     */
    public void computeAffectedCells(int boardSize) {
        if (this.seaBombItem != null) {
            this.affectedCells = new ArrayList<>(this.seaBombItem.getAffectedFields(this.x, this.y, boardSize));
        } else if (this.airStrikeItem != null) {
            // Bei AirStrike wird die betroffene Reihe/Spalte anhand der Orientierung berechnet
            System.out.println("AirStrikeItem: " + this.airStrikeItem.getOrientation());
            System.out.println("x: " + this.x);
            System.out.println("y: " + this.y);
            this.affectedCells = new ArrayList<>(this.airStrikeItem.getAffectedFields(
                    this.rowOrColumn,
                    boardSize));
        } else if (this.radarItem != null) {
            // Radar beeinflusst keine Treffer, daher bleibt die Liste leer
            this.affectedCells = new ArrayList<>();
        } else {
            this.affectedCells = new ArrayList<>();
            this.affectedCells.add(new Cell(this.x, this.y));
        }
    }

    public Item getSelectedItem() {
        if (this.seaBombItem != null) {
            return this.seaBombItem;
        } else if (this.airStrikeItem != null) {
            return this.airStrikeItem;
        } else if (this.radarItem != null) {
            return this.radarItem;
        }
        return null;
    }

    public boolean isItemMove() {
        return this.seaBombItem != null || this.airStrikeItem != null || this.radarItem != null;
    }

    public int getRowOrColumn() {
        return rowOrColumn;
    }

    /**
     * Gibt die Liste der betroffenen Zellen zurück.
     */
    public ArrayList<Cell> getAffectedCells() {
        return affectedCells;
    }

    public int getRadarShipsIn3x3Area() {
        return radarShipsIn3x3Area;
    }

    public void setRadarShipsIn3x3Area(int radarShipsIn3x3Area) {
        this.radarShipsIn3x3Area = radarShipsIn3x3Area;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public AirStrikeItem getAirStrikeItem() {
        return airStrikeItem;
    }

    public RadarItem getRadarItem() {
        return radarItem;
    }

    public SeaBombItem getSeaBombItem() {
        return seaBombItem;
    }
}