package server;
import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private JButton toggleButton;
    private JTextArea playerList;
    private JLabel playerCountLabel;
    private Server server;
    private boolean serverRunning = false;

    public ServerGUI(Server server) {
        this.server = server;
        setTitle("Schiffe Versenken - Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        playerList = new JTextArea();
        playerList.setEditable(false);
        add(new JScrollPane(playerList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        toggleButton = new JButton("Server Starten");
        toggleButton.setBackground(Color.RED);
        toggleButton.setOpaque(true);
        toggleButton.setBorderPainted(false);

        playerCountLabel = new JLabel("Spieler verbunden: 0", SwingConstants.CENTER);

        toggleButton.addActionListener(e -> toggleServer());

        bottomPanel.add(toggleButton, BorderLayout.CENTER);
        bottomPanel.add(playerCountLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void toggleServer() {
        if (serverRunning) {
            server.stopServer();
            toggleButton.setText("Server Starten");
            toggleButton.setBackground(Color.RED);
        } else {
            server.startServer();
            toggleButton.setText("Server Stoppen");
            toggleButton.setBackground(Color.GREEN);
        }
        serverRunning = !serverRunning;
    }

    public void updatePlayerList(String players) {
        playerList.setText(players);
        playerCountLabel.setText("Spieler verbunden: " + players.split("\n").length);
    }
}