package client;

import protocol.messages.GameStateUpdateMessage;
import protocol.messages.ErrorMessage;
import protocol.ErrorType;
import protocol.messages.GameStartingMessage;
import protocol.messages.Message;
import protocol.messages.QueueUpdateMessage;
import protocol.messages.RegisterMessage;
import server.GameState;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler {

    private String username;
    private UUID userId, secret, gameId;
    private int queueLength = 0;

    private boolean isInQueue = false;
    private GameState lastGameState;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private StageManager stageManager;

    public ClientHandler(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            stageManager = new StageManager(this);
            listenForMessages();

            //show error when connection to server fails

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Verbindung zum Server fehlgeschlagen.", "Fehler", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    Object received = in.readObject();

                    System.out.println("Received: " + received.getClass().getSimpleName());

                    switch (received.getClass().getSimpleName()) {
                        case "RegisterMessage":
                            RegisterMessage registerMessage = (RegisterMessage) received;

                            this.secret = registerMessage.getSecret();
                            this.userId = registerMessage.getUserId();
                            this.username = registerMessage.getUsername();
                            this.queueLength = registerMessage.getQueueLength();

                            this.stageManager.homeScreen.setUsername(username);
                            this.stageManager.homeScreen.setQueueLength(this.queueLength);
                            break;
                        case "QueueUpdateMessage":
                            QueueUpdateMessage queueUpdateMessage = (QueueUpdateMessage) received;
                            this.queueLength = queueUpdateMessage.getQueueSize();
                            this.stageManager.homeScreen.setQueueLength(this.queueLength);

                            System.out.println("Received queue update: " + queueLength);

                            break;
                        case "GameStartingMessage":
                            GameStartingMessage gameStartingMessage = (GameStartingMessage) received;
                            this.lastGameState = gameStartingMessage.getGameState();
                            this.gameId = lastGameState.getId();

                            System.out.println("Received game starting message: " + lastGameState.getId() + " " + lastGameState.getStatus().toString());

                            this.getStageManager().switchScene(StageManager.Stages.MAP_BUILDER);
                            this.getStageManager().mapBuilder.setGameState(lastGameState);
                            break;
                        case "GameStateUpdateMessage":
                            GameStateUpdateMessage gameStateUpdateMessage = (GameStateUpdateMessage) received;

                            System.out.println(gameStateUpdateMessage.getGameState().getStatus().toString());
                            System.out.println(lastGameState.getStatus().toString());

                            if(lastGameState != null && !lastGameState.getStatus().equals(gameStateUpdateMessage.getGameState().getStatus())) {
                                if(gameStateUpdateMessage.getGameState().getStatus().equals(GameState.GameStatus.IN_GAME)) {
                                    this.getStageManager().switchScene(StageManager.Stages.GAME);
                                    System.out.println("Switched to game scene");
                                }
                            }

                            this.lastGameState = gameStateUpdateMessage.getGameState();
                            this.getStageManager().gameBoard.setGameState(lastGameState);

                            System.out.println("Received game state update: " + lastGameState.getId() + " " + lastGameState.getStatus().toString());
                            break;
                        case "ErrorMessage":
                            ErrorMessage errorMessage = (ErrorMessage) received;
                            if(errorMessage.getError().equals(ErrorType.SERVER_CLOSED)) {
                                showError("Server wurde geschlossen.");
                                System.exit(1);
                            }

                            showError(errorMessage.getError().toString());
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException  | ClassCastException e) {
                JOptionPane.showMessageDialog(null, "Verbindung unterbrochen.", "Fehler", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }).start();
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            showError("Nachricht konnte nicht gesendet werden.");
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(stageManager.getContentPane(), message, "Fehler", JOptionPane.ERROR_MESSAGE)
        );
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSecret() {
        return secret;
    }

    public UUID getGameId() {
        return gameId;
    }

    public boolean isInQueue() {
        return isInQueue;
    }

    public void setInQueue(boolean inQueue) {
        isInQueue = inQueue;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }

    public int getQueueLength() {
        return queueLength;
    }
}