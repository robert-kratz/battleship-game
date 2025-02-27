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
    private ArrayList<Thread> gameThreads = new ArrayList<>();

    private ArrayList<PlayerInfo> queue = new ArrayList<>();

    private final ServerGUI gui;

    public Server() {
        this.gui = new ServerGUI(this);
        instance = this;
    }

    public void startServer() {
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

    /**
     * Returns the instance of the server
     * @return instance of the server
     */
    public ArrayList<PlayerInfo> getPlayersInLobby() {
        ArrayList<PlayerInfo> playersInLobby = new ArrayList<>();
        for (PlayerInfo player : players) {
            if (!player.isInGame()) {
                playersInLobby.add(player);
            }
        }
        return playersInLobby;
    }

    /**
     * Stops the server and closes all connections
     */
    public void stopServer() {
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

    /**
     * Registers a new game and starts the game thread
     * @param game game to register
     * @param thread thread to start
     */
    public void registerGame(BattleShipGame game, Thread thread) {
        games.add(game);
        gameThreads.add(thread);

        thread.start();
    }

    /**
     * Removes a game from the list of active games
     * @param id id of the game to remove
     */
    public void unregisterGame(UUID id) {
        BattleShipGame targetGame = null;
        Thread targetThread = null;
        for (BattleShipGame game : games) {
            if (game.getGameState().getId().equals(id)) {
                targetGame = game;
                targetThread = gameThreads.get(games.indexOf(game));
            }
        }

        if (targetGame != null) {
            //Stop the game thread
            targetThread.interrupt();
            games.remove(targetGame);
        }
    }

    /**
     * Returns the game the player is currently in
     * @param player player to get the game for
     * @return game the player is in
     */
    public synchronized BattleShipGame getGame(PlayerInfo player) {
        for (BattleShipGame game : games) {
            if (game.getPlayerA().getId().equals(player.getId()) || game.getPlayerB().getId().equals(player.getId())) {
                return game;
            }
        }
        return null;
    }

    public void removePlayer(PlayerInfo player) {
        players.remove(player);
        updatePlayerList();
    }

    public void addToQueue(PlayerInfo player) {
        queue.add(player);
        player.sendMessage(new protocol.messages.QueueUpdateMessage(queue.size(), true));
    }

    public void removeFromQueue(UUID id) {
        queue.removeIf(player -> {
            if(player.getId().equals(id)) {
                player.sendMessage(new protocol.messages.QueueUpdateMessage(queue.size(), false));
                return true;
            }
            return false;
        });

    }

    private void updatePlayerList() {
        StringBuilder sb = new StringBuilder();
        for (PlayerInfo p : players) {
            sb.append(p.getUsername()).append(" (").append(p.getIp()).append(")\n");
        }
        gui.updatePlayerList(sb.toString());
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public ArrayList<PlayerInfo> getQueue() {
        return queue;
    }

    public ArrayList<BattleShipGame> getGames() {
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