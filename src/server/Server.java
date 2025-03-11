package server;

import protocol.GameState;
import protocol.messages.ErrorMessage;
import protocol.ErrorType;
import protocol.messages.lobby.QueueUpdateMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Server {

    private static Server instance;
    private static int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running = false;

    private final List<ServerPlayer> players = new ArrayList<>();
    private final List<Thread> clientThreads = new ArrayList<>();

    private final Map<UUID, GameContainer> games = new HashMap<>();

    private final ArrayList<ServerPlayer> queue = new ArrayList<>();
    private final ServerGUI gui;

    /**
     * Main method to start the server
     * -p <port> to specify the port
     * @param args command line arguments
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--p")) {
                PORT = Integer.parseInt(args[i + 1]);
            }
        }
        printLocalAddress();

        instance = new Server(); // Create a new instance of the server
        instance.startServer();
    }

    /**
     * Creates a new server instance and initializes the GUI.
     */
    public Server() {
        this.gui = new ServerGUI(this);
        instance = this;
    }

    /**
     * Starts the server and listens for incoming connections.
     */
    public void startServer() {
        if (running) return;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                running = true;
                this.gui.updateServerOnlineStatus(true);
                System.out.println("[Server] Starting server on port " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[Server] New connection from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " (" + clientSocket.getInetAddress().getHostName() + ");");

                    ServerPlayer player = new ServerPlayer(clientSocket, this);
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
     * Stops the server, closes all connections and unregisters all games.
     */
    public void stopServer() {
        running = false;
        try {
            // Alle laufenden Spiele abmelden
            for (UUID gameId : new ArrayList<>(games.keySet())) {
                unregisterGame(gameId);
            }
            for (ServerPlayer p : players) {
                p.sendMessage(new ErrorMessage(ErrorType.SERVER_CLOSED));
            }
            if (serverSocket != null) {
                serverSocket.close();
                System.out.println("[Server] Stopping server on port " + PORT);
            }
            for (Thread t : clientThreads) {
                t.interrupt();
            }
            players.clear();
            clientThreads.clear();
            updatePlayerList();
            updateGameList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a new game. Der Server erstellt intern den Game-Thread und startet ihn.
     * @param game the game to register
     */
    public void registerGame(BattleShipGame game) {
        Thread gameThread = new Thread(game);
        GameContainer container = new GameContainer(game, gameThread);
        games.put(game.getGameState().getId(), container);
        gameThread.start();
        updateGameList();
    }

    /**
     * Unregisters a game with the given id by interrupting its thread and removing it from the map.
     * @param id id of the game to remove
     */
    public void unregisterGame(UUID id) {
        GameContainer container = games.get(id);
        if (container != null) {
            container.getThread().interrupt();
            games.remove(id);
            System.out.println("[Server] Game " + id + " removed");
            updateGameList();
        }
    }

    /**
     * Returns the game the player is currently in.
     * @param player the player for whom to retrieve the game
     * @return the game the player is in or null if none
     */
    public synchronized BattleShipGame getGame(ServerPlayer player) {
        for (GameContainer container : games.values()) {
            BattleShipGame game = container.getGame();
            if (game == null) continue;
            if (game.getPlayerA() != null && game.getPlayerA().getId().equals(player.getId())) {
                return game;
            }
            if (game.getPlayerB() != null && game.getPlayerB().getId().equals(player.getId())) {
                return game;
            }
        }
        return null;
    }

    /**
     * Returns the game with the given id.
     * @param joinCode the join code of the game to retrieve
     * @return the game with the given id or null if none
     */
    public BattleShipGame getGameFromJoinCode(int joinCode) {
        for (GameContainer container : games.values()) {
            BattleShipGame game = container.getGame();
            if (game.getGameState().getSessionCode() == joinCode) {
                return game;
            }
        }
        return null;
    }

    /**
     * Adds a player to the queue and sends an update message to the player.
     * @param player the player to add to the queue
     */
    public void addToQueue(ServerPlayer player) {
        queue.add(player);
        player.sendMessage(new QueueUpdateMessage(queue.size(), true));
    }

    /**
     * Removes a player from the queue and sends an update message to the player.
     * @param id the id of the player to remove from the queue
     */
    public void removeFromQueue(UUID id) {
        queue.removeIf(player -> {
            if (player.getId().equals(id)) {
                player.sendMessage(new QueueUpdateMessage(queue.size(), false));
                return true;
            }
            return false;
        });
    }

    /**
     * Updates the player list in the GUI.
     */
    private void updatePlayerList() {
        StringBuilder sb = new StringBuilder();
        for (ServerPlayer p : players) {
            sb.append(p.getUsername()).append(" (").append(p.getIp()).append(")\n");
        }
        gui.updatePlayerList(sb.toString());
        gui.updatePlayerCount(players.size());
    }

    /**
     * Updates the game list in the GUI.
     */
    public void updateGameList() {
        StringBuilder sb = new StringBuilder();
        for (GameContainer container : games.values()) {
            BattleShipGame game = container.getGame();
            // Nur aktive Spiele anzeigen
            if (game.getGameState().getStatus().equals(GameState.GameStatus.GAME_OVER)) continue;
            String gameId = game.getGameState().getId().toString();
            String playerAName = game.getPlayerA() != null ? game.getPlayerA().getUsername() : "waiting";
            String playerBName = game.getPlayerB() != null ? game.getPlayerB().getUsername() : "waiting";
            int moveACount = game.getGameState().getPlayerA() != null ? game.getGameState().getPlayerA().getMoves().size() : 0;
            int moveBCount = game.getGameState().getPlayerB() != null ? game.getGameState().getPlayerB().getMoves().size() : 0;
            String status = game.getGameState().getStatus().toString();
            sb.append("Game ID: ").append(gameId).append("\n");
            sb.append("Size: ").append(game.getSize()).append("\n");
            sb.append("Join Code: ").append(game.getGameState().getSessionCode()).append("\n");
            sb.append("Game ID: ").append(gameId).append("\n");
            sb.append("Current Turn: ").append(game.getGameState().getCurrentTurnPlayer() != null ? game.getGameState().getCurrentTurnPlayer().getName() : "-").append("\n");
            sb.append("Player A: ").append(playerAName).append(" (Moves: ").append(moveACount).append(")\n");
            sb.append("Player B: ").append(playerBName).append(" (Moves: ").append(moveBCount).append(")\n");
            sb.append("Status: ").append(status).append("\n");
            sb.append("--------------\n");
        }
        gui.updateGameList(sb.toString());
    }

    public List<ServerPlayer> getPlayers() {
        return players;
    }

    public ArrayList<ServerPlayer> getQueue() {
        return queue;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Removes a player from the server. If the player is in a game, the game is ended and unregistered.
     * @param player the player leaving the server
     */
    public void removePlayer(ServerPlayer player) {
        BattleShipGame game = getGame(player);
        System.out.println("[Server] Player " + player.getUsername() + " left the server");

        if (game != null) {
            game.removePlayer(player);
        }
        players.remove(player);
        updatePlayerList();
        updateGameList();
    }

    /**
     * Prints the local IP address of the client.
     */
    private static void printLocalAddress() {
        try {
            System.out.println("\n__________         __    __  .__           _________.__    .__              \n" +
                    "\\______   \\_____ _/  |__/  |_|  |   ____  /   _____/|  |__ |__|_____  ______\n" +
                    " |    |  _/\\__  \\\\   __\\   __\\  | _/ __ \\ \\_____  \\ |  |  \\|  \\____ \\/  ___/\n" +
                    " |    |   \\ / __ \\|  |  |  | |  |_\\  ___/ /        \\|   Y  \\  |  |_> >___ \\ \n" +
                    " |______  /(____  /__|  |__| |____/\\___  >_______  /|___|  /__|   __/____  >\n" +
                    "        \\/      \\/                     \\/        \\/      \\/   |__|       \\/");

            System.out.println("\n\nWelcome to BattleShip Server! \n" +
                    "Server started on port " + PORT + "\n" +
                    "To stop the server, press Ctrl+C\n\n");
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("[Server] Connect to in locally with --p " + PORT + " --h 127.0.0.1" + " (localhost)");
            System.out.println("[Server] Connect within your network with --p " + PORT + " --h " + localHost.getHostAddress() + " (local IP)");
        } catch (UnknownHostException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}