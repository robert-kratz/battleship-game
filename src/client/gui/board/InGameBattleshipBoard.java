package client.gui.board;

import client.gui.Board;
import client.gui.painter.BoardPainter;
import protocol.Ship;
import protocol.game.Cell;
import protocol.game.Move;
import protocol.game.items.AirStrikeItem;
import protocol.game.items.RadarItem;
import protocol.game.items.SeaBombItem;
import protocol.game.Item;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class InGameBattleshipBoard extends AbstractBattleshipBoard {

    private InGameBoardListener listener;
    private ArrayList<Move> moves = new ArrayList<>();
    private final int boardSize;

    private ArrayList<Cell> affectedCells = new ArrayList<>();

    // Currently hovered cell (for mouse hover)
    private int hoveredRow = -1;
    private int hoveredCol = -1;

    // Currently selected item (e.g. AirStrikeItem, SeaBombItem, RadarItem)
    private Item selectedItem = null;
    private boolean interactive = true;

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public InGameBattleshipBoard(int boardSize, List<Ship> placedShips, BoardPainter boardPainter,
                                 InGameBoardListener listener) {
        super(boardSize, placedShips, boardPainter);
        this.listener = listener;
        this.boardSize = boardSize;

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!interactive) return;
                if (selectedItem instanceof AirStrikeItem) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_R) {
                        AirStrikeItem airStrike = (AirStrikeItem) selectedItem;
                        if (airStrike.getOrientation() == AirStrikeItem.Orientation.HORIZONTAL) {
                            airStrike.setOrientation(AirStrikeItem.Orientation.VERTICAL);
                        } else {
                            airStrike.setOrientation(AirStrikeItem.Orientation.HORIZONTAL);
                        }
                        if (listener != null) {
                            listener.onSelectedItemChanged(selectedItem);
                        }
                        repaint();
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!interactive) return;
                Point cell = getCellFromMouseEvent(e);
                if (cell != null && listener != null) {
                    listener.onCellClick(cell.y, cell.x);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                hoveredCol = -1;
                if (listener != null) {
                    listener.onHover(-1, -1, new ArrayList<>());
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!interactive) {
                    hoveredRow = -1;
                    hoveredCol = -1;
                    return;
                }
                Point cell = getCellFromMouseEvent(e);
                if (cell != null) {
                    hoveredRow = cell.y;
                    hoveredCol = cell.x;
                    ArrayList<Cell> computed = computeAffectedCellsForSelectedItem(cell.y, cell.x);
                    if (listener != null) {
                        listener.onHover(cell.y, cell.x, computed);
                    }
                } else {
                    hoveredRow = -1;
                    hoveredCol = -1;
                    if (listener != null) {
                        listener.onHover(-1, -1, new ArrayList<>());
                    }
                }
                repaint();
            }
        });
    }

    /**
     * Sets the listener for board events.
     * @param placedShips The list of ships to be placed on the board.
     */
    @Override
    public void setPlacedShips(List<Ship> placedShips) {
        super.setPlacedShips(placedShips);
    }

    /**
     * Returns the list of placed ships on the board.
     * @param e the mouse event
     * @return The cell position as a Point (x, y), or null if the position is out of bounds.
     */
    private Point getCellFromMouseEvent(MouseEvent e) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int pixelBoardSize = Math.min(panelWidth, panelHeight);
        double cellWidth = pixelBoardSize / (double) cols;
        double cellHeight = pixelBoardSize / (double) rows;
        int col = (int) (e.getX() / cellWidth);
        int row = (int) (e.getY() / cellHeight);
        if (col < 0 || col >= cols || row < 0 || row >= rows) return null;
        return new Point(col, row);
    }

    /**
     * Computes the list of affected cells based on the selected item and the given cell coordinates.
     * @param row the row of the cell
     * @param col the column of the cell
     * @return a list of affected cells
     */
    private ArrayList<Cell> computeAffectedCellsForSelectedItem(int row, int col) {
        ArrayList<Cell> affected = new ArrayList<>();
        // Standard: nur die Zelle selbst
        affected.add(new Cell(col, row));
        if (selectedItem == null) {
            return affected;
        }
        if (selectedItem instanceof RadarItem) {
            return affected;
        } else if (selectedItem instanceof SeaBombItem) {
            affected.clear();
            if (col < boardSize - 1 && row < boardSize - 1) {
                affected.add(new Cell(col, row));
                affected.add(new Cell(col + 1, row));
                affected.add(new Cell(col, row + 1));
                affected.add(new Cell(col + 1, row + 1));
            }
            return affected;
        } else if (selectedItem instanceof AirStrikeItem) {
            affected.clear();
            AirStrikeItem airStrike = (AirStrikeItem) selectedItem;
            if (airStrike.getOrientation() == AirStrikeItem.Orientation.HORIZONTAL) {
                for (int c = 0; c < boardSize; c++) {
                    affected.add(new Cell(c, row));
                }
            } else {
                for (int r = 0; r < boardSize; r++) {
                    affected.add(new Cell(col, r));
                }
            }
            return affected;
        }
        return affected;
    }

    /**
     * Sets the selected item for the board and notifies the listener.
     * @param item The item to be selected.
     */
    public void setSelectedItem(Item item) {
        this.selectedItem = item;
        if (listener != null) {
            listener.onSelectedItemChanged(selectedItem);
        }
        repaint();
    }

    /**
     * Clears the selected item and notifies the listener.
     */
    public void clearSelectedItem() {
        this.selectedItem = null;
        if (listener != null) {
            listener.onSelectedItemChanged(selectedItem);
        }
        repaint();
    }

    /**
     * Returns the currently selected item.
     * @return The currently selected item.
     */
    public Item getSelectedItem() {
        return selectedItem;
    }

    /**
     * Sets the moves for the board and repaints it.
     * @param moves The list of moves to be set.
     */
    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
        repaint();
    }

    /**
     * Sets the affected cells for the opponent and repaints the board.
     * @param affectedCells The list of affected cells.
     */
    public void setOpponentAffectedCells(ArrayList<Cell> affectedCells) {
        this.affectedCells = affectedCells;
        repaint();
    }

    /**
     * Clears the affected cells for the opponent and repaints the board.
     */
    public void clearOpponentAffectedCells() {
        this.affectedCells = new ArrayList<>();
        repaint();
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
        int pixelBoardSize = Math.min(panelWidth, panelHeight);
        double cellWidth = pixelBoardSize / (double) cols;
        double cellHeight = pixelBoardSize / (double) rows;

        HashSet<String> paintedCells = new HashSet<>();

        // Paint all moves (hits and misses)
        for (Move move : moves) {
            for (Cell cell : move.getAffectedCells()) {
                int col = cell.getX();
                int row = cell.getY();
                String key = col + "-" + row;

                if (paintedCells.contains(key)) {
                    continue; // Cell was already painted
                }

                paintedCells.add(key);

                if (cell.isHit()) {
                    Board.darkenCell(g, row, col, cellWidth, cellHeight, Color.RED);
                    Board.drawShipHitCell(g, row, col, cellWidth, cellHeight);
                } else {
                    Board.darkenCell(g, row, col, cellWidth, cellHeight, Color.BLACK);
                    Board.drawShipMissCell(g, row, col, cellWidth, cellHeight);
                }
            }
        }

        // Paint all placed ships
        for (Move move : moves) {
            if (move.getRadarItem() != null) {
                Board.drawRadar(g, move.getY(), move.getX(), cellWidth, cellHeight, move.getRadarShipsIn3x3Area());
            }
        }

        // Hover-Overlay (for the currently selected item)
        if (interactive && hoveredRow != -1 && hoveredCol != -1) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 100));
            ArrayList<Cell> computed = computeAffectedCellsForSelectedItem(hoveredRow, hoveredCol);
            for (Cell cell : computed) {
                int c = cell.getX();
                int r = cell.getY();
                int x = (int) (c * cellWidth);
                int y = (int) (r * cellHeight);
                g2.fillRect(x, y, (int) cellWidth, (int) cellHeight);
            }
            g2.dispose();
        }

        // Paint the opponent's affected cells by hover
        for (Cell cell : affectedCells) {
            int col = cell.getX();
            int row = cell.getY();
            Board.darkenCell(g, row, col, cellWidth, cellHeight, Color.BLACK);
        }
    }

    public interface InGameBoardListener {
        /**
         * Is called when a cell is clicked.
         */
        void onCellClick(int row, int col);

        /**
         * Is called when the mouse hovers over a cell.
         */
        void onHover(int row, int col, ArrayList<Cell> affectedCells);

        /**
         * Will be called when the selected item changes.
         */
        void onSelectedItemChanged(Item newItem);
    }
}