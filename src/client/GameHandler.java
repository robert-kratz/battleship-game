package client;

import protocol.Ship;
import protocol.game.Cell;
import protocol.game.Move;
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
                    if(this.clientHandler.getStageManager().gameIngameScene == null) {
                        System.out.println("Starting in game scene");
                        this.clientHandler.getStageManager().startInGameScene(this.playersShips);
                    }
                }
                case GAME_OVER -> {}
            }
        }

        //ALWAYS EXECUTE
        switch (gameState.getStatus()) {
            case LOBBY_WAITING -> {
            }
            case BUILD_GAME_BOARD -> {
                if(this.clientHandler.getStageManager().gameBuildScene != null) {
                    boolean isOpponentReady = this.gameState.hasOpponentSubmittedPlacement(this.clientHandler.getUserId());

                    this.clientHandler.getStageManager().gameBuildScene.setOpponentState(isOpponentReady);
                }
            }
            case IN_GAME -> {
                if (this.clientHandler.getStageManager().gameIngameScene != null) {
                    boolean isPlayerATurn = gameState.isPlayersTurn(this.clientHandler.getUserId());

                    this.clientHandler.getStageManager().gameIngameScene.toggleTurn(isPlayerATurn);
                    this.clientHandler.getStageManager().gameIngameScene.extendCurrentTurn(gameState.getPlayersTurnEnd());
                    this.clientHandler.getStageManager().gameIngameScene.setPlayerEnergy(gameState.getPlayerEnergy(this.clientHandler.getUserId()));
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
            clientHandler.showError("Game is not in game phase.");
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

        System.out.println("Player turn has changed: " + playerTurnHasChanged);

        if(playerTurnHasChanged) {
            boolean isPlayerATurn = gameState.isPlayersTurn(this.clientHandler.getUserId());

            this.clientHandler.getStageManager().gameIngameScene.toggleTurn(isPlayerATurn);
            this.clientHandler.getStageManager().gameIngameScene.extendCurrentTurn(gameState.getPlayersTurnEnd());
            this.clientHandler.getStageManager().gameIngameScene.setPlayerEnergy(gameState.getPlayerEnergy(this.clientHandler.getUserId()));
        }

        //Check weather a move has been made

        boolean moveMade = gameState.getMoveA().size() != this.gameState.getMoveA().size() || gameState.getMoveB().size() != this.gameState.getMoveB().size();

        if (moveMade) {
            System.out.println("Move GUI Update");
            // Prüfe, ob der lokale Spieler Spieler A ist:
            if (clientHandler.getUserId().equals(gameState.getPlayerA())) {
                // Spieler A:
                // - Das Player Board zeigt die Züge des Gegners (moveB, da diese auf A's Schiffe zielen)
                // - Das Opponent Board zeigt die eigenen Züge (moveA)
                clientHandler.getStageManager().gameIngameScene.playerBoard.setMoves(gameState.getMoveB());
                clientHandler.getStageManager().gameIngameScene.opponentBoard.setMoves(gameState.getMoveA());
            } else {
                // Andernfalls (lokaler Spieler ist Spieler B):
                // - Das Player Board zeigt die Züge des Gegners (moveA, da diese auf B's Schiffe zielen)
                // - Das Opponent Board zeigt die eigenen Züge (moveB)
                clientHandler.getStageManager().gameIngameScene.playerBoard.setMoves(gameState.getMoveA());
                clientHandler.getStageManager().gameIngameScene.opponentBoard.setMoves(gameState.getMoveB());
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
    }

    /**
     * Send the hover event to the server
     * @param x The x coordinate of the tile
     * @param y The y coordinate of the tile
     */
    @Override
    public void sendPlayerHoverEvent(int x, int y, ArrayList<Cell> affectedFields) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            clientHandler.showError("Game is not in game phase.");
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
    public void sendSubmitPlacementEvent(ArrayList<Ship> ships) {
        if(gameState.getStatus() != GameState.GameStatus.BUILD_GAME_BOARD) {
            clientHandler.showError("Game is not in build phase.");
            return;
        }

        clientHandler.sendMessage(new SubmitPlacementMessage(ships));
    }

    /**
     * Send the move to the server
     * @param move The move object containing the move
     */
    @Override
    public void sendGameMoveEvent(Move move) {
        if(gameState.getStatus() != GameState.GameStatus.IN_GAME) {
            clientHandler.showError("Game is not in game phase.");
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
