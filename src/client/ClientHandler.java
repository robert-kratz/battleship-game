package client;

import protocol.ErrorType;
import protocol.GameState;
import protocol.Ship;
import protocol.messages.*;

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

    public ClientHandler(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            this.lobbyHandler = new LobbyHandler(this);
            this.stageManager = new StageManager(this);

            listenForMessages();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Verbindung zum Server fehlgeschlagen.", "Fehler", JOptionPane.ERROR_MESSAGE);
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

                    System.out.println("Received: " + received.getClass().getSimpleName());

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
                                //show dialog which says that the server is closed, if then ok button is pressed, exit
                                JOptionPane.showMessageDialog(null, "Server closed.", "Fehler", JOptionPane.ERROR_MESSAGE);

                                System.exit(1);
                            }

                            //During game forward to gameHandler
                            if(gameHandler != null) {
                                gameHandler.onGameError(errorMessage);
                                return;
                            }

                            //During lobby forward to lobbyHandler
                            lobbyHandler.onLobbyError(errorMessage);
                        }
                        //Game messages
                        case MessageType.JOIN_GAME -> {
                            JoinGameMessage joinGameMessage = (JoinGameMessage) received;

                            if(gameHandler != null) {
                                gameHandler.sendLeaveGame();
                            }

                            gameHandler = new GameHandler(this, joinGameMessage.getGameState());
                            this.getStageManager().startWaitingLobbyScene(); //Start waiting lobby scene
                            this.lobbyHandler.setInQueue(false);
                            //If no GameHandler is present, create one

                        }
                        case MessageType.GAME_STATE_UPDATE -> {
                            GameStateUpdateMessage gameStateUpdateMessage = (GameStateUpdateMessage) received;

                            System.out.println("Game state updated1: " + gameStateUpdateMessage.getGameState().getStatus());
                            System.out.println("GameHandler: " + gameHandler);

                            if(this.gameHandler == null) return;

                            this.lobbyHandler.setInQueue(false);

                            //System.out.println("Player A: " + gameStateUpdateMessage.getGameState().getPlayerA().getName());
                            //System.out.println("Player B: " + gameStateUpdateMessage.getGameState().getPlayerB().getName());

                            gameHandler.onGameStateUpdate(gameStateUpdateMessage);
                        }
                        case MessageType.GAME_UPDATE -> {
                            GameUpdateMessage gameUpdateMessage = (GameUpdateMessage) received;

                            if(this.gameHandler == null) return;

                            System.out.println("Game update received");

                            for (Ship ship : gameUpdateMessage.getShips()) {
                                System.out.println("Ship: " + ship);
                            }

                            gameHandler.onGameUpdate(gameUpdateMessage.getGameState(), gameUpdateMessage.getShips());
                        }
                        case MessageType.PLAYER_HOVER -> {
                            PlayerHoverMessage playerHoverMessage = (PlayerHoverMessage) received;

                            if(this.gameHandler == null) return;

                            System.out.println(playerHoverMessage.getAffectedFields() != null ? "Affected fields: " + playerHoverMessage.getAffectedFields().size() : "No affected fields");

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
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            showError("Unable to send message to server.");
        }
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public LobbyHandler getLobbyHandler() {
        return lobbyHandler;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this.stageManager.getContentPane(), message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }
}