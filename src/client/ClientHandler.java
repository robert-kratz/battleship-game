package client;

import protocol.ErrorType;
import protocol.messages.*;
import protocol.messages.RegisterMessage;
import protocol.messages.game.BuildingPhaseStartMessage;
import protocol.messages.game.GameInGameStartMessage;
import protocol.messages.game.GameOverMessage;
import protocol.messages.game.JoinGameMessage;
import protocol.messages.game.building.BuildReadyStateChange;
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
                            BuildingPhaseStartMessage gameStartedMessage = (BuildingPhaseStartMessage) received;

                            if(this.gameHandler == null) return;

                            this.gameHandler.onBuildPhaseStarts(gameStartedMessage);
                        }
                        case MessageType.GAME_IN_GAME_STARTS -> {
                            GameInGameStartMessage gameInGameStartMessage = (GameInGameStartMessage) received;

                            if(this.gameHandler == null) return;

                            System.out.println("Game in game starts: " + gameInGameStartMessage.getGameState().getStatus());

                            this.gameHandler.onGameInGameStarts(gameInGameStartMessage);
                        }
                        case MessageType.GAME_END -> {
                            GameOverMessage gameOverMessage = (GameOverMessage) received;

                            if(this.gameHandler == null) return;

                            System.out.println("Game over: " + gameOverMessage.getGameState().getStatus());

                            this.gameHandler.onGameOver(gameOverMessage);
                        }
                        case MessageType.MOVE_MADE -> {
                            MoveMadeMessage moveMadeMessage = (MoveMadeMessage) received;

                            if(this.gameHandler == null) return;

                            System.out.println("Move made: " + moveMadeMessage.getGameState().getStatus());

                            this.gameHandler.onMoveMade(moveMadeMessage);
                        }
                        case MessageType.BUILD_READY_STATE_CHANGE -> {
                            BuildReadyStateChange buildReadyStateChange = (BuildReadyStateChange) received;

                            if(this.gameHandler == null) return;

                            System.out.println("Building ready state change: " + buildReadyStateChange.getGameState().getStatus());

                            this.gameHandler.onBuildReadyStateChange(buildReadyStateChange);
                        }
                        case MessageType.TURN_CHANGE -> {
                            PlayerTurnChangeMessage playerTurnChangeMessage = (PlayerTurnChangeMessage) received;

                            if(this.gameHandler == null) return;

                            System.out.println("Turn change: " + playerTurnChangeMessage.getGameState().getStatus());

                            this.gameHandler.onTurnChange(playerTurnChangeMessage);
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
            out.reset();
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