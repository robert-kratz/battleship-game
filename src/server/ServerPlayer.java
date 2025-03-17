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
 * Aufgabe 2 B:
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
     * Aufgabe 2b1: Implementieren das Server Protokoll
     */
    
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream()); //0.5P
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream()); //0.5P

            sendMessage(new RegisterMessage(username, id)); //0.5P
            sendMessage(new QueueUpdateMessage(server.getQueue().size(), false)); //0.5P

            while (socket.isConnected()) {
                Message received = (Message) in.readObject(); //0.5P

                BattleShipGame game = server.getGame(this);

                logToConsole("Received: " + received.getClass().getSimpleName());

                switch (received.getType()) {
                    case MessageType.JOIN_QUEUE -> { //0.5P

                        if(game != null) {
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME)); // 0.5
                            return;
                        }

                        if(server.getQueue().contains(this)) {
                            broadcastQueueStateUpdate();
                            return;
                        }

                        server.addToQueue(this); // 0.5

                        broadcastQueueStateUpdate();

                        checkQueueForPossibleGame();
                    }
                    case MessageType.LEAVE_QUEUE -> { //0.5P
                        if(game != null) {
                            game.removePlayer(this);
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME)); // 0.5
                            return;
                        }

                        if(!server.getQueue().contains(this)) {
                            broadcastQueueStateUpdate();
                            return;
                        }

                        server.removeFromQueue(this.getId()); // 0.5

                        broadcastQueueStateUpdate();
                    }
                    case MessageType.CREATE_GAME -> { //0.5P
                        CreateGameMessage createGameMessage = (CreateGameMessage) received; // 0.5

                        if(createGameMessage.getGameOptions().getBoardSize() < 5 || createGameMessage.getGameOptions().getBoardSize() > 20) { // 0.5
                            sendMessage(new ErrorMessage(ErrorType.INVALID_GAME_SIZE));
                            return;
                        }

                        createGame(createGameMessage.getGameOptions()); // 0.5

                        server.getQueue().remove(this);

                        this.isInGame = true;
                    }
                    case MessageType.JOIN_GAME_WITH_CODE -> { //0.5P
                        JoinGameWithCodeMessage joinGameWithCodeMessage = (JoinGameWithCodeMessage) received; // 0.5

                        if(game != null) {
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            return;
                        }

                        BattleShipGame targetGame = server.getGameFromJoinCode(joinGameWithCodeMessage.getSessionCode()); // 0.5

                        if (targetGame == null) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_SESSION_CODE)); // 0.5
                            return;
                        }

                        if(!targetGame.getGameState().getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) { // 0.5
                            sendMessage(new ErrorMessage(ErrorType.GAME_ALREADY_STARTED));
                            return;
                        }
 
                        server.getQueue().remove(this); // 0.5

                        targetGame.addPlayer(this); // 0.5

                        this.isInGame = true;
                    }
                    case MessageType.LEAVE_GAME -> { //0.5P
                        LeaveGameMessage leaveGameMessage = (LeaveGameMessage) received; // 0.5

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.removePlayer(this); // 0.5

                        this.isInGame = false;
                    }
                    case MessageType.PLAYER_UPDATE_SHIP_PLACEMENT -> { //0.5P
                        PlayerUpdateShipPlacement playerUpdateShipPlacement = (PlayerUpdateShipPlacement) received; // 0.5

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerPlaceShips(this, playerUpdateShipPlacement.getShips()); // 0.5
                    }
                    case MessageType.PLAYER_READY -> { //0.5P
                        PlayerReadyMessage playerReadyMessage = (PlayerReadyMessage) received; // 0.5

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerReadyStateChange(this, playerReadyMessage.ready); // 0.5
                    }
                    case MessageType.PLAYER_HOVER -> { //0.5P
                        PlayerHoverMessage playerHoverMessage = (PlayerHoverMessage) received; // 0.5

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        if(!game.getGameState().getStatus().equals(GameState.GameStatus.IN_GAME)) return; // 0.5

                        if(game.getPlayerA().getId().equals(this.getId())) {
                            game.getPlayerB().sendMessage(new PlayerHoverMessage(playerHoverMessage)); // 0.5
                        } else if(game.getPlayerB().getId().equals(this.getId())) {
                            game.getPlayerA().sendMessage(new PlayerHoverMessage(playerHoverMessage)); // 0.5
                        }
                    }
                    case MessageType.PLAYER_MOVE -> { //0.5P
                        PlayerMoveMessage playerMoveMessage = (PlayerMoveMessage) received; // 0.5

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerAttemptMove(this, playerMoveMessage.getMove()); // 0.5
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            server.removePlayer(this); // 0.5
            server.removeFromQueue(this.getId()); // 0.5
        }
    }
    
    /**
     * Aufgabe 2b2: Implementieren Sie die Methode sendMessage 1P
     */
    
    /**
     * Sends a message to the player.
     * @param message the message to send
     */
    public void sendMessage(Message message) {
        try {
            if(!message.getClass().getSimpleName().equals("PlayerHoverMessage")) logToConsole("Sending " + message.toString());
            out.writeObject(message); // 0.5
            out.flush(); // 0.5
        } catch (IOException e) {
            logToConsole("Failed to send message to player " + username + " (" + message.getType().toString() + ")");
            e.printStackTrace();
        }
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