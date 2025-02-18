import client.ClientHandler;
import server.Server;

public class Main {

    /**
     * Main method
     * @apiNote ATTENTION: This method is only used for testing purposes
     * @param args
     */
    public static void main(String[] args) {
        ClientHandler clientHandler1 = new ClientHandler("localhost", 12345);
        ClientHandler clientHandler2 = new ClientHandler("localhost", 12345);

        server = new Server();
        server.startServer();
    }

    public static Server server;
}