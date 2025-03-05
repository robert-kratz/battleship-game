package client.gui;

import client.GameHandler;
import protocol.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class GameOverScene extends JPanel {
    private final GameHandler gameHandler;

    public GameOverScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Hole den aktuellen GameState und die ID des aktuellen Users
        GameState gameState = gameHandler.getGameState();
        UUID currentUserId = gameHandler.getClientHandler().getUserId();

        // Bestimme, welcher Spieler der aktuelle User ist (PlayerA oder PlayerB)
        boolean isUserPlayerA = currentUserId.equals(gameState.getPlayerA());
        // boolean isUserPlayerB = currentUserId.equals(gameState.getPlayerB()); // nicht zwingend nötig

        // Hole Gewinner und Verlierer aus dem GameState (bei GAME_OVER ist winner gesetzt)
        UUID winnerId = gameState.getWinner();
        String winnerName = winnerId.equals(gameState.getPlayerA()) ? gameState.getPlayerAName() : gameState.getPlayerBName();
        String loserName;
        if (winnerId.equals(gameState.getPlayerA())) {
            loserName = gameState.getPlayerBName();
        } else {
            loserName = gameState.getPlayerAName();
        }

        // Oberste Überschrift: "You won" oder "You lost"
        JLabel resultLabel;
        if (currentUserId.equals(winnerId)) {
            resultLabel = new JLabel("You won");
        } else {
            resultLabel = new JLabel("You lost");
        }
        resultLabel.setFont(new Font("Arial", Font.BOLD, 32));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Gewinner-Klarname unterhalb der Überschrift (kleiner dargestellt)
        JLabel winnerLabel = new JLabel("Winner: " + winnerName);
        winnerLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Verlierer-Anzeige – wenn der User verloren hat, wird "(You)" angehängt
        String loserText = "Loser: " + loserName;
        if (!currentUserId.equals(winnerId)) {
            loserText += " (You)";
        }
        JLabel loserLabel = new JLabel(loserText);
        loserLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        loserLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel für Statistiken (nebeneinander in zwei Spalten)
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 2, 10, 0));
        statsPanel.setMaximumSize(new Dimension(460, 200));

        // Statistik-Panel für Player A
        JPanel playerAPanel = new JPanel();
        playerAPanel.setLayout(new BoxLayout(playerAPanel, BoxLayout.Y_AXIS));
        playerAPanel.setBorder(BorderFactory.createTitledBorder(gameState.getPlayerAName()));
        JLabel energyALabel = new JLabel("Energy: " + gameState.getEnergyA());
        energyALabel.setFont(new Font("Arial", Font.PLAIN, 18));
        energyALabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel movesALabel = new JLabel("Moves: " + gameState.getMoveA().size());
        movesALabel.setFont(new Font("Arial", Font.PLAIN, 18));
        movesALabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerAPanel.add(Box.createVerticalGlue());
        playerAPanel.add(energyALabel);
        playerAPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        playerAPanel.add(movesALabel);
        playerAPanel.add(Box.createVerticalGlue());

        // Statistik-Panel für Player B
        JPanel playerBPanel = new JPanel();
        playerBPanel.setLayout(new BoxLayout(playerBPanel, BoxLayout.Y_AXIS));
        playerBPanel.setBorder(BorderFactory.createTitledBorder(gameState.getPlayerBName()));
        JLabel energyBLabel = new JLabel("Energy: " + gameState.getEnergyB());
        energyBLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        energyBLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel movesBLabel = new JLabel("Moves: " + gameState.getMoveB().size());
        movesBLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        movesBLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBPanel.add(Box.createVerticalGlue());
        playerBPanel.add(energyBLabel);
        playerBPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        playerBPanel.add(movesBLabel);
        playerBPanel.add(Box.createVerticalGlue());

        statsPanel.add(playerAPanel);
        statsPanel.add(playerBPanel);

        // Back-Button: Beim Klick verlässt der User das aktuelle Spiel und gelangt zurück zur Lobby.
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.addActionListener(e -> {
            gameHandler.getClientHandler().getStageManager().exitGameScene();
            System.out.println("Back to lobby");
        });

        // Komponenten zum Panel hinzufügen
        add(resultLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(winnerLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(loserLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(statsPanel);
        add(Box.createVerticalGlue());
        add(backButton);
    }
}