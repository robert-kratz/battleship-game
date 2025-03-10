package client;

import protocol.ErrorType;
import protocol.messages.*;
import protocol.messages.RegisterMessage;
import protocol.messages.game.building.GameBuildingStartMessage;
import protocol.messages.game.ingame.GameInGameStartMessage;
import protocol.messages.game.GameOverMessage;
import protocol.messages.game.JoinGameMessage;
import protocol.messages.game.building.PlayerReadyStateChangeMessage;
import protocol.messages.game.ingame.MoveMadeMessage;
import protocol.messages.game.ingame.PlayerHoverMessage;
import protocol.messages.game.ingame.PlayerTurnChangeMessage;
import protocol.messages.lobby.QueueUpdateMessage;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler {

    private GameHandler gameHandler;
    private LobbyHandler lobbyHandler;

    private StageManager stageManager;

    public static final MediaPlayer mediaPlayer = new MediaPlayer();

    private UUID userId;
    private String username;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Creates a new ClientHandler instance.
     * @param serverAddress The address of the server.
     * @param port The port of the server.
     */
    public ClientHandler(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            this.lobbyHandler = new LobbyHandler(this);
            this.stageManager = new StageManager(this);

            listenForMessages();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection to server failed.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Listens for incoming messages from the server.
     */
    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    Object received = in.readObject();
                    Message message = (Message) received;

                    System.out.println("Received message: " + message.getType());

                    switch (message.getType()) {
                        //Lobby messages
                        case MessageType.REGISTER -> {
                            RegisterMessage registerMessage = (RegisterMessage) received;

                            System.out.println("Registered as: " + registerMessage.getUsername() + " with ID: " + registerMessage.getUserId());

                            this.userId = registerMessage.getUserId();
                            this.username = registerMessage.getUsername();

                            this.getStageManager().lobbyScene.setUsername(username);
                        }
                        case MessageType.QUEUE_UPDATE -> {
                            QueueUpdateMessage queueUpdateMessage = (QueueUpdateMessage) received;

                            lobbyHandler.onQueueUpdate(queueUpdateMessage);
                        }
                        case MessageType.ERROR -> {
                            ErrorMessage errorMessage = (ErrorMessage) received;

                            if(errorMessage.getError().equals(ErrorType.SERVER_CLOSED)) {
                                JOptionPane.showMessageDialog(null, "Server closed.", "Fehler", JOptionPane.ERROR_MESSAGE);

                                System.exit(1);
                            }

                            System.out.println("Received error: " + errorMessage.getError());

                            if (gameHandler != null) {
                                gameHandler.onGameError(errorMessage);
                            } else {
                                lobbyHandler.onLobbyError(errorMessage);
                            }
                        }
                        //Game messages
                        case MessageType.JOIN_GAME -> {
                            JoinGameMessage joinGameMessage = (JoinGameMessage) received;

                            if(gameHandler != null) {
                                gameHandler.sendLeaveGame();
                            }

                            gameHandler = new GameHandler(this, joinGameMessage.getGameState());
                            this.getStageManager().startWaitingLobbyScene(); //Start waiting lobby scene

                            System.out.println("Game state: " + joinGameMessage.getGameState().getStatus());

                            this.lobbyHandler.setInQueue(false);

                        }
                        case MessageType.BUILDING_PHASE_STARTS -> {
                            GameBuildingStartMessage gameStartedMessage = (GameBuildingStartMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onBuildPhaseStarts(gameStartedMessage);
                        }
                        case MessageType.GAME_IN_GAME_START -> {
                            GameInGameStartMessage gameInGameStartMessage = (GameInGameStartMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onGameInGameStarts(gameInGameStartMessage);
                        }
                        case MessageType.GAME_OVER -> {
                            GameOverMessage gameOverMessage = (GameOverMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onGameOver(gameOverMessage);
                        }
                        case MessageType.MOVE_MADE -> {
                            MoveMadeMessage moveMadeMessage = (MoveMadeMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onMoveMade(moveMadeMessage);
                        }
                        case MessageType.BUILD_READY_STATE_CHANGE -> {
                            PlayerReadyStateChangeMessage playerReadyStateChangeMessage = (PlayerReadyStateChangeMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onBuildReadyStateChange(playerReadyStateChangeMessage);
                        }
                        case MessageType.TURN_CHANGE -> {
                            PlayerTurnChangeMessage playerTurnChangeMessage = (PlayerTurnChangeMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onTurnChange(playerTurnChangeMessage);
                        }
                        case MessageType.PLAYER_HOVER -> {
                            PlayerHoverMessage playerHoverMessage = (PlayerHoverMessage) received;

                            if(this.gameHandler == null) return;

                            gameHandler.onPlayerHoverEvent(playerHoverMessage);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException  | ClassCastException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Connection lost", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }).start();
    }

    /**
     * Ends the current game and returns to the lobby.
     */
    public void endCurrentGame() {
        if(this.gameHandler == null) return;

        this.gameHandler = null;
        this.stageManager.exitGameScene();
    }

    /**
     * Sends a message to the server.
     * @param message The message to send.
     */
    public void sendMessage(Message message) {
        try {
            out.reset();
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            showError("Unable to send message to server.");
        }
    }

    /**
     * returns the game handler
     * @return the game handler, if the game is running
     */
    public GameHandler getGameHandler() {
        return gameHandler;
    }

    /**
     * returns the lobby handler
     * @return the lobby handler
     */
    public LobbyHandler getLobbyHandler() {
        return lobbyHandler;
    }

    /**
     * Gets the stage manager.
     * @return The stage manager.
     */
    public StageManager getStageManager() {
        return stageManager;
    }

    /**
     * Shows an error message dialog.
     * @param message The error message to display.
     */
    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this.stageManager.getContentPane(), message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    /**
     * Gets the username of the client.
     * @return The username of the client.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the user ID of the client.
     * @return The user ID of the client.
     */
    public UUID getUserId() {
        return userId;
    }
}