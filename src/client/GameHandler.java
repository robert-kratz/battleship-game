package client;

import protocol.ErrorType;
import protocol.messages.*;
import server.GameState;

import javax.swing.*;
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

    @Override
    public void queueUpdate(QueueUpdateMessage queueUpdateMessage) {
        System.out.println("Queue length: " + queueLength);
        this.queueLength = queueUpdateMessage.getQueueSize();
        this.isInQueue = queueUpdateMessage.isPlayerInQueue();

        this.stageManager.lobbyScene.setQueueLength(queueLength);
    }

    @Override
    public void onErrorMessage(ErrorMessage errorMessage) {
        if(errorMessage.getError().equals(ErrorType.SERVER_CLOSED)) {
            showError("Server closed.");
            System.exit(1);
        }

        showError("Error: " + errorMessage.getError().toString());
    }

    @Override
    public void onGameStateUpdate(GameStateUpdateMessage message) {
        this.gameState = message.getGameState();

        if(message.getGameState().getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) {
            this.stageManager.initGameScene(message.getGameState().getBoardSize());
        }
    }

    @Override
    public void createGameMessage(int size) {
        clientHandler.sendMessage(new CreateGameMessage(size));
    }

    @Override
    public void gameStarted(GameStartingMessage gameStartingMessage) {
        this.gameId = gameStartingMessage.getGameState().getId();
        this.gameState = gameStartingMessage.getGameState();

        this.stageManager.gameGUI.startGame();
    }

    @Override
    public void logout() {

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

    @Override
    public void joinGame(int sessionCode) {
        System.out.println("Joining game with session code: " + sessionCode);
        clientHandler.sendMessage(new JoinGameMessage(sessionCode));
    }

    @Override
    public void leaveGame() {
        clientHandler.sendMessage(new LeaveGameMessage());
        this.stageManager.removeGameScene();
        this.stageManager.switchScene(StageManager.Stages.LOBBY_SCENE);
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
