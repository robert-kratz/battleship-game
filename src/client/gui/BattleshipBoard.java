package client.gui;

import client.GameHandler;
import client.MediaPlayer;
import client.SoundType;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BattleshipBoard extends JPanel {

    private int width;
    private final int rows;
    private final int cols;
    private final JButton[][] gridButtons;
    private final ImageIcon backgroundImage;
    private final BackgroundPanel backgroundPanel;
    private BattleShipBoardListener listener;
    private Ship selectedShip;
    // Liste der bereits platzierten Schiffe (von GameBuildScene übergeben)
    private List<Ship> placedShips;
    private Integer lastHoveredRow = null;
    private Integer lastHoveredCol = null;

    private GameHandler gameHandler;
    private final MediaPlayer mediaPlayer = new MediaPlayer();

    // Farben für Hover bzw. Collision
    private final Color HOVER_COLOR = new Color(25, 130, 196, 150);
    private final Color COLLISION_COLOR = new Color(255, 89, 94, 150);
    private final Color SHIP_COLOR = new Color(25, 130, 196);

    // Flag, ob das Board gesperrt ist (nach Ready-Klick)
    private boolean locked = false;

    public BattleshipBoard(GameHandler gameHandler, int width, List<Ship> placedShips, BattleShipBoardListener listener) {
        this.gameHandler = gameHandler;
        int boardSize = gameHandler.getGameState().getBoardSize();
        this.listener = listener;
        this.rows = boardSize;
        this.cols = boardSize;
        this.gridButtons = new JButton[boardSize][boardSize];
        this.backgroundImage = new ImageIcon("resource/background.png");
        this.width = width;
        this.placedShips = placedShips;
        // Erstelle ein Ghost‑Schiff; dessen Position ist standardmäßig ungültig (-1, -1).
        this.selectedShip = new Ship(0, Ship.Orientation.HORIZONTAL, 3, 2);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        backgroundPanel = new BackgroundPanel(backgroundImage.getImage(), rows, cols);
        backgroundPanel.setLayout(new GridLayout(rows, cols, 0, 0));
        initializeBoard(backgroundPanel);
        add(backgroundPanel, BorderLayout.CENTER);

        setFocusable(true);
        requestFocusInWindow();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                backgroundPanel.reevaluateComponent();
                repaint();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "rotateShip");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("R"), "rotateShip");
        getActionMap().put("rotateShip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!locked) {
                    toggleShipOrientation();
                    reevaluateComponent();
                }
            }
        });
    }

    public void lockBoard() {
        locked = true;
        repaint();
    }

    public void reevaluateComponent() {
        requestFocusInWindow();
        backgroundPanel.reevaluateComponent();
        repaint();
    }

    private void initializeBoard(JPanel panel) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                HoverButton button = new HoverButton(row, col);
                button.setPreferredSize(new Dimension(width / cols, width / rows));
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setBorder(BorderFactory.createEmptyBorder());
                gridButtons[row][col] = button;
                panel.add(button);
            }
        }
    }

    public void setSelectedShip(Ship ship) {
        this.selectedShip = ship;
        repaint();
    }

    private void toggleShipOrientation() {
        if (selectedShip != null) {
            selectedShip.setOrientation(
                    selectedShip.getOrientation() == Ship.Orientation.HORIZONTAL ?
                            Ship.Orientation.VERTICAL : Ship.Orientation.HORIZONTAL);
            if (lastHoveredRow != null && lastHoveredCol != null) {
                repaint();
            }
        }
    }

    /**
     * Liefert das Schiff, das an der Zelle (row, col) platziert ist, oder null.
     */
    public Ship getPlacedShipAt(int row, int col) {
        for (Ship ship : placedShips) {
            boolean isHorizontal = ship.getOrientation() == Ship.Orientation.HORIZONTAL;
            int startRow = ship.getY();
            int startCol = ship.getX();
            int endRow = isHorizontal ? startRow + ship.getWidth() - 1 : startRow + ship.getLength() - 1;
            int endCol = isHorizontal ? startCol + ship.getLength() - 1 : startCol + ship.getWidth() - 1;
            if (row >= startRow && row <= endRow && col >= startCol && col <= endCol) {
                return ship;
            }
        }
        return null;
    }

    /**
     * Public wrapper, damit von außen geprüft werden kann, ob beim Platzieren eines Schiffes
     * an (row, col) eine Kollision auftritt.
     */
    public boolean isCollisionAt(int startRow, int startCol) {
        return checkCollision(startRow, startCol);
    }

    private boolean checkCollision(int startRow, int startCol) {
        if (selectedShip == null) {
            return false;
        }
        int length = selectedShip.getLength();
        int shipWidth = selectedShip.getWidth();
        boolean isHorizontal = selectedShip.getOrientation() == Ship.Orientation.HORIZONTAL;

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < shipWidth; j++) {
                int targetRow = startRow + (isHorizontal ? j : i);
                int targetCol = startCol + (isHorizontal ? i : j);

                if (targetRow < 0 || targetRow >= rows || targetCol < 0 || targetCol >= cols) {
                    return true;
                }

                for (Ship ship : placedShips) {
                    for (int x = 0; x < ship.getLength(); x++) {
                        for (int y = 0; y < ship.getWidth(); y++) {
                            int shipRow = ship.getY() + (ship.getOrientation() == Ship.Orientation.HORIZONTAL ? y : x);
                            int shipCol = ship.getX() + (ship.getOrientation() == Ship.Orientation.HORIZONTAL ? x : y);
                            if (shipRow == targetRow && shipCol == targetCol) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private class BackgroundPanel extends JPanel {
        private final Image backgroundImage;
        private final int rows, cols;

        public BackgroundPanel(Image backgroundImage, int rows, int cols) {
            this.backgroundImage = backgroundImage;
            this.rows = rows;
            this.cols = cols;
        }

        @Override
        public Dimension getPreferredSize() {
            Container parent = getParent();
            if (parent != null) {
                int size = Math.min(parent.getWidth(), parent.getHeight());
                return new Dimension(size, size);
            }
            return super.getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int boardSize = Math.min(panelWidth, panelHeight);

            // Hintergrundbild zeichnen
            g.drawImage(backgroundImage, 0, 0, boardSize, boardSize, this);

            double cellWidth = (double) boardSize / cols;
            double cellHeight = (double) boardSize / rows;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            g.setColor(Color.WHITE);
            for (int i = 0; i <= cols; i++) {
                int x = (int) Math.round(i * cellWidth);
                g.drawLine(x, 0, x, boardSize);
            }
            for (int i = 0; i <= rows; i++) {
                int y = (int) Math.round(i * cellHeight);
                g.drawLine(0, y, boardSize, y);
            }

            // Zeichne alle platzierten Schiffe
            for (Ship ship : placedShips) {
                drawPlacedShip(g, ship, boardSize, cellWidth, cellHeight);
            }

            // Zeichne Ghost-Schiff, falls vorhanden
            if (selectedShip != null && lastHoveredRow != null && lastHoveredCol != null) {
                drawGhostShip(g, lastHoveredRow, lastHoveredCol, boardSize, cellWidth, cellHeight);
            }

            // Falls das Board gesperrt ist, overlay-Text anzeigen
            if (locked) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRect(0, 0, boardSize, boardSize);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                FontMetrics fm = g.getFontMetrics();
                String text = "Waiting for other players...";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g.drawString(text, (boardSize - textWidth) / 2, (boardSize + textHeight) / 2);
            }
        }

        private void drawPlacedShip(Graphics g, Ship ship, int boardSize, double cellWidth, double cellHeight) {
            g.setColor(SHIP_COLOR);
            int length = ship.getLength();
            int shipWidth = ship.getWidth();
            boolean isHorizontal = ship.getOrientation() == Ship.Orientation.HORIZONTAL;

            for (int i = 0; i < length; i++) {
                for (int j = 0; j < shipWidth; j++) {
                    int targetRow = ship.getY() + (isHorizontal ? j : i);
                    int targetCol = ship.getX() + (isHorizontal ? i : j);
                    if (targetRow >= 0 && targetRow < rows && targetCol >= 0 && targetCol < cols) {
                        int x = (int) Math.round(targetCol * cellWidth);
                        int y = (int) Math.round(targetRow * cellHeight);
                        int w = (int) Math.round(cellWidth);
                        int h = (int) Math.round(cellHeight);
                        g.fillRect(x, y, w, h);
                    }
                }
            }
        }

        private void drawGhostShip(Graphics g, int startRow, int startCol, int boardSize, double cellWidth, double cellHeight) {
            int length = selectedShip.getLength();
            int shipWidth = selectedShip.getWidth();
            boolean isHorizontal = selectedShip.getOrientation() == Ship.Orientation.HORIZONTAL;

            boolean collision = checkCollision(startRow, startCol);
            g.setColor(collision ? COLLISION_COLOR : HOVER_COLOR);

            for (int i = 0; i < length; i++) {
                for (int j = 0; j < shipWidth; j++) {
                    int targetRow = startRow + (isHorizontal ? j : i);
                    int targetCol = startCol + (isHorizontal ? i : j);
                    if (targetRow >= 0 && targetRow < rows && targetCol >= 0 && targetCol < cols) {
                        int x = (int) Math.round(targetCol * cellWidth);
                        int y = (int) Math.round(targetRow * cellHeight);
                        int w = (int) Math.round(cellWidth);
                        int h = (int) Math.round(cellHeight);
                        g.fillRect(x, y, w, h);
                    }
                }
            }
        }

        public void reevaluateComponent() {
            repaint();
        }
    }

    private class HoverButton extends JButton {
        private final int row, col;

        public HoverButton(int row, int col) {
            this.row = row;
            this.col = col;
            setContentAreaFilled(false);
            setOpaque(false);
            setFocusPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (locked) return;  // Keine Events, wenn gesperrt
                    lastHoveredRow = row;
                    lastHoveredCol = col;
                    if (listener != null) {
                        listener.onCellClick(row, col, selectedShip);
                    }
                    reevaluateComponent();
                    repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (locked) return;
                    lastHoveredRow = row;
                    lastHoveredCol = col;
                    reevaluateComponent();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (locked) return;
                    lastHoveredRow = null;
                    lastHoveredCol = null;
                    reevaluateComponent();
                    repaint();
                }
            });
        }
    }

    public interface BattleShipBoardListener {
        void onCellClick(int row, int col, Ship selectedShip);
        void onShipPlace(Ship placedShip);
        void onShipDelete(Ship deletedShip);
    }
}