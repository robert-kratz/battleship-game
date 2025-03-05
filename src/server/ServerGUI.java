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

        // Textbereich für die Spielerliste
        playerList = new JTextArea();
        playerList.setEditable(false);
        JScrollPane playerScrollPane = new JScrollPane(playerList);
        playerScrollPane.setBorder(BorderFactory.createTitledBorder("Players online"));

        // Textbereich für die Liste der aktiven Spiele
        gameList = new JTextArea();
        gameList.setEditable(false);
        JScrollPane gameScrollPane = new JScrollPane(gameList);
        gameScrollPane.setBorder(BorderFactory.createTitledBorder("Active games"));

        // Aufteilen in einen vertikalen SplitPane
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

    public void updateServerOnlineStatus(boolean isOnline) {
        toggleButton.setText(isOnline ? "Stop Server" : "Start Server");
        toggleButton.setBackground(isOnline ? Color.GREEN : Color.RED);
    }

    private void toggleServer() {
        if (server.isRunning()) {
            server.stopServer();
        } else {
            server.startServer();
        }
        updateServerOnlineStatus(server.isRunning());
    }

    public void updatePlayerList(String players) {
        playerList.setText(players);
    }

    public void updatePlayerCount(int count) {
        playerCountLabel.setText("Connected Players: " + count);
    }

    public void updateGameList(String games) {
        gameList.setText(games);
    }
}