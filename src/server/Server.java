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

/**
 * Aufgabe 2 A: 13P
 */

public class Server {

    private static Server instance;
    private static int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running = false;
    private final ServerGUI gui;
    
    /**
     * Aufgabe 2a1: Implementiere die Listen und Maps: (2P)
     */

    private final List<ServerPlayer> players = new ArrayList<>(); //0.5P
    private final List<Thread> clientThreads = new ArrayList<>(); //0.5P

    private final Map<UUID, GameContainer> games = new HashMap<>(); //0.5P

    private final ArrayList<ServerPlayer> queue = new ArrayList<>(); //0.5P

    /**
     * Aufgabe 2a2: Implementieren die Methode: (3P)
     */
    
    public void startServer() {
        if (running) return;

        new Thread(() -> { // 0.5P
        	
            try {
            	
                serverSocket = new ServerSocket(PORT); // Starte Socket 0.5P
                
                running = true;
                
                this.gui.updateServerOnlineStatus(true);
                System.out.println("[Server] Starting server on port " + PORT);

                while (running) { //0.5P
                	
                    Socket clientSocket = serverSocket.accept(); // 0.5P
                    System.out.println("[Server] New connection from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " (" + clientSocket.getInetAddress().getHostName() + ");");

                    ServerPlayer player = new ServerPlayer(clientSocket, this); //0.5P
                    players.add(player);

                    Thread clientThread = new Thread(player); //0.5P
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
     * Aufgabe 2a3: Implementieren die Methode: 2P
     */
    
    public void registerGame(BattleShipGame game) {
        Thread gameThread = new Thread(game); // 0.5P
        GameContainer container = new GameContainer(game, gameThread); // 0.5P
        games.put(game.getGameState().getId(), container); // 0.5P
        gameThread.start(); // 0.5P
        updateGameList();
    }

    /**
     * Aufgabe 2a4: Implementieren die Methode: 1P
     */
    
    public void unregisterGame(UUID id) {
        GameContainer container = games.get(id); // 0.5P
        if (container != null) {
            container.getThread().interrupt();// 0.5P
            games.remove(id);
            System.out.println("[Server] Game " + id + " removed");
            updateGameList();
        }
    }
    
    /**
     * Aufgabe 2a4: Implementieren die Methode: 2P
     */

    public synchronized BattleShipGame getGame(ServerPlayer player) {
        for (GameContainer container : games.values()) { //0.5P
            BattleShipGame game = container.getGame();  //0.5P
            if (game == null) continue;
            if (game.getPlayerA() != null && game.getPlayerA().getId().equals(player.getId())) {  //0.5P
                return game;
            }
            if (game.getPlayerB() != null && game.getPlayerB().getId().equals(player.getId())) {  //0.5P
                return game;
            }
        }
        return null;
    }
    
    /**
     * Aufgabe 2a5: Implementieren die Methode: 1.5P
     */

    public BattleShipGame getGameFromJoinCode(int joinCode) {
        for (GameContainer container : games.values()) { // 0.5P
            BattleShipGame game = container.getGame(); // 0.5P
            if (game.getGameState().getSessionCode() == joinCode) { // 0.5P
                return game;
            }
        }
        return null;
    }
    
    /**
     * Aufgabe 2a6: Implementieren die Methode: 0.5P
     */

    public void addToQueue(ServerPlayer player) {
        queue.add(player);
        player.sendMessage(new QueueUpdateMessage(queue.size(), true)); // 0.5P
    }
    
    /**
     * Aufgabe 2a7: Implementieren die Methode: 1P
     */

    public void removeFromQueue(UUID id) {
        queue.removeIf(player -> {
            if (player.getId().equals(id)) {
                player.sendMessage(new QueueUpdateMessage(queue.size(), false)); //1P
                return true;
            }
            return false;
        });
    }
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * ACHTUNG! AB HIER DÜRFEN KEINE ÄNDERUNGEN MEHR VORGENOMMEN WERDEN!
     */
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
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