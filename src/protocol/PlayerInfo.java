package protocol;

import server.Server;
import utils.Usernames;

import java.io.*;
import java.net.Socket;

public class PlayerInfo implements Runnable {

    private Server server;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private String ip;

    public PlayerInfo(Socket socket, Server server) {
        this.socket = socket;
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

            sendMessage(new RegisterMessage(username));
            while (socket.isConnected()) {
                Object received = in.readObject();
                if (received instanceof protocol.Message) {
                    System.out.println("Nachricht von " + username + ": " + received);
                } else {
                    System.out.println("Ungültige Nachricht von " + username + ": " + received);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Verbindung mit " + username + " verloren.");
            server.removePlayer(this);
        }
    }

    public void sendMessage(protocol.Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}