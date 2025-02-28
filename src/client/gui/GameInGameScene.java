package client.gui;

import client.ClientHandler;
import client.GameHandler;
import client.SoundType;
import client.gui.board.InGameBattleshipBoard;
import client.gui.painter.BoardPainter;
import client.gui.painter.InGameBoardPainter;
import protocol.GameState;
import protocol.Ship;
import protocol.game.items.AirStrikeItem;
import protocol.game.items.RadarItem;
import protocol.game.items.SeaBombItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

public class GameInGameScene extends JPanel implements Runnable {

    private Point lastHoveredTile = new Point(-1, -1);

    private GameHandler gameHandler;

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel timerLabel;

    private InGameBattleshipBoard playerBoard, opponentBoard;

    private boolean playersTurn;
    private Date currentTurnStart;
    private Date currentTurnEnd;

    private JLabel playerTurnLabel;
    private JLabel opponentTurnLabel;

    // Neue Felder für Energieanzeige und Item-Buttons
    private JLabel energyLabel;
    private JButton bombButton;
    private JButton radarButton;
    private JButton airStrikeButton;

    public GameInGameScene(GameHandler gameHandler, ArrayList<Ship> placedShips) {
        this.gameHandler = gameHandler;
        this.playersTurn = gameHandler.getGameState().isPlayersTurn(this.gameHandler.getClientHandler().getUserId());
        this.currentTurnStart = this.gameHandler.getGameState().getPlayersTurnStart();
        this.currentTurnEnd = this.gameHandler.getGameState().getPlayersTurnEnd();

        // Board-Painter für InGame-Phase
        Image inGameBackgroundImage = new ImageIcon("resource/background-2.png").getImage();
        BoardPainter inGamePainter = new InGameBoardPainter(inGameBackgroundImage);

        System.out.println("GameInGameScene created");
        for (Ship ship : placedShips) {
            System.out.println("Ship: " + ship);
        }

        playerBoard = new InGameBattleshipBoard(gameHandler.getGameState().getBoardSize(), placedShips, inGamePainter,
                (row, col) -> {
                    System.out.println("playerBoard clicked at: " + row + ", " + col);
                });

        opponentBoard = new InGameBattleshipBoard(gameHandler.getGameState().getBoardSize(),
                this.gameHandler.getGameState().getUncoveredShips(this.gameHandler.getClientHandler().getUserId()),
                inGamePainter,
                (row, col) -> {
                    System.out.println("opponentBoard clicked at: " + row + ", " + col);
                });

        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Titel
        JLabel title = new JLabel("Battleships");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Energieanzeige
        int energy = this.gameHandler.getGameState().getEnergy(this.gameHandler.getClientHandler().getUserId());
        energyLabel = new JLabel("Energy: " + energy);
        energyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        energyLabel.setForeground(Color.WHITE);
        energyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        leftPanel.add(energyLabel);
        leftPanel.add(Box.createVerticalStrut(10));

        // Item-Buttons

        // Bomb (SeaBombItem)
        bombButton = new JButton("Bombs (2x2): " + new SeaBombItem().getEnergyCost() + " Energy");
        bombButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bombButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bombButton.setBackground(Color.WHITE);
        bombButton.addActionListener(e -> {
            // Hier wird der Bomben-Item-Action ausgeführt
            System.out.println("Bomb item selected");
        });
        leftPanel.add(bombButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Radar (RadarItem)
        radarButton = new JButton("Radar: " + new RadarItem().getEnergyCost() + " Energy");
        radarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        radarButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        radarButton.setBackground(Color.WHITE);
        radarButton.addActionListener(e -> {
            // Hier wird der Radar-Item-Action ausgeführt
            System.out.println("Radar item selected");
        });
        leftPanel.add(radarButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Air Strike (AirStrikeItem)
        airStrikeButton = new JButton("Air Strike: " + new AirStrikeItem().getEnergyCost() + " Energy");
        airStrikeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        airStrikeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        airStrikeButton.setBackground(Color.WHITE);
        airStrikeButton.addActionListener(e -> {
            // Hier wird der Air Strike-Item-Action ausgeführt
            System.out.println("Air Strike item selected");
        });
        leftPanel.add(airStrikeButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Buttons abhängig von der aktuellen Energie aktivieren/deaktivieren
        updateItemButtons(energy);

        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Timer-Anzeige
        timerLabel = new JLabel("00:00");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(timerLabel);

        // Rechtes Panel (Spieler- und Gegnerinfos)
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.DARK_GRAY);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel yourLabel = new JLabel("You:");
        yourLabel.setForeground(Color.WHITE);
        yourLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        yourLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(yourLabel);

        JLabel usernameLabel = new JLabel(gameHandler.getClientHandler().getUsername());
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(usernameLabel);

        playerTurnLabel = new JLabel(this.playersTurn ? "Your Turn" : "Waiting for Opponent");
        playerTurnLabel.setForeground(this.playersTurn ? Color.GREEN : Color.LIGHT_GRAY);
        playerTurnLabel.setFont(new Font("Arial", Font.BOLD, 12));
        playerTurnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(playerTurnLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        JLabel opponentLabel = new JLabel("Your Opponent:");
        opponentLabel.setForeground(Color.WHITE);
        opponentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        opponentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentLabel);

        String opponentName = !gameHandler.getGameState().getPlayerA().equals(gameHandler.getClientHandler().getUserId()) ?
                gameHandler.getGameState().getPlayerAName() : gameHandler.getGameState().getPlayerBName();

        JLabel opponentNameLabel = new JLabel(opponentName);
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        opponentNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentNameLabel);

        opponentTurnLabel = new JLabel(!this.playersTurn ? "Opponents Turn" : "Waiting for your Move");
        opponentTurnLabel.setForeground(!this.playersTurn ? Color.GREEN : Color.LIGHT_GRAY);
        opponentTurnLabel.setFont(new Font("Arial", Font.BOLD, 12));
        opponentTurnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentTurnLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        setLayout(new BorderLayout());
        add(leftPanel, BorderLayout.WEST);
        add(playersTurn ? opponentBoard : playerBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void extendCurrentTurn(Date newEndTime) {
        this.currentTurnEnd = newEndTime;
    }

    public void toggleTurn(boolean playerATurn) {
        this.playersTurn = playerATurn;
        playerTurnLabel.setText(playerATurn ? "Your Turn" : "Waiting for Opponent");
        opponentTurnLabel.setText(!playerATurn ? "Opponents Turn" : "Waiting for your Move");
        playerTurnLabel.setForeground(playerATurn ? Color.GREEN : Color.LIGHT_GRAY);
        opponentTurnLabel.setForeground(!playerATurn ? Color.GREEN : Color.LIGHT_GRAY);

        // Sicherstellen, dass auch die Turn-Zeiten (falls benötigt) aktualisiert werden
        this.gameHandler.getGameState().setPlayersTurnStart(this.gameHandler.getGameState().getPlayersTurnStart());
        this.gameHandler.getGameState().setPlayersTurnEnd(this.gameHandler.getGameState().getPlayersTurnEnd());

        remove(playersTurn ? playerBoard : opponentBoard);
        add(playersTurn ? opponentBoard : playerBoard, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void setOpponentHover(Point lastHoveredTile) {
        this.lastHoveredTile = lastHoveredTile;
    }

    /**
     * Aktualisiert die Energieanzeige und setzt den enabled/disabled-Status der Item-Buttons.
     */
    public void setPlayerEnergy(int energy) {
        energyLabel.setText("Energy: " + energy);
        updateItemButtons(energy);
    }

    private void updateItemButtons(int energy) {
        bombButton.setEnabled(energy >= new SeaBombItem().getEnergyCost());
        radarButton.setEnabled(energy >= new RadarItem().getEnergyCost());
        airStrikeButton.setEnabled(energy >= new AirStrikeItem().getEnergyCost());
    }

    @Override
    public void run() {
        System.out.println("GameInGameScene started");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000); // 1 Sekunde warten
            } catch (InterruptedException e) {
                break;
            }

            long remainingMillis = currentTurnEnd.getTime() - System.currentTimeMillis();
            long minutes = (remainingMillis / (60 * 1000)) % 60;
            long seconds = (remainingMillis / 1000) % 60;

            if (seconds == 3 || seconds == 2 || seconds == 1) {
                ClientHandler.mediaPlayer.playSound(SoundType.CLOCK_TICK);
            }

            if (remainingMillis <= 0) {
                SwingUtilities.invokeLater(() -> timerLabel.setText("Waiting for Server"));
                System.out.println("Timer expired. Executing expiration logic.");
            } else {
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                SwingUtilities.invokeLater(() -> timerLabel.setText(timeStr));
            }
        }
    }
}