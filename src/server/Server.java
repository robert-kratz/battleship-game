package server;

import protocol.messages.ErrorMessage;
import protocol.ErrorType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Server {

    private static Server instance;

    private static int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final List<PlayerInfo> players = new ArrayList<>();
    private final List<Thread> clientThreads = new ArrayList<>();

    private ArrayList<BattleShipGame> games = new ArrayList<>();
    private ArrayList<PlayerInfo> queue = new ArrayList<>();

    private ServerGUI gui;

    public Server() {
        this.gui = new ServerGUI(this);
        instance = this;
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

    public synchronized void createGame(BattleShipGame game) {
        games.add(game);
    }

    public synchronized void removeGame(UUID id) {
        games.removeIf(game -> game.getGameState().getId().equals(id));
    }

    public synchronized void removePlayer(PlayerInfo player) {
        players.remove(player);
        updatePlayerList();
    }

    public synchronized void addToQueue(PlayerInfo player) {
        queue.add(player);
    }

    public synchronized void removeFromQueue(UUID id) {
        queue.removeIf(player -> player.getId().equals(id));
    }

    private synchronized void updatePlayerList() {
        StringBuilder sb = new StringBuilder();
        for (PlayerInfo p : players) {
            sb.append(p.getUsername()).append(" (").append(p.getIp()).append(")\n");
        }
        gui.updatePlayerList(sb.toString());
    }

    public synchronized List<PlayerInfo> getPlayers() {
        return players;
    }

    public synchronized ArrayList<PlayerInfo> getQueue() {
        return queue;
    }

    public synchronized ArrayList<BattleShipGame> getGames() {
        return games;
    }

    /**
     * Main method to start the server
     * -p <port> to specify the port
     * --autostart to start the server automatically
     * @param args command line arguments
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--p")) {
                PORT = Integer.parseInt(args[i + 1]);
            }
        }
        instance = new Server();

        if (args.length >= 1 && args[0].contains("--autostart")) {
            instance.startServer();
        }
    }
}