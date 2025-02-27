package server;

import protocol.GameState;
import protocol.ShipPlacementValidator;
import protocol.messages.GameStateUpdateMessage;
import protocol.Ship;
import protocol.messages.GameStartingMessage;
import protocol.messages.JoinGameMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class BattleShipGame implements Game, Runnable {

    private Server server;

    private PlayerInfo playerA, playerB;
    private GameState gameState = null;
    private final int size;

    private final ArrayList<Ship> availableShips = new ArrayList<>();

    private ArrayList<Ship> shipsPlayerA;
    private ArrayList<Ship> shipsPlayerB;

    public BattleShipGame(Server server, int size) {
        this.server = server;
        this.size = size;

        initShips();

        this.gameState = new GameState(size, availableShips);
    }

    @Override
    public void run() {
        while (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) {
            sleep(1000);
            System.out.println("Build Game Board");
            if(this.gameState.getBuildGameBoardFinished().before(new Date())) {

                System.out.println("Build Game Board Finished");

                System.out.println("Player A: " + gameState.hasPlayerASubmittedPlacement());
                System.out.println("Player B: " + gameState.hasPlayerBSubmittedPlacement());

                if(!this.gameState.hasPlayerASubmittedPlacement()) {
                    this.shipsPlayerA = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips);
                    this.gameState.playerSubmitPlacement(playerA.getId());
                }

                if(!this.gameState.hasPlayerBSubmittedPlacement()) {
                    this.shipsPlayerB = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips);
                    this.gameState.playerSubmitPlacement(playerB.getId());
                }

                this.gameState.setStatus(GameState.GameStatus.IN_GAME);

                GameState gameState = new GameState(this.getGameState());

                this.playerA.sendMessage(new GameStateUpdateMessage(gameState));
                this.playerB.sendMessage(new GameStateUpdateMessage(gameState));
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
        availableShips.add(new Ship(0, Ship.Orientation.NORTH, 5, 1));
        availableShips.add(new Ship(1, Ship.Orientation.NORTH, 4, 1));
        availableShips.add(new Ship(2, Ship.Orientation.NORTH, 3, 1));
        availableShips.add(new Ship(3, Ship.Orientation.NORTH, 2, 2));
        availableShips.add(new Ship(4, Ship.Orientation.NORTH, 2, 1));
        availableShips.add(new Ship(5, Ship.Orientation.NORTH, 6, 1));
    }

    @Override
    public synchronized GameState addPlayer(PlayerInfo player) {
        //ONLY TRIGGER IF GAME IS IN LOBBY WAITING
        if(!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return null;

        if (playerA == null) {
            playerA = player;
            GameState gameState = new GameState(this.getGameState());
            gameState.setPlayerA(playerA.getId(), playerA.getUsername());

            System.out.println("Player A: " + playerA.getUsername());

            playerA.sendMessage(new JoinGameMessage(gameState)); //First player is always A, no need to send to B
            playerA.sendMessage(new GameStateUpdateMessage(gameState));

            this.gameState = gameState;
        } else if (playerB == null) {
            playerB = player;
            GameState gameState = new GameState(this.getGameState());
            gameState.setPlayerB(playerB.getId(), playerB.getUsername());

            System.out.println("Player B: " + playerB.getUsername());

            playerB.sendMessage(new JoinGameMessage(gameState));
            playerA.sendMessage(new GameStateUpdateMessage(gameState));
            playerB.sendMessage(new GameStateUpdateMessage(gameState));

            this.gameState = gameState;
        }

        if(playerA != null && playerB != null) startBuildPhase(); //START BUILD PHASE
        return this.gameState;
    }

    private synchronized void startBuildPhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date(System.currentTimeMillis() + Parameters.TIMER_BEFORE_LOBBY_START * 1000);

        GameState gameState = new GameState(this.getGameState());

        gameState.setBuildGameBoardStarted(date);
        gameState.setBuildGameBoardFinished(new Date(date.getTime() + (Parameters.SHOOT_TIME_IN_SECONDS * 1000)));

        this.gameState = gameState;

        playerA.sendMessage(new GameStateUpdateMessage(gameState));
        playerB.sendMessage(new GameStateUpdateMessage(gameState));
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

    public void setPlayerShips(UUID playerId, ArrayList<Ship> ships) {
        if(playerA.getId().equals(playerId)) {
            this.shipsPlayerA = ships;
            this.gameState.playerSubmitPlacement(playerA.getId());
        } else if(playerB.getId().equals(playerId)) {
            this.shipsPlayerB = ships;
            this.gameState.playerSubmitPlacement(playerB.getId());
        }
    }

    public synchronized void sleep(int time) {
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
