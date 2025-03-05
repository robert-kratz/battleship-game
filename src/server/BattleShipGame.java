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

        // Build-Phase
        while (this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) {
            sleep(1000);

            System.out.println("Build Game Board");
            if(this.gameState.getBuildGameBoardFinished().before(new Date())) {

                System.out.println("Build Game Board Finished");

                System.out.println("Player A: " + gameState.getPlayerA().isReady());
                System.out.println("Player B: " + gameState.getPlayerB().isReady());

                if(!this.gameState.getPlayerA().isReady()) {
                    System.out.println("Player A did not submit placement");
                    this.shipsPlayerA = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips);
                    this.gameState.getPlayerA().setReady(true);
                }

                if(!this.gameState.getPlayerB().isReady()) {
                    System.out.println("Player B did not submit placement");
                    this.shipsPlayerB = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips);
                    this.gameState.getPlayerB().setReady(true);
                }

                playerA.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerA));
                playerB.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerB));

                GameState newState = new GameState(this.getGameState());

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

                newState.setPlayersTurnStart(start);
                newState.setPlayersTurnEnd(end);

                newState.setStatus(GameState.GameStatus.IN_GAME);

                newState.setNextTurn();

                playerA.sendMessage(new GameStateUpdateMessage(newState));
                playerB.sendMessage(new GameStateUpdateMessage(newState));

                this.gameState = newState;
                server.updateGameList();
            }
        }

        // In-Game Phase
        while (gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sleep(1000);
            System.out.println( "Game in progress");

            if(this.gameState.getPlayersTurnEnd().before(new Date())) {
                GameState newState = new GameState(this.getGameState());

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

                newState.setPlayersTurnStart(start);
                newState.setPlayersTurnEnd(end);

                newState.getCurrentTurnPlayer().addEnergy(Parameters.ENERGY_TURN_BONUS);

                newState.setNextTurn();

                System.out.println("Player A: " + this.gameState.getPlayerA().getMoves().size() + " - " + this.gameState.getCurrentGameRound());
                System.out.println("Player B: " + this.gameState.getPlayerB().getMoves().size() + " - " + this.gameState.getCurrentGameRound());

                MoveManager moveManager = new MoveManager(newState);

                if(!moveManager.isAMoveStillPossible()) {
                    System.out.println("No more moves possible");
                    endGamePhase();
                    break;
                }

                //TODO CHANGE THIS TO THE LOGIC OF HOW MANY MOVES A PLAYER CAN MAKE PER ROUND

                if(this.gameState.getPlayerA().isTurn() && this.gameState.getPlayerA().getMoves().size() < this.gameState.getCurrentGameRound()) {

                    System.out.println("Player A did not submit move");

                    Move move = moveManager.makeRandomMove(playerA.getId());

                    newState.addMove(playerA.getId(), move);
                    newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
                    newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
                    newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);

                    System.out.println("Move: " + move.getX() + " - " + move.getY());

                } else if(this.gameState.getPlayerB().isTurn() && this.gameState.getPlayerB().getMoves().size() < this.gameState.getCurrentGameRound()) {
                    System.out.println("Player B did not submit move");

                    Move move = moveManager.makeRandomMove(playerB.getId());

                    System.out.println("Move: " + move.getX() + " - " + move.getY());

                    newState.addMove(playerB.getId(), move);
                    newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
                    newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
                    newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);
                }

                playerA.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerA));
                playerB.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerB));

                this.gameState = newState;
                server.updateGameList();
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
        if(!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        if (playerA == null) {
            playerA = player;
            GameState newState = new GameState(this.getGameState());
            newState.setPlayerA(new ClientPlayer(playerA.getId(), playerA.getUsername()));

            System.out.println("Player A: " + playerA.getUsername());

            playerA.sendMessage(new JoinGameMessage(newState));
            playerA.sendMessage(new GameStateUpdateMessage(newState));

            this.gameState = newState;
            server.updateGameList();
        } else if (playerB == null) {
            playerB = player;
            GameState newState = new GameState(this.getGameState());
            newState.setPlayerB(new ClientPlayer(playerB.getId(), playerB.getUsername()));

            System.out.println("Player B: " + playerB.getUsername());

            playerB.sendMessage(new JoinGameMessage(newState));
            playerA.sendMessage(new GameStateUpdateMessage(newState));
            playerB.sendMessage(new GameStateUpdateMessage(newState));

            this.gameState = newState;
            server.updateGameList();
        }

        if(playerA != null && playerB != null) startBuildPhase();
    }

    private synchronized void startBuildPhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date(System.currentTimeMillis() + Parameters.TIMER_BEFORE_LOBBY_START * 1000);

        GameState newState = new GameState(this.getGameState());

        newState.setBuildGameBoardStarted(date);
        newState.setBuildGameBoardFinished(new Date(date.getTime() + (Parameters.BUILD_TIME_IN_SECONDS * 1000)));

        this.gameState = newState;

        playerA.sendMessage(new GameStateUpdateMessage(newState));
        playerB.sendMessage(new GameStateUpdateMessage(newState));
        server.updateGameList();
    }

    private synchronized void endGamePhase() {
        if(!gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        gameState.setStatus(GameState.GameStatus.GAME_OVER);

        Date date = new Date();

        GameState newState = new GameState(this.getGameState());
        // End Game-Logik

        this.gameState = newState;
        server.updateGameList();
    }

    @Override
    public void playerMove(PlayerInfo player, Move move) {
        if (!this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        MoveManager moveManager = new MoveManager(gameState);
        if (!moveManager.isPlayerMoveMoveValid(player.getId(), move)) {
            player.sendMessage(new ErrorMessage(ErrorType.INVALID_MOVE));
            return;
        }

        GameState newState = new GameState(this.getGameState());

        move.computeAffectedCells(this.size);

        if (playerA.getId().equals(player.getId())) {
            newState.addMove(playerA.getId(), move);
        } else if (playerB.getId().equals(player.getId())) {
            newState.addMove(playerB.getId(), move);
        }

        boolean moveIsHit = MoveManager.moveHasHit(
                player.getId().equals(playerA.getId()) ? this.shipsPlayerB : this.shipsPlayerA,
                move
        );

        System.out.println("Move: " + move.getX() + " - " + move.getY() + " - Is a Hit: " + moveIsHit);

        if (moveIsHit) {
            newState.getCurrentTurnPlayer().addEnergy(Parameters.ENERGY_SHIP_HIT);
        }

        newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
        newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
        newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);

        newState.setPlayersTurnEnd(new Date(newState.getPlayersTurnEnd().getTime() + 1000));

        playerA.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerA));
        playerB.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerB));

        // Verzögerte Ausführung zum Wechseln des Turns
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GameState stateWithTurn = new GameState(newState);

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);
                stateWithTurn.setPlayersTurnStart(start);
                stateWithTurn.setPlayersTurnEnd(end);

                stateWithTurn.setNextTurn();

                stateWithTurn.uncoverHitShips(shipsPlayerA, shipsPlayerB);
                stateWithTurn.updateHitList(shipsPlayerA, shipsPlayerB);
                stateWithTurn.loadRadars(shipsPlayerA, shipsPlayerB);

                playerA.sendMessage(new GameUpdateMessage(stateWithTurn, shipsPlayerA));
                playerB.sendMessage(new GameUpdateMessage(stateWithTurn, shipsPlayerB));

                server.updateGameList();

                gameState = stateWithTurn;
            }
        }, 1000);

        this.gameState = newState;
        server.updateGameList();
    }

    @Override
    public void leaveGame(PlayerInfo player) {
        GameState newState = new GameState(this.getGameState());

        newState.setStatus(GameState.GameStatus.GAME_OVER);

        PlayerInfo winner = player.getId().equals(playerA.getId()) ? playerB : playerA;

        newState.setWinner(winner.getId());

        winner.sendMessage(new GameStateUpdateMessage(newState));

        this.gameState = newState;

        player.setInGame(false);

        server.unregisterGame(this.gameState.getId());
        server.updateGameList();
    }

    @Override
    public void onPlayerPlaceShips(PlayerInfo player, ArrayList<Ship> ships) {

    }

    @Override
    public void onPlayerReadyStateChange(PlayerInfo player, boolean ready) {

    }

    public void setPlayerShips(UUID playerId, ArrayList<Ship> ships) {
        if(playerA.getId().equals(playerId)) {
            this.shipsPlayerA = ships;

            if(ships.size() == availableShips.size()) {
                this.gameState.getPlayerA().setReady(true);
            }
        } else if(playerB.getId().equals(playerId)) {
            this.shipsPlayerB = ships;

            if(ships.size() == availableShips.size()) {
                this.gameState.getPlayerB().setReady(true);
            }
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