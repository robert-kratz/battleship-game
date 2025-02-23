package client;

import protocol.ErrorType;
import protocol.Ship;
import protocol.messages.*;
import server.GameState;

import javax.swing.*;
import java.util.ArrayList;
import java.util.UUID;

public class GameHandler implements GameClient {

    private GameState gameState;

    private String username;
    private UUID userId, secret, gameId;
    private int queueLength = 0;

    private boolean isInQueue = false;

    private final ClientHandler clientHandler;
    private final StageManager stageManager;

    public GameHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.stageManager = new StageManager(this);
    }

    @Override
    public void register(RegisterMessage registerMessage) {
        this.userId = registerMessage.getUserId();
        this.secret = registerMessage.getSecret();
        this.username = registerMessage.getUsername();

        this.stageManager.lobbyScene.setUsername(username);
    }

    /**
     * Queue logic
     */

    @Override
    public void queueUpdate(QueueUpdateMessage queueUpdateMessage) {
        this.queueLength = queueUpdateMessage.getQueueSize();
        this.isInQueue = queueUpdateMessage.isPlayerInQueue();

        this.stageManager.lobbyScene.setQueueLength(queueLength);
    }

    @Override
    public void joinQueue() {
        if(isInQueue) {
            showError("Already in queue.");
            return;
        }

        clientHandler.sendMessage(new JoinQueueMessage());
    }

    @Override
    public void leaveQueue() {
        if(!isInQueue) {
            showError("Not in queue.");
            return;
        }

        clientHandler.sendMessage(new QuitQueueMessage());
    }

    /**
     * Game logic
     */

    @Override
    public void onGameStateUpdate(GameStateUpdateMessage message) {
        this.gameState = message.getGameState();

        System.out.println("Game state updated: " + gameState.getStatus());

        switch (gameState.getStatus()) {
            case LOBBY_WAITING -> {
                this.stageManager.startWaitingLobbyScene();
            }
            case BUILD_GAME_BOARD -> {
                this.stageManager.startBuildScene();
            }
            case IN_GAME -> {
                this.stageManager.startInGameScene();
            }
            case GAME_OVER -> {}
        }
    }

    @Override
    public void placeShips(ArrayList<Ship> ships) {
        if(gameState == null) return;

        if(gameState.getStatus() != GameState.GameStatus.BUILD_GAME_BOARD) {
            showError("Game is not in build phase.");
            return;
        }

        clientHandler.sendMessage(new SubmitPlacementMessage(ships));
    }

    /**
     * Creates a new game with the given size. Trigger CLIENT -> SERVER
     * @param size size of the game board
     */
    @Override
    public void createGameMessage(int size) {
        clientHandler.sendMessage(new CreateGameMessage(size));
    }

    @Override
    public void gameStarted(GameStartingMessage gameStartingMessage) {
        this.gameId = gameStartingMessage.getGameState().getId();
        this.gameState = gameStartingMessage.getGameState();

        this.stageManager.startBuildScene();
    }

    @Override
    public void joinGame(int sessionCode) {
        System.out.println("Joining game with session code: " + sessionCode);
        clientHandler.sendMessage(new JoinGameMessage(sessionCode));
    }

    @Override
    public void leaveGame() {
        clientHandler.sendMessage(new LeaveGameMessage());
        this.stageManager.switchScene(Stage.LOBBY_SCENE);
    }

    @Override
    public void onErrorMessage(ErrorMessage errorMessage) {
        if(errorMessage.getError().equals(ErrorType.SERVER_CLOSED)) {
            showError("Server closed.");
            System.exit(1);
        }

        showError("Error: " + errorMessage.getError().toString());
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this.getStageManager().getContentPane(), message, "Fehler", JOptionPane.ERROR_MESSAGE)
        );
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UUID getGameId() {
        return gameId;
    }

    public UUID getSecret() {
        return secret;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public GameState getGameState() {
        return gameState;
    }
}
