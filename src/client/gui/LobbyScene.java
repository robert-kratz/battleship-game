package client.gui;

import client.GameHandler;
import client.Stage;
import client.StageManager;
import javax.swing.*;
import java.awt.*;

public class LobbyScene extends JPanel {

    private GameHandler gameHandler;

    JLabel titleLabel;
    JLabel welcomeLabel;

    JPanel buttonPanel;
    JButton queueButton;
    JButton createButton;
    JButton joinButton;

    public LobbyScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding um das Panel

        // Große Überschrift
        titleLabel = new JLabel("Battleships");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Begrüßungstext
        welcomeLabel = new JLabel("Welcome, " + gameHandler.getUsername() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button-Panel
        buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // 10px Horizontalabstand zwischen Buttons
        queueButton = new JButton("Queue");
        createButton = new JButton("Create");
        joinButton = new JButton("Join");

        // Größe der Buttons anpassen
        Dimension buttonSize = new Dimension(140, 40);
        queueButton.setPreferredSize(buttonSize);
        createButton.setPreferredSize(buttonSize);
        joinButton.setPreferredSize(buttonSize);

        createButton.addActionListener(e -> {
            gameHandler.getStageManager().switchScene(Stage.CREATE_GAME_SCENE);
        });

        joinButton.addActionListener(e -> {
            JPanel panel = new JPanel(new BorderLayout(5, 5));

            JLabel label = new JLabel("Enter 6-digit Game ID:");
            JTextField textField = new JTextField();

            // InputVerifier für die Validierung (nur 6-stellige Zahlen)
            textField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    String text = ((JTextField) input).getText();
                    return text.matches("\\d{6}");
                }
            });

            panel.add(label, BorderLayout.NORTH);
            panel.add(textField, BorderLayout.CENTER);

            int result;
            do {
                result = JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Join Game",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    String gameId = textField.getText();

                    // Validierung der Game-ID
                    if (gameId.matches("\\d{6}")) {
                        System.out.println("Joining game with id: " + gameId);
                        this.gameHandler.joinGame(Integer.parseInt(gameId));
                        // Hier die Logik zum Joinen des Spiels einfügen
                        break;
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Invalid Game ID. Please enter exactly 6 digits (0-9).",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                } else {
                    // Abbruch des Dialogs
                    break;
                }
            } while (true);
        });

        String queueSize = "(" + gameHandler.getQueueLength() + ")";

        queueButton.setText(gameHandler.isInQueue() ? "Leave Queue" : "Enter Queue " + queueSize);
        queueButton.addActionListener(e -> {

            System.out.println("Queue clicked" + gameHandler.isInQueue());

            if (!gameHandler.isInQueue()) {
                System.out.println("Joining queue");
                gameHandler.joinQueue();
            } else {
                gameHandler.leaveQueue();
            }
        });

        buttonPanel.add(queueButton);
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Padding oberhalb der Buttons

        // Komponenten hinzufügen
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 10))); // Abstand zwischen Überschrift und Begrüßungstext
        add(welcomeLabel);
        add(Box.createRigidArea(new Dimension(0, 20))); // Abstand zwischen Begrüßungstext und Buttons
        add(buttonPanel);
    }

    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    public void setQueueLength(int queueLength) {
        if(gameHandler.isInQueue()) {
            queueButton.setText("Leave Queue");
        } else {
            queueButton.setText("Enter Queue (" + queueLength + ")");
        }
    }
}
