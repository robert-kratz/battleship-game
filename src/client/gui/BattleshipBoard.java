package client.gui;

import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BattleshipBoard extends JPanel {
    private final int rows;
    private final int cols;
    private final JButton[][] gridButtons;
    private final ImageIcon backgroundImage;
    private BattleShipBoardListener listener;
    private boolean showHoverAnimation = true;

    private Ship selectedShip;

    public BattleshipBoard(int rows, int cols, String imagePath) {
        this.rows = rows;
        this.cols = cols;
        this.gridButtons = new JButton[rows][cols];
        this.backgroundImage = new ImageIcon(imagePath);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Background panel with grid and ships
        BackgroundPanel mainPanel = new BackgroundPanel(backgroundImage.getImage(), rows, cols);
        mainPanel.setLayout(new GridLayout(rows, cols, 0, 0));

        initializeBoard(mainPanel);
        add(mainPanel, BorderLayout.CENTER);

        // KeyListener for rotating ships
        setFocusable(true);
        requestFocusInWindow();
    }

    private void initializeBoard(JPanel panel) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                HoverButton button = new HoverButton(row, col);
                button.setPreferredSize(new Dimension(804 / cols, 804 / rows));
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setBorder(BorderFactory.createEmptyBorder());

                gridButtons[row][col] = button;
                panel.add(button);
            }
        }
    }

    public void setSelectedShip(Ship ship) {
        this.selectedShip = ship;
    }

    public void setShowHoverAnimation(boolean showHoverAnimation) {
        this.showHoverAnimation = showHoverAnimation;
    }

    public void setListener(BattleShipBoardListener listener) {
        this.listener = listener;
    }

    // **Panel to draw background, grid, and ships**
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

            g.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, this);

            // Draw Grid
            g.setColor(Color.WHITE);
            for (int i = 0; i <= cols; i++) {
                int x = i * panelWidth / cols;
                g.drawLine(x, 0, x, panelHeight);
            }
            for (int i = 0; i <= rows; i++) {
                int y = i * panelHeight / rows;
                g.drawLine(0, y, panelWidth, y);
            }
        }
    }

    // **Custom Button for Hover Interaction**
    private class HoverButton extends JButton {
        private final int row, col;

        private float opacity = 0f; // Initial transparent
        private Timer fadeInTimer, fadeOutTimer;

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
                    if (listener != null) {
                        listener.onCellHover(row, col);
                    }

                    if(!showHoverAnimation) return;

                    if (fadeOutTimer != null && fadeOutTimer.isRunning()) fadeOutTimer.stop();
                    fadeInTimer = new Timer(20, evt -> {
                        opacity = Math.min(0.18f, opacity + 0.05f);
                        repaint();
                        if (opacity >= 0.18f) ((Timer) evt.getSource()).stop();
                    });
                    fadeInTimer.start();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (listener != null) {
                        listener.onCellHover(-1, -1);
                    }

                    if(!showHoverAnimation) return;

                    if (fadeInTimer != null && fadeInTimer.isRunning()) fadeInTimer.stop();
                    fadeOutTimer = new Timer(20, evt -> {
                        opacity = Math.max(0f, opacity - 0.05f);
                        repaint();
                        if (opacity <= 0f) ((Timer) evt.getSource()).stop();
                    });
                    fadeOutTimer.start();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (listener != null) {
                        listener.onCellClick(row, col);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(opacity));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    interface BattleShipBoardListener {
        void onCellHover(int row, int col);
        void onCellClick(int row, int col);
    }
}