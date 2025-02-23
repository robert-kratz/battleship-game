package server;

import protocol.messages.ErrorMessage;
import protocol.ErrorType;
import protocol.messages.*;
import utils.Usernames;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class PlayerInfo implements Runnable {

    private Server server;

    private Socket socket;
    private UUID id;
    private UUID secret;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String ip;

    private boolean isInGame = false;

    public PlayerInfo(Socket socket, Server server) {
        this.socket = socket;
        this.id = UUID.randomUUID();
        this.secret = UUID.randomUUID();
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
            in = new ObjectInputStream(socket.getInputStream());

            sendMessage(new RegisterMessage(username, id, secret, server.getQueue().size()));
            sendMessage(new QueueUpdateMessage(server.getQueue().size(), false));
            while (socket.isConnected()) {
                Object received = in.readObject();

                System.out.println("Received: " + received.getClass().getSimpleName());

                switch (received.getClass().getSimpleName()) {
                    case "JoinQueueMessage" -> {
                        if(server.getQueue().contains(this)) {
                            sendMessage(new ErrorMessage(ErrorType.ALREADY_IN_QUEUE));
                            break;
                        }
                        server.addToQueue(this);

                        for (PlayerInfo player : server.getPlayers()) {
                            boolean isInQueue = server.getQueue().contains(player);
                            System.out.println("Player " + player.getUsername() + " is in queue: " + server.getQueue().contains(player));
                            player.sendMessage(new QueueUpdateMessage(server.getQueue().size(), isInQueue));
                        }

                        System.out.println("Player " + username + " joined the queue");
                        System.out.println("Queue size: " + server.getQueue().size());

                        if(server.getQueue().size() >= 2) {
                            PlayerInfo playerA = server.getQueue().get(0);
                            PlayerInfo playerB = server.getQueue().get(1);
                            server.getQueue().remove(playerA);
                            server.getQueue().remove(playerB);

                            createGame(10, playerA, playerB);
                        }
                    }
                    case "QuitQueueMessage" -> {
                        if(!server.getQueue().contains(this)) {
                            sendMessage(new ErrorMessage(ErrorType.NOT_IN_QUEUE));
                            break;
                        }
                        server.removeFromQueue(this.getId());

                        for (PlayerInfo player : server.getPlayers()) {
                            System.out.println("Player " + player.getUsername() + " is in queue: " + server.getQueue().contains(player));
                            boolean isInQueue = server.getQueue().contains(player);
                            player.sendMessage(new QueueUpdateMessage(server.getQueue().size(), isInQueue));
                        }
                    }
                    case "JoinGameMessage" -> {
                        JoinGameMessage joinGameMessage = (JoinGameMessage) received;

                        this.isInGame = true;

                        BattleShipGame targetGame = null;

                        for (BattleShipGame game : server.getGames()) {
                            if (game.getGameState().getSessionCode() == joinGameMessage.getSessionCode()) {
                                targetGame = game;
                            }
                        }

                        if (targetGame == null) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_SESSION_CODE));
                            break;
                        }

                        targetGame.addPlayer(this);
                        System.out.println("Player B joined the game");
                    }
                    case "CreateGameMessage" -> {
                        CreateGameMessage createGameMessage = (CreateGameMessage) received;

                        this.isInGame = true;

                        if(createGameMessage.getSize() < 5 || createGameMessage.getSize() > 20) {
                            sendMessage(new ErrorMessage(ErrorType.INVALID_GAME_SIZE));
                            break;
                        }

                        this.createGame(createGameMessage.getSize());

                        System.out.println("Player A created a game");
                    }
                    case "SubmitPlacementMessage" -> {
                        SubmitPlacementMessage submitPlacementMessage = (SubmitPlacementMessage) received;

                        BattleShipGame game = server.getGame(this);

                        if(game == null) {
                            sendMessage(new ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS));
                            break;
                        }

                        System.out.println("Player " + username + " submitted placement");
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
     * Create a new game with the given size for the current player
     * @param size size of the game board
     */
    private void createGame(int size) {
        BattleShipGame game = new BattleShipGame(this.server, size);
        Thread gameThread = new Thread(game);

        game.addPlayer(this);

        server.registerGame(game, gameThread);
    }

    /**
     * Create a new game with the given size for the two given players
     * @param size size of the game board
     * @param playerA playerA
     * @param playerB playerB
     */
    private void createGame(int size, PlayerInfo playerA, PlayerInfo playerB) {
        BattleShipGame game = new BattleShipGame(this.server, size);
        Thread gameThread = new Thread(game);

        game.addPlayer(playerA);
        game.addPlayer(playerB);

        server.registerGame(game, gameThread);
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID getId() {
        return id;
    }
}