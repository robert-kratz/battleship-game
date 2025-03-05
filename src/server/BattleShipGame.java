package server;

import protocol.*;
import protocol.game.Move;
import protocol.messages.*;

import java.util.*;

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
        sleep(1000);

        while (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) {

            System.out.println("Build Game Board");
            if(this.gameState.getBuildGameBoardFinished().before(new Date())) {

                System.out.println("Build Game Board Finished");

                System.out.println("Player A: " + gameState.hasPlayerASubmittedPlacement());
                System.out.println("Player B: " + gameState.hasPlayerBSubmittedPlacement());

                if(!this.gameState.hasPlayerASubmittedPlacement()) {
                    System.out.println("Player A did not submit placement");
                    this.shipsPlayerA = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips);
                    this.gameState.playerSubmitPlacement(playerA.getId());
                }

                if(!this.gameState.hasPlayerBSubmittedPlacement()) {
                    System.out.println("Player B did not submit placement");
                    this.shipsPlayerB = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips);
                    this.gameState.playerSubmitPlacement(playerB.getId());
                }

                this.playerA.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerA));
                this.playerB.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerB));

                GameState gameState = new GameState(this.getGameState());

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

                gameState.setPlayersTurnStart(start);
                gameState.setPlayersTurnEnd(end);

                gameState.setStatus(GameState.GameStatus.IN_GAME);

                gameState.setNextTurn();

                this.playerA.sendMessage(new GameStateUpdateMessage(gameState));
                this.playerB.sendMessage(new GameStateUpdateMessage(gameState));

                this.gameState = gameState;
            }
        }

        while (gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {

            System.out.println();

            //This triggers the next turn
            if(this.gameState.getPlayersTurnEnd().before(new Date())) {
                GameState gameState = new GameState(this.getGameState());

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

                gameState.setPlayersTurnStart(start);
                gameState.setPlayersTurnEnd(end);

                gameState.addEnergy(gameState.isPlayerATurn() ? gameState.getPlayerA() : gameState.getPlayerB(), Parameters.ENERGY_TURN_BONUS);

                gameState.setNextTurn();

                System.out.println("Changed round to " + gameState.getCurrentGameRound() + " and player " + (gameState.isPlayerATurn() ? "A" : "B") + " turn");

                //check if player who's turn it is has submitted their move. this can be done by comparing the this.gameState.getHitsA()

                //Check
                System.out.println("Player A: " + this.gameState.getMoveA().size() + " - " + this.gameState.getCurrentGameRound());
                System.out.println("Player B: " + this.gameState.getMoveB().size() + " - " + this.gameState.getCurrentGameRound());

                MoveManager moveManager = new MoveManager(gameState);

                if(!moveManager.isAMoveStillPossible()) {
                    System.out.println("No more moves possible");
                    endGamePhase();
                    break;
                }

                if(this.gameState.isPlayerATurn() && this.gameState.getMoveA().size() < this.gameState.getCurrentGameRound()) {

                    System.out.println("Player A did not submit move");

                    Move move = moveManager.makeRandomMove(playerA.getId());

                    gameState.addMove(playerA.getId(), move);
                    gameState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
                    gameState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
                    gameState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);

                    System.out.println("Move: " + move.getX() + " - " + move.getY());

                } else if(!this.gameState.isPlayerATurn() && this.gameState.getMoveB().size() < this.gameState.getCurrentGameRound()) {
                    System.out.println("Player B did not submit move");

                    Move move = moveManager.makeRandomMove(playerB.getId());

                    System.out.println("Move: " + move.getX() + " - " + move.getY());

                    gameState.addMove(playerB.getId(), move);
                    gameState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
                    gameState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
                    gameState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);
                }

                this.playerA.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerA));
                this.playerB.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerB));

                this.gameState = gameState;
            }

        }

        if (gameState.getStatus().equals(GameState.GameStatus.GAME_OVER)) {
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
    public synchronized void addPlayer(PlayerInfo player) {
        //ONLY TRIGGER IF GAME IS IN LOBBY WAITING
        if(!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

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
    }

    private synchronized void startBuildPhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date(System.currentTimeMillis() + Parameters.TIMER_BEFORE_LOBBY_START * 1000);

        GameState gameState = new GameState(this.getGameState());

        gameState.setBuildGameBoardStarted(date);
        gameState.setBuildGameBoardFinished(new Date(date.getTime() + (Parameters.BUILD_TIME_IN_SECONDS * 1000)));

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
    public void playerMove(PlayerInfo player, Move move) {
        if (!this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        MoveManager moveManager = new MoveManager(gameState);
        if (!moveManager.isPlayerMoveMoveValid(player.getId(), move)) {
            player.sendMessage(new ErrorMessage(ErrorType.INVALID_MOVE));
            return;
        }

        // Create a new game state copy to work on
        GameState newState = new GameState(this.getGameState());

        move.computeAffectedCells(this.size);

        // Add the move to the game state for the current player
        if (playerA.getId().equals(player.getId())) {
            newState.addMove(playerA.getId(), move);
        } else if (playerB.getId().equals(player.getId())) {
            newState.addMove(playerB.getId(), move);
        }

        // Determine if the move results in a hit by checking against the opponent’s ships
        boolean moveIsHit = MoveManager.moveHasHit(
                player.getId().equals(playerA.getId()) ? this.shipsPlayerB : this.shipsPlayerA,
                move
        );

        System.out.println("Move: " + move.getX() + " - " + move.getY() + " - Is a Hit: " + moveIsHit);

        if (moveIsHit) {
            newState.addEnergy(player.getId(), Parameters.ENERGY_SHIP_HIT);
        }

        // Update the hit-related information
        newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
        newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
        newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);

        newState.setPlayersTurnEnd(new Date(newState.getPlayersTurnEnd().getTime() + 1000));

        // Immediately send update message with the hit result (but without changing turn)
        this.playerA.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerA));
        this.playerB.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerB));

        // Schedule a delayed task (1 second delay) to switch turn and update the game state further
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Create a new game state copy for the turn change
                GameState stateWithTurn = new GameState(newState);

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);
                stateWithTurn.setPlayersTurnStart(start);
                stateWithTurn.setPlayersTurnEnd(end);

                stateWithTurn.setNextTurn();

                // Recalculate hit info (if needed)
                stateWithTurn.uncoverHitShips(shipsPlayerA, shipsPlayerB);
                stateWithTurn.updateHitList(shipsPlayerA, shipsPlayerB);
                stateWithTurn.loadRadars(shipsPlayerA, shipsPlayerB);

                System.out.println("Changed round to " + stateWithTurn.getCurrentGameRound() +
                        " and player " + (stateWithTurn.isPlayerATurn() ? "A" : "B") + " turn");

                // Send the update message that enforces the board switch
                playerA.sendMessage(new GameUpdateMessage(stateWithTurn, shipsPlayerA));
                playerB.sendMessage(new GameUpdateMessage(stateWithTurn, shipsPlayerB));

                // Update the main game state reference
                gameState = stateWithTurn;
            }
        }, 1000);

        // Immediately update the current game state to reflect the hit result.
        this.gameState = newState;
    }

    @Override
    public void leaveGame(PlayerInfo player) {
        GameState gameState = new GameState(this.getGameState());

        gameState.setStatus(GameState.GameStatus.GAME_OVER);

        PlayerInfo winner = player.getId().equals(playerA.getId()) ? playerB : playerA;

        gameState.setWinner(winner.getId());

        winner.sendMessage(new GameStateUpdateMessage(gameState));

        this.gameState = gameState;

        server.unregisterGame(this.gameState.getId());
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
