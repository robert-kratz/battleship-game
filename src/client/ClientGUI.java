package client;

import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private JLabel usernameLabel;

    public ClientGUI() {
        setTitle("Schiffe Versenken - Client");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        usernameLabel = new JLabel("Warte auf Server...");
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(usernameLabel, BorderLayout.CENTER);

        setVisible(true);
    }

    public void setUsername(String username) {
        usernameLabel.setText("Dein Benutzername: " + username);
    }
}