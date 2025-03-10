package client.gui.board;

import client.gui.painter.BoardPainter;
import protocol.Ship;
import protocol.ShipPlacementHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BuildBattleshipBoard extends AbstractBattleshipBoard {

    private Ship selectedShip;
    private Integer lastHoveredRow = null;
    private Integer lastHoveredCol = null;
    private boolean locked = false;

    //Colors for the ship preview
    private final Color HOVER_COLOR = new Color(175, 175, 175, 150);
    private final Color COLLISION_COLOR = new Color(255, 89, 94, 150);

    private final int boardSize;

    private final BuildBoardListener listener;

    /**
     * Creates a new board with the given size and placed ships.
     * @param boardSize The size of the board (number of rows and columns).
     * @param placedShips The list of ships that are placed on the board.
     * @param boardPainter The painter used to draw the board.
     * @param listener The listener for cell clicks.
     */
    public BuildBattleshipBoard(int boardSize, List<Ship> placedShips, BoardPainter boardPainter, BuildBoardListener listener) {
        super(boardSize, placedShips, boardPainter);
        this.listener = listener;
        this.boardSize = boardSize;
        initializeBoard();
        bindRotationKey();
    }

    /**
     * Initializes the board with mouse listeners for cell clicks and hover effects.
     */
    private void initializeBoard() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (locked) return;
                Point cell = getCellFromMouseEvent(e);
                if (cell == null) return;
                lastHoveredRow = cell.y;
                lastHoveredCol = cell.x;
                if (listener != null) {
                    listener.onCellClick(cell.y, cell.x, selectedShip);
                }
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (locked) return;
                lastHoveredRow = null;
                lastHoveredCol = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (locked) return;
                Point cell = getCellFromMouseEvent(e);
                if (cell != null) {
                    lastHoveredRow = cell.y;
                    lastHoveredCol = cell.x;
                    repaint();
                }
            }
        });
    }

    /**
     * Converts a mouse event to a cell position on the board.
     * @param e The mouse event.
     * @return The cell position as a Point (x, y), or null if the position is out of bounds.
     */
    private Point getCellFromMouseEvent(MouseEvent e) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int boardSize = Math.min(panelWidth, panelHeight);
        double cellWidth = boardSize / (double) cols;
        double cellHeight = boardSize / (double) rows;
        int col = (int) (e.getX() / cellWidth);
        int row = (int) (e.getY() / cellHeight);
        if (col < 0 || col >= cols || row < 0 || row >= rows) return null;
        return new Point(col, row);
    }

    /**
     * Binds the rotation key (SPACE or R) to rotate the selected ship.
     */
    private void bindRotationKey() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "rotateShip");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("R"), "rotateShip");
        getActionMap().put("rotateShip", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!locked && selectedShip != null) {
                    selectedShip.setOrientation(selectedShip.getOrientation().next());
                    repaint();
                }
            }
        });
    }

    /**
     * Removes a placed ship from the board.
     * @param shipId The ID of the ship to be removed.
     */
    public void removePlacedShip(int shipId) {
        this.placedShips.removeIf(ship -> ship.getId() == shipId);
        repaint();
    }

    /**
     * Sets the selected ship for placement.
     * @return The currently selected ship.
     */
    public Ship getSelectedShip() {
        return selectedShip;
    }

    /**
     * Sets the selected ship for placement.
     * @param selectedShip The ship to be placed.
     */
    public void setSelectedShip(Ship selectedShip) {
        this.selectedShip = selectedShip;
    }

    /**
     * Sets the placed ships on the board and repaints it.
     * @param placedShips The list of ships to be placed on the board.
     */
    @Override
    public void setPlacedShips(List<Ship> placedShips) {
        super.setPlacedShips(placedShips);
    }

    /**
     * Returns the list of placed ships on the board.
     * @return The list of placed ships.
     */
    @Override
    public List<Ship> getPlacedShips() {
        return super.getPlacedShips();
    }

    /**
     * Locks the board to prevent further interactions.
     */
    public void lockBoard() {
        locked = true;
        repaint();
    }

    /**
     * Unlocks the board to allow interactions.
     */
    public void unlockBoard() {
        locked = false;
        repaint();
    }

    /**
     * Checks if the board is locked.
     * @return true if the board is locked, false otherwise.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Paints the component.
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int boardSize = Math.min(panelWidth, panelHeight);
        double cellWidth = boardSize / (double) cols;
        double cellHeight = boardSize / (double) rows;

        // Zeichne das "Ghost-Ship" (Vorschau) falls vorhanden
        if (selectedShip != null && lastHoveredRow != null && lastHoveredCol != null) {
            java.util.List<Point> ghostCells = selectedShip.getOccupiedCellsAt(lastHoveredCol, lastHoveredRow);
            boolean collision = ShipPlacementHelper.isCollision(selectedShip, this.boardSize, lastHoveredRow, lastHoveredCol, placedShips);
            g.setColor(collision ? COLLISION_COLOR : HOVER_COLOR);
            int fillWidth = (int) Math.ceil(cellWidth);
            int fillHeight = (int) Math.ceil(cellHeight);
            for (Point p : ghostCells) {
                int x = (int) Math.round(p.x * cellWidth);
                int y = (int) Math.round(p.y * cellHeight);
                g.fillRect(x, y, fillWidth, fillHeight);
            }
        }

        // Falls das Board gesperrt ist, zeichne ein Overlay
        if (locked) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, boardSize, boardSize);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g.getFontMetrics();
            String text = "Waiting for opponent...";
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g.drawString(text, (boardSize - textWidth) / 2, (boardSize + textHeight) / 2);
        }
    }

    /**
     * Interface for handling cell click events.
     */
    public interface BuildBoardListener {
        /**
         * Wird aufgerufen, wenn auf eine Zelle geklickt wurde.
         *
         * @param row          Zeile
         * @param col          Spalte
         * @param selectedShip Aktuell selektiertes Schiff
         */
        void onCellClick(int row, int col, Ship selectedShip);
    }
}