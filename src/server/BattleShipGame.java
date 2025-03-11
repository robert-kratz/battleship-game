package server;

import protocol.*;
import protocol.game.Move;
import protocol.messages.*;
import protocol.messages.game.building.GameBuildingStartMessage;
import protocol.messages.game.ingame.GameInGameStartMessage;
import protocol.messages.game.GameOverMessage;
import protocol.messages.game.JoinGameMessage;
import protocol.messages.game.building.PlayerReadyStateChangeMessage;
import protocol.messages.game.ingame.MoveMadeMessage;
import protocol.messages.game.ingame.PlayerTurnChangeMessage;

import java.util.*;

public class BattleShipGame implements Game, Runnable {

    private Server server;

    private ServerPlayer playerA, playerB;
    private GameState gameState = null;
    private final int size;

    private final ArrayList<Ship> availableShips = new ArrayList<>();

    private boolean playerTurnMadeMove = false;
    private boolean allowAnotherMove = true;

    private ArrayList<Ship> shipsPlayerA = new ArrayList<>();
    private ArrayList<Ship> shipsPlayerB = new ArrayList<>();

    private Timer turnDelayTimer;

    /**
     * Constructor for the BattleShipGame class.
     * @param server The server instance that manages the game.
     */
    public BattleShipGame(Server server) {
        this.server = server;

        GameOptions gameOptions = new GameOptions(); // Default game options

        this.size = gameOptions.getBoardSize();

        // Initialize available ships
        initAvailableShips();

        this.gameState = new GameState(gameOptions, availableShips);
    }

    /**
     * Constructor for the BattleShipGame class with custom
     * @param server The server instance that manages the game.
     * @param gameOptions The game options to be used for the game.
     */
    public BattleShipGame(Server server, GameOptions gameOptions) {
        this.server = server;
        this.size = gameOptions.getBoardSize();

        // Initialize available ships
        initAvailableShips();

        this.gameState = new GameState(gameOptions, availableShips);
    }

    /**
     * Main method to run the game loop.
     */
    @Override
    public void run() {
        while (this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) {
            sleep(1000);
            logToConsole("Waiting for players to join");
        }
        // Build-Phase
        while (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) {
            sleep(1000);

            if (this.gameState.getBuildGameBoardFinished().before(new Date())) {

                logToConsole("Build phase finished; Starting In-Game phase");

                sendInGameStartEvent();
            }
        }

        // In-Game Phase
        while (gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sleep(1000);

            if (this.gameState.getPlayersTurnEnd().before(new Date())) {
                logToConsole("Players turn ended; sending turn change event");

                sendTurnChangeEvent();
            }
        }

        if (gameState.getStatus().equals(GameState.GameStatus.GAME_OVER)) {
            logToConsole("Game Over");
            unregisterGame(); // Unregister the game from the server
        }
    }

    /**
     * Adds a player to the game.
     * @param player The player to add.
     */
    @Override
    public synchronized void addPlayer(ServerPlayer player) {
        if (!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        player.setInGame(true);

        if (playerA == null) {
            playerA = player;
            GameState newState = new GameState(this.getGameState());
            newState.setPlayerA(new ClientPlayer(playerA.getId(), playerA.getUsername())); //Create Wrapper for ServerPlayer
            newState.getPlayerA().setEnergy(gameState.getGameOptions().getEnergyGameStart());

            logToConsole("Player A " + playerA.getUsername() + " joined the game");

            playerA.sendMessage(new JoinGameMessage(newState));

            this.gameState = newState;
        } else if (playerB == null) {
            playerB = player;
            GameState newState = new GameState(this.getGameState());
            newState.setPlayerB(new ClientPlayer(playerB.getId(), playerB.getUsername())); //Create Wrapper for ServerPlayer
            newState.getPlayerB().setEnergy(gameState.getGameOptions().getEnergyGameStart());

            logToConsole("Player B " + playerB.getUsername() + " joined the game");

            playerB.sendMessage(new JoinGameMessage(newState));

            this.gameState = newState;
        }

        server.updateGameList();

        //If both players are present, start the game
        if (playerA != null && playerB != null) sendGameStartingEvent();
    }

    /**
     * Removes a player from the game.
     * @param player The player to remove.
     */
    @Override
    public synchronized void removePlayer(ServerPlayer player) {
        this.gameState.getPlayer(player.getId()).setInGame(false);
        player.setInGame(false);

        logToConsole("Player " + player.getUsername() + " left the game");

        if (this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) {
            sendGameOverEvent(GameOverReason.PLAYER_LEFT_LOBBY);
        }

        if (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD) || this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sendGameOverEvent(GameOverReason.PLAYER_LEFT_IN_GAME);
        }
    }

    /**
     * Sends a game starting event to both players.
     */
    @Override
    public synchronized void sendGameStartingEvent() {
        // Only start the game if the game is in the lobby phase
        if (!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date(System.currentTimeMillis() + Parameters.LOBBY_GAME_START_TIME * 1000);

        GameState newState = new GameState(this.getGameState());

        newState.setBuildGameBoardStarted(date);
        newState.setBuildGameBoardFinished(new Date(date.getTime() + (newState.getGameOptions().getBuildTime() * 1000L)));

        broadcastMessage(new GameBuildingStartMessage(newState));

        server.updateGameList();

        this.gameState = newState;
    }

    /**
     * Handles the event when a player hovers over a tile.
     * @param player The player who hovered over the tile.
     * @param ships The ships that the player has.
     */
    @Override
    public void onPlayerPlaceShips(ServerPlayer player, ArrayList<Ship> ships) {

        boolean validatePlacement = ShipPlacementHelper.shipsAreShipsTheSame(this.gameState.getAvailableShips(), ships, this.size);

        if (!validatePlacement) {
            player.sendMessage(new ErrorMessage(ErrorType.INVALID_PLACEMENT));
            return;
        }

        if (playerA != null && playerA.getId().equals(player.getId())) {
            this.shipsPlayerA = ships;
        } else if (playerB != null && playerB.getId().equals(player.getId())) {
            this.shipsPlayerB = ships;
        }
    }

    /**
     * Handles the event when a player changes their ready state.
     * @param player The player to check.
     * @param ready Whether the player is ready or not.
     */
    @Override
    public void onPlayerReadyStateChange(ServerPlayer player, boolean ready) {
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

        if (playerA != null)
            playerA.sendMessage(new PlayerReadyStateChangeMessage(gameState));
        if (playerB != null)
            playerB.sendMessage(new PlayerReadyStateChangeMessage(gameState));

        // Optional: Ausgabe, wenn beide Spieler ready sind
        if (gameState.getPlayerA() != null && gameState.getPlayerB() != null &&
                gameState.getPlayerA().isReady() && gameState.getPlayerB().isReady()) {

            //Skip build phase if both players are ready
            gameState.setBuildGameBoardFinished(new Date(System.currentTimeMillis() + 500));
        }

        this.gameState = gameState;
    }

    /**
     * Handles the event when a player hovers over a tile.
     */
    @Override
    public synchronized void sendInGameStartEvent() {
        //Only start the game if the game is in the build phase
        if(!gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        // Check if both players are ready
        checkForUnplacedShips();

        GameState newState = new GameState(this.getGameState());

        Date start = new Date();
        Date end = new Date(start.getTime() + newState.getGameOptions().getMoveTime() * 1000L);

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
            logToConsole("Player A did not submit placement");
            this.shipsPlayerA = ShipPlacementHelper.createRandomizedGameBoard(size, availableShips, this.shipsPlayerA);
            gameState.getPlayerA().setReady(true);
        }

        if (gameState.getPlayerB() != null && !gameState.getPlayerB().isReady()) {
            logToConsole("Player B did not submit placement");
            this.shipsPlayerB = ShipPlacementHelper.createRandomizedGameBoard(size, availableShips, this.shipsPlayerB);
            gameState.getPlayerB().setReady(true);
        }
    }

    /**
     * Handles the event when a player hovers over a tile.
     */
    @Override
    public void sendTurnChangeEvent() {
        this.allowAnotherMove = true;

        GameState newState = new GameState(this.getGameState());

        Date start = new Date();
        Date end = new Date(start.getTime() + newState.getGameOptions().getMoveTime() * 1000L);

        newState.setPlayersTurnStart(start);
        newState.setPlayersTurnEnd(end);

        newState.getCurrentTurnPlayer().addEnergy(newState.getGameOptions().getEnergyTurnBonus());

        newState.setNextTurn();

        logToConsole("New player turn: " + newState.getCurrentTurnPlayer().getName());

        MoveManager moveManager = new MoveManager(newState);

        if (!moveManager.isAMoveStillPossible()) {
            logToConsole("No more moves possible");
            sendGameOverEvent(GameOverReason.NO_MORE_MOVES);
            return;
        }

        if (gameState.getPlayerA() != null && gameState.getPlayerA().isTurn() &&
                !this.playerTurnMadeMove) {

            logToConsole("Player A did not submit move; placing random move");

            Move move = moveManager.makeRandomMove(playerA.getId());
            newState.addMove(playerA.getId(), move);
            //this.onPlayerAttemptMove(playerA, move);
        } else if (gameState.getPlayerB() != null && gameState.getPlayerB().isTurn() &&
                !this.playerTurnMadeMove) {
            logToConsole("Player B did not submit move; placing random move");

            Move move = moveManager.makeRandomMove(playerB.getId());
            newState.addMove(playerB.getId(), move);
            //this.onPlayerAttemptMove(playerB, move);
        }

        ArrayList<Ship> uncoveredShipsA = newState.getSunkenShips(newState.getPlayerA(), this.shipsPlayerB);
        ArrayList<Ship> uncoveredShipsB = newState.getSunkenShips(newState.getPlayerB(), this.shipsPlayerA);

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

    /**
     * Handles the event when a game is over.
     * @param reason The reason for the game over.
     */
    @Override
    public void sendGameOverEvent(GameOverReason reason) {
        gameState.setStatus(GameState.GameStatus.GAME_OVER);

        if(this.turnDelayTimer != null) {
            this.turnDelayTimer.cancel();
            this.turnDelayTimer = null;
        }

        if(this.playerA != null)this.playerA.setInGame(false);
        if(this.playerB != null) this.playerB.setInGame(false);

        switch (reason) {
            case PLAYER_LEFT_IN_GAME -> {
                GameState newState = new GameState(this.getGameState());

                if(playerA != null && this.gameState.getPlayerA().isInGame()) {
                    newState.setWinner(playerA.getId());
                    logToConsole("Player A (" + playerA.getUsername() + ") won the game");
                    playerA.sendMessage(new GameOverMessage(newState));
                }
                if(playerB != null && this.gameState.getPlayerB().isInGame()) {
                    newState.setWinner(playerB.getId());
                    logToConsole("Player B (" + playerB.getUsername() + ") won the game");
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
                    logToConsole("Player A (" + playerA.getUsername() + ") won the game");
                    newState.setWinner(playerA.getId());
                } else if(hasPlayerBWon) {
                    logToConsole("Player B (" + playerB.getUsername() + ") won the game");
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

    /**
     * Handles the event when a player attempts to make a move.
     * @param player The player who attempted to make a move.
     * @param move The move object containing the move.
     */
    @Override
    public void onPlayerAttemptMove(ServerPlayer player, Move move) {
        if (!this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        if(playerTurnMadeMove && !allowAnotherMove) return;
        this.allowAnotherMove = false;

        MoveManager moveManager = new MoveManager(gameState);
        if (!moveManager.isPlayerMoveMoveValid(player.getId(), move)) {
            player.sendMessage(new ErrorMessage(ErrorType.INVALID_MOVE));
            return;
        }

        GameState newState = new GameState(this.getGameState());

        move.computeAffectedCells(this.size);

        if(move.isItemMove()) {
            int itemCost = move.getSelectedItem().getEnergyCost();

            ClientPlayer clientPlayer = newState.getPlayer(player.getId());

            if(clientPlayer.getEnergy() < itemCost) {
                player.sendMessage(new ErrorMessage(ErrorType.NOT_ENOUGH_ENERGY));
                return;
            }

            clientPlayer.removeEnergy(itemCost);
        }

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

        if (moveIsHit) {
            newState.getCurrentTurnPlayer().addEnergy(newState.getGameOptions().getEnergyShipHit());

            long buffer =  (newState.getGameOptions().getMoveHitTimeBonus() * 1000L);
            Date lastDate = newState.getPlayersTurnEnd().after(new Date()) ? (newState.getPlayersTurnEnd()) : new Date();

            newState.setPlayersTurnEnd(new Date(buffer + lastDate.getTime()));
            this.allowAnotherMove = true;
        } else {
            this.allowAnotherMove = false;

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

        newState.getPlayerA().setUncoveredShips(uncoveredShipsA);
        newState.getPlayerB().setUncoveredShips(uncoveredShipsB);

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

    /**
     * Checks if there is a winner in the game.
     */
    private void checkForWinner() {
        // Check if the game is over
        boolean hasPlayerAWon = this.gameState.hasPlayerSunkAllShips(this.gameState.getPlayerA(), this.shipsPlayerB);
        boolean hasPlayerBWon = this.gameState.hasPlayerSunkAllShips(this.gameState.getPlayerB(), this.shipsPlayerA);

        Timer gameOverTimer;
        if(hasPlayerAWon || hasPlayerBWon) {
            gameOverTimer = new Timer();

            gameOverTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendGameOverEvent(GameOverReason.PLAYER_WON);
                }
            }, 500);
        }

        boolean noMovePossibleA = this.gameState.noMoreMovesPossible(this.gameState.getPlayerA());
        boolean noMovePossibleB = this.gameState.noMoreMovesPossible(this.gameState.getPlayerB());

        if(noMovePossibleA || noMovePossibleB) {
            gameOverTimer = new Timer();

            gameOverTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendGameOverEvent(GameOverReason.NO_MORE_MOVES);
                }
            }, 500);
        }
    }

    /**
     * Sleeps the current thread for a specified amount of time.
     * @param time The time to sleep in milliseconds.
     */
    private synchronized void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Initializes the available ships for the game.
     */
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

    /**
     * Broadcasts a message to both players in the game.
     * @param message The message to be broadcast.
     */
    private void broadcastMessage(Message message) {
        if (playerA != null) playerA.sendMessage(message);
        if (playerB != null) playerB.sendMessage(message);
    }

    /**
     * Unregisters the game from the server.
     */
    private synchronized void unregisterGame() {
        server.unregisterGame(this.gameState.getId());
        server.updateGameList();
    }

    private void logToConsole(String message) {
        System.out.println("[Game " + this.gameState.getId() + "] " + message);
    }

    /**
     * Returns the available ships for the game.
     * @return The list of available ships.
     */
    @Override
    public GameState getGameState() {
        return this.gameState;
    }

    /**
     * Sets the game state for the game.
     * @param gameState The game state to be set.
     */
    @Override
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getSize() {
        return size;
    }

    public ServerPlayer getPlayerA() {
        return playerA;
    }

    public ServerPlayer getPlayerB() {
        return playerB;
    }
}