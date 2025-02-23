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
import java.util.ArrayList;
import java.util.Map;

public class GameBuildScene extends JPanel implements Runnable {

    private int height, width;
    private GameHandler gameHandler;

    private final BattleshipBoard battleshipBoard;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private JLabel timerLabel;
    private JButton readyButton;

    // Listen der Schiffe
    private ArrayList<Ship> availableShips = new ArrayList<>();
    private ArrayList<Ship> placedShips = new ArrayList<>();

    // Mapping von Schiffen zu ihren Buttons im Left‑Panel
    private Map<Ship, JButton> shipButtonMap = new HashMap<>();

    public GameBuildScene(GameHandler gameHandler) {
        setLayout(new BorderLayout());
        this.gameHandler = gameHandler;

        // Verfügbare Schiffe aus dem GameState laden
        this.availableShips = new ArrayList<>(gameHandler.getGameState().getShipsA());
        this.placedShips = new ArrayList<>();

        // BattleshipBoard erstellen – es erhält die Liste der platzierten Schiffe
        battleshipBoard = new BattleshipBoard(gameHandler,
                this.gameHandler.getStageManager().getWindowsWidth(),
                placedShips,
                new BattleshipBoard.BattleShipBoardListener() {
                    @Override
                    public void onCellClick(int row, int col, Ship currentSelectedShip) {
                        Ship placed = battleshipBoard.getPlacedShipAt(row, col);
                        if (placed != null) {
                            // Bereits platziertes Schiff wird zur Neupositionierung freigegeben:
                            placedShips.remove(placed);
                            if (!availableShips.contains(placed)) {
                                availableShips.add(placed);
                            }
                            JButton btn = shipButtonMap.get(placed);
                            if (btn != null) {
                                btn.setText("Ship (" + placed.getWidth() + "x" + placed.getLength() + ")");
                            }
                            battleshipBoard.setSelectedShip(placed);
                            // Callback: Ship gelöscht (optional)
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
                                    }
                                    availableShips.remove(currentSelectedShip);
                                    // Callback: Ship platziert (optional)
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

        // Linke Seitenleiste: Oben der Titel, Separator, Schiff-Buttons, Separator, Ready-Button, Timer
        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Titel
        JLabel title = new JLabel("BattleShipps");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        leftPanel.add(title);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        leftPanel.add(Box.createVerticalStrut(10));

        // Schiff-Buttons
        for (Ship ship : new ArrayList<>(availableShips)) {
            JButton shipButton = new JButton("Ship (" + ship.getWidth() + "x" + ship.getLength() + ")");
            shipButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            shipButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            shipButton.setMargin(new Insets(5, 5, 5, 5));
            shipButton.addActionListener(e -> {
                System.out.println("Ship (" + ship.getWidth() + "x" + ship.getLength() + ") clicked (left panel)");
                if (placedShips.contains(ship)) {
                    placedShips.remove(ship);
                    if (!availableShips.contains(ship)) {
                        availableShips.add(ship);
                    }
                    shipButton.setText("Ship (" + ship.getWidth() + "x" + ship.getLength() + ")");
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

        // Ready-Button
        readyButton = new JButton("Ready");
        readyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        readyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        readyButton.setMargin(new Insets(5, 5, 5, 5));
        readyButton.setBackground(Color.GREEN);
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
            JOptionPane.showMessageDialog(
                    GameBuildScene.this,
                    "All ships placed successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        leftPanel.add(readyButton);
        leftPanel.add(Box.createVerticalStrut(10));

        // Timer
        timerLabel = new JLabel("Timer: ");
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        leftPanel.add(timerLabel);

        // Rechte Seitenleiste: Gleiches Design, jedoch linksbündig ausgerichtet
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.DARK_GRAY);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // "Your:" und Username (linksbündig)
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

        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        // "Your Opponent:" und Placeholder (linksbündig)
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

        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        rightPanel.add(Box.createVerticalStrut(10));

        // Tipp-Text: "Press R to Rotate"
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
        readyButton.setEnabled(availableShips.isEmpty());
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

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Ignorieren
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