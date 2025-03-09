package server;

import protocol.*;
import protocol.game.Move;
import protocol.messages.*;
import protocol.messages.game.BuildingPhaseStartMessage;
import protocol.messages.game.GameInGameStartMessage;
import protocol.messages.game.GameOverMessage;
import protocol.messages.game.JoinGameMessage;
import protocol.messages.game.building.BuildReadyStateChange;
import protocol.messages.game.ingame.MoveMadeMessage;
import protocol.messages.game.ingame.PlayerTurnChangeMessage;

import java.util.*;

public class BattleShipGame implements Game, Runnable {

    private Server server;

    private PlayerInfo playerA, playerB;
    private GameState gameState = null;
    private final int size;

    private final ArrayList<Ship> availableShips = new ArrayList<>();

    private boolean playerTurnMadeMove = false;

    private ArrayList<Ship> shipsPlayerA = new ArrayList<>();
    private ArrayList<Ship> shipsPlayerB = new ArrayList<>();

    private Timer turnDelayTimer, gameOverTimer;

    public BattleShipGame(Server server, int size) {
        this.server = server;
        this.size = size;

        // Initialize available ships
        initAvailableShips();

        this.gameState = new GameState(size, availableShips);
    }

    @Override
    public void run() {
        // Build-Phase
        while (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) {
            sleep(1000);

            System.out.println("Build Game Board");
            if (this.gameState.getBuildGameBoardFinished().before(new Date())) {

                System.out.println("Build Game Board Finished");

                sendInGameStartEvent();
            }
        }

        // In-Game Phase
        while (gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sleep(1000);
            System.out.println("Game in progress");

            if (this.gameState.getPlayersTurnEnd().before(new Date())) {
                sendTurnChangeEvent();
            }
        }

        if (gameState.getStatus().equals(GameState.GameStatus.GAME_OVER)) {
            System.out.println("Game Over");
            unregisterGame(); // Unregister the game from the server
        }
    }

    @Override
    public synchronized void addPlayer(PlayerInfo player) {
        if (!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        player.setInGame(true);

        if (playerA == null) {
            playerA = player;
            GameState newState = new GameState(this.getGameState());
            newState.setPlayerA(new ClientPlayer(playerA.getId(), playerA.getUsername())); //Create Wrapper for PlayerInfo

            System.out.println("Player A: " + playerA.getUsername());

            playerA.sendMessage(new JoinGameMessage(newState));

            this.gameState = newState;
        } else if (playerB == null) {
            playerB = player;
            GameState newState = new GameState(this.getGameState());
            newState.setPlayerB(new ClientPlayer(playerB.getId(), playerB.getUsername())); //Create Wrapper for PlayerInfo

            System.out.println("Player B: " + playerB.getUsername());

            playerB.sendMessage(new JoinGameMessage(newState));

            this.gameState = newState;
        }

        server.updateGameList();

        //If both players are present, start the game
        if (playerA != null && playerB != null) sendGameStartingEvent();
    }

    @Override
    public synchronized void removePlayer(PlayerInfo player) {
        this.gameState.getPlayer(player.getId()).setInGame(false);
        player.setInGame(false);

        if (this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) {
            sendGameOverEvent(GameOverReason.PLAYER_LEFT_LOBBY);
        }

        if (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD) || this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sendGameOverEvent(GameOverReason.PLAYER_LEFT_IN_GAME);
        }
    }

    @Override
    public synchronized void sendGameStartingEvent() {
        // Only start the game if the game is in the lobby phase
        if (!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date(System.currentTimeMillis() + Parameters.TIMER_BEFORE_LOBBY_START * 1000);

        GameState newState = new GameState(this.getGameState());

        newState.setBuildGameBoardStarted(date);
        newState.setBuildGameBoardFinished(new Date(date.getTime() + (Parameters.BUILD_TIME_IN_SECONDS * 1000)));

        broadcastMessage(new BuildingPhaseStartMessage(newState));

        server.updateGameList();

        this.gameState = newState;
    }

    @Override
    public void onPlayerPlaceShips(PlayerInfo player, ArrayList<Ship> ships) {
        System.out.println("Received placement from player " + player.getUsername());

        // VALIDIERUNG DER SCHIFFSPLATZIERUNG
        boolean validatePlacement = ShipPlacementHelper.shipsAreShipsTheSame(this.gameState.getAvailableShips(), ships, this.size);

        System.out.println("Validate placement: " + validatePlacement);

        if (!validatePlacement) {
            player.sendMessage(new ErrorMessage(ErrorType.INVALID_PLACEMENT));
            return;
        }

        if (playerA != null && playerA.getId().equals(player.getId())) {
            this.shipsPlayerA = ships;
        } else if (playerB != null && playerB.getId().equals(player.getId())) {
            this.shipsPlayerB = ships;
        }

        System.out.println("Player " + player.getUsername() + " submitted placement");
    }

    @Override
    public void onPlayerReadyStateChange(PlayerInfo player, boolean ready) {
        GameState gameState = new GameState(this.getGameState());

        if (playerA != null && playerA.getId().equals(player.getId())) {
            if (ShipPlacementHelper.shipsAreAllPlacedAndTheSame(gameState.getAvailableShips(), this.shipsPlayerA, this.size)) {
                gameState.getPlayerA().setReady(ready);
            } else {
                player.sendMessage(new ErrorMessage(ErrorType.INVALID_PLACEMENT));
                return;
            }
        } else if (playerB != null && playerB.getId().equals(player.getId())) {
            if (ShipPlacementHelper.shipsAreAllPlacedAndTheSame(gameState.getAvailableShips(), this.shipsPlayerB, this.size)) {
                gameState.getPlayerB().setReady(ready);
            } else {
                player.sendMessage(new ErrorMessage(ErrorType.INVALID_PLACEMENT));
                return;
            }
        }

        System.out.println("PlayerA ready: " + gameState.getPlayerA().isReady());
        System.out.println("PlayerB ready: " + gameState.getPlayerB().isReady());

        // Statt einen neuen GameState zu erzeugen, verwende das existierende:
        if (playerA != null)
            playerA.sendMessage(new BuildReadyStateChange(gameState));
        if (playerB != null)
            playerB.sendMessage(new BuildReadyStateChange(gameState));

        // Optional: Ausgabe, wenn beide Spieler ready sind
        if (gameState.getPlayerA() != null && gameState.getPlayerB() != null &&
                gameState.getPlayerA().isReady() && gameState.getPlayerB().isReady()) {
            System.out.println("Both players are ready");

            //Skip build phase if both players are ready
            gameState.setBuildGameBoardFinished(new Date(System.currentTimeMillis() + 500));
        }

        this.gameState = gameState;
    }

    @Override
    public synchronized void sendInGameStartEvent() {
        //Only start the game if the game is in the build phase
        if(!gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        // Check if both players are ready
        checkForUnplacedShips();

        GameState newState = new GameState(this.getGameState());

        Date start = new Date();
        Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

        newState.setPlayersTurnStart(start);
        newState.setPlayersTurnEnd(end);

        newState.setStatus(GameState.GameStatus.IN_GAME);

        newState.setNextTurn();

        if (gameState.getPlayerA() != null)
            playerA.sendMessage(new GameInGameStartMessage(newState, this.shipsPlayerA));
        if (gameState.getPlayerB() != null)
            playerB.sendMessage(new GameInGameStartMessage(newState, this.shipsPlayerB));

        this.gameState = newState;
        server.updateGameList();
    }

    /**
     * Checks if both players have placed their ships, if not, place them randomly.
     */
    private void checkForUnplacedShips() {
        if (gameState.getPlayerA() != null && !gameState.getPlayerA().isReady()) {
            System.out.println("Player A did not submit placement");
            this.shipsPlayerA = ShipPlacementHelper.createRandomizedGameBoard(size, availableShips, this.shipsPlayerA);
            gameState.getPlayerA().setReady(true);
        }

        if (gameState.getPlayerB() != null && !gameState.getPlayerB().isReady()) {
            System.out.println("Player B did not submit placement");
            this.shipsPlayerB = ShipPlacementHelper.createRandomizedGameBoard(size, availableShips, this.shipsPlayerB);
            gameState.getPlayerB().setReady(true);
        }
    }

    @Override
    public void sendTurnChangeEvent() {

        GameState newState = new GameState(this.getGameState());

        Date start = new Date();
        Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

        newState.setPlayersTurnStart(start);
        newState.setPlayersTurnEnd(end);

        newState.getCurrentTurnPlayer().addEnergy(Parameters.ENERGY_TURN_BONUS);

        newState.setNextTurn();

        // Weitere Logik ...
        MoveManager moveManager = new MoveManager(newState);

        if (!moveManager.isAMoveStillPossible()) {
            System.out.println("No more moves possible");
            sendGameOverEvent(GameOverReason.NO_MORE_MOVES);
            return;
        }

        if (gameState.getPlayerA() != null && gameState.getPlayerA().isTurn() &&
                !this.playerTurnMadeMove) {
            System.out.println("Player A did not submit move");

            Move move = moveManager.makeRandomMove(playerA.getId());
            newState.addMove(playerA.getId(), move);
            //this.onPlayerAttemptMove(playerA, move);
        } else if (gameState.getPlayerB() != null && gameState.getPlayerB().isTurn() &&
                !this.playerTurnMadeMove) {
            System.out.println("Player B did not submit move");

            Move move = moveManager.makeRandomMove(playerB.getId());
            newState.addMove(playerB.getId(), move);
            //this.onPlayerAttemptMove(playerB, move);
        }

        ArrayList<Ship> uncoveredShipsA = newState.getSunkenShips(newState.getPlayerA(), this.shipsPlayerB);
        ArrayList<Ship> uncoveredShipsB = newState.getSunkenShips(newState.getPlayerB(), this.shipsPlayerA);

        System.out.println("Player A: " + uncoveredShipsA.size());
        System.out.println("Player B: " + uncoveredShipsB.size());

        newState.getPlayerA().setUncoveredShips(uncoveredShipsA);
        newState.getPlayerB().setUncoveredShips(uncoveredShipsB);

        //newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
        newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
        newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);

        if (gameState.getPlayerA() != null)
            playerA.sendMessage(new PlayerTurnChangeMessage(newState));
        if (gameState.getPlayerB() != null)
            playerB.sendMessage(new PlayerTurnChangeMessage(newState));

        this.playerTurnMadeMove = false;

        this.gameState = newState;
        server.updateGameList();

        checkForWinner();
    }

    @Override
    public void sendGameOverEvent(GameOverReason reason) {
        gameState.setStatus(GameState.GameStatus.GAME_OVER);

        System.out.println("Sending game over event " + reason);

        if(this.turnDelayTimer != null) {
            this.turnDelayTimer.cancel();
            this.turnDelayTimer = null;
        }

        this.playerA.setInGame(false);
        this.playerB.setInGame(false);

        switch (reason) {
            case PLAYER_LEFT_IN_GAME -> {
                GameState newState = new GameState(this.getGameState());

                if(playerA != null && this.gameState.getPlayerA().isInGame()) {
                    newState.setWinner(playerA.getId());
                    System.out.println("Player A won");
                    playerA.sendMessage(new GameOverMessage(newState));
                }
                if(playerB != null && this.gameState.getPlayerB().isInGame()) {
                    newState.setWinner(playerB.getId());
                    System.out.println("Player B won");
                    playerB.sendMessage(new GameOverMessage(newState));
                }

                this.gameState = newState;
            }
            case NO_MORE_MOVES -> {
                GameState newState = new GameState(this.getGameState());

                if (playerA != null && playerA.isInGame()) {
                    newState.setWinner(playerA.getId());
                    newState.setWinner(playerB.getId());
                    playerA.sendMessage(new GameOverMessage(newState));
                    playerB.sendMessage(new GameOverMessage(newState));
                }
                this.gameState = newState;
            }
            case TIMEOUT -> {
                GameState newState = new GameState(this.getGameState());

                if (playerA != null && playerA.isInGame()) {
                    playerA.sendMessage(new GameOverMessage(newState));
                    playerB.sendMessage(new GameOverMessage(newState));
                }

                this.gameState = newState;
            }
            case PLAYER_WON -> {

                GameState newState = new GameState(this.getGameState());

                boolean hasPlayerAWon = this.gameState.hasPlayerSunkAllShips(newState.getPlayerA(), this.shipsPlayerB);
                boolean hasPlayerBWon = this.gameState.hasPlayerSunkAllShips(newState.getPlayerB(), this.shipsPlayerA);

                if(hasPlayerAWon) {
                    System.out.println("Player A won");
                    newState.setWinner(playerA.getId());
                } else if(hasPlayerBWon) {
                    System.out.println("Player B won");
                    newState.setWinner(playerB.getId());
                }

                playerA.sendMessage(new GameOverMessage(newState));
                playerB.sendMessage(new GameOverMessage(newState));

                this.gameState = newState;
            }
            case PLAYER_LEFT_LOBBY -> {
                //No action needed, the game will be unregistered automatically because the game status is set to GAME_OVER
            }
        }

        server.updateGameList();
    }

    @Override
    public void onPlayerAttemptMove(PlayerInfo player, Move move) {
        if (!this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        MoveManager moveManager = new MoveManager(gameState);
        if (!moveManager.isPlayerMoveMoveValid(player.getId(), move)) {
            player.sendMessage(new ErrorMessage(ErrorType.INVALID_MOVE));
            return;
        }

        GameState newState = new GameState(this.getGameState());

        move.computeAffectedCells(this.size);

        if (playerA != null && playerA.getId().equals(player.getId())) {
            newState.addMove(playerA.getId(), move);
        } else if (playerB != null && playerB.getId().equals(player.getId())) {
            newState.addMove(playerB.getId(), move);
        }

        this.playerTurnMadeMove = true;

        boolean moveIsHit = MoveManager.moveHasHit(
                player.getId().equals(playerA.getId()) ? this.shipsPlayerB : this.shipsPlayerA,
                move
        );

        System.out.println("Move: " + move.getX() + " - " + move.getY() + " - Is a Hit: " + moveIsHit);

        if (moveIsHit) {
            newState.getCurrentTurnPlayer().addEnergy(Parameters.ENERGY_SHIP_HIT);

            long buffer =  (Parameters.HIT_BONUS_TIME_IN_SECONDS * 1000);
            Date lastDate = newState.getPlayersTurnEnd().after(new Date()) ? (newState.getPlayersTurnEnd()) : new Date();

            newState.setPlayersTurnEnd(new Date(buffer + lastDate.getTime()));
            System.out.println("Extending turn time by " + Parameters.HIT_BONUS_TIME_IN_SECONDS + " seconds");
            System.out.println("New turn end: " + newState.getPlayersTurnEnd().toString());
        } else {
            newState.setPlayersTurnEnd(new Date(newState.getPlayersTurnEnd().getTime() + 1000));

            turnDelayTimer = new Timer();
            turnDelayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendTurnChangeEvent(); // Send turn change event after the timer expires
                }
            }, 1000);
        }

        ArrayList<Ship> uncoveredShipsA = newState.getSunkenShips(newState.getPlayerA(), this.shipsPlayerB);
        ArrayList<Ship> uncoveredShipsB = newState.getSunkenShips(newState.getPlayerB(), this.shipsPlayerA);

        System.out.println("Player A: " + uncoveredShipsA.size());
        System.out.println("Player B: " + uncoveredShipsB.size());

        newState.getPlayerA().setUncoveredShips(uncoveredShipsA);
        newState.getPlayerB().setUncoveredShips(uncoveredShipsB);

        //newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
        newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
        newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);

        newState.setPlayersTurnEnd(new Date(newState.getPlayersTurnEnd().getTime() + 1000));

        if (playerA != null)
            playerA.sendMessage(new MoveMadeMessage(newState));
        if (playerB != null)
            playerB.sendMessage(new MoveMadeMessage(newState));

        this.gameState = newState;
        server.updateGameList();

        checkForWinner();
    }

    private void checkForWinner() {
        // Check if the game is over
        boolean hasPlayerAWon = this.gameState.hasPlayerSunkAllShips(this.gameState.getPlayerA(), this.shipsPlayerB);
        boolean hasPlayerBWon = this.gameState.hasPlayerSunkAllShips(this.gameState.getPlayerB(), this.shipsPlayerA);

        if(hasPlayerAWon || hasPlayerBWon) {
            gameOverTimer = new Timer();

            gameOverTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Game Ended by player win");
                    sendGameOverEvent(GameOverReason.PLAYER_WON);
                }
            }, 500);
        }
    }

    public int getPlayerAmount() {
        int playerCount = 0;
        if (gameState.getPlayerA().isInGame()) playerCount++;
        if (gameState.getPlayerB().isInGame()) playerCount++;
        return playerCount;
    }

    private synchronized void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void initAvailableShips() {
        if (!availableShips.isEmpty()) return;

        availableShips.add(new Ship(0, Ship.Orientation.NORTH, 5, 1));
        availableShips.add(new Ship(1, Ship.Orientation.NORTH, 4, 1));
        availableShips.add(new Ship(2, Ship.Orientation.NORTH, 3, 1));
        availableShips.add(new Ship(3, Ship.Orientation.NORTH, 2, 2));
        availableShips.add(new Ship(4, Ship.Orientation.NORTH, 2, 1));
        availableShips.add(new Ship(5, Ship.Orientation.NORTH, 6, 1));
    }

    @Override
    public GameState getGameState() {
        return this.gameState;
    }

    @Override
    public ArrayList<Ship> getAvailableShips() {
        return availableShips;
    }

    @Override
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Broadcasts a message to both players in the game.
     * @param message The message to be broadcast.
     */
    private void broadcastMessage(Message message) {
        if (playerA != null) playerA.sendMessage(message);
        if (playerB != null) playerB.sendMessage(message);
    }

    private void unregisterGame() {
        server.unregisterGame(this.gameState.getId());
        server.updateGameList();
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