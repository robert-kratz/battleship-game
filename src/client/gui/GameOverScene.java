package client.gui;

import client.GameHandler;
import protocol.ClientPlayer;
import protocol.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

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
        ArrayList<ClientPlayer> winners = gameState.getWinner();
        ArrayList<ClientPlayer> loosers = gameState.getLoser();

        String winnerNames = winners.stream().map(ClientPlayer::getName).collect(Collectors.joining(", "));
        String loserNames = loosers.stream().map(ClientPlayer::getName).collect(Collectors.joining(", "));

        // Oberste Überschrift: "You won" oder "You lost"
        JLabel resultLabel;
        if (winners.stream().anyMatch(player -> player.getId().equals(currentUserId))) {
            resultLabel = new JLabel("You won");
        } else {
            resultLabel = new JLabel("You lost");
        }
        resultLabel.setFont(new Font("Arial", Font.BOLD, 32));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Gewinner-Klarname unterhalb der Überschrift (kleiner dargestellt)
        String winnerText = "Winner: " + winnerNames;

        if(winners.stream().anyMatch(player -> player.getId().equals(currentUserId))) {
            winnerText += " (You)";
        }

        JLabel winnerLabel = new JLabel(winnerText);
        winnerLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Verlierer-Anzeige – wenn der User verloren hat, wird "(You)" angehängt
        String loserText = "Loser: " + loserNames;
        if (loosers.stream().anyMatch(player -> player.getId().equals(currentUserId))) {
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
        playerAPanel.setBorder(BorderFactory.createTitledBorder(gameState.getPlayerA().getName()));
        JLabel energyALabel = new JLabel("Energy: " + gameState.getPlayerA().getEnergy());
        energyALabel.setFont(new Font("Arial", Font.PLAIN, 18));
        energyALabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel movesALabel = new JLabel("Moves: " + gameState.getPlayerA().getMoves().size());
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
        playerBPanel.setBorder(BorderFactory.createTitledBorder(gameState.getPlayerB().getName()));
        JLabel energyBLabel = new JLabel("Energy: " + gameState.getPlayerB().getEnergy());
        energyBLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        energyBLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel movesBLabel = new JLabel("Moves: " + gameState.getPlayerB().getMoves().size());
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
            gameHandler.getClientHandler().endCurrentGame();
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