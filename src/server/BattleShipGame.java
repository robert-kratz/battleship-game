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

    private ArrayList<Ship> shipsPlayerA = new ArrayList<>();
    private ArrayList<Ship> shipsPlayerB = new ArrayList<>();

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
            if (this.gameState.getBuildGameBoardFinished().before(new Date())) {

                System.out.println("Build Game Board Finished");

                if (gameState.getPlayerA() != null) {
                    System.out.println("Player A: " + gameState.getPlayerA().isReady());
                } else {
                    System.out.println("Player A is null");
                }
                if (gameState.getPlayerB() != null) {
                    System.out.println("Player B: " + gameState.getPlayerB().isReady());
                } else {
                    System.out.println("Player B is null");
                }

                if (gameState.getPlayerA() != null && !gameState.getPlayerA().isReady()) {
                    System.out.println("Player A did not submit placement");
                    this.shipsPlayerA = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips, this.shipsPlayerA);
                    gameState.getPlayerA().setReady(true);
                }

                if (gameState.getPlayerB() != null && !gameState.getPlayerB().isReady()) {
                    System.out.println("Player B did not submit placement");
                    this.shipsPlayerB = ShipPlacementValidator.createRandomizedGameBoard(size, availableShips, this.shipsPlayerB);
                    gameState.getPlayerB().setReady(true);
                }

                if (gameState.getPlayerA() != null)
                    playerA.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerA));
                if (gameState.getPlayerB() != null)
                    playerB.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerB));

                GameState newState = new GameState(this.getGameState());

                Date start = new Date();
                Date end = new Date(start.getTime() + Parameters.SHOOT_TIME_IN_SECONDS * 1000);

                newState.setPlayersTurnStart(start);
                newState.setPlayersTurnEnd(end);

                newState.setStatus(GameState.GameStatus.IN_GAME);

                newState.setNextTurn();

                if (gameState.getPlayerA() != null)
                    playerA.sendMessage(new GameStateUpdateMessage(newState));
                if (gameState.getPlayerB() != null)
                    playerB.sendMessage(new GameStateUpdateMessage(newState));

                this.gameState = newState;
                server.updateGameList();
            }
        }

        // In-Game Phase
        while (gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) {
            sleep(1000);
            System.out.println("Game in progress");

            if (this.gameState.getPlayersTurnEnd().before(new Date())) {
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
                    endGamePhase();
                    break;
                }

                // Beispiel für zufälligen Move, wenn kein Move eingereicht wurde
                if (gameState.getPlayerA() != null && gameState.getPlayerA().isTurn() &&
                        gameState.getPlayerA().getMoves().size() < gameState.getCurrentGameRound()) {
                    System.out.println("Player A did not submit move");

                    Move move = moveManager.makeRandomMove(playerA.getId());
                    newState.addMove(playerA.getId(), move);
                    newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
                    newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
                    newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);
                } else if (gameState.getPlayerB() != null && gameState.getPlayerB().isTurn() &&
                        gameState.getPlayerB().getMoves().size() < gameState.getCurrentGameRound()) {
                    System.out.println("Player B did not submit move");

                    Move move = moveManager.makeRandomMove(playerB.getId());
                    newState.addMove(playerB.getId(), move);
                    newState.uncoverHitShips(this.shipsPlayerA, this.shipsPlayerB);
                    newState.updateHitList(this.shipsPlayerA, this.shipsPlayerB);
                    newState.loadRadars(this.shipsPlayerA, this.shipsPlayerB);
                }

                if (gameState.getPlayerA() != null)
                    playerA.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerA));
                if (gameState.getPlayerB() != null)
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
        if (!this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

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
            if (playerA != null)
                playerA.sendMessage(new GameStateUpdateMessage(newState));
            playerB.sendMessage(new GameStateUpdateMessage(newState));

            this.gameState = newState;
            server.updateGameList();
        }

        if (playerA != null && playerB != null) startBuildPhase();
    }

    private synchronized void startBuildPhase() {
        if (!gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        gameState.setStatus(GameState.GameStatus.BUILD_GAME_BOARD);

        Date date = new Date(System.currentTimeMillis() + Parameters.TIMER_BEFORE_LOBBY_START * 1000);

        GameState newState = new GameState(this.getGameState());

        newState.setBuildGameBoardStarted(date);
        newState.setBuildGameBoardFinished(new Date(date.getTime() + (Parameters.BUILD_TIME_IN_SECONDS * 1000)));

        this.gameState = newState;

        if (playerA != null)
            playerA.sendMessage(new GameStateUpdateMessage(newState));
        if (playerB != null)
            playerB.sendMessage(new GameStateUpdateMessage(newState));
        server.updateGameList();
    }

    private synchronized void endGamePhase() {
        if (!gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        gameState.setStatus(GameState.GameStatus.GAME_OVER);

        GameState newState = new GameState(this.getGameState());

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

        if (playerA != null && playerA.getId().equals(player.getId())) {
            newState.addMove(playerA.getId(), move);
        } else if (playerB != null && playerB.getId().equals(player.getId())) {
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

        if (playerA != null)
            playerA.sendMessage(new GameUpdateMessage(newState, this.shipsPlayerA));
        if (playerB != null)
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

                if (playerA != null)
                    playerA.sendMessage(new GameUpdateMessage(stateWithTurn, shipsPlayerA));
                if (playerB != null)
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
        if(this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING) && getPlayerAmount() != 2) {
            server.unregisterGame(this.gameState.getId());
            server.updateGameList();
            return;
        }

        GameState newState = new GameState(this.getGameState());
        newState.setStatus(GameState.GameStatus.GAME_OVER);

        this.gameState.getPlayer(player.getId()).setInGame(false);

        // Überprüfen, ob beide Spieler vorhanden sind.
        if (playerA != null && playerB != null) {
            PlayerInfo winner = player.getId().equals(playerA.getId()) ? playerB : playerA;
            newState.setWinner(winner.getId());
            winner.sendMessage(new GameStateUpdateMessage(newState));
        } else if (playerA != null) {
            newState.setWinner(playerA.getId());
            playerA.sendMessage(new GameStateUpdateMessage(newState));
        } else if (playerB != null) {
            newState.setWinner(playerB.getId());
            playerB.sendMessage(new GameStateUpdateMessage(newState));
        }

        this.gameState = newState;

        if (playerA != null) {
            playerA.setInGame(false);
        }
        if (playerB != null) {
            playerB.setInGame(false);
        }

        server.unregisterGame(this.gameState.getId());
        server.updateGameList();
    }

    @Override
    public void onPlayerPlaceShips(PlayerInfo player, ArrayList<Ship> ships) {
        System.out.println("Received placement from player " + player.getUsername());

        // VALIDIERUNG DER SCHIFFSPLATZIERUNG
        boolean validatePlacement = ShipPlacementValidator.shipsAreShipsTheSame(this.gameState.getAvailableShips(), ships, this.size);

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

        GameState gameState = new GameState(this.getGameState());

        if (playerA != null)
            playerA.sendMessage(new GameStateUpdateMessage(gameState));
        if (playerB != null)
            playerB.sendMessage(new GameStateUpdateMessage(gameState));

        System.out.println("Player " + player.getUsername() + " submitted placement");
    }

    @Override
    public void onPlayerReadyStateChange(PlayerInfo player, boolean ready) {
        GameState gameState = new GameState(this.getGameState());

        if (playerA != null && playerA.getId().equals(player.getId())) {
            if (ShipPlacementValidator.shipsAreAllPlacedAndTheSame(gameState.getAvailableShips(), this.shipsPlayerA, this.size)) {
                gameState.getPlayerA().setReady(ready);
            } else {
                player.sendMessage(new ErrorMessage(ErrorType.INVALID_PLACEMENT));
                return;
            }
        } else if (playerB != null && playerB.getId().equals(player.getId())) {
            if (ShipPlacementValidator.shipsAreAllPlacedAndTheSame(gameState.getAvailableShips(), this.shipsPlayerB, this.size)) {
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
            playerA.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerA));
        if (playerB != null)
            playerB.sendMessage(new GameUpdateMessage(gameState, this.shipsPlayerB));

        // Optional: Ausgabe, wenn beide Spieler ready sind
        if (gameState.getPlayerA() != null && gameState.getPlayerB() != null &&
                gameState.getPlayerA().isReady() && gameState.getPlayerB().isReady()) {
            System.out.println("Both players are ready");

            //Skip build phase if both players are ready
            gameState.setBuildGameBoardFinished(new Date(System.currentTimeMillis() + 1000));
        }

        this.gameState = gameState;
    }

    public int getPlayerAmount() {
        int playerCount = 0;
        if (playerA != null) playerCount++;
        if (playerB != null) playerCount++;
        return playerCount;
    }

    public void setPlayerShips(UUID playerId, ArrayList<Ship> ships) {
        if (playerA != null && playerA.getId().equals(playerId)) {
            this.shipsPlayerA = ships;
            if (ships.size() == availableShips.size()) {
                this.gameState.getPlayerA().setReady(true);
            }
        } else if (playerB != null && playerB.getId().equals(playerId)) {
            this.shipsPlayerB = ships;
            if (ships.size() == availableShips.size()) {
                this.gameState.getPlayerB().setReady(true);
            }
        }
    }

    public synchronized void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
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