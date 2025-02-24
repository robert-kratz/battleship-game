package client.gui;

import client.GameHandler;
import client.MediaPlayer;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class BattleshipBoard extends JPanel {

    private int width;
    private final int rows;
    private final int cols;
    private final JButton[][] gridButtons;
    private final ImageIcon backgroundImage;
    private final BackgroundPanel backgroundPanel;
    private BattleShipBoardListener listener;
    private Ship selectedShip;
    // List of placed ships (passed from GameBuildScene)
    private List<Ship> placedShips;
    private Integer lastHoveredRow = null;
    private Integer lastHoveredCol = null;

    private GameHandler gameHandler;
    private final MediaPlayer mediaPlayer = new MediaPlayer();

    // Colors for hover, collision, ship fill, and outline
    private final Color HOVER_COLOR = new Color(25, 130, 196, 150);
    private final Color COLLISION_COLOR = new Color(255, 89, 94, 150);
    private final Color SHIP_COLOR = new Color(25, 130, 196);
    private final Color OUTLINE_COLOR = Color.BLACK;

    // Flag indicating if the board is locked (after ready is clicked)
    private boolean locked = false;

    public BattleshipBoard(GameHandler gameHandler, int width, List<Ship> placedShips, BattleShipBoardListener listener) {
        this.gameHandler = gameHandler;
        int boardSize = gameHandler.getGameState().getBoardSize();
        this.listener = listener;
        this.rows = boardSize;
        this.cols = boardSize;
        this.gridButtons = new JButton[boardSize][boardSize];
        // Use the new background image for each cell
        this.backgroundImage = new ImageIcon("resource/background-2.png");
        this.width = width;
        this.placedShips = placedShips;

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

        // Bind keys to rotate the ship (rotate 90° clockwise through NORTH, EAST, SOUTH, WEST)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "rotateShip");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("R"), "rotateShip");
        getActionMap().put("rotateShip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!locked && selectedShip != null) {
                    selectedShip.setOrientation(selectedShip.getOrientation().next());
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

    /**
     * Returns the ship placed at cell (row, col), or null if none.
     */
    public Ship getPlacedShipAt(int row, int col) {
        for (Ship ship : placedShips) {
            for (Point p : ship.getOccupiedCells()) {
                if (p.x == col && p.y == row) {
                    return ship;
                }
            }
        }
        return null;
    }

    /**
     * Public wrapper to check if placing a ship at (startRow, startCol) would cause a collision.
     */
    public boolean isCollisionAt(int startRow, int startCol) {
        return checkCollision(startRow, startCol);
    }

    private boolean checkCollision(int startRow, int startCol) {
        if (selectedShip == null) return false;
        // Get candidate cells if the selected ship were placed at (startCol, startRow)
        List<Point> candidateCells = selectedShip.getOccupiedCellsAt(startCol, startRow);
        for (Point p : candidateCells) {
            if (p.x < 0 || p.x >= cols || p.y < 0 || p.y >= rows) {
                return true;
            }
            for (Ship ship : placedShips) {
                if (ship == selectedShip) continue;
                for (Point sp : ship.getOccupiedCells()) {
                    if (p.x == sp.x && p.y == sp.y) {
                        return true;
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

            // Calculate cell dimensions
            double cellWidth = (double) boardSize / cols;
            double cellHeight = (double) boardSize / rows;

            // Draw the background image in each cell
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int x = (int) Math.round(col * cellWidth);
                    int y = (int) Math.round(row * cellHeight);
                    g.drawImage(backgroundImage, x, y, (int) cellWidth, (int) cellHeight, this);
                }
            }

            // Draw grid lines
            g.setColor(Color.WHITE);
            for (int i = 0; i <= cols; i++) {
                int x = (int) Math.round(i * cellWidth);
                g.drawLine(x, 0, x, boardSize);
            }
            for (int i = 0; i <= rows; i++) {
                int y = (int) Math.round(i * cellHeight);
                g.drawLine(0, y, boardSize, y);
            }

            // Draw all placed ships
            for (Ship ship : placedShips) {
                drawPlacedShip(g, ship, boardSize, cellWidth, cellHeight);
            }

            // Draw ghost ship if one is selected and the mouse is hovering a cell
            if (selectedShip != null && lastHoveredRow != null && lastHoveredCol != null) {
                drawGhostShip(g, lastHoveredRow, lastHoveredCol, boardSize, cellWidth, cellHeight);
            }

            // Draw row labels (numbers) on the left
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            for (int row = 0; row < rows; row++) {
                String label = String.valueOf(row + 1);
                int textWidth = fm.stringWidth(label);
                int textHeight = fm.getAscent();
                int x = 2;
                int y = (int) (row * cellHeight + cellHeight / 2 + textHeight / 2);
                g.drawString(label, x, y);
            }

            // Draw column labels (letters) at the top
            for (int col = 0; col < cols; col++) {
                char letter = (char) ('A' + col);
                String label = String.valueOf(letter);
                int textWidth = fm.stringWidth(label);
                int textHeight = fm.getAscent();
                int x = (int) (col * cellWidth + cellWidth / 2 - textWidth / 2);
                int y = textHeight;
                g.drawString(label, x, y);
            }

            // If the board is locked, draw an overlay
            if (locked) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRect(0, 0, boardSize, boardSize);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                fm = g.getFontMetrics();
                String text = "Waiting for opponent...";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g.drawString(text, (boardSize - textWidth) / 2, (boardSize + textHeight) / 2);
            }
        }

        private void drawPlacedShip(Graphics g, Ship ship, int boardSize, double cellWidth, double cellHeight) {
            g.setColor(SHIP_COLOR);
            List<Point> cells = ship.getOccupiedCells();
            // Fill each occupied cell using ceil values so it covers grid lines
            int fillWidth = (int) Math.ceil(cellWidth);
            int fillHeight = (int) Math.ceil(cellHeight);
            for (Point p : cells) {
                int x = (int) Math.round(p.x * cellWidth);
                int y = (int) Math.round(p.y * cellHeight);
                g.fillRect(x, y, fillWidth, fillHeight);
            }
            // Draw outline: compute bounding box of occupied cells
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Point p : cells) {
                if (p.x < minX) minX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.x > maxX) maxX = p.x;
                if (p.y > maxY) maxY = p.y;
            }
            int outlineX = (int) Math.round(minX * cellWidth);
            int outlineY = (int) Math.round(minY * cellHeight);
            int outlineWidth = (int) Math.round((maxX - minX + 1) * cellWidth);
            int outlineHeight = (int) Math.round((maxY - minY + 1) * cellHeight);
            g.setColor(OUTLINE_COLOR);
            g.drawRect(outlineX, outlineY, outlineWidth, outlineHeight);
        }

        private void drawGhostShip(Graphics g, int startRow, int startCol, int boardSize, double cellWidth, double cellHeight) {
            List<Point> cells = selectedShip.getOccupiedCellsAt(lastHoveredCol, lastHoveredRow);
            boolean collision = checkCollision(lastHoveredRow, lastHoveredCol);
            g.setColor(collision ? COLLISION_COLOR : HOVER_COLOR);
            int fillWidth = (int) Math.ceil(cellWidth);
            int fillHeight = (int) Math.ceil(cellHeight);
            for (Point p : cells) {
                int x = (int) Math.round(p.x * cellWidth);
                int y = (int) Math.round(p.y * cellHeight);
                g.fillRect(x, y, fillWidth, fillHeight);
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
                    if (locked) return;
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