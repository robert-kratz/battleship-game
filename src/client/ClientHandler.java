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
     * Aufgabe 3a1: Implementieren das Client Protokoll 
     */
    
    /**
     * Creates a new ClientHandler instance.
     * @param serverAddress The address of the server.
     * @param port The port of the server.
     */
    public ClientHandler(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port); // 0.5
            out = new ObjectOutputStream(socket.getOutputStream()); // 0.5
            in = new ObjectInputStream(socket.getInputStream()); // 0.5

            this.lobbyHandler = new LobbyHandler(this);
            this.stageManager = new StageManager(this);  // 0.5

            listenForMessages();
        } catch (IOException e) {
            showError("Unable to connect to server");
            System.exit(1);
        }
    }
    
    /**
     * Aufgabe 3a2: Implementieren das Client Protokoll 
     */

    /**
     * Listens for incoming messages from the server.
     */
    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    Object received = in.readObject(); // 0.5
                    Message message = (Message) received; // 0.5

                    System.out.println("Received message: " + message.toString());

                    switch (message.getType()) {
                        //Lobby messages
                        case MessageType.REGISTER -> {
                            RegisterMessage registerMessage = (RegisterMessage) received; // 0.5

                            System.out.println("Registered as: " + registerMessage.getUsername() + " with ID: " + registerMessage.getUserId());

                            this.userId = registerMessage.getUserId(); // 0.5
                            this.username = registerMessage.getUsername(); // 0.5

                            this.getStageManager().lobbyScene.setUsername(username); // 0.5
                        }
                        case MessageType.QUEUE_UPDATE -> {
                            QueueUpdateMessage queueUpdateMessage = (QueueUpdateMessage) received; // 0.5

                            lobbyHandler.onQueueUpdate(queueUpdateMessage); // 0.5
                        }
                        case MessageType.ERROR -> {
                            ErrorMessage errorMessage = (ErrorMessage) received; // 0.5

                            if(errorMessage.getError().equals(ErrorType.SERVER_CLOSED)) { // 0.5
                                JOptionPane.showMessageDialog(null, "Server closed.", "Error", JOptionPane.ERROR_MESSAGE);

                                System.exit(1);
                            }

                            System.err.println("Received error: " + errorMessage.getError());

                            if (gameHandler != null) {
                                gameHandler.onGameError(errorMessage); // 0.5
                            } else {
                                lobbyHandler.onLobbyError(errorMessage); // 0.5
                            }
                        }
                        //Game messages
                        case MessageType.JOIN_GAME -> {
                            JoinGameMessage joinGameMessage = (JoinGameMessage) received; // 0.5

                            if(gameHandler != null) {
                                gameHandler.sendLeaveGame(); // 0.5
                            }

                            gameHandler = new GameHandler(this, joinGameMessage.getGameState()); // 0.5
                            this.getStageManager().startWaitingLobbyScene(); //Start waiting lobby scene

                            this.lobbyHandler.setInQueue(false); // 0.5

                        }
                        case MessageType.BUILDING_PHASE_STARTS -> {
                            GameBuildingStartMessage gameStartedMessage = (GameBuildingStartMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            this.gameHandler.onBuildPhaseStarts(gameStartedMessage); // 0.5
                        }
                        case MessageType.GAME_IN_GAME_START -> {
                            GameInGameStartMessage gameInGameStartMessage = (GameInGameStartMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            this.gameHandler.onGameInGameStarts(gameInGameStartMessage); // 0.5
                        }
                        case MessageType.GAME_OVER -> {
                            GameOverMessage gameOverMessage = (GameOverMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            this.gameHandler.onGameOver(gameOverMessage); // 0.5
                        }
                        case MessageType.MOVE_MADE -> {
                            MoveMadeMessage moveMadeMessage = (MoveMadeMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            this.gameHandler.onMoveMade(moveMadeMessage); // 0.5
                        }
                        case MessageType.BUILD_READY_STATE_CHANGE -> {
                            PlayerReadyStateChangeMessage playerReadyStateChangeMessage = (PlayerReadyStateChangeMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            this.gameHandler.onBuildReadyStateChange(playerReadyStateChangeMessage); // 0.5
                        }
                        case MessageType.TURN_CHANGE -> {
                            PlayerTurnChangeMessage playerTurnChangeMessage = (PlayerTurnChangeMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            this.gameHandler.onTurnChange(playerTurnChangeMessage); // 0.5
                        }
                        case MessageType.PLAYER_HOVER -> {
                            PlayerHoverMessage playerHoverMessage = (PlayerHoverMessage) received; // 0.5

                            if(this.gameHandler == null) return;

                            gameHandler.onPlayerHoverEvent(playerHoverMessage); // 0.5
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
     * ACHTUNG! AB HIER DÜRFEN KEINE ÄNDERUNGEN MEHR VORGENOMMEN WERDEN
     */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
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