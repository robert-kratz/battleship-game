package server;
import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {

    private JButton toggleButton;
    private JTextArea playerList;
    private JTextArea gameList;
    private JLabel playerCountLabel;
    private Server server;

    public ServerGUI(Server server) {

        this.server = server;
        setTitle("BattleShip Server");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        playerList = new JTextArea();
        playerList.setEditable(false);
        JScrollPane playerScrollPane = new JScrollPane(playerList);
        playerScrollPane.setBorder(BorderFactory.createTitledBorder("Players online"));

        gameList = new JTextArea();
        gameList.setEditable(false);
        JScrollPane gameScrollPane = new JScrollPane(gameList);
        gameScrollPane.setBorder(BorderFactory.createTitledBorder("Active games"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, playerScrollPane, gameScrollPane);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        toggleButton = new JButton("Start Server");
        toggleButton.setBackground(server.isRunning() ? Color.GREEN : Color.RED);
        toggleButton.setOpaque(true);
        toggleButton.setBorderPainted(false);

        playerCountLabel = new JLabel("Connected Players: 0", SwingConstants.CENTER);

        toggleButton.addActionListener(e -> toggleServer());

        bottomPanel.add(toggleButton, BorderLayout.CENTER);
        bottomPanel.add(playerCountLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    /**
     * Updates the server online status.
     * @param isOnline true if the server is online, false otherwise.
     */
    public void updateServerOnlineStatus(boolean isOnline) {
        toggleButton.setText(isOnline ? "Stop Server" : "Start Server");
        toggleButton.setBackground(isOnline ? Color.GREEN : Color.RED);
    }

    /**
     * Toggles the server state between running and stopped.
     */
    private void toggleServer() {
        if (server.isRunning()) {
            server.stopServer();
        } else {
            server.startServer();
        }
        updateServerOnlineStatus(server.isRunning());
    }

    /**
     * Updates the player list displayed in the GUI.
     * @param players A string representation of the players currently connected to the server.
     */
    public void updatePlayerList(String players) {
        playerList.setText(players);
    }

    /**
     * Updates the game list displayed in the GUI.
     * @param count The number of players currently connected to the server.
     */
    public void updatePlayerCount(int count) {
        playerCountLabel.setText("Connected Players: " + count);
    }

    /**
     * Updates the game list displayed in the GUI.
     * @param games A string representation of the games currently active on the server.
     */
    public void updateGameList(String games) {
        gameList.setText(games);
    }
}