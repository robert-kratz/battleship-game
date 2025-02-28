package protocol;

import protocol.game.Hit;

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

    private boolean shipsPlacedA, shipsPlacedB;
    private int energyA = 0, energyB = 0;
    private ArrayList<Hit> hitsA = new ArrayList<>(), hitsB = new ArrayList<>();

    private final ArrayList<Ship> availableShips;

    //IMPORTANT: uncoveredShipsA are ships uncovered by player B
    private ArrayList<Ship> uncoveredShipsFromA = new ArrayList<>(), uncoveredShipsFromB = new ArrayList<>();

    private final int size;

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
        this.hitsA = gameState.hitsA;
        this.hitsB = gameState.hitsB;
        this.energyA = gameState.energyA;
        this.energyB = gameState.energyB;
        this.playersTurnStart = gameState.playersTurnStart;
        this.playersTurnEnd = gameState.playersTurnEnd;
        this.playerATurn = gameState.playerATurn;
        this.shipsPlacedA = gameState.shipsPlacedA;
        this.shipsPlacedB = gameState.shipsPlacedB;
        this.size = gameState.size;
        this.status = gameState.status;
        this.availableShips = gameState.availableShips;
        this.uncoveredShipsFromA = gameState.uncoveredShipsFromA;
        this.uncoveredShipsFromB = gameState.uncoveredShipsFromB;
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
            shipsPlacedA = true;
        } else if(playerB.equals(player)) {
            shipsPlacedB = true;
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
            return shipsPlacedB;
        } else if(playerB.equals(player)) {
            return shipsPlacedA;
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
        return shipsPlacedA;
    }

    public boolean hasPlayerBSubmittedPlacement() {
        return shipsPlacedB;
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

    private int generateSessionCode() {
        return (int) (Math.random() * 900000) + 100000;
    }
}
