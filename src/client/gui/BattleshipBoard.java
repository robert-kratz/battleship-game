package client.gui;

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
    private BackgroundPanel backgroundPanel;
    private BattleShipBoardListener listener;
    private boolean showHoverAnimation = true;
    private Ship selectedShip;
    private List<Ship> placedShips;
    private Integer lastHoveredRow = null;
    private Integer lastHoveredCol = null;

    public BattleshipBoard(int rows, int cols, String imagePath, int width, List<Ship> placedShips) {
        this.rows = rows;
        this.cols = cols;
        this.gridButtons = new JButton[rows][cols];
        this.backgroundImage = new ImageIcon(imagePath);
        this.width = width;
        this.placedShips = placedShips;

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

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "rotateShip");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "rotateShip");
        getActionMap().put("rotateShip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleShipOrientation();
                reevaluateComponent();
            }
        });
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
            selectedShip.setOrientation(selectedShip.getOrientation() == Ship.Orientation.HORIZONTAL ?
                    Ship.Orientation.VERTICAL : Ship.Orientation.HORIZONTAL);
            if (lastHoveredRow != null && lastHoveredCol != null) {
                repaint();
            }
        }
    }

    private boolean checkCollision(int startRow, int startCol) {
        int length = selectedShip.getLength();
        int width = selectedShip.getWidth();
        boolean isHorizontal = selectedShip.getOrientation() == Ship.Orientation.HORIZONTAL;

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
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
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int minSize = Math.min(panelWidth, panelHeight);
            setSize(minSize, minSize);
            g.drawImage(backgroundImage, 0, 0, minSize, minSize, this);

            g.setColor(Color.WHITE);
            for (int i = 0; i <= cols; i++) {
                int x = i * minSize / cols;
                g.drawLine(x, 0, x, minSize);
            }
            for (int i = 0; i <= rows; i++) {
                int y = i * minSize / rows;
                g.drawLine(0, y, minSize, y);
            }

            if (selectedShip != null && lastHoveredRow != null && lastHoveredCol != null) {
                drawShip(g, lastHoveredRow, lastHoveredCol, minSize);
            }
        }

        public void reevaluateComponent() {
            repaint();
        }

        private void drawShip(Graphics g, int startRow, int startCol, int minSize) {
            int length = selectedShip.getLength();
            int width = selectedShip.getWidth();
            boolean isHorizontal = selectedShip.getOrientation() == Ship.Orientation.HORIZONTAL;

            boolean collision = checkCollision(startRow, startCol);
            g.setColor(collision ? new Color(255, 0, 0, 150) : new Color(0, 0, 0, 150));

            int cellWidth = minSize / cols;
            int cellHeight = minSize / rows;

            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    int targetRow = startRow + (isHorizontal ? j : i);
                    int targetCol = startCol + (isHorizontal ? i : j);

                    if (targetRow >= 0 && targetRow < rows && targetCol >= 0 && targetCol < cols) {
                        g.fillRect(targetCol * cellWidth, targetRow * cellHeight, cellWidth, cellHeight);
                    }
                }
            }
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
                public void mouseEntered(MouseEvent e) {
                    if (selectedShip != null) {
                        lastHoveredRow = row;
                        lastHoveredCol = col;
                        reevaluateComponent();
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lastHoveredRow = null;
                    lastHoveredCol = null;
                    reevaluateComponent();
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (listener != null) {
                        listener.onCellClick(row, col);
                    }
                }
            });
        }
    }

    interface BattleShipBoardListener {
        void onCellHover(int row, int col);
        void onCellClick(int row, int col);
    }
}
