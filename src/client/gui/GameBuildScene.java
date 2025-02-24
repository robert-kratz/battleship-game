package client.gui;

import client.GameHandler;
import protocol.Ship;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GameBuildScene extends JPanel implements Runnable {

    private int height, width;
    private GameHandler gameHandler;

    private final BattleshipBoard battleshipBoard;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private JLabel timerLabel;
    private JButton readyButton;

    // Lists of ships
    private ArrayList<Ship> availableShips = new ArrayList<>();
    private ArrayList<Ship> placedShips = new ArrayList<>();

    // Mapping from ships to their buttons in the left panel
    private Map<Ship, JButton> shipButtonMap = new HashMap<>();

    // New status indicator labels for the player and opponent
    private JLabel playerStatusLabel;
    private JLabel opponentStatusLabel;

    public GameBuildScene(GameHandler gameHandler) {
        setLayout(new BorderLayout());
        this.gameHandler = gameHandler;

        // Load available ships from the GameState
        this.availableShips = new ArrayList<>(gameHandler.getGameState().getShipsA());
        this.placedShips = new ArrayList<>();

        // Create the BattleshipBoard – it receives the list of placed ships
        battleshipBoard = new BattleshipBoard(gameHandler,
                this.gameHandler.getStageManager().getWindowsWidth(),
                placedShips,
                new BattleshipBoard.BattleShipBoardListener() {
                    @Override
                    public void onCellClick(int row, int col, Ship currentSelectedShip) {
                        Ship placed = battleshipBoard.getPlacedShipAt(row, col);
                        if (placed != null) {
                            // A placed ship is freed for repositioning:
                            placedShips.remove(placed);
                            if (!availableShips.contains(placed)) {
                                availableShips.add(placed);
                            }
                            JButton btn = shipButtonMap.get(placed);
                            if (btn != null) {
                                btn.setText("Ship (" + placed.getWidth() + "x" + placed.getLength() + ")");
                                btn.setBackground(Color.WHITE);
                            }
                            battleshipBoard.setSelectedShip(placed);
                        } else {
                            if (battleshipBoard.isCollisionAt(row, col)) {
                                System.out.println("Cannot place ship here due to collision with border or another ship.");
                                return;
                            }
                            if (currentSelectedShip != null) {
                                currentSelectedShip.setX(col);
                                currentSelectedShip.setY(row);
                                if (!placedShips.contains(currentSelectedShip)) {
                                    placedShips.add(currentSelectedShip);
                                    JButton btn = shipButtonMap.get(currentSelectedShip);
                                    if (btn != null) {
                                        btn.setText("Ship (" + currentSelectedShip.getWidth() + "x" + currentSelectedShip.getLength() + ") placed");
                                        // Placed ship gets a gray background
                                        btn.setBackground(Color.LIGHT_GRAY);
                                    }
                                    availableShips.remove(currentSelectedShip);
                                    if (!availableShips.isEmpty()) {
                                        battleshipBoard.setSelectedShip(availableShips.get(0));
                                    } else {
                                        battleshipBoard.setSelectedShip(null);
                                    }
                                }
                            }
                        }
                        updateReadyButton();
                        battleshipBoard.reevaluateComponent();
                    }
                    @Override
                    public void onShipPlace(Ship placedShip) {
                        System.out.println("Ship placed: " + placedShip.getId());
                    }
                    @Override
                    public void onShipDelete(Ship deletedShip) {
                        System.out.println("Ship deleted: " + deletedShip.getId());
                    }
                });

        // Left panel: Title, separators, ship buttons, ready button, etc.
        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("BattleShipps");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Ship buttons
        for (Ship ship : new ArrayList<>(availableShips)) {
            JButton shipButton = new JButton("Ship (" + ship.getWidth() + "x" + ship.getLength() + ")");
            shipButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            shipButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            shipButton.setMargin(new Insets(5, 5, 5, 5));
            // Default background: white (not yet placed)
            shipButton.setBackground(Color.WHITE);
            shipButton.addActionListener(e -> {
                System.out.println("Ship (" + ship.getWidth() + "x" + ship.getLength() + ") clicked (left panel)");
                if (placedShips.contains(ship)) {
                    placedShips.remove(ship);
                    if (!availableShips.contains(ship)) {
                        availableShips.add(ship);
                    }
                    shipButton.setText("Ship (" + ship.getWidth() + "x" + ship.getLength() + ")");
                    shipButton.setBackground(Color.WHITE);
                    readyButton.setEnabled(false);
                }
                battleshipBoard.setSelectedShip(ship);
            });
            shipButtonMap.put(ship, shipButton);
            leftPanel.add(shipButton);
            leftPanel.add(Box.createVerticalStrut(5));
        }

        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Ready button
        readyButton = new JButton("Ready");
        readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        readyButton.setMargin(new Insets(5, 5, 5, 5));
        // Disabled state: yellow; enabled state: green
        readyButton.setBackground(Color.YELLOW);
        readyButton.setOpaque(true);
        readyButton.setEnabled(false);
        readyButton.addActionListener(e -> {
            System.out.println("Ready button clicked");
            gameHandler.placeShips(placedShips);
            battleshipBoard.lockBoard();
            for (JButton btn : shipButtonMap.values()) {
                btn.setEnabled(false);
            }
            readyButton.setEnabled(false);
            // Update player's status indicator to "Ready" (green)
            playerStatusLabel.setText("Ready");
            playerStatusLabel.setForeground(Color.GREEN);
        });
        leftPanel.add(readyButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Right panel: Contains timer, player info and opponent info
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.DARK_GRAY);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Timer – centered horizontally in the right panel
        timerLabel = new JLabel("00:30");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        rightPanel.add(timerLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        // "Your:" and Username (left aligned)
        JLabel yourLabel = new JLabel("You:");
        yourLabel.setForeground(Color.WHITE);
        yourLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        yourLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(yourLabel);

        JLabel usernameLabel = new JLabel("Player");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(usernameLabel);

        // Player status indicator (shows "Builds Board" or "Ready")
        playerStatusLabel = new JLabel("Builds Board");
        playerStatusLabel.setForeground(Color.YELLOW);
        playerStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        playerStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(playerStatusLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        // "Your Opponent:" and placeholder for opponent name (left aligned)
        JLabel opponentLabel = new JLabel("Your Opponent:");
        opponentLabel.setForeground(Color.WHITE);
        opponentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        opponentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentLabel);

        JLabel opponentNameLabel = new JLabel("Waiting...");
        opponentNameLabel.setForeground(Color.WHITE);
        opponentNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        opponentNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentNameLabel);

        // Opponent status indicator
        opponentStatusLabel = new JLabel("Builds Board");
        opponentStatusLabel.setForeground(Color.YELLOW);
        opponentStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        opponentStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(opponentStatusLabel);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        // Tip text: "Press R to Rotate" (left aligned)
        JLabel tipLabel = new JLabel("Press R to Rotate");
        tipLabel.setForeground(Color.WHITE);
        tipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(tipLabel);

        add(leftPanel, BorderLayout.WEST);
        add(battleshipBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("GameBuildScene: Resized: " + getWidth() + "x" + getHeight());
            }
        });
    }

    private void updateReadyButton() {
        if (availableShips.isEmpty()) {
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
        System.out.println("GameBuildScene: Updated dimensions: " + width + "x" + height);
        battleshipBoard.setPreferredSize(new Dimension(width - 400, height));
        leftPanel.setPreferredSize(new Dimension(200, height));
        rightPanel.setPreferredSize(new Dimension(200, height));
        revalidate();
        repaint();
    }

    // Setter method to update the opponent state indicator.
    // Pass true if the opponent is ready, false if still building.
    public void setOpponentState(boolean ready) {
        if (ready) {
            opponentStatusLabel.setText("Ready");
            opponentStatusLabel.setForeground(Color.GREEN);
        } else {
            opponentStatusLabel.setText("Build Board");
            opponentStatusLabel.setForeground(Color.YELLOW);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignore
            }
            Date start = gameHandler.getGameState().getBuildGameBoardStarted();
            Date end = gameHandler.getGameState().getBuildGameBoardFinished();
            if (start != null && end != null) {
                long currentTime = System.currentTimeMillis();
                long remainingMillis = end.getTime() - currentTime;
                if (remainingMillis <= 0) {
                    SwingUtilities.invokeLater(() -> timerLabel.setText("Time over"));
                    break;
                } else {
                    long minutes = (remainingMillis / (60 * 1000)) % 60;
                    long seconds = (remainingMillis / 1000) % 60;
                    String timeStr = String.format("%02d:%02d", minutes, seconds);
                    SwingUtilities.invokeLater(() -> timerLabel.setText(timeStr));
                }
            }
        }
    }
}