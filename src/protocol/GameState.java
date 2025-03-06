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
        GAME_OVER;
    }

    private final UUID id;

    private int sessionCode;

    private Date created = new Date();
    private Date lastUpdated = new Date();

    private Date buildGameBoardStarted = null;
    private Date buildGameBoardFinished = null;

    private Date playersTurnStart = null;
    private Date playersTurnEnd = null;

    private ClientPlayer playerA, playerB;

    private final ArrayList<Ship> availableShips;

    private final int size;
    private int currentGameRound = 0;

    private GameStatus status = GameStatus.LOBBY_WAITING;

    public GameState(GameState gameState) {
        if(gameState.playerA != null) this.playerA = new ClientPlayer(gameState.playerA);
        if(gameState.playerB != null) this.playerB = new ClientPlayer(gameState.playerB);
        this.sessionCode = gameState.sessionCode;
        this.created = gameState.created;
        this.lastUpdated = gameState.lastUpdated;
        this.buildGameBoardStarted = gameState.buildGameBoardStarted;
        this.buildGameBoardFinished = gameState.buildGameBoardFinished;
        this.playersTurnStart = gameState.playersTurnStart;
        this.playersTurnEnd = gameState.playersTurnEnd;
        this.size = gameState.size;
        this.status = gameState.status;
        this.availableShips = new ArrayList<>(gameState.availableShips);
        this.currentGameRound = gameState.currentGameRound;
        this.id = gameState.id;
    }

    public GameState(int size, ArrayList<Ship> ships) {
        this.size = size;
        this.sessionCode = generateSessionCode();
        this.id = UUID.randomUUID();

        this.availableShips = ships;
    }

    public void setPlayerA(ClientPlayer playerA) {
        System.out.println("Setting player A: " + playerA);
        this.playerA = playerA;
    }

    public void setPlayerB(ClientPlayer playerB) {
        System.out.println("Setting player B: " + playerB);
        this.playerB = playerB;

        // Randomly assign turn to one of the players

        if(Math.random() < 0.5) {
            playerA.setTurn(true);
            playerB.setTurn(false);
        } else {
            playerA.setTurn(false);
            playerB.setTurn(true);
        }
    }

    public boolean setPlayerReady(UUID player, boolean ready) {
        if(playerA.isPlayer(player)) {
            playerA.setReady(ready);
        } else if(playerB.isPlayer(player)) {
            playerB.setReady(ready);
        }

        return playerA.isReady() && playerB.isReady();
    }

    public boolean isPlayersTurn(UUID player) {
        if (playerA.isPlayer(player)) {
            return playerA.isTurn();
        } else {
            return playerB.isTurn();
        }
    }

    public ArrayList<Ship> getUncoveredShips(UUID player) {
        if(playerA.isPlayer(player)) {
            return playerA.getUncoveredShips();
        } else if(playerB.isPlayer(player)) {
            return playerB.getUncoveredShips();
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
        for (Move move : playerA.getMoves()) {
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
        for (Move move : playerB.getMoves()) {
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
        for (Move move : playerA.getMoves()) {
            if(move.getRadarItem() != null) {
                move.setRadarShipsIn3x3Area(ItemManager.getAmountOfShipsIn3x3Area(shipsB, move.getX(), move.getY()));
            }
        }

        for (Move move : playerB.getMoves()) {
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
            for (Ship s : playerB.getUncoveredShips()) {
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
                for (Move move : playerB.getMoves()) {
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
                playerB.getUncoveredShips().add(ship);
            }
        }

        // Prüfe Schiffe von Spieler B (wird vom Gegner, also Spieler A, angegriffen)
        for (Ship ship : shipsB) {
            boolean alreadyUncovered = false;
            for (Ship s : playerA.getUncoveredShips()) {
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
                for (Move move : playerA.getMoves()) {
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
                playerA.getUncoveredShips().add(ship);
            }
        }
    }

    public void addUncoveredShip(UUID player, Ship ship) {
        if(playerA.isPlayer(player)) {
            playerA.getUncoveredShips().add(ship);
        } else if(playerB.isPlayer(player)) {
            playerB.getUncoveredShips().add(ship);
        }
    }

    /**
     * Returns a list of all cells targeted by player A’s moves.
     * Each Cell in the returned list carries its hit attribute (true if a ship was hit, false otherwise).
     */
    public ArrayList<Cell> getAttackedCellsForPlayerA() {
        ArrayList<Cell> attackedCells = new ArrayList<>();
        for (Move move : playerA.getMoves()) {
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
        for (Move move : playerB.getMoves()) {
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

    public UUID getId() {
        return id;
    }

    public ClientPlayer getPlayerA() {
        return playerA;
    }

    public ClientPlayer getPlayerB() {
        return playerB;
    }

    public int getSessionCode() {
        return sessionCode;
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

    public ClientPlayer getPlayer(UUID player) {
        if(playerA.isPlayer(player)) {
            return playerA;
        } else if(playerB.isPlayer(player)) {
            return playerB;
        }
        return null;
    }

    public ClientPlayer getOpponent(UUID player) {
        if(playerA.isPlayer(player)) {
            return playerB;
        } else if(playerB.isPlayer(player)) {
            return playerA;
        }
        return null;
    }

    public ClientPlayer getCurrentTurnPlayer() {
        if(playerA == null || playerB == null) return null;

        if(playerA.isTurn()) {
            return playerA;
        } else if(playerB.isTurn()) {
            return playerB;
        }
        return null;
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

    public void setWinner(UUID winner) {
        if(playerA == null || playerB == null) return;

        if(playerA.isPlayer(winner)) {
            playerA.setWinner(true);
        } else if(playerB.isPlayer(winner)) {
            playerB.setWinner(true);
        }
    }

    public ArrayList<ClientPlayer> getWinner() {
        if(this.status != GameStatus.GAME_OVER) return null;

        ArrayList<ClientPlayer> winners = new ArrayList<>();

        if(playerA != null && playerA.isWinner()) {
            winners.add(playerA);
        }

        if(playerB != null && playerB.isWinner()) {
            winners.add(playerB);
        }

        return winners;
    }

    public ArrayList<ClientPlayer> getLoser() {
        if(this.status != GameStatus.GAME_OVER) return null;

        ArrayList<ClientPlayer> losers = new ArrayList<>();

        if(playerA != null && !playerA.isWinner()) {
            losers.add(playerA);
        }

        if(playerB != null && !playerB.isWinner()) {
            losers.add(playerB);
        }

        return losers;
    }

    public void setNextTurn() {
        if (playerA.isTurn()) {
            playerA.setTurn(false);
            playerB.setTurn(true);
        } else if (playerB.isTurn()) {
            playerB.setTurn(false);
            playerA.setTurn(true);
        } else {
            // Falls keiner als aktiv markiert ist, legen wir einen Standard fest
            playerA.setTurn(true);
            playerB.setTurn(false);
        }
        nextGameRound();
    }

    public void addMove(UUID player, Move move) {
        System.out.println("Adding move: " + move);
        if(playerA.isPlayer(player)) {
            playerA.getMoves().add(move);
        } else if(playerB.isPlayer(player)) {
            playerB.getMoves().add(move);
        }
    }

    public void setPlayersTurnStart(Date playersTurnStart) {
        this.playersTurnStart = playersTurnStart;
    }

    public void setPlayersTurnEnd(Date playersTurnEnd) {
        this.playersTurnEnd = playersTurnEnd;
    }

    private int generateSessionCode() {
        return (int) (Math.random() * 900000) + 100000;
    }
}
