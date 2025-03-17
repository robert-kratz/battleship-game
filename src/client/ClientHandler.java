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

/**
 * Aufgabe 3
 */

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
     * Aufgabe 3: Implementieren das Client Protokoll
     */

    /**
     * Creates a new ClientHandler instance.
     * @param serverAddress The address of the server.
     * @param port The port of the server.
     */
    public ClientHandler(String serverAddress, int port) {
        // TODO
    }
    
    /**
     * Aufgabe 3: Implementieren das Client Protokoll
     */

    /**
     * Listens for incoming messages from the server.
     */
    private void listenForMessages() {
        // TODO
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