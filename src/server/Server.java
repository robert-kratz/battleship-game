package server;

import protocol.ErrorMessage;
import protocol.ErrorType;
import protocol.PlayerInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static Server instance;

    private static int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final List<PlayerInfo> players = new ArrayList<>();
    private final List<Thread> clientThreads = new ArrayList<>();
    private ServerGUI gui;

    public Server() {
        this.gui = new ServerGUI(this);
    }

    public synchronized void startServer() {
        if (running) return;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                System.out.println("Server gestartet auf Port " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Neuer Client verbunden: " + clientSocket);

                    PlayerInfo player = new PlayerInfo(clientSocket, this);
                    players.add(player);

                    Thread clientThread = new Thread(player);
                    clientThreads.add(clientThread);
                    clientThread.start();

                    updatePlayerList();
                }
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }).start();
    }

    public synchronized void stopServer() {
        running = false;
        try {
            for (PlayerInfo p : players) {
                p.sendMessage(new ErrorMessage(ErrorType.SERVER_CLOSED));
            }
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("Server gestoppt.");
            }
            for (Thread t : clientThreads) {
                t.interrupt();
            }
            players.clear();
            clientThreads.clear();
            updatePlayerList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void removePlayer(PlayerInfo player) {
        players.remove(player);
        updatePlayerList();
    }

    private synchronized void updatePlayerList() {
        StringBuilder sb = new StringBuilder();
        for (PlayerInfo p : players) {
            sb.append(p.getUsername()).append(" (").append(p.getIp()).append(")\n");
        }
        gui.updatePlayerList(sb.toString());
    }

    /**
     * Main method to start the server
     * -p <port> to specify the port
     * --autostart to start the server automatically
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].contains("--autostart")) {
            instance.startServer();
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--p")) {
                PORT = Integer.parseInt(args[i + 1]);
            }
        }
        instance = new Server();
    }
}