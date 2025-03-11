package client.gui;

import client.ClientHandler;
import client.Stage;
import javax.swing.*;
import java.awt.*;

public class LobbyScene extends JPanel {

    private ClientHandler clientHandler;

    JLabel titleLabel;
    JLabel welcomeLabel;

    JPanel buttonPanel;
    JButton queueButton;
    JButton createButton;
    JButton joinButton;

    public Dimension getWindowSize() {
        return new Dimension(430, 270);
    }

    public LobbyScene(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(getWindowSize());

        titleLabel = new JLabel("Battleships");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeLabel = new JLabel("Welcome, " + clientHandler.getUsername() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        queueButton = new JButton("Queue");
        createButton = new JButton("Create");
        joinButton = new JButton("Join");

        Dimension buttonSize = new Dimension(140, 40);
        queueButton.setPreferredSize(buttonSize);
        createButton.setPreferredSize(buttonSize);
        joinButton.setPreferredSize(buttonSize);

        createButton.addActionListener(e -> {
            this.clientHandler.getStageManager().switchScene(Stage.CREATE_GAME_SCENE);
        });

        joinButton.addActionListener(e -> {
            JPanel panel = new JPanel(new BorderLayout(5, 5));

            JLabel label = new JLabel("Enter 6-digit Game ID:");
            JTextField textField = new JTextField();

            // Validation of the Game ID
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

                    // Validation of the Game ID
                    if (gameId.matches("\\d{6}")) {
                        System.out.println("Joining game with id: " + gameId);
                        this.clientHandler.getLobbyHandler().sendJoinGameWithCodeEvent(Integer.parseInt(gameId));
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
                    // User clicked Cancel or closed the dialog
                    break;
                }
            } while (true);
        });

        String queueSize = "(" + this.clientHandler.getLobbyHandler().getQueueLength() + ")";

        queueButton.setText(this.clientHandler.getLobbyHandler().isInQueue() ? "Leave Queue" : "Enter Queue " + queueSize);
        queueButton.addActionListener(e -> {

            try {
                if (!this.clientHandler.getLobbyHandler().isInQueue()) {
                    this.clientHandler.getLobbyHandler().sendJoinQueueEvent();
                } else {
                    this.clientHandler.getLobbyHandler().sendLeaveQueueEvent();
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });

        buttonPanel.add(queueButton);
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Komponenten hinzufügen
        add(titleLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(welcomeLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(buttonPanel);
    }

    /**
     * Update the queue button text based on the queue status.
     */
    public void updateQueueButton() {
        queueButton.setText(this.clientHandler.getLobbyHandler().isInQueue() ? "Leave Queue" : "Enter Queue");
    }

    /**
     * Set the username on the welcome label.
     * @param username The username to be displayed.
     */
    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    /**
     * Set the queue length on the queue button.
     * @param queueLength The queue length.
     */
    public void setQueueLength(int queueLength) {
        if(this.clientHandler.getLobbyHandler().isInQueue()) {
            queueButton.setText("Leave Queue");
        } else {
            queueButton.setText("Enter Queue (" + queueLength + ")");
        }
    }
}
