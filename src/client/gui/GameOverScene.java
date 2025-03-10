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

    /**
     * Returns the size of the window.
     * @return The size of the window as a Dimension object.
     */
    public Dimension getWindowSize() {
        return new Dimension(510, 590);
    }

    /**
     * Creates a new GameOverScene.
     * @param gameHandler The GameHandler instance used to manage the game state.
     */
    public GameOverScene(GameHandler gameHandler) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(getWindowSize());

        GameState gameState = gameHandler.getGameState();
        UUID currentUserId = gameHandler.getClientHandler().getUserId();

        // Get the winner and loser players
        ArrayList<ClientPlayer> winners = gameState.getWinner();
        ArrayList<ClientPlayer> loosers = gameState.getLoser();

        String winnerNames = winners.stream().map(ClientPlayer::getName).collect(Collectors.joining(", "));
        String loserNames = loosers.stream().map(ClientPlayer::getName).collect(Collectors.joining(", "));

        JLabel resultLabel;
        if (winners.stream().anyMatch(player -> player.getId().equals(currentUserId))) {
            resultLabel = new JLabel("You won");
        } else {
            resultLabel = new JLabel("You lost");
        }
        resultLabel.setFont(new Font("Arial", Font.BOLD, 32));
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String winnerText = "Winner: " + winnerNames;

        if(winners.stream().anyMatch(player -> player.getId().equals(currentUserId))) {
            winnerText += " (You)";
        }

        JLabel winnerLabel = new JLabel(winnerText);
        winnerLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

               String loserText = "Loser: " + loserNames;
        if (loosers.stream().anyMatch(player -> player.getId().equals(currentUserId))) {
            loserText += " (You)";
        }
        JLabel loserLabel = new JLabel(loserText);
        loserLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        loserLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

           JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 2, 10, 0));
        statsPanel.setMaximumSize(new Dimension(460, 200));

        // Stats Panel für Player A
        JPanel playerAPanel = new JPanel();
        playerAPanel.setLayout(new BoxLayout(playerAPanel, BoxLayout.Y_AXIS));
        playerAPanel.setBorder(BorderFactory.createTitledBorder(gameState.getPlayerA().getName() + " (" + (winners.stream().anyMatch(p -> p.getId().equals(gameState.getPlayerA().getId())) ? "Winner" : "Looser") + ")"));
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

        // Stats Panel für Player B
        JPanel playerBPanel = new JPanel();
        playerBPanel.setLayout(new BoxLayout(playerBPanel, BoxLayout.Y_AXIS));
        playerBPanel.setBorder(BorderFactory.createTitledBorder(gameState.getPlayerB().getName() + " (" + (winners.stream().anyMatch(p -> p.getId().equals(gameState.getPlayerB().getId())) ? "Winner" : "Looser") + ")"));
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

        // Button back
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backButton.addActionListener(e -> {
            gameHandler.getClientHandler().endCurrentGame();
            System.out.println("Back to lobby");
        });

        // Add components to the panel
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