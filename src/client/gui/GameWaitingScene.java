package client.gui;

import client.GameHandler;
import client.StageManager;

import javax.swing.*;
import java.awt.*;

public class GameWaitingScene extends JPanel {
    private final JLabel waitingLabel;
    private final Timer animationTimer;
    private int dotCount = 0;
    private final GameHandler gameHandler;

    int gameCode;

    public GameWaitingScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.gameCode = gameHandler.getGameState().getSessionCode();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 220));

        // Überschrift mit Game-Code
        JLabel codeLabel = new JLabel("Game Code: " + gameCode);
        codeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Modus-Label (z. B. 8x8)
        JLabel modeLabel = new JLabel("Mode: " + gameHandler.getGameState().getBoardSize() + "x" + gameHandler.getGameState().getBoardSize());
        modeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Wartetext mit animierten Punkten
        waitingLabel = new JLabel("Waiting for players");
        waitingLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Animation der Punkte "..."
        animationTimer = new Timer(500, e -> {
            dotCount = (dotCount + 1) % 4; // Zählt von 0 bis 3
            String dots = ".".repeat(dotCount);
            waitingLabel.setText("Waiting for players" + dots);
        });
        animationTimer.start();

        // Back-Button (volle Breite)
        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // Volle Breite
        backButton.setFont(new Font("Arial", Font.BOLD, 14));

        backButton.addActionListener(e -> {
            animationTimer.stop();
            gameHandler.getStageManager().switchScene(StageManager.Stages.LOBBY_SCENE);
            gameHandler.leaveGame();
        });

        // Komponenten hinzufügen
        add(codeLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(modeLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(waitingLabel);
        add(Box.createVerticalGlue()); // Schiebt den Button nach unten
        add(backButton);
    }

    public void stopAnimation() {
        animationTimer.stop();
    }
}
