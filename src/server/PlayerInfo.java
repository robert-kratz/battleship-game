package server;

import protocol.GameState;
import protocol.messages.ErrorMessage;
import protocol.ErrorType;
import protocol.messages.*;
import protocol.messages.game.LeaveGameMessage;
import protocol.messages.game.building.PlayerReadyMessage;
import protocol.messages.game.building.UpdateBuildBoardMessage;
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

public class PlayerInfo implements Runnable {

    private final Server server;

    private final Socket socket;
    private final UUID id;
    private ObjectOutputStream out;
    private final String username;
    private final String ip;

    private boolean isInGame = false;

    public PlayerInfo(Socket socket, Server server) {
        this.socket = socket;
        this.id = UUID.randomUUID();
        this.ip = socket.getInetAddress().getHostAddress();
        this.username = Usernames.generate();
        this.server = server;
    }

    public String getUsername() { return username; }
    public String getIp() { return ip; }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            sendMessage(new RegisterMessage(username, id, server.getQueue().size()));
            sendMessage(new QueueUpdateMessage(server.getQueue().size(), false));

            while (socket.isConnected()) {
                Message received = (Message) in.readObject();

                BattleShipGame game = server.getGame(this);

                if(!received.getClass().getSimpleName().equals("PlayerHoverMessage")) System.out.println("Received: " + received.getClass().getSimpleName());

                switch (received.getType()) {
                    case MessageType.JOIN_QUEUE -> {
                        //Check if the player is already in a game

                        if(game != null) {
                            //remove player from game
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            return;
                        }

                        // Check if player is already in queue
                        if(server.getQueue().contains(this)) {
                            broadcastQueueStateUpdate();
                            return;
                        }

                        // Add player to queue
                        server.addToQueue(this);

                        // Notify all players in queue
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

                        if(createGameMessage.getSize() < 5 || createGameMessage.getSize() > 20) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_GAME_SIZE));
                            return;
                        }

                        createGame(createGameMessage.getSize());

                        server.getQueue().remove(this);

                        this.isInGame = true;
                    }
                    case MessageType.JOIN_GAME_WITH_CODE -> {
                        JoinGameWithCodeMessage joinGameWithCodeMessage = (JoinGameWithCodeMessage) received;

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
                    case MessageType.UPDATE_BUILD_BOARD -> {
                        UpdateBuildBoardMessage updateBuildBoardMessage = (UpdateBuildBoardMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        game.onPlayerPlaceShips(this, updateBuildBoardMessage.getShips());
                    }
                    case MessageType.PLAYER_READY -> {
                        PlayerReadyMessage playerReadyMessage = (PlayerReadyMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            return;
                        }

                        System.out.println("Received a player ready message from " + this.username + " with ready state: " + playerReadyMessage.ready);

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

                        if(!game.getGameState().getStatus().equals(GameState.GameStatus.IN_GAME)) return;

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

    private void broadcastQueueStateUpdate() {
        for (PlayerInfo player : server.getPlayers()) {
            boolean isInQueue = server.getQueue().contains(player);
            player.sendMessage(new QueueUpdateMessage(server.getQueue().size(), isInQueue));
        }
    }

    private void checkQueueForPossibleGame() {
        // Check if there are enough players in queue to start a game
        if(server.getQueue().size() >= 2) {
            PlayerInfo playerA = server.getQueue().get(0);
            PlayerInfo playerB = server.getQueue().get(1);

            server.getQueue().remove(playerA);
            server.getQueue().remove(playerB);

            createGame(10, playerA, playerB);
        }
    }

    /**
     * Create a new game with the given size for the current player.
     * Übergibt die gesamte Thread-Erstellung und -Logik an den Server.
     * @param size size of the game board
     */
    private void createGame(int size) {
        BattleShipGame game = new BattleShipGame(this.server, size);
        game.addPlayer(this);
        server.registerGame(game);
    }

    /**
     * Create a new game with the given size for the two given players.
     * Übergibt die gesamte Thread-Erstellung und -Logik an den Server.
     * @param size size of the game board
     * @param playerA playerA
     * @param playerB playerB
     */
    private void createGame(int size, PlayerInfo playerA, PlayerInfo playerB) {
        BattleShipGame game = new BattleShipGame(this.server, size);
        System.out.println("Game created with player A: " + playerA.getUsername() +
                " and player B: " + playerB.getUsername());

        game.addPlayer(playerA);
        game.addPlayer(playerB);

        server.registerGame(game);
    }

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