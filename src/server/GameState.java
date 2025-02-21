package server;

import protocol.Board;
import protocol.Ship;

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
        GAME_OVER
    }

    private final UUID id = UUID.randomUUID();

    public UUID playerA, playerB;
    private UUID secret = UUID.randomUUID();;

    private int sessionCode;

    private Date created = new Date();
    private Date lastUpdated = new Date();

    private Date buildGameBoardStarted = null;
    private Date buildGameBoardFinished = null;

    private Date playersTurnStart = null;
    private Date playersTurnEnd = null;

    private UUID playersTurn = null;

    private Board boardA, boardB;
    private ArrayList<Ship> shipsA, shipsB;

    private int size;

    private GameStatus status = GameStatus.LOBBY_WAITING;

    public GameState(GameState gameState) {
        this.playerA = gameState.playerA;
        this.playerB = gameState.playerB;
        this.secret = gameState.secret;
        this.sessionCode = gameState.sessionCode;
        this.created = gameState.created;
        this.lastUpdated = gameState.lastUpdated;
        this.buildGameBoardStarted = gameState.buildGameBoardStarted;
        this.buildGameBoardFinished = gameState.buildGameBoardFinished;
        this.playersTurnStart = gameState.playersTurnStart;
        this.playersTurnEnd = gameState.playersTurnEnd;
        this.playersTurn = gameState.playersTurn;
        this.boardA = gameState.boardA;
        this.boardB = gameState.boardB;
        this.shipsA = gameState.shipsA;
        this.shipsB = gameState.shipsB;
        this.size = gameState.size;
        this.status = gameState.status;
    }

    public GameState(int size, ArrayList<Ship> ships) {
        this.boardA = new Board(size, size);

        this.size = size;
        this.sessionCode = generateSessionCode();

        setShips(ships);
    }

    public GameState(UUID playerA, UUID playerB, int size, ArrayList<Ship> ships) {
        this.playerA = playerA;
        this.boardA = new Board(size, size);

        this.playerB = playerB;
        this.boardB = new Board(size, size);

        setShips(ships);
    }

    public GameState addPlayer(UUID player) {
        if(playerA == null) {
            playerA = player;

        } else if(playerB == null) {
            playerB = player;
        }
        return this;
    }

    public GameState removePlayer(UUID player) {
        if(playerA.equals(player)) {
            playerA = null;
            boardA = null;
            shipsA = null;
        } else if(playerB.equals(player)) {
            playerB = null;
            boardB = null;
            shipsB = null;
        }
        return this;
    }

    public GameState setShips(ArrayList<Ship> ships) {
        shipsA = ships;
        shipsB = ships;
        return this;
    }

    public int getPlayerCount() {
        return (playerA != null ? 1 : 0) + (playerB != null ? 1 : 0);
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

    public UUID getSecret() {
        return secret;
    }

    public int getSessionCode() {
        return sessionCode;
    }

    public GameState setSessionCode(int sessionCode) {
        this.sessionCode = sessionCode;
        return this;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }

    public ArrayList<Ship> getShipsA() {
        return shipsA;
    }

    public ArrayList<Ship> getShipsB() {
        return shipsB;
    }

    public Board getBoardA() {
        return boardA;
    }

    public Board getBoardB() {
        return boardB;
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

    public UUID getPlayersTurn() {
        return playersTurn;
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

    public void setPlayersTurn(UUID playersTurn) {
        this.playersTurn = playersTurn;
    }

    public void setPlayersTurnStart(Date playersTurnStart) {
        this.playersTurnStart = playersTurnStart;
    }

    public void setPlayersTurnEnd(Date playersTurnEnd) {
        this.playersTurnEnd = playersTurnEnd;
    }

    public boolean isPlayerATurn() {
        return playerA.equals(playersTurn);
    }

    private int generateSessionCode() {
        return (int) (Math.random() * 900000) + 100000;
    }
}
