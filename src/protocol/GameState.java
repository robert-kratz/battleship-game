package protocol;

import protocol.game.Cell;
import protocol.game.Move;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class GameState implements Serializable  {

    private static final long serialVersionUID = 1L;

    public enum GameStatus {
        LOBBY_WAITING,
        BUILD_GAME_BOARD,
        IN_GAME,
        GAME_OVER,
    }

    private final UUID id = UUID.randomUUID();

    public UUID playerA, playerB;
    public String playerAName, playerBName;

    private int sessionCode;

    private Date created = new Date();
    private Date lastUpdated = new Date();

    private Date buildGameBoardStarted = null;
    private Date buildGameBoardFinished = null;

    private Date playersTurnStart = null;
    private Date playersTurnEnd = null;

    private boolean playerATurn = true;

    private boolean isAReady, isBReady;
    private int energyA = 30, energyB = 30;
    private ArrayList<Move> moveA = new ArrayList<>(), moveB = new ArrayList<>();

    private final ArrayList<Ship> availableShips;

    //IMPORTANT: uncoveredShipsA are ships uncovered by player B
    private ArrayList<Ship> uncoveredShipsFromA = new ArrayList<>(), uncoveredShipsFromB = new ArrayList<>();

    private final int size;
    private int currentGameRound = 0;

    private GameStatus status = GameStatus.LOBBY_WAITING;

    public GameState(GameState gameState) {
        this.playerA = gameState.playerA;
        this.playerB = gameState.playerB;
        this.playerAName = gameState.playerAName;
        this.playerBName = gameState.playerBName;
        this.sessionCode = gameState.sessionCode;
        this.created = gameState.created;
        this.lastUpdated = gameState.lastUpdated;
        this.buildGameBoardStarted = gameState.buildGameBoardStarted;
        this.buildGameBoardFinished = gameState.buildGameBoardFinished;
        this.moveA = new ArrayList<>(gameState.moveA);
        this.moveB = new ArrayList<>(gameState.moveB);
        this.energyA = gameState.energyA;
        this.energyB = gameState.energyB;
        this.playersTurnStart = gameState.playersTurnStart;
        this.playersTurnEnd = gameState.playersTurnEnd;
        this.playerATurn = gameState.playerATurn;
        this.isAReady = gameState.isAReady;
        this.isBReady = gameState.isBReady;
        this.size = gameState.size;
        this.status = gameState.status;
        this.availableShips = new ArrayList<>(gameState.availableShips);
        this.uncoveredShipsFromA = new ArrayList<>(gameState.uncoveredShipsFromA);
        this.uncoveredShipsFromB = new ArrayList<>(gameState.uncoveredShipsFromB);
        this.currentGameRound = gameState.currentGameRound;
    }

    public GameState(int size, ArrayList<Ship> ships) {
        this.size = size;
        this.sessionCode = generateSessionCode();

        this.availableShips = ships;
        this.playerATurn = Math.random() < 0.5;
    }

    public void setPlayerA(UUID playerA, String name) {
        this.playerA = playerA;
        this.playerAName = name;
    }

    public void setPlayerB(UUID playerB, String name) {
        this.playerB = playerB;
        this.playerBName = name;
    }

    public int getEnergyA() {
        return energyA;
    }

    public int getEnergyB() {
        return energyB;
    }

    public ArrayList<Ship> getUncoveredShipsFromA() {
        return uncoveredShipsFromA;
    }

    public ArrayList<Ship> getUncoveredShipsFromB() {
        return uncoveredShipsFromB;
    }

    public int getEnergy(UUID player) {
        if(playerA.equals(player)) {
            return energyA;
        } else if(playerB.equals(player)) {
            return energyB;
        }
        return -1;
    }

    public GameState removePlayer(UUID player) {
        if(playerA.equals(player)) {
            playerA = null;
            playerAName = null;
        } else if(playerB.equals(player)) {
            playerB = null;
            playerBName = null;
        }
        return this;
    }

    public void playerSubmitPlacement(UUID player) {
        if(playerA.equals(player)) {
            isAReady = true;
        } else if(playerB.equals(player)) {
            isBReady = true;
        }
    }

    public boolean isPlayersTurn(UUID player) {
        if (playerATurn) {
            return playerA.equals(player);
        } else {
            return playerB.equals(player);
        }
    }

    public ArrayList<Ship> getUncoveredShips(UUID player) {
        if(playerA.equals(player)) {
            return uncoveredShipsFromB;
        } else if(playerB.equals(player)) {
            return uncoveredShipsFromA;
        }
        return null;
    }

    /**
     * Aktualisiert die Hit-Informationen in den Moves.
     * Für jeden Move in moveA wird überprüft, ob die betroffenen Zellen (auf dem gegnerischen Spielfeld, also shipsB)
     * von einem Schiff belegt sind. Analog für moveB (hier werden shipsA geprüft).
     *
     * @param shipsA Die Liste der Schiffe von Spieler A
     * @param shipsB Die Liste der Schiffe von Spieler B
     */
    public void updateHitList(ArrayList<Ship> shipsA, ArrayList<Ship> shipsB) {
        int boardSize = getBoardSize();
        // Für die Züge von Spieler A: Überprüfe gegen die Schiffe von Spieler B
        for (Move move : moveA) {
            move.computeAffectedCells(boardSize);
            for (Cell cell : move.getAffectedCells()) {
                boolean hit = false;
                for (Ship ship : shipsB) {
                    ArrayList<Point> occupiedCells = ship.getOccupiedCells();
                    for (Point p : occupiedCells) {
                        if (p.x == cell.getX() && p.y == cell.getY()) {
                            hit = true;
                            break;
                        }
                    }
                    if (hit) break;
                }
                cell.setHit(hit);
            }
        }
        // Für die Züge von Spieler B: Überprüfe gegen die Schiffe von Spieler A
        for (Move move : moveB) {
            move.computeAffectedCells(boardSize);
            for (Cell cell : move.getAffectedCells()) {
                boolean hit = false;
                for (Ship ship : shipsA) {
                    ArrayList<Point> occupiedCells = ship.getOccupiedCells();
                    for (Point p : occupiedCells) {
                        if (p.x == cell.getX() && p.y == cell.getY()) {
                            hit = true;
                            break;
                        }
                    }
                    if (hit) break;
                }
                cell.setHit(hit);
            }
        }
    }

    public void loadRadars(ArrayList<Ship> shipsA, ArrayList<Ship> shipsB) {
        for (Move move : moveA) {
            if(move.getRadarItem() != null) {
                move.setRadarShipsIn3x3Area(ItemManager.getAmountOfShipsIn3x3Area(shipsB, move.getX(), move.getY()));
            }
        }

        for (Move move : moveB) {
            if(move.getRadarItem() != null) {
                move.setRadarShipsIn3x3Area(ItemManager.getAmountOfShipsIn3x3Area(shipsA, move.getX(), move.getY()));
            }
        }
    }

    /**
     * Prüft für beide Spieler, ob Schiffe komplett getroffen wurden.
     * Für Spieler A (eigene Schiffe, angegriffen von Spieler B) werden alle
     * Schiffe, deren alle belegt Zellen getroffen wurden, in uncoveredShipsFromB
     * aufgenommen – da diese für den Gegner (Spieler B) sichtbar sein sollen.
     * Analog werden für Spieler B alle vollständig getroffenen Schiffe in
     * uncoveredShipsFromA aufgenommen.
     *
     * Es wird darauf geachtet, dass kein Schiff doppelt hinzugefügt wird.
     *
     * @param shipsA Liste der Schiffe von Spieler A
     * @param shipsB Liste der Schiffe von Spieler B
     */
    public void uncoverHitShips(ArrayList<Ship> shipsA, ArrayList<Ship> shipsB) {
        // Prüfe Schiffe von Spieler A (wird vom Gegner, also Spieler B, angegriffen)
        for (Ship ship : shipsA) {
            // Falls das Schiff bereits als aufgedeckt in uncoveredShipsFromB vorhanden ist, überspringen
            boolean alreadyUncovered = false;
            for (Ship s : uncoveredShipsFromB) {
                if (s.getId() == ship.getId()) {
                    alreadyUncovered = true;
                    break;
                }
            }
            if (alreadyUncovered) continue;

            boolean fullyHit = true;
            // Hole alle Zellen, die dieses Schiff belegt
            java.util.List<Point> occupiedCells = ship.getOccupiedCells();
            // Für jede belegte Zelle wird geprüft, ob es in einem Move von Spieler B
            // (also in moveB) eine betroffene Zelle gibt, die als hit markiert wurde.
            for (Point p : occupiedCells) {
                boolean cellHit = false;
                for (Move move : moveB) {
                    // Stelle sicher, dass für den Move die affectedCells berechnet wurden
                    move.computeAffectedCells(getBoardSize());
                    for (protocol.game.Cell cell : move.getAffectedCells()) {
                        if (cell.getX() == p.x && cell.getY() == p.y && cell.isHit()) {
                            cellHit = true;
                            break;
                        }
                    }
                    if (cellHit) break;
                }
                if (!cellHit) {
                    fullyHit = false;
                    break;
                }
            }
            // Wenn alle Zellen getroffen wurden, wird das Schiff zur Liste hinzugefügt
            if (fullyHit) {
                uncoveredShipsFromB.add(ship);
            }
        }

        // Prüfe Schiffe von Spieler B (wird vom Gegner, also Spieler A, angegriffen)
        for (Ship ship : shipsB) {
            boolean alreadyUncovered = false;
            for (Ship s : uncoveredShipsFromA) {
                if (s.getId() == ship.getId()) {
                    alreadyUncovered = true;
                    break;
                }
            }
            if (alreadyUncovered) continue;

            boolean fullyHit = true;
            java.util.List<Point> occupiedCells = ship.getOccupiedCells();
            for (Point p : occupiedCells) {
                boolean cellHit = false;
                for (Move move : moveA) {
                    move.computeAffectedCells(getBoardSize());
                    for (protocol.game.Cell cell : move.getAffectedCells()) {
                        if (cell.getX() == p.x && cell.getY() == p.y && cell.isHit()) {
                            cellHit = true;
                            break;
                        }
                    }
                    if (cellHit) break;
                }
                if (!cellHit) {
                    fullyHit = false;
                    break;
                }
            }
            if (fullyHit) {
                uncoveredShipsFromA.add(ship);
            }
        }
    }

    public void addUncoveredShip(UUID player, Ship ship) {
        if(playerA.equals(player)) {
            uncoveredShipsFromA.add(ship);
        } else if(playerB.equals(player)) {
            uncoveredShipsFromB.add(ship);
        }
    }

    /**
     * Returns a list of all cells targeted by player A’s moves.
     * Each Cell in the returned list carries its hit attribute (true if a ship was hit, false otherwise).
     */
    public ArrayList<Cell> getAttackedCellsForPlayerA() {
        ArrayList<Cell> attackedCells = new ArrayList<>();
        for (Move move : moveA) {
            // If you want all cells (hits and misses), simply add them all:
            attackedCells.addAll(move.getAffectedCells());
        }
        return attackedCells;
    }

    /**
     * Returns a list of all cells targeted by player B’s moves.
     * Each Cell in the returned list carries its hit attribute (true if a ship was hit, false otherwise).
     */
    public ArrayList<Cell> getAttackedCellsForPlayerB() {
        ArrayList<Cell> attackedCells = new ArrayList<>();
        for (Move move : moveB) {
            attackedCells.addAll(move.getAffectedCells());
        }
        return attackedCells;
    }

    public int getPlayerCount() {
        return (playerA != null ? 1 : 0) + (playerB != null ? 1 : 0);
    }

    public ArrayList<Ship> getAvailableShips() {
        return availableShips;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameStatus getStatus() {
        return status;
    }

    public String getPlayerAName() {
        return playerAName;
    }

    public String getPlayerBName() {
        return playerBName;
    }

    public UUID getId() {
        return id;
    }

    public int getSessionCode() {
        return sessionCode;
    }

    public String getOpponentName(UUID player) {
        if(playerA.equals(player)) {
            return playerBName;
        } else if(playerB.equals(player)) {
            return playerAName;
        }
        return null;
    }

    public GameState setSessionCode(int sessionCode) {
        this.sessionCode = sessionCode;
        return this;
    }

    public int getCurrentGameRound() {
        return currentGameRound;
    }

    private void nextGameRound() {
        currentGameRound++;
    }

    public Date getBuildGameBoardFinished() {
        return buildGameBoardFinished;
    }

    public Date getBuildGameBoardStarted() {
        return buildGameBoardStarted;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getPlayersTurnEnd() {
        return playersTurnEnd;
    }

    public Date getPlayersTurnStart() {
        return playersTurnStart;
    }

    public int getBoardSize() {
        return size;
    }

    public UUID getPlayerA() {
        return playerA;
    }

    public UUID getPlayerB() {
        return playerB;
    }

    public boolean hasOpponentSubmittedPlacement(UUID player) {
        if(playerA.equals(player)) {
            return isBReady;
        } else if(playerB.equals(player)) {
            return isAReady;
        }
        return false;
    }

    public int getPlayerEnergy(UUID player) {
        if(playerA.equals(player)) {
            return energyA;
        } else if(playerB.equals(player)) {
            return energyB;
        }
        return -1;
    }

    public void setEnergy(UUID player, int energy) {
        if(playerA.equals(player)) {
            energyA = energy;
        } else if(playerB.equals(player)) {
            energyB = energy;
        }
    }

    public void addEnergy(UUID player, int energy) {
        if(playerA.equals(player)) {
            energyA += energy;
        } else if(playerB.equals(player)) {
            energyB += energy;
        }
    }

    public void setEnergyA(int energyA) {
        this.energyA = energyA;
    }

    public void setEnergyB(int energyB) {
        this.energyB = energyB;
    }

    public boolean hasPlayerASubmittedPlacement() {
        return isAReady;
    }

    public boolean hasPlayerBSubmittedPlacement() {
        return isBReady;
    }

    public void setBuildGameBoardFinished(Date buildGameBoardFinished) {
        this.buildGameBoardFinished = buildGameBoardFinished;
    }

    public void setBuildGameBoardStarted(Date buildGameBoardStarted) {
        this.buildGameBoardStarted = buildGameBoardStarted;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setNextTurn() {
        playerATurn = !playerATurn;
        nextGameRound();
    }

    public void addMove(UUID player, Move move) {
        System.out.println("Adding move: " + move);
        if(playerA.equals(player)) {
            moveA.add(move);
        } else if(playerB.equals(player)) {
            moveB.add(move);
        }
    }

    public void setPlayersTurnStart(Date playersTurnStart) {
        this.playersTurnStart = playersTurnStart;
    }

    public void setPlayersTurnEnd(Date playersTurnEnd) {
        this.playersTurnEnd = playersTurnEnd;
    }

    public boolean isPlayerATurn() {
        return playerATurn;
    }

    public ArrayList<Move> getMoveA() {
        return moveA;
    }

    public ArrayList<Move> getMoveB() {
        return moveB;
    }

    private int generateSessionCode() {
        return (int) (Math.random() * 900000) + 100000;
    }
}
