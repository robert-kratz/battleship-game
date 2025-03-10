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

    /**
     * Enum representing the different game statuses.
     */
    public enum GameStatus {
        LOBBY_WAITING,
        BUILD_GAME_BOARD,
        IN_GAME,
        GAME_OVER;
    }

    private final UUID id;

    private final int sessionCode;

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

    /**
     * Copy constructor for creating a new GameState from an existing one.
     * @param gameState The GameState to copy from
     */
    public GameState(GameState gameState) {
        if(gameState.playerA != null) {
            this.playerA = new ClientPlayer(gameState.playerA);
            this.playerA.setUncoveredShips(new ArrayList<>(gameState.playerA.getUncoveredShips()));
        }
        if(gameState.playerB != null) {
            this.playerB = new ClientPlayer(gameState.playerB);
            this.playerB.setUncoveredShips(new ArrayList<>(gameState.playerB.getUncoveredShips()));
        }
        this.sessionCode = gameState.sessionCode;
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

    /**
     * Constructor for creating a new GameState with the given parameters.
     * @param gameOptions The game options to be used
     * @param ships The list of ships to be used
     */
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

    /**
     * Checks if the given player is the current player's turn.
     * @param player The UUID of the player to check
     * @return true if it's the player's turn, false otherwise
     */
    public boolean isPlayersTurn(UUID player) {
        if (playerA.isPlayer(player)) {
            return playerA.isTurn();
        } else {
            return playerB.isTurn();
        }
    }

    /**
     * Checks if the given player is the current player's turn.
     * @param player The UUID of the player to check
     * @return true if it's the player's turn, false otherwise
     */
    public ArrayList<Ship> getUncoveredShips(UUID player) {
        if(playerA.isPlayer(player)) {
            return playerA.getUncoveredShips();
        } else if(playerB.isPlayer(player)) {
            return playerB.getUncoveredShips();
        }
        return null;
    }

    /**
     * Updates the hit list for both players based on their moves and the ships of the opponent.
     * @param shipsA the ships of player A
     * @param shipsB the ships of player B
     */
    public void updateHitList(ArrayList<Ship> shipsA, ArrayList<Ship> shipsB) {
        int boardSize = getBoardSize();
        // For each move of player A, check against the ships of player B
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

        // For each move of player B, check against the ships of player A
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

    /**
     * Loads the radar information for both players based on their moves and the ships of the opponent.
     * @param shipsA the ships of player A
     * @param shipsB the ships of player B
     */
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
     * Checks if the given player has sunk all ships of the opponent.
     * @param player The player who made the move
     * @param targetShips The opponent's ships
     * @return true, if all ships are sunk
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
                sunkenShips.add(ship);
            }
        }

        return sunkenShips;
    }

    /**
     * Checks if the given player has sunk all ships of the opponent.
     * @param player The player who made the move
     * @param targetShips The opponent's ships
     * @return true, if all ships are sunk
     */
    public boolean hasPlayerSunkAllShips(ClientPlayer player, ArrayList<Ship> targetShips) {
        ArrayList<Ship> sunkenShips = getSunkenShips(player, targetShips);

        return (sunkenShips.size() == this.availableShips.size());
    }

    /**
     * Checks if the given player has no more moves possible.
     * @param player The player who made the move
     * @return true, if no more moves are possible
     */
    public boolean noMoreMovesPossible(ClientPlayer player) {
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

    /**
     * Checks if the given player is a player in the game.
     * @param player The UUID of the player to check
     * @return true if the player is in the game, false otherwise
     */
    public ClientPlayer getPlayer(UUID player) {
        if(playerA.isPlayer(player)) {
            return playerA;
        } else if(playerB.isPlayer(player)) {
            return playerB;
        }
        return null;
    }

    /**
     * Checks if the given player is the opponent of the given player.
     * @param player The UUID of the player to check
     * @return the opponent player
     */
    public ClientPlayer getOpponent(UUID player) {
        if(playerA.isPlayer(player)) {
            return playerB;
        } else if(playerB.isPlayer(player)) {
            return playerA;
        }
        return null;
    }

    /**
     * Checks if the given player is the current player's turn.
     * @return the current turn player
     */
    public ClientPlayer getCurrentTurnPlayer() {
        if(playerA == null || playerB == null) return null;

        if(playerA.isTurn()) {
            return playerA;
        } else if(playerB.isTurn()) {
            return playerB;
        }
        return null;
    }

    /**
     * Sets the winner of the game.
     * @param winner The UUID of the player who won
     */
    public void setWinner(UUID winner) {
        if(playerA == null || playerB == null) return;

        if(playerA.isPlayer(winner)) {
            playerA.setWinner(true);
        } else if(playerB.isPlayer(winner)) {
            playerB.setWinner(true);
        }
    }

    /**
     * Returns the winner of the game.
     * @return the winner player
     */
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

    /**
     * Returns the loser of the game.
     * @return the loser player
     */
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

    /**
     * Sets the next player's turn.
     */
    public void setNextTurn() {
        if (playerA.isTurn()) {
            playerA.setTurn(false);
            playerB.setTurn(true);
        } else if (playerB.isTurn()) {
            playerB.setTurn(false);
            playerA.setTurn(true);
        } else {
            // If no one is marked as active, we set a default
            playerA.setTurn(true);
            playerB.setTurn(false);
        }
        nextGameRound();
    }

    /**
     * Adds a move to the given player.
     * @param player the UUID of the player to add the move to
     * @param move the move to be added
     */
    public void addMove(UUID player, Move move) {
        if(playerA.isPlayer(player)) {
            playerA.getMoves().add(move);
        } else if(playerB.isPlayer(player)) {
            playerB.getMoves().add(move);
        }
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

    private void nextGameRound() {
        currentGameRound++;
    }

    public Date getBuildGameBoardFinished() {
        return buildGameBoardFinished;
    }

    public Date getBuildGameBoardStarted() {
        return buildGameBoardStarted;
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

    public void setBuildGameBoardFinished(Date buildGameBoardFinished) {
        this.buildGameBoardFinished = buildGameBoardFinished;
    }

    public void setBuildGameBoardStarted(Date buildGameBoardStarted) {
        this.buildGameBoardStarted = buildGameBoardStarted;
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
