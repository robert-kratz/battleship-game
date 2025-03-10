package client;

import protocol.ClientPlayer;
import protocol.ErrorType;
import protocol.Ship;
import protocol.game.Cell;
import protocol.game.Move;
import protocol.messages.*;
import protocol.GameState;
import protocol.messages.game.BuildingPhaseStartMessage;
import protocol.messages.game.GameInGameStartMessage;
import protocol.messages.game.GameOverMessage;
import protocol.messages.game.LeaveGameMessage;
import protocol.messages.game.building.BuildReadyStateChange;
import protocol.messages.game.building.PlayerReadyMessage;
import protocol.messages.game.ingame.MoveMadeMessage;
import protocol.messages.game.ingame.PlayerHoverMessage;
import protocol.messages.game.ingame.PlayerMoveMessage;
import protocol.messages.game.ingame.PlayerTurnChangeMessage;
import protocol.messages.game.building.UpdateBuildBoardMessage;

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
     * Triggers when the game starts
     * @param gameStartMessage The message containing the game starting information
     */
    @Override
    public void onBuildPhaseStarts(BuildingPhaseStartMessage gameStartMessage) {
        this.gameState = gameStartMessage.getGameState();

        if(!this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        if(!isInGame) {
            if(this.gameState.getPlayerCount() == 2) {
                UUID userId = this.clientHandler.getUserId();
                String opponentName = this.gameState.getOpponent(userId).getName();

                this.clientHandler.getStageManager().gameWaitingScene.setOpponentName(opponentName);
            }

            this.clientHandler.getStageManager().gameWaitingScene.setGameStartTime(this.gameState.getBuildGameBoardStarted());
        }
    }

    /**
     * Triggers when the game starts
     * @param gameInGameStartMessage The message containing the game starting information
     */
    @Override
    public void onGameInGameStarts(GameInGameStartMessage gameInGameStartMessage) {
        this.gameState = gameInGameStartMessage.getGameState();

        if(!this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        this.playersShips = gameInGameStartMessage.getYourShips();

        if(this.clientHandler.getStageManager().gameIngameScene == null) {
            this.clientHandler.getStageManager().startInGameScene(this.playersShips);
        }
    }

    /**
     * Triggers when the game is over
     * @param gameOverMessage The message containing the game over information
     */
    @Override
    public void onGameOver(GameOverMessage gameOverMessage) {
        this.gameState = gameOverMessage.getGameState();

        this.clientHandler.getStageManager().startGameOverScene();
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

        //Forward the hover event to the game scene only if the uuid in the message is the opponent's
        boolean isPlayersTurn = gameState.isPlayersTurn(this.clientHandler.getUserId());

        if(isPlayersTurn) return;

        clientHandler.getStageManager().gameIngameScene.setOpponentHover(playerHoverMessage.getX(), playerHoverMessage.getY(), playerHoverMessage.getAffectedFields());
    }

    /**
     * Triggers when the player turn changes
     * @param playerTurnChangeMessage The message containing the player turn change information
     */
    @Override
    public void onTurnChange(PlayerTurnChangeMessage playerTurnChangeMessage) {

        boolean playerTurnHasChanged = this.gameState.isPlayersTurn(this.clientHandler.getUserId()) != playerTurnChangeMessage.getGameState().isPlayersTurn(this.clientHandler.getUserId());

        this.gameState = playerTurnChangeMessage.getGameState();

        boolean isPlayersTurn = this.gameState.isPlayersTurn(this.clientHandler.getUserId());

        if(this.getClientHandler().getStageManager().gameIngameScene != null) {
            this.clientHandler.getStageManager().gameIngameScene.toggleTurn(isPlayersTurn);
            this.clientHandler.getStageManager().gameIngameScene.extendCurrentTurn(this.gameState.getPlayersTurnEnd());
            this.clientHandler.getStageManager().gameIngameScene.setPlayerEnergy(this.gameState.getPlayer(this.clientHandler.getUserId()).getEnergy());
        }

        updateMoves();
    }

    /**
     * Triggers when the player makes a move
     * @param moveMadeMessage The message containing the move made information
     */
    @Override
    public void onMoveMade(MoveMadeMessage moveMadeMessage) {
        if(!this.gameState.getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        boolean extendedTime = moveMadeMessage.getGameState().getPlayersTurnEnd().equals(this.gameState.getPlayersTurnEnd());

        this.gameState = moveMadeMessage.getGameState();

        if(!extendedTime) this.clientHandler.getStageManager().gameIngameScene.extendCurrentTurn(this.gameState.getPlayersTurnEnd());

        ClientPlayer player = this.gameState.getPlayer(this.clientHandler.getUserId());
        ClientPlayer opponent = this.gameState.getOpponent(this.clientHandler.getUserId());


        this.clientHandler.getStageManager().gameIngameScene.opponentBoard.setPlacedShips(player.getUncoveredShips());
        this.clientHandler.getStageManager().gameIngameScene.setPlayerEnergy(player.getEnergy());

        //Show the move on the board
        updateMoves();
    }

    /**
     * Update the moves of the player and opponent
     */
    private void updateMoves() {
        if (clientHandler.getUserId().equals(gameState.getPlayerA().getId())) {
            clientHandler.getStageManager().gameIngameScene.playerBoard.setMoves(gameState.getPlayerB().getMoves());
            clientHandler.getStageManager().gameIngameScene.opponentBoard.setMoves(gameState.getPlayerA().getMoves());
        } else {
            clientHandler.getStageManager().gameIngameScene.playerBoard.setMoves(gameState.getPlayerA().getMoves());
            clientHandler.getStageManager().gameIngameScene.opponentBoard.setMoves(gameState.getPlayerB().getMoves());
        }
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

    /**
     * Send the ready state of the player to the server
     * @param ready The ready state of the player
     */
    @Override
    public void sendPlayerReadyMessage(boolean ready) {
        if(gameState.getStatus() != GameState.GameStatus.BUILD_GAME_BOARD) {
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
        this.clientHandler.endCurrentGame(); // Removes the game handler from the client handler and switch scene to lobby
    }

    /**
     * Triggers when the player is ready to start the game
     * @param buildReadyStateChange The message containing the build ready state change information
     */
    @Override
    public void onBuildReadyStateChange(BuildReadyStateChange buildReadyStateChange) {
        if(!this.gameState.getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        this.gameState = buildReadyStateChange.getGameState();
        updateOpponentReadyState();
    }

    /**
     * Show the build phase
     */
    @Override
    public void showBuildPhase() {
        if(this.gameState.getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        this.isInGame = true;

        this.getClientHandler().getStageManager().startBuildScene();
    }

    /**
     * Update the opponent's ready state in the build phase
     */
    private void updateOpponentReadyState() {
        if(this.clientHandler.getStageManager().gameBuildScene != null) {
            ClientPlayer player = this.gameState.getPlayer(this.clientHandler.getUserId());
            ClientPlayer opponent = this.gameState.getOpponent(this.clientHandler.getUserId());
            this.clientHandler.getStageManager().gameBuildScene.setPlayerState(player.isReady());
            this.clientHandler.getStageManager().gameBuildScene.setOpponentState(opponent.isReady());
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
