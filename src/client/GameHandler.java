package client;

import protocol.ErrorType;
import protocol.Ship;
import protocol.game.Cell;
import protocol.game.Move;
import protocol.messages.*;
import protocol.GameState;

import java.util.ArrayList;
import java.util.UUID;

public class GameHandler implements GameClient {

    private GameState gameState;
    private final ClientHandler clientHandler;

    private ArrayList<Ship> playersShips;

    private boolean isInGame = false;

    /**
     * Initializes the GameHandler
     * @param clientHandler the client handler
     * @param gameState the game state from the GameStateUpdateMessage received from the server
     */
    public GameHandler(ClientHandler clientHandler, GameState gameState) {
        this.clientHandler = clientHandler;

        this.gameState = gameState;
    }

    /**
     * Game logic
     */

    @Override
    public void onGameStateUpdate(GameStateUpdateMessage gameStateUpdateMessage) {

        boolean gameStateUpdated = gameStateUpdateMessage.getGameState().getStatus() != gameState.getStatus();

        this.gameState = gameStateUpdateMessage.getGameState();

        System.out.println("Game state updated: " + gameState.getStatus());

        System.out.println("Player A ready: " + gameState.getPlayerA().isReady());
        System.out.println("Player B ready: " + gameState.getPlayerA().isReady());

        //ONLY EXECUTE IF GAME STATE HAS CHANGED
        if(gameStateUpdated) {
            switch (gameState.getStatus()) {
                //Trigger when someone joins the lobby
                case LOBBY_WAITING -> { }
                //Trigger when game build phase starts
                case BUILD_GAME_BOARD -> {

                    if(!isInGame) {
                        if(this.gameState.getPlayerCount() == 2) {
                            UUID userId = this.clientHandler.getUserId();
                            String opponentName = this.gameState.getOpponent(userId).getName();

                            this.clientHandler.getStageManager().gameWaitingScene.setOpponentName(opponentName);
                        }

                        this.clientHandler.getStageManager().gameWaitingScene.setGameStartTime(this.gameState.getBuildGameBoardStarted());
                    }
                }
                case IN_GAME -> {
                    if(this.clientHandler.getStageManager().gameIngameScene == null) {
                        System.out.println("Starting in game scene");
                        this.clientHandler.getStageManager().startInGameScene(this.playersShips);
                    }
                }
                case GAME_OVER -> {
                    if(this.clientHandler.getStageManager().gameOverScene == null) {
                        System.out.println("Starting game over scene");
                        this.clientHandler.getStageManager().startGameOverScene();
                    }
                }
            }
        }

        //ALWAYS EXECUTE
        switch (gameState.getStatus()) {
            case LOBBY_WAITING -> {
            }
            case BUILD_GAME_BOARD -> {
                if(this.clientHandler.getStageManager().gameBuildScene != null) {
                    boolean isOpponentReady = this.gameState.getOpponent(this.clientHandler.getUserId()).isReady();

                    this.clientHandler.getStageManager().gameBuildScene.setOpponentState(isOpponentReady);
                }
            }
            case IN_GAME -> {
                if (this.clientHandler.getStageManager().gameIngameScene != null) {
                    boolean isPlayerATurn = gameState.isPlayersTurn(this.clientHandler.getUserId());

                    this.clientHandler.getStageManager().gameIngameScene.toggleTurn(isPlayerATurn);
                    this.clientHandler.getStageManager().gameIngameScene.extendCurrentTurn(gameState.getPlayersTurnEnd());
                    this.clientHandler.getStageManager().gameIngameScene.setPlayerEnergy(gameState.getPlayer(this.clientHandler.getUserId()).getEnergy());
                }
            }
        }
    }

    /**
     * Triggers when the opponent hovers his mouse over a tile during his turn
     * @param playerHoverMessage The message containing the player's hover information
     */
    @Override
    public void onPlayerHoverEvent(PlayerHoverMessage playerHoverMessage) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            return;
        }

        System.out.println("Player hover event received");

        //Forward the hover event to the game scene only if the uuid in the message is the opponent's
        boolean isPlayersTurn = gameState.isPlayersTurn(this.clientHandler.getUserId());

        if(isPlayersTurn) return;

        System.out.println("Setting opponent hover");

        clientHandler.getStageManager().gameIngameScene.setOpponentHover(playerHoverMessage.getX(), playerHoverMessage.getY(), playerHoverMessage.getAffectedFields());
    }

    /**
     * Update the game state during the in-game phase
     * @param gameState The updated game state
     * @param yourShips The ships of the player
     */
    @Override
    public void onGameUpdate(GameState gameState, ArrayList<Ship> yourShips) {
        //TODO: Implement in game logic

        System.out.println("a");
        this.playersShips = yourShips;

        boolean playerTurnHasChanged = this.gameState.isPlayersTurn(this.clientHandler.getUserId()) != gameState.isPlayersTurn(this.clientHandler.getUserId());

        System.out.println("PlayerTurn: " + this.gameState.isPlayersTurn(this.clientHandler.getUserId()));

        System.out.println("Player turn has changed: " + playerTurnHasChanged);

        boolean isPlayersTurn = gameState.isPlayersTurn(this.clientHandler.getUserId());

        if(this.getClientHandler().getStageManager().gameIngameScene != null) {
            this.clientHandler.getStageManager().gameIngameScene.toggleTurn(isPlayersTurn);
            this.clientHandler.getStageManager().gameIngameScene.extendCurrentTurn(gameState.getPlayersTurnEnd());
            this.clientHandler.getStageManager().gameIngameScene.setPlayerEnergy(gameState.getPlayer(this.clientHandler.getUserId()).getEnergy());
        }

        //Check weather a move has been made

        boolean moveMade = gameState.getPlayerA().getMoves().size() != this.gameState.getPlayerA().getMoves().size() || gameState.getPlayerB().getMoves().size() != this.gameState.getPlayerB().getMoves().size();

        if (moveMade) {
            System.out.println("Move GUI Update");
            // Prüfe, ob der lokale Spieler Spieler A ist:
            if (clientHandler.getUserId().equals(gameState.getPlayerA().getId())) {
                // Spieler A:
                // - Das Player Board zeigt die Züge des Gegners (moveB, da diese auf A's Schiffe zielen)
                // - Das Opponent Board zeigt die eigenen Züge (moveA)
                clientHandler.getStageManager().gameIngameScene.playerBoard.setMoves(gameState.getPlayerB().getMoves());
                clientHandler.getStageManager().gameIngameScene.opponentBoard.setMoves(gameState.getPlayerA().getMoves());
            } else {
                // Andernfalls (lokaler Spieler ist Spieler B):
                // - Das Player Board zeigt die Züge des Gegners (moveA, da diese auf B's Schiffe zielen)
                // - Das Opponent Board zeigt die eigenen Züge (moveB)
                clientHandler.getStageManager().gameIngameScene.playerBoard.setMoves(gameState.getPlayerA().getMoves());
                clientHandler.getStageManager().gameIngameScene.opponentBoard.setMoves(gameState.getPlayerB().getMoves());
            }
        }

        this.gameState = gameState;
    }

    /**
     * Handle the error message by showing it to the user in a dialog
     * @param message The error message
     */
    @Override
    public void onGameError(ErrorMessage message) {
        clientHandler.showError(message.getError().getMessage());
        if(message.getError().equals(ErrorType.NO_GAME_IN_PROGRESS)) {
            sendLeaveGame();
            this.clientHandler.getStageManager().exitGameScene();
        }
    }

    /**
     * Send the hover event to the server
     * @param x The x coordinate of the tile
     * @param y The y coordinate of the tile
     */
    @Override
    public void sendPlayerHoverEvent(int x, int y, ArrayList<Cell> affectedFields) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            return;
        }
        // Deep copy der affectedFields, um spätere Änderungen zu vermeiden:
        ArrayList<Cell> copy = new ArrayList<>();
        if(affectedFields != null) {
            for (Cell c : affectedFields) {
                // Voraussetzung: Die Klasse Cell hat einen geeigneten Kopierkonstruktor oder Getter
                copy.add(new Cell(c.getX(), c.getY()));
            }
        }
        clientHandler.sendMessage(new PlayerHoverMessage(this.clientHandler.getUserId(), x, y, copy));
    }

    /**
     * Submit the placement of ships to the server
     * @param ships The ships to be placed
     */
    @Override
    public void sendUpdateBuildBoardMessage(ArrayList<Ship> ships) {
        if(gameState.getStatus() != GameState.GameStatus.BUILD_GAME_BOARD) {
            return;
        }

        clientHandler.sendMessage(new UpdateBuildBoardMessage(ships));
    }

    @Override
    public void sendPlayerReadyMessage(boolean ready) {
        if(gameState.getStatus() != GameState.GameStatus.BUILD_GAME_BOARD) {
            return;
        }

        //Check if all ships are placed before sending the ready message
        if(ready && this.playersShips != null && this.playersShips.size() != this.gameState.getAvailableShips().size()) {
            clientHandler.showError("You have to place all ships before you can start the game.");
            return;
        }

        clientHandler.sendMessage(new PlayerReadyMessage(ready));
    }

    /**
     * Send the move to the server
     * @param move The move object containing the move
     */
    @Override
    public void sendGameMoveEvent(Move move) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            return;
        }

        clientHandler.sendMessage(new PlayerMoveMessage(move));
    }

    /**
     * Leave the game
     */
    @Override
    public void sendLeaveGame() {
        clientHandler.sendMessage(new LeaveGameMessage());
        this.isInGame = false;
        this.clientHandler.endCurrentGame(); //Removes the game handler from the client handler and switch scene to lobby
    }

    public void startBuildPhase() {

        if(this.gameState.getStatus() == GameState.GameStatus.IN_GAME || this.gameState.getStatus() == GameState.GameStatus.GAME_OVER) return;

        this.getClientHandler().getStageManager().startBuildScene();

        boolean isOpponentReady = this.getGameState().getOpponent(this.getClientHandler().getUserId()).isReady();

        if(clientHandler.getStageManager().gameBuildScene != null) {
            this.getClientHandler().getStageManager().gameBuildScene.setOpponentState(isOpponentReady);

            this.isInGame = true;
        }
    }

    public ArrayList<Ship> getPlayersShips() {
        return playersShips;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public GameState getGameState() {
        return gameState;
    }
}
