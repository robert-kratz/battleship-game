package server;

import protocol.messages.GameStateUpdateMessage;
import protocol.Ship;
import protocol.messages.GameStartingMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class BattleShipGame implements Game, Runnable {

    private PlayerInfo playerA, playerB;
    private GameState gameState = null;
    private int size;

    private ArrayList<Ship> ships = new ArrayList<>();

    @Override
    public void run() {
        //TODO MORGEN TAKT IMPLEMENTIEREN
        try {
            System.out.println("Game started");

            while (true) {
                Thread.sleep(1000);

                switch (gameState.getStatus()) {
                    case BUILD_GAME_BOARD -> {
                        if (gameState.getBuildGameBoardFinished().before(new Date())) {
                            System.out.println("Build time is up!");
                            this.gameState.setStatus(GameState.GameStatus.IN_GAME);
                            this.gameState.setPlayersTurn(this.gameState.playerA);

                            Date playersTurnStart = new Date();
                            Date playersTurnEnd = new Date(playersTurnStart.getTime() + (1000 * Parameters.SHOOT_TIME_IN_SECONDS));

                            this.gameState.setPlayersTurnStart(playersTurnStart);
                            this.gameState.setPlayersTurnEnd(playersTurnEnd);

                            System.out.println("Player A's turn started at: " + playersTurnStart);
                            System.out.println("Player A's turn ends at: " + playersTurnEnd);
                            System.out.println("Gamestate: " + this.gameState.getStatus().toString());

                            System.out.println("Server GameState vor sendGameStateUpdate(): " + this.gameState.getStatus());
                            sendGameStateUpdate();
                            System.out.println("Server GameState nach sendGameStateUpdate(): " + this.gameState.getStatus());
                        }
                    }
                    case IN_GAME -> {
                        if (gameState.getPlayersTurnEnd().before(new Date())) {
                            System.out.println("Player's turn is up!");
                            if (gameState.getPlayersTurn().equals(gameState.playerA)) {
                                this.gameState.setPlayersTurn(gameState.playerB);
                            } else {
                                this.gameState.setPlayersTurn(gameState.playerA);
                            }

                            Date playersTurnStart = new Date();
                            Date playersTurnEnd = new Date(playersTurnStart.getTime() + (1000 * Parameters.SHOOT_TIME_IN_SECONDS));

                            this.gameState.setPlayersTurnStart(playersTurnStart);
                            this.gameState.setPlayersTurnEnd(playersTurnEnd);

                            System.out.println("Player's turn started at: " + playersTurnStart);
                            System.out.println("Player's turn ends at: " + playersTurnEnd);
                            System.out.println("Gamestate: " + this.gameState.getStatus().toString());

                            System.out.println("Server GameState vor sendGameStateUpdate(): " + this.gameState.getStatus());
                            sendGameStateUpdate();
                            System.out.println("Server GameState nach sendGameStateUpdate(): " + this.gameState.getStatus());
                        }
                    }
                    case GAME_OVER -> {
                        System.out.println("Game is over");
                        return;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized GameState createGame(PlayerInfo playerA, int size) {
        initShips();
        this.size = size;

        this.playerA = playerA;

        this.gameState = new GameState(playerA.getId(), size, ships);
        this.gameState = this.gameState.setSessionCode(generateSessionCode());

        return this.gameState;
    }

    //Game is created and auto starts
    @Override
    public synchronized GameState createGame(PlayerInfo playerA, PlayerInfo playerB, int size) {
        initShips();
        this.size = size;

        this.playerA = playerA;
        this.playerB = playerB;

        this.gameState = new GameState(playerA.getId(), playerB.getId(), size, ships);

        this.gameState = this.gameState.setSessionCode(generateSessionCode());

        System.out.println("Game created with session code: " + this.gameState.getSessionCode());

        startBuildPhase();

        return this.gameState;
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
    public synchronized UUID joinGame(PlayerInfo player, int sessionCode) {
        if(sessionCode != this.gameState.getSessionCode()) {
            throw new IllegalArgumentException("Invalid session code");
        }

        if(gameState.getPlayerCount() == 1) {
            this.gameState = gameState.addPlayer(player.getId());
            this.playerB = player;
            this.gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);
            System.out.println("Player B joined the game");
            return this.gameState.getSecret();
        }

        this.gameState.addPlayer(player.getId());
        return this.gameState.getSecret();
    }

    @Override
    public synchronized GameState leaveGame(PlayerInfo player) {
        switch (gameState.getStatus()) {
            case LOBBY_WAITING -> {
                if (gameState.playerA.equals(player.getId())) {
                    this.gameState = gameState.removePlayer(player.getId());
                    System.out.println("Player A left the game");
                } else if (gameState.playerB.equals(player.getId())) {
                    this.gameState = gameState.removePlayer(player.getId());
                    System.out.println("Player B left the game");
                }
            }
            case GAME_OVER -> System.out.println("Game is already over");
            default -> System.out.println("Game is already running");
        }

        return gameState;
    }

    private synchronized void startBuildPhase() {
        this.gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date startTime = new Date();
        int buildTime = (1000 * Parameters.BUILD_TIME_IN_SECONDS);

        this.gameState.setBuildGameBoardStarted(startTime);
        this.gameState.setBuildGameBoardFinished(new Date(startTime.getTime() + buildTime));

        System.out.println("Game starting in " + Parameters.BUILD_TIME_IN_SECONDS + " seconds");
        System.out.println(this.gameState.getBuildGameBoardFinished());

        playerA.sendMessage(new GameStartingMessage(this.gameState));
        playerB.sendMessage(new GameStartingMessage(this.gameState));
    }


    public synchronized void sendGameStateUpdate() {
        playerA.sendMessage(new GameStateUpdateMessage(this.gameState));
        playerB.sendMessage(new GameStateUpdateMessage(this.gameState));
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

    private int generateSessionCode() {
        return (int) (Math.random() * 100000);
    }
}
