package client.gui.board;

import client.gui.Board;
import client.gui.painter.BoardPainter;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public abstract class AbstractBattleshipBoard extends JPanel {

    protected final int rows;
    protected final int cols;
    protected final List<Ship> placedShips;
    protected BoardPainter boardPainter;

    /**
     * Creates a new board with the given size and placed ships.
     * @param boardSize The size of the board (number of rows and columns).
     * @param placedShips The list of ships that are placed on the board.
     * @param boardPainter The painter used to draw the board.
     */
    public AbstractBattleshipBoard(int boardSize, List<Ship> placedShips, BoardPainter boardPainter) {
        this.rows = boardSize;
        this.cols = boardSize;
        this.placedShips = placedShips;
        this.boardPainter = boardPainter;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setFocusable(true);
        requestFocusInWindow();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }

    /**
     * Sets the placed ships on the board and repaints it.
     * @param placedShips The list of ships to be placed on the board.
     */
    public void setPlacedShips(List<Ship> placedShips) {
        this.placedShips.clear();
        this.placedShips.addAll(placedShips);
        repaint();
    }

    /**
     * Returns the list of placed ships on the board.
     * @return The list of placed ships.
     */
    public List<Ship> getPlacedShips() {
        return placedShips;
    }

    /**
     * Returns the ship at the specified coordinates.
     * @param row the row of the cell
     * @param col the column of the cell
     * @return the ship at the specified coordinates, or null if no ship is present
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
     * Draws the ships on the board.
     * @param g the graphics context
     * @param cellWidth the width of each cell
     * @param cellHeight the height of each cell
     */
    protected void drawShips(Graphics g, double cellWidth, double cellHeight) {
        for (Ship ship : placedShips) {
            drawShip(g, ship, cellWidth, cellHeight);
        }
    }

    /**
     * Draws a single ship on the board.
     * @param g the graphics context
     * @param ship the ship to draw
     * @param cellWidth the width of each cell
     * @param cellHeight the height of each cell
     */
    protected void drawShip(Graphics g, Ship ship, double cellWidth, double cellHeight) {
        // Fülle die Zellen des Schiffs
        g.setColor(new Color(175, 175, 175));
        java.util.List<Point> cells = ship.getOccupiedCells();
        int fillWidth = (int) Math.ceil(cellWidth);
        int fillHeight = (int) Math.ceil(cellHeight);
        for (Point p : cells) {
            int x = (int) Math.round(p.x * cellWidth);
            int y = (int) Math.round(p.y * cellHeight);
            g.fillRect(x, y, fillWidth, fillHeight);
        }
        // Zeichne einen Rahmen um das Schiff
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Point p : cells) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }
        int outlineX = (int) Math.round(minX * cellWidth);
        int outlineY = (int) Math.round(minY * cellHeight);
        int outlineWidth = (int) Math.round((maxX - minX + 1) * cellWidth);
        int outlineHeight = (int) Math.round((maxY - minY + 1) * cellHeight);
        g.setColor(Color.BLACK);
        g.drawRect(outlineX, outlineY, outlineWidth, outlineHeight);
    }

    /**
     * Draws the background and ships on the board.
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

        boardPainter.paintBackground(g, boardSize, rows, cols);
        drawShips(g, cellWidth, cellHeight);

        Board.drawLegend(g, boardSize, rows, cols);
    }
}