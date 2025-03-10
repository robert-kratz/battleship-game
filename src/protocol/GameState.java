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

    private final GameOptions gameOptions;

    public GameState(GameState gameState) {
        if(gameState.playerA != null) {
            this.playerA = new ClientPlayer(gameState.playerA);
            // Stelle sicher, dass die uncoveredShips übertragen werden:
            this.playerA.setUncoveredShips(new ArrayList<>(gameState.playerA.getUncoveredShips()));
        }
        if(gameState.playerB != null) {
            this.playerB = new ClientPlayer(gameState.playerB);
            this.playerB.setUncoveredShips(new ArrayList<>(gameState.playerB.getUncoveredShips()));
        }
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
        this.gameOptions = gameState.gameOptions;
    }

    public GameState(GameOptions gameOptions, ArrayList<Ship> ships) {
        this.size = gameOptions.getBoardSize();
        this.sessionCode = generateSessionCode();
        this.id = UUID.randomUUID();

        this.gameOptions = gameOptions;

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
     * Überprüft, ob ein Schiff versenkt wurde.
     * @param player The player who made the move
     * @param targetShips The opponent's ships
     * @return An ArrayList of sunken ships
     */
    public ArrayList<Ship> getSunkenShips(ClientPlayer player, ArrayList<Ship> targetShips) {

        ArrayList<Ship> sunkenShips = new ArrayList<>();
        ArrayList<Point> totalHitCells = new ArrayList<>();

        for(Move move : player.getMoves()) {
            move.computeAffectedCells(this.getBoardSize());
            ArrayList<Cell> affectedCells = move.getAffectedCells();

            for (Cell cell : affectedCells) {
                totalHitCells.add(new Point(cell.getX(), cell.getY()));
            }
        }

        for (Ship ship : targetShips) {
            ArrayList<Point> hitCells = new ArrayList<>();
            for (Point p : ship.getOccupiedCells()) {
                for (Point c : totalHitCells) {
                    if (p.x == c.x && p.y == c.y) {
                        hitCells.add(p);
                        break;
                    }
                }
            }

            if (hitCells.size() == ship.getOccupiedCells().size()) {
                System.out.println("Ship sunken: " + ship.getId());
                sunkenShips.add(ship);
            }
        }

        return sunkenShips;
    }

    /**
     * Überprüft, ob alle Schiffe eines Spielers versenkt wurden.
     * @param player The player who made the move
     * @param targetShips The opponent's ships
     * @return true, if all ships are sunk
     */
    public boolean hasPlayerSunkAllShips(ClientPlayer player, ArrayList<Ship> targetShips) {
        ArrayList<Ship> sunkenShips = getSunkenShips(player, targetShips);

        return (sunkenShips.size() == this.availableShips.size());
    }

    /**
     * Überprüft, ob keine Züge mehr möglich sind.
     * @param player The player who made the move
     * @param targetShips The opponent's ships
     * @return true, if no more moves are possible
     */
    public boolean noMoreMovesPossible(ClientPlayer player, ArrayList<Ship> targetShips) {
        ArrayList<Point> totalHitCells = new ArrayList<>();

        for(Move move : player.getMoves()) {
            move.computeAffectedCells(this.getBoardSize());
            ArrayList<Cell> affectedCells = move.getAffectedCells();

            for (Cell cell : affectedCells) {
                totalHitCells.add(new Point(cell.getX(), cell.getY()));
            }
        }

        int boardSizeAmount = this.getBoardSize() * this.getBoardSize();

        return totalHitCells.size() >= boardSizeAmount;
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

    public GameOptions getGameOptions() {
        return gameOptions;
    }
}
