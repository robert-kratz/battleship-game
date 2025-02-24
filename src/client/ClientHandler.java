package client;

import protocol.messages.*;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientHandler {

    private GameHandler gameHandler = new GameHandler(this);

    public static final MediaPlayer mediaPlayer = new MediaPlayer();

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            listenForMessages();
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

                            if(gameHandler != null) gameHandler.register(registerMessage);
                            break;
                        case "QueueUpdateMessage":
                            QueueUpdateMessage queueUpdateMessage = (QueueUpdateMessage) received;

                            if(gameHandler != null) gameHandler.queueUpdate(queueUpdateMessage);
                            break;
                        case "GameStateUpdateMessage":
                            GameStateUpdateMessage gameStateUpdateMessage = (GameStateUpdateMessage) received;

                            if(gameHandler != null) gameHandler.onGameStateUpdate(gameStateUpdateMessage);
                            break;
                        case "GameStartingMessage":
                            GameStartingMessage gameStartingMessage = (GameStartingMessage) received;

                            if(gameHandler != null) gameHandler.gameStarted(gameStartingMessage);
                            break;
                        case "ErrorMessage":
                            ErrorMessage errorMessage = (ErrorMessage) received;

                            if(gameHandler != null) gameHandler.onErrorMessage(errorMessage);
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
                JOptionPane.showMessageDialog(gameHandler.getStageManager().getContentPane(), message, "Fehler", JOptionPane.ERROR_MESSAGE)
        );
    }
}