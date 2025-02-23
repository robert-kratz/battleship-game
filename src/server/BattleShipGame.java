package server;

import protocol.messages.GameStateUpdateMessage;
import protocol.Ship;
import protocol.messages.GameStartingMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class BattleShipGame implements Game, Runnable {

    private Server server;

    private PlayerInfo playerA, playerB;
    private GameState gameState = null;
    private final int size;

    private ArrayList<Ship> ships = new ArrayList<>();

    public BattleShipGame(Server server, int size) {
        this.server = server;
        this.size = size;

        initShips();

        this.gameState = new GameState(size, ships);
    }

    @Override
    public void run() {
        while (gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) {
            sleep(1000);
            System.out.println("Build Game Board");
            if(gameState.getBuildGameBoardFinished().before(new Date())) {

                System.out.println("Build Game Board Finished");

                //CHECK IF EVERY USER HAS PLACED SHIPS, IF NOT, RANDOMLY PLACE SHIPS

                this.gameState.setStatus(GameState.GameStatus.IN_GAME);

                GameState gameState = new GameState(this.getGameState());

                playerA.sendMessage(new GameStateUpdateMessage(gameState));
                playerB.sendMessage(new GameStateUpdateMessage(gameState));
            }
        }

        while (gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sleep(1000);
            System.out.println("In Game");
        }

        while (gameState.getStatus().equals(GameState.GameStatus.GAME_OVER)) {
            sleep(1000);
            System.out.println("Game Over");
        }
    }

    private void initShips() {
        ships.add(new Ship(0, Ship.Orientation.HORIZONTAL, 5, 1));
        ships.add(new Ship(1, Ship.Orientation.HORIZONTAL, 4, 1));
        ships.add(new Ship(2, Ship.Orientation.HORIZONTAL, 3, 1));
        ships.add(new Ship(3, Ship.Orientation.HORIZONTAL, 2, 2));
        ships.add(new Ship(4, Ship.Orientation.HORIZONTAL, 2, 1));
        ships.add(new Ship(5, Ship.Orientation.HORIZONTAL, 6, 1));
    }

    @Override
    public GameState addPlayer(PlayerInfo player) {
        //ONLY TRIGGER IF GAME IS IN LOBBY WAITING
        if(!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return null;

        if (playerA == null) {
            playerA = player;
            playerA.sendMessage(new GameStateUpdateMessage(gameState));
        } else if (playerB == null) {
            playerB = player;
            playerB.sendMessage(new GameStateUpdateMessage(gameState));
        }

        if(playerA != null && playerB != null) startBuildPhase();
        return this.gameState;
    }

    private synchronized void startBuildPhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date();

        GameState gameState = new GameState(this.getGameState());

        gameState.setBuildGameBoardStarted(date);
        gameState.setBuildGameBoardFinished(new Date(date.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000));

        this.gameState = gameState;

        playerA.sendMessage(new GameStartingMessage(gameState));
        playerB.sendMessage(new GameStartingMessage(gameState));
    }

    private synchronized void startGamePhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        gameState.setStatus(GameState.GameStatus.IN_GAME);

        Date date = new Date();

        GameState gameState = new GameState(this.getGameState());

        //start game logic
    }

    private synchronized void endGamePhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        gameState.setStatus(GameState.GameStatus.GAME_OVER);

        Date date = new Date();

        GameState gameState = new GameState(this.getGameState());

        //end game logic

    }

    @Override
    public GameState leaveGame(PlayerInfo player) {

        return gameState;
    }

    public synchronized void sendGameStateUpdate() {

    }

    private synchronized void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GameState getGameState() {
        return this.gameState;
    }

    @Override
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getSize() {
        return size;
    }

    public PlayerInfo getPlayerA() {
        return playerA;
    }

    public PlayerInfo getPlayerB() {
        return playerB;
    }
}
