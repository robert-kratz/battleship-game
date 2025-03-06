package server;

import protocol.GameState;
import protocol.Ship;
import protocol.messages.ErrorMessage;
import protocol.ErrorType;
import protocol.messages.*;
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

                System.out.println("Received: " + received.getClass().getSimpleName());

                switch (received.getType()) {
                    case MessageType.JOIN_QUEUE -> {
                        //Check if the player is already in a game

                        if(game != null) {
                            //remove player from game
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            break;
                        }

                        // Check if player is already in queue
                        if(server.getQueue().contains(this)) {
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_QUEUE));
                            break;
                        }

                        // Add player to queue
                        server.addToQueue(this);

                        // Notify all players in queue
                        for (PlayerInfo player : server.getPlayers()) {
                            boolean isInQueue = server.getQueue().contains(player);
                            player.sendMessage(new QueueUpdateMessage(server.getQueue().size(), isInQueue));
                        }

                        // Check if there are enough players in queue to start a game
                        if(server.getQueue().size() >= 2) {
                            PlayerInfo playerA = server.getQueue().get(0);
                            PlayerInfo playerB = server.getQueue().get(1);

                            server.getQueue().remove(playerA);
                            server.getQueue().remove(playerB);

                            createGame(10, playerA, playerB);
                        }
                    }
                    case MessageType.LEAVE_QUEUE -> {
                        if(game != null) {
                            game.leaveGame(this);
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_GAME));
                            break;
                        }

                        if(!server.getQueue().contains(this)) {
                            sendMessage(new ErrorMessage(ErrorType.NOT_IN_QUEUE));
                            break;
                        }

                        server.removeFromQueue(this.getId());

                        for (PlayerInfo player : server.getPlayers()) {
                            boolean isInQueue = server.getQueue().contains(player);
                            System.out.println("Player " + player.getUsername() + " is in queue: " + isInQueue);
                            player.sendMessage(new QueueUpdateMessage(server.getQueue().size(), isInQueue));
                        }
                    }
                    case MessageType.CREATE_GAME -> {
                        CreateGameMessage createGameMessage = (CreateGameMessage) received;

                        if(createGameMessage.getSize() < 5 || createGameMessage.getSize() > 20) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_GAME_SIZE));
                            return;
                        }

                        createGame(createGameMessage.getSize());

                        this.isInGame = true;
                    }
                    case MessageType.JOIN_GAME_WITH_CODE -> {
                        JoinGameWithCodeMessage joinGameWithCodeMessage = (JoinGameWithCodeMessage) received;

                        BattleShipGame targetGame = server.getGameFromJoinCode(joinGameWithCodeMessage.getSessionCode());

                        if (targetGame == null) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_SESSION_CODE));
                            return;
                        }

                        targetGame.addPlayer(this);

                        this.isInGame = true;
                    }
                    case MessageType.LEAVE_GAME -> {
                        LeaveGameMessage leaveGameMessage = (LeaveGameMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            break;
                        }

                        System.out.println("Player " + this.username + " left game " + game.getGameState().getId());
                        System.out.println("Playeramount: " + game.getPlayerAmount());

                        game.leaveGame(this);

                    }
                    case MessageType.UPDATE_BUILD_BOARD -> {
                        //Wir gehen davon aus, dass jedes mal wenn ein Schiff platziert wird, auch die gesamte Liste der Schiffe

                        UpdateBuildBoardMessage updateBuildBoardMessage = (UpdateBuildBoardMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            break;
                        }

                        game.onPlayerPlaceShips(this, updateBuildBoardMessage.getShips());

                        break;
                    }
                    case MessageType.PLAYER_READY -> {
                        PlayerReadyMessage playerReadyMessage = (PlayerReadyMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            break;
                        }

                        System.out.println("Received a player ready message from " + this.username + " with ready state: " + playerReadyMessage.ready);

                        game.onPlayerReadyStateChange(this, playerReadyMessage.ready);
                    }
                    case MessageType.PLAYER_HOVER -> {
                        PlayerHoverMessage playerHoverMessage = (PlayerHoverMessage) received;

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            break;
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
                            break;
                        }

                        if(!game.getGameState().getStatus().equals(GameState.GameStatus.IN_GAME)) return;

                        game.playerMove(this, playerMoveMessage.getMove());
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

        playerA.setInGame(true);
        playerB.setInGame(true);

        game.addPlayer(playerA);
        game.addPlayer(playerB);

        server.registerGame(game);
    }

    public void sendMessage(Message message) {
        try {
            System.out.println("Sending: " + message.getClass().getSimpleName());
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