package server;

import protocol.GameOptions;
import protocol.GameState;
import protocol.messages.ErrorMessage;
import protocol.ErrorType;
import protocol.messages.*;
import protocol.messages.game.LeaveGameMessage;
import protocol.messages.game.building.PlayerReadyMessage;
import protocol.messages.game.building.PlayerUpdateShipPlacement;
import protocol.messages.RegisterMessage;
import protocol.messages.game.ingame.PlayerHoverMessage;
import protocol.messages.game.ingame.PlayerMoveMessage;
import protocol.messages.lobby.CreateGameMessage;
import protocol.messages.lobby.JoinGameWithCodeMessage;
import protocol.messages.lobby.QueueUpdateMessage;
import utils.Usernames;

import java.io.*;
import java.net.Socket;
import java.util.UUID;


/**
 * Aufgabe 2
 */

public class ServerPlayer implements Runnable {

    private final Server server;

    private final Socket socket;
    private final UUID id;
    private ObjectOutputStream out;
    private final String username;
    private final String ip;

    private boolean isInGame = false;

    /**
     * Aufgabe 2: Implementieren das Server Protokoll
     */
    
    @Override
    public void run() {
        // TODO
    }
    
    /**
     * Aufgabe 2: Implementieren Sie die Methode sendMessage 1P
     */
    
    /**
     * Sends a message to the player.
     * @param message the message to send
     */
    public void sendMessage(Message message) {
        // TODO
    }
    
    
    
    
    
    /**
     * ACHTUNG! AB HIER DÜRFEN KEINE ÄNDERUNGEN MEHR VORGENOMMEN WERDEN
     */

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public ServerPlayer(Socket socket, Server server) {
        this.socket = socket;
        this.id = UUID.randomUUID();
        this.ip = socket.getInetAddress().getHostAddress();
        this.username = Usernames.generate();
        this.server = server;
    }
    
    /**
     * Broadcasts the current queue state to all players.
     */
    private void broadcastQueueStateUpdate() {
        for (ServerPlayer player : server.getPlayers()) {
            boolean isInQueue = server.getQueue().contains(player);
            player.sendMessage(new QueueUpdateMessage(server.getQueue().size(), isInQueue));
        }
    }

    /**
     * Checks the queue for possible game creation.
     */
    private void checkQueueForPossibleGame() {
        // Check if there are enough players in queue to start a game
        if(server.getQueue().size() >= 2) {
            ServerPlayer playerA = server.getQueue().get(0);
            ServerPlayer playerB = server.getQueue().get(1);

            server.getQueue().remove(playerA);
            server.getQueue().remove(playerB);

            createGame(playerA, playerB);
        }
    }

    /**
     * Create a new game with the given size for the current player.
     * @param gameOptions gameOptions
     */
    private void createGame(GameOptions gameOptions) {
        BattleShipGame game = new BattleShipGame(this.server, gameOptions);
        game.addPlayer(this);
        server.registerGame(game);
    }

    /**
     * Create a new game with the given size for the two given players.
     * @param playerA playerA
     * @param playerB playerB
     */
    private void createGame(ServerPlayer playerA, ServerPlayer playerB) {
        BattleShipGame game = new BattleShipGame(this.server);
        System.out.println("[Server] Game created with player A: " + playerA.getUsername() +
                " and player B: " + playerB.getUsername());

        game.addPlayer(playerA);
        game.addPlayer(playerB);

        server.registerGame(game);
    }

    /**
     * Logs a message to the console.
     * @param message the message to log
     */
    private void logToConsole(String message) {
        if(message.contains("Error")) {
            System.err.println("[Player " + username + "] " + message);
        } else {
            System.out.println("[Player " + username + "] " + message);
        }
    }
    
    public String getUsername() { return username; }
    public String getIp() { return ip; }
    
    public void setInGame(boolean inGame) {
        isInGame = inGame;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public UUID getId() {
        return id;
    }
}