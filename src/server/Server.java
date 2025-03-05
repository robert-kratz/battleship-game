package server;

import protocol.GameState;
import protocol.messages.ErrorMessage;
import protocol.ErrorType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    private final List<PlayerInfo> players = new ArrayList<>();
    private final List<Thread> clientThreads = new ArrayList<>();

    // HashMap, die sowohl das Spiel als auch den zugehörigen Thread speichert
    private final Map<UUID, GameContainer> games = new HashMap<>();

    private final ArrayList<PlayerInfo> queue = new ArrayList<>();
    private final ServerGUI gui;

    // Innerer Container, der das BattleShipGame und den zugehörigen Thread zusammenhält
    private static class GameContainer {
        private final BattleShipGame game;
        private final Thread thread;

        public GameContainer(BattleShipGame game, Thread thread) {
            this.game = game;
            this.thread = thread;
        }

        public BattleShipGame getGame() {
            return game;
        }

        public Thread getThread() {
            return thread;
        }
    }

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
                this.gui.updateServerOnlineStatus(true);
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
     * Returns the players that are currently in the lobby (not in a game).
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
     * Stops the server, closes all connections and unregisters all games.
     */
    public void stopServer() {
        running = false;
        try {
            // Alle laufenden Spiele abmelden
            for (UUID gameId : new ArrayList<>(games.keySet())) {
                unregisterGame(gameId);
            }
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
            System.out.println("Game " + id + " removed");
            updateGameList();
        }
    }

    /**
     * Returns the game the player is currently in.
     * @param player the player for whom to retrieve the game
     * @return the game the player is in or null if none
     */
    public synchronized BattleShipGame getGame(PlayerInfo player) {
        for (GameContainer container : games.values()) {
            BattleShipGame game = container.getGame();
            if (game.getPlayerA().getId().equals(player.getId()) || game.getPlayerB().getId().equals(player.getId())) {
                return game;
            }
        }
        return null;
    }

    /**
     * Removes a player from the server. If the player is in a game, the game is ended and unregistered.
     * @param player the player leaving the server
     */
    public void removePlayer(PlayerInfo player) {
        BattleShipGame game = getGame(player);
        System.out.println("Player " + player.getUsername() + " left the server");

        if (game != null) {
            game.leaveGame(player);
        }
        players.remove(player);
        updatePlayerList();
        updateGameList();
    }

    public void addToQueue(PlayerInfo player) {
        queue.add(player);
        player.sendMessage(new protocol.messages.QueueUpdateMessage(queue.size(), true));
    }

    public void removeFromQueue(UUID id) {
        queue.removeIf(player -> {
            if (player.getId().equals(id)) {
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
        gui.updatePlayerCount(players.size());
    }

    /**
     * Aktualisiert in der GUI die Liste der aktiven Spiele.
     * Für jedes Spiel werden die Game-ID (aus dem GameState), die Spielernamen,
     * die Anzahl der Züge (Move-Arrays) und der aktuelle Status ausgegeben.
     * Beendete Spiele (GAME_OVER) werden nicht angezeigt.
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
            int moveACount = game.getGameState().getMoveA().size();
            int moveBCount = game.getGameState().getMoveB().size();
            String status = game.getGameState().getStatus().toString();
            sb.append("Game ID: ").append(gameId).append("\n");
            sb.append("Player A: ").append(playerAName).append(" (Moves: ").append(moveACount).append(")\n");
            sb.append("Player B: ").append(playerBName).append(" (Moves: ").append(moveBCount).append(")\n");
            sb.append("Status: ").append(status).append("\n");
            sb.append("--------------\n");
        }
        gui.updateGameList(sb.toString());
    }

    public List<PlayerInfo> getPlayers() {
        return players;
    }

    public ArrayList<PlayerInfo> getQueue() {
        return queue;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Returns a list of all active games.
     */
    public ArrayList<BattleShipGame> getGames() {
        return new ArrayList<>(games.values().stream().map(GameContainer::getGame).collect(Collectors.toList()));
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