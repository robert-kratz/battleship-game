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

public class ServerPlayer implements Runnable {

    private final Server server;

    private final Socket socket;
    private final UUID id;
    private ObjectOutputStream out;
    private final String username;
    private final String ip;

    private boolean isInGame = false;

    public ServerPlayer(Socket socket, Server server) {
        this.socket = socket;
        this.id = UUID.randomUUID();
        this.ip = socket.getInetAddress().getHostAddress();
        this.username = Usernames.generate();
        this.server = server;
    }

    public String getUsername() { return username; }
    public String getIp() { return ip; }

    /**
     * This method is called when the player is registered.
     */
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            sendMessage(new RegisterMessage(username, id));
            sendMessage(new QueueUpdateMessage(server.getQueue().size(), false));

            while (socket.isConnected()) {
                Message received = (Message) in.readObject();

                BattleShipGame game = server.getGame(this);

                if(!received.getClass().getSimpleName().equals("PlayerHoverMessage")) System.out.println("Received: " + received.getClass().getSimpleName());

                switch (received.getType()) {
                    case MessageType.JOIN_QUEUE -> {

                        if(game != null) {
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            return;
                        }

                        if(server.getQueue().contains(this)) {
                            broadcastQueueStateUpdate();
                            return;
                        }

                        server.addToQueue(this);

                        broadcastQueueStateUpdate();

                        checkQueueForPossibleGame();
                    }
                    case MessageType.LEAVE_QUEUE -> {
                        if(game != null) {
                            game.removePlayer(this);
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            return;
                        }

                        if(!server.getQueue().contains(this)) {
                            broadcastQueueStateUpdate();
                            return;
                        }

                        server.removeFromQueue(this.getId());

                        broadcastQueueStateUpdate();
                    }
                    case MessageType.CREATE_GAME -> {
                        CreateGameMessage createGameMessage = (CreateGameMessage) received;

                        if(createGameMessage.getGameOptions().getBoardSize() < 5 || createGameMessage.getGameOptions().getBoardSize() > 20) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_GAME_SIZE));
                            return;
                        }

                        createGame(createGameMessage.getGameOptions());

                        server.getQueue().remove(this);

                        this.isInGame = true;
                    }
                    case MessageType.JOIN_GAME_WITH_CODE -> {
                        JoinGameWithCodeMessage joinGameWithCodeMessage = (JoinGameWithCodeMessage) received;

                        if(game != null) {
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            return;
                        }

                        BattleShipGame targetGame = server.getGameFromJoinCode(joinGameWithCodeMessage.getSessionCode());

                        if (targetGame == null) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_SESSION_CODE));
                            return;
                        }

                        if(!targetGame.getGameState().getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) {
                            sendMessage(new ErrorMessage(ErrorType.GAME_ALREADY_STARTED));
                            return;
                        }

                        server.getQueue().remove(this);

                        targetGame.addPlayer(this);

                        this.isInGame = true;
                    }
                    case MessageType.LEAVE_GAME -> {
                        LeaveGameMessage leaveGameMessage = (LeaveGameMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.removePlayer(this);

                        this.isInGame = false;
                    }
                    case MessageType.PLAYER_UPDATE_SHIP_PLACEMENT -> {
                        PlayerUpdateShipPlacement playerUpdateShipPlacement = (PlayerUpdateShipPlacement) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerPlaceShips(this, playerUpdateShipPlacement.getShips());
                    }
                    case MessageType.PLAYER_READY -> {
                        PlayerReadyMessage playerReadyMessage = (PlayerReadyMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerReadyStateChange(this, playerReadyMessage.ready);
                    }
                    case MessageType.PLAYER_HOVER -> {
                        PlayerHoverMessage playerHoverMessage = (PlayerHoverMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        if(!game.getGameState().getStatus().equals(GameState.GameStatus.IN_GAME)) return;

                        if(game.getPlayerA().getId().equals(this.getId())) {
                            game.getPlayerB().sendMessage(new PlayerHoverMessage(playerHoverMessage));
                        } else if(game.getPlayerB().getId().equals(this.getId())) {
                            game.getPlayerA().sendMessage(new PlayerHoverMessage(playerHoverMessage));
                        }
                    }
                    case MessageType.PLAYER_MOVE -> {
                        PlayerMoveMessage playerMoveMessage = (PlayerMoveMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerAttemptMove(this, playerMoveMessage.getMove());
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Lost connection to player " + username);
            server.removePlayer(this);
            server.removeFromQueue(this.getId());
        }
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
        System.out.println("Game created with player A: " + playerA.getUsername() +
                " and player B: " + playerB.getUsername());

        game.addPlayer(playerA);
        game.addPlayer(playerB);

        server.registerGame(game);
    }

    /**
     * Sends a message to the player.
     * @param message the message to send
     */
    public void sendMessage(Message message) {
        try {
            if(!message.getClass().getSimpleName().equals("PlayerHoverMessage"))System.out.println("Sending: " + message.getClass().getSimpleName());
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to player " + username + " (" + message.getType().toString() + ")");
            e.printStackTrace();
        }
    }

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