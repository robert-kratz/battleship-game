package client.gui;

import client.ClientHandler;
import client.GameHandler;
import client.SoundType;
import client.gui.board.BuildBattleshipBoard;
import client.gui.painter.BoardPainter;
import client.gui.painter.BuildBoardPainter;
import protocol.Ship;
import protocol.ShipPlacementValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GameBuildScene extends JPanel implements Runnable {
    private int height, width;
    private GameHandler gameHandler;

    private BuildBattleshipBoard buildBoard;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel timerLabel;
    private JButton randomizeButton;
    private JButton readyButton;

    // Alle Schiffe, die im Spiel zur Verfügung stehen
    private ArrayList<Ship> allShips;

    // Mapping: Ship-ID -> Button
    private Map<Integer, JButton> shipButtonMap = new HashMap<>();

    private JLabel playerStatusLabel;
    private JLabel opponentStatusLabel;

    private JLabel tipLabel;

    public GameBuildScene(GameHandler gameHandler) {
        setLayout(new BorderLayout());
        this.gameHandler = gameHandler;

        // Alle möglichen Schiffe aus dem GameState laden
        this.allShips = new ArrayList<>(gameHandler.getGameState().getAvailableShips());

        // Zentrale Liste der platzierten Schiffe – diese wird im Board gepflegt
        ArrayList<Ship> centralPlacedShips = new ArrayList<>();

        // Board-Painter für die Build-Phase
        Image buildBackgroundImage = new ImageIcon("resource/background-2.png").getImage();
        BoardPainter buildPainter = new BuildBoardPainter(buildBackgroundImage);

        buildBoard = new BuildBattleshipBoard(gameHandler.getGameState().getBoardSize(), centralPlacedShips, buildPainter,
                (row, col, selectedShip) -> {
                    Ship placed = buildBoard.getPlacedShipAt(row, col);
                    if (placed != null) {
                        // Falls hier bereits ein Schiff liegt, entferne es aus der zentralen Liste
                        buildBoard.getPlacedShips().remove(placed);
                        // Aktualisiere den Button für dieses Schiff
                        JButton btn = shipButtonMap.get(placed.getId());
                        if (btn != null) {
                            btn.setText("Ship (" + placed.getWidth() + "x" + placed.getLength() + ")");
                            btn.setBackground(Color.WHITE);
                        }
                        // Setze das entfernte Schiff als selektiertes Schiff
                        buildBoard.setSelectedShip(placed);
                    } else {
                        // Prüfe, ob an dieser Position eine Kollision entsteht
                        if (ShipPlacementValidator.isCollision(selectedShip, gameHandler.getGameState().getBoardSize(), row, col, buildBoard.getPlacedShips())) {
                            System.out.println("Cannot place ship here due to collision.");
                            return;
                        }
                        if (selectedShip != null) {
                            selectedShip.setX(col);
                            selectedShip.setY(row);
                            if (!buildBoard.getPlacedShips().contains(selectedShip)) {
                                buildBoard.getPlacedShips().add(selectedShip);

                                gameHandler.sendUpdateBuildBoardMessage(new ArrayList<>(buildBoard.getPlacedShips())); // Sende die Platzierung an den Server

                                JButton btn = shipButtonMap.get(selectedShip.getId());
                                if (btn != null) {
                                    btn.setText("Ship (" + selectedShip.getWidth() + "x" + selectedShip.getLength() + ") placed");
                                    btn.setBackground(Color.LIGHT_GRAY);
                                }
                                // Berechne die noch verfügbaren Schiffe als Differenz zwischen allShips und den platzierten Schiffen
                                ArrayList<Ship> availableShips = computeAvailableShips();

                                System.out.println("Available ships: " + availableShips);

                                if (!availableShips.isEmpty()) {
                                    buildBoard.setSelectedShip(availableShips.get(0));
                                } else {
                                    buildBoard.setSelectedShip(null);
                                }
                            }
                        }
                    }
                    updateReadyButton();
                    buildBoard.repaint();
                });

        // Linkes Panel (Titel, Schiff-Buttons, Ready-Button, Timer)
        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Battleships");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Erstelle für jedes Schiff einen Button
        for (Ship ship : new ArrayList<>(allShips)) {
            JButton shipButton = new JButton("Ship (" + ship.getWidth() + "x" + ship.getLength() + ")");
            shipButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            shipButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            shipButton.setBackground(Color.WHITE);
            shipButton.addActionListener(e -> {
                System.out.println("Ship button clicked: " + ship.getId());

                boolean shipAlreadyPlaced = buildBoard.getPlacedShips().stream().anyMatch(s -> s.getId() == ship.getId());

                // Falls das Schiff bereits platziert wurde, entferne es vom Board
                if (shipAlreadyPlaced) {
                    buildBoard.getPlacedShips().removeIf(s -> s.getId() == ship.getId());
                    buildBoard.removePlacedShip(ship.getId());
                    shipButton.setText("Ship (" + ship.getWidth() + "x" + ship.getLength() + ")");
                    shipButton.setBackground(Color.WHITE);
                    if (buildBoard.getSelectedShip() != null && buildBoard.getSelectedShip().getId() == ship.getId()) {
                        buildBoard.setSelectedShip(null);
                    }
                    buildBoard.repaint();
                    updateReadyButton();
                } else {
                    // Wähle das Schiff zur Platzierung aus
                    buildBoard.setSelectedShip(ship);
                }
            });
            shipButtonMap.put(ship.getId(), shipButton);
            leftPanel.add(shipButton);
            leftPanel.add(Box.createVerticalStrut(5));
        }

        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        readyButton = new JButton("Ready");
        readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        readyButton.setBackground(Color.YELLOW);
        readyButton.setOpaque(true);
        readyButton.setEnabled(false);
        readyButton.addActionListener(e -> {
            System.out.println("Ready button clicked");
            gameHandler.sendUpdateBuildBoardMessage(new ArrayList<>(buildBoard.getPlacedShips()));
            if(!buildBoard.isLocked()) {
                buildBoard.lockBoard();
                for (JButton btn : shipButtonMap.values()) {
                    btn.setEnabled(false);
                }
                randomizeButton.setEnabled(false);
                playerStatusLabel.setText("Ready");
                readyButton.setText("Not Ready");
                playerStatusLabel.setForeground(Color.GREEN);
                gameHandler.sendPlayerReadyMessage(true);
            } else {
                buildBoard.unlockBoard();
                for (JButton btn : shipButtonMap.values()) {
                    btn.setEnabled(true);
                }
                randomizeButton.setEnabled(true);
                playerStatusLabel.setText("Builds Board");
                readyButton.setText("Ready");
                playerStatusLabel.setForeground(Color.YELLOW);
                gameHandler.sendPlayerReadyMessage(false);
            }
        });
        leftPanel.add(readyButton);
        leftPanel.add(Box.createVerticalStrut(10));

        timerLabel = new JLabel(" ");
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

        playerStatusLabel = new JLabel("Builds Board");
        playerStatusLabel.setForeground(Color.YELLOW);
        playerStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        playerStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(playerStatusLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        JLabel opponentLabel = new JLabel("Your Opponent:");
        opponentLabel.setForeground(Color.WHITE);
        opponentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        opponentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentLabel);

        String opponentName = this.gameHandler.getGameState().getOpponent(this.gameHandler.getClientHandler().getUserId()).getName();

        JLabel opponentNameLabel = new JLabel(opponentName);
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        opponentNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentNameLabel);

        opponentStatusLabel = new JLabel("Build Board");
        opponentStatusLabel.setForeground(Color.YELLOW);
        opponentStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        opponentStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentStatusLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        randomizeButton = new JButton("Randomize");
        randomizeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        randomizeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        randomizeButton.setBackground(Color.LIGHT_GRAY);
        randomizeButton.setOpaque(true);
        randomizeButton.addActionListener(e -> {
            int boardSize = gameHandler.getGameState().getBoardSize();
            System.out.println("Randomizing ships");

            ArrayList<Ship> randomizedShips = ShipPlacementValidator.createRandomizedGameBoard(boardSize, new ArrayList<>(allShips), new ArrayList<>());
            buildBoard.getPlacedShips().clear();
            buildBoard.getPlacedShips().addAll(randomizedShips);
            buildBoard.repaint();
            buildBoard.setSelectedShip(null);

            for (Ship ship : randomizedShips) {
                JButton btn = shipButtonMap.get(ship.getId());
                if (btn != null) {
                    btn.setText("Ship (" + ship.getWidth() + "x" + ship.getLength() + ") placed");
                    btn.setBackground(Color.WHITE);
                }
            }

            System.out.println("Randomized ships: " + buildBoard.getPlacedShips());
            updateReadyButton();
        });
        rightPanel.add(randomizeButton);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        tipLabel = new JLabel("Press 'R' to rotate ship");
        tipLabel.setForeground(Color.WHITE);
        tipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(tipLabel);

        add(leftPanel, BorderLayout.WEST);
        add(buildBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("GameBuildScene: Resized: " + getWidth() + "x" + getHeight());
            }
        });
    }

    /**
     * Berechnet die verfügbaren Schiffe als Differenz zwischen allShips und den im Board bereits platzierten Schiffen.
     */
    private ArrayList<Ship> computeAvailableShips() {
        ArrayList<Ship> available = new ArrayList<>(allShips);

        for (Ship ship : this.buildBoard.getPlacedShips()) {
            available.removeIf(s -> s.getId() == ship.getId());
        }

        return available;
    }

    private void updateReadyButton() {
        if (computeAvailableShips().isEmpty()) {
            readyButton.setEnabled(true);
            readyButton.setBackground(Color.GREEN);
        } else {
            readyButton.setEnabled(false);
            readyButton.setBackground(Color.YELLOW);
        }
    }

    public void updateDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        buildBoard.setPreferredSize(new Dimension(width - 400, height));
        leftPanel.setPreferredSize(new Dimension(200, height));
        rightPanel.setPreferredSize(new Dimension(200, height));
        revalidate();
        repaint();
    }

    public void setOpponentState(boolean ready) {
        if (ready) {
            opponentStatusLabel.setText("Ready");
            opponentStatusLabel.setForeground(Color.GREEN);
        } else {
            opponentStatusLabel.setText("Build Board");
            opponentStatusLabel.setForeground(Color.YELLOW);
        }
    }

    public void setPlayerState(boolean ready) {
        if (ready) {
            playerStatusLabel.setText("Ready");
            playerStatusLabel.setForeground(Color.GREEN);
        } else {
            playerStatusLabel.setText("Build Board");
            playerStatusLabel.setForeground(Color.YELLOW);
        }
    }

    public void showTooltip(String text) {
        tipLabel.setText(text);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            Date start = gameHandler.getGameState().getBuildGameBoardStarted();
            Date end = gameHandler.getGameState().getBuildGameBoardFinished();
            if (start != null && end != null) {
                long currentTime = System.currentTimeMillis();
                long remainingMillis = end.getTime() - currentTime;

                long minutes = (remainingMillis / (60 * 1000)) % 60;
                long seconds = (remainingMillis / 1000) % 60;

                if (seconds == 30 || seconds == 15 || seconds == 10 || seconds == 5 || seconds == 3 || seconds == 2 || seconds == 1) {
                    ClientHandler.mediaPlayer.playSound(SoundType.CLOCK_TICK);
                }

                if (remainingMillis <= 0) {
                    ClientHandler.mediaPlayer.playSound(SoundType.TIME_UP);
                    SwingUtilities.invokeLater(() -> timerLabel.setText("Time over"));

                    SwingUtilities.invokeLater(() -> {
                        readyButton.setBackground(Color.GREEN);
                        buildBoard.lockBoard();
                        randomizeButton.setEnabled(false);
                    });
                    break;
                } else {
                    String timeStr = String.format("%02d:%02d", minutes, seconds);
                    SwingUtilities.invokeLater(() -> timerLabel.setText(timeStr));
                }
            }
        }
    }
}