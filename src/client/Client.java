package client;

public class Client {

    private static int PORT = 12345;
    private static String HOST = "localhost";

    /**
     * Main method to start the client
     * --p <port>:  set the port
     * --h <host>: set the host
     * @param args command line arguments
     */
    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--p")) {
                PORT = Integer.parseInt(args[i + 1]);
            } else if (args[i].equals("--h")) {
                HOST = args[i + 1];
            }
        }

        new ClientHandler(HOST, PORT);
    }
}
