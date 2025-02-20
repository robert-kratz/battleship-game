package client.gui;

import client.ClientHandler;
import client.StageManager;
import protocol.messages.JoinQueueMessage;
import protocol.messages.QuitQueueMessage;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JPanel {
    private JLabel usernameLabel;
    private JButton queue;
    private ClientHandler clientHandler;

    public HomeScreen(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;

        setSize(300, 200);
        setLayout(new BorderLayout());

        usernameLabel = new JLabel("Warte auf Server...");
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(usernameLabel, BorderLayout.CENTER);

        String queueSize = "(" + clientHandler.getQueueLength() + ")";

        queue = new JButton("");
        queue.setText(clientHandler.isInQueue() ? "Leave Queue" : "Enter Queue " + queueSize);
        queue.addActionListener(e -> {

            System.out.println("Queue clicked" + clientHandler.isInQueue());

            if (!clientHandler.isInQueue()) {
                clientHandler.sendMessage(new JoinQueueMessage());
                setQueueLength(clientHandler.getQueueLength());
                clientHandler.setInQueue(true);
            } else {
                clientHandler.sendMessage(new QuitQueueMessage());
                setQueueLength(clientHandler.getQueueLength());
                clientHandler.setInQueue(false);
            }
        });


        JButton settingsButton = new JButton("Einstellungen");
        settingsButton.addActionListener(e -> clientHandler.getStageManager().switchScene(StageManager.Stages.BOARD));

        add(queue, BorderLayout.NORTH);
        add(settingsButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void setUsername(String username) {
        usernameLabel.setText("Dein Benutzername: " + username);
    }

    public void setQueueLength(int queueLength) {
        if(clientHandler.isInQueue()) {
            queue.setText("Leave Queue");
        } else {
            if (queueLength == 0) {
                queue.setText("Enter Queue");
            } else {
                queue.setText("Enter Queue (" + queueLength + ")");
            }
        }
    }
}
