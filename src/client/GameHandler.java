package client;

import protocol.Ship;
import protocol.game.Hit;
import protocol.messages.*;
import protocol.GameState;

import java.awt.*;
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

        System.out.println("Player A ready: " + gameState.hasPlayerASubmittedPlacement());
        System.out.println("Player B ready: " + gameState.hasPlayerBSubmittedPlacement());

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
                            String opponentName = this.gameState.getOpponentName(userId);

                            this.clientHandler.getStageManager().gameWaitingScene.setOpponentName(opponentName);
                        }

                        this.clientHandler.getStageManager().gameWaitingScene.setGameStartTime(this.gameState.getBuildGameBoardStarted());
                    }
                }
                case IN_GAME -> {
                    this.clientHandler.getStageManager().startInGameScene();
                }
                case GAME_OVER -> {}
            }
        }

        //ALWAYS EXECUTE

        System.out.println("User ID: " + this.clientHandler.getUserId());
        System.out.println("Opponent ID: " + this.gameState.hasOpponentSubmittedPlacement(this.clientHandler.getUserId()));

        switch (gameState.getStatus()) {
            case LOBBY_WAITING -> {
            }
            case BUILD_GAME_BOARD -> {
                if(this.clientHandler.getStageManager().gameBuildScene != null) {
                    boolean isOpponentReady = this.gameState.hasOpponentSubmittedPlacement(this.clientHandler.getUserId());

                    this.clientHandler.getStageManager().gameBuildScene.setOpponentState(isOpponentReady);
                }
            }
            case IN_GAME -> {}
        }
    }

    /**
     * Triggers when the opponent hovers his mouse over a tile during his turn
     * @param playerHoverMessage The message containing the player's hover information
     */
    @Override
    public void onPlayerHoverEvent(PlayerHoverMessage playerHoverMessage) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            clientHandler.showError("Game is not in game phase.");
            return;
        }
        //Only accept opponent hover events
        if(playerHoverMessage.getUserId().equals(clientHandler.getUserId())) return;

        Point point = new Point(playerHoverMessage.getX(), playerHoverMessage.getY());

        //Forward the hover event to the game scene
        clientHandler.getStageManager().gameIngameScene.setOpponentHover(point);
    }

    /**
     * Update the game state during the in-game phase
     * @param gameState The updated game state
     * @param yourShips The ships of the player
     */
    @Override
    public void onGameUpdate(GameState gameState, ArrayList<Ship> yourShips) {
        //TODO: Implement in game logic
    }

    /**
     * Handle the error message by showing it to the user in a dialog
     * @param message The error message
     */
    @Override
    public void onGameError(ErrorMessage message) {
        clientHandler.showError(message.getError().getMessage());
    }

    /**
     * Send the hover event to the server
     * @param x The x coordinate of the tile
     * @param y The y coordinate of the tile
     */
    @Override
    public void sendPlayerHoverEvent(int x, int y) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            clientHandler.showError("Game is not in game phase.");
            return;
        }

        clientHandler.sendMessage(new PlayerHoverMessage(this.clientHandler.getUserId(), x, y));
    }

    /**
     * Submit the placement of ships to the server
     * @param ships The ships to be placed
     */
    @Override
    public void sendSubmitPlacementEvent(ArrayList<Ship> ships) {
        if(gameState.getStatus() != GameState.GameStatus.BUILD_GAME_BOARD) {
            clientHandler.showError("Game is not in build phase.");
            return;
        }

        clientHandler.sendMessage(new SubmitPlacementMessage(ships));
    }

    /**
     * Send the move to the server
     * @param hit The hit object containing the move
     */
    @Override
    public void sendGameMoveEvent(Hit hit) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            clientHandler.showError("Game is not in game phase.");
            return;
        }

        clientHandler.sendMessage(new PlayerMoveMessage(hit));
    }

    /**
     * Leave the game
     */
    @Override
    public void sendLeaveGame() {
        clientHandler.sendMessage(new LeaveGameMessage());
        this.clientHandler.endCurrentGame(); //Removes the game handler from the client handler and switch scene to lobby
    }

    public void startBuildPhase() {
        this.getClientHandler().getStageManager().startBuildScene();

        boolean isOpponentReady = this.getGameState().hasOpponentSubmittedPlacement(this.getClientHandler().getUserId());

        this.getClientHandler().getStageManager().gameBuildScene.setOpponentState(isOpponentReady);

        this.isInGame = true;
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
