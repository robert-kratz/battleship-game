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
import java.util.List;

public class InGameBattleshipBoard extends AbstractBattleshipBoard {
    private InGameBoardListener listener;
    private ArrayList<Move> moves = new ArrayList<>();
    private final int boardSize;

    // Hier speichern wir die von einem Gegner übertragenen Hover-Daten
    private ArrayList<Cell> affectedCells = new ArrayList<>();

    private int hoveredRow = -1;
    private int hoveredCol = -1;

    // Aktuell ausgewähltes Item (z.B. Radar, SeaBomb, AirStrike)
    private Item selectedItem = null;

    // Flag, ob das Board interaktiv ist (Maus- und Tastatureingaben werden nur verarbeitet, wenn true)
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

        // MouseListener für Mausklick und MouseExit
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
                // Beim Verlassen des Boards: Hover-Daten löschen und leere Liste an den Listener senden
                hoveredRow = -1;
                hoveredCol = -1;
                if (listener != null) {
                    listener.onHover(-1, -1, new ArrayList<>());
                }
                repaint();
            }
        });

        // MouseMotionListener: Verarbeitet Mausbewegungen
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

    @Override
    public void setPlacedShips(List<Ship> placedShips) {
        super.setPlacedShips(placedShips);
    }

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
     * Berechnet die Liste der von der Maus an der Zelle (row, col) betroffenen Zellen,
     * abhängig davon, ob ein Item ausgewählt wurde.
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

    public void setSelectedItem(Item item) {
        this.selectedItem = item;
        if (listener != null) {
            listener.onSelectedItemChanged(selectedItem);
        }
        repaint();
    }

    public void clearSelectedItem() {
        this.selectedItem = null;
        if (listener != null) {
            listener.onSelectedItemChanged(selectedItem);
        }
        repaint();
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
        System.out.println("Moves: " + moves.size() + " (InGameBattleshipBoard) repainting");
        repaint();
    }

    public void setOpponentAffectedCells(ArrayList<Cell> affectedCells) {
        this.affectedCells = affectedCells;
        repaint();
    }

    public void clearOpponentAffectedCells() {
        this.affectedCells = new ArrayList<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int pixelBoardSize = Math.min(panelWidth, panelHeight);
        double cellWidth = pixelBoardSize / (double) cols;
        double cellHeight = pixelBoardSize / (double) rows;

        HashMap<Integer, Integer> takenCells = new HashMap<>();

        // Zeichne alle Moves (Treffer, Fehlschüsse, Radar)
        for (Move move : moves) {
            for (Cell cell : move.getAffectedCells()) {
                int col = cell.getX();
                int row = cell.getY();

                if(takenCells.containsKey(col) && takenCells.get(col) == row) continue;
                if (cell.isHit()) {
                    Board.darkenCell(g, row, col, cellWidth, cellHeight, Color.RED);
                    Board.drawShipHitCell(g, row, col, cellWidth, cellHeight);

                } else {
                    Board.darkenCell(g, row, col, cellWidth, cellHeight, Color.BLACK);
                    Board.drawShipMissCell(g, row, col, cellWidth, cellHeight);

                }
                takenCells.put(col, row);
            }
        }

        for (Move move : moves) {
            if (move.getRadarItem() != null) {
                Board.drawRadar(g, move.getY(), move.getX(), cellWidth, cellHeight, move.getRadarShipsIn3x3Area(), Color.WHITE);
            }
        }

        // Hover-Overlay (für das aktuell im Board berechnete Hover-Event)
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

        // Zeichne alle vom Gegner übermittelten (Hover-)Zellen
        for (Cell cell : affectedCells) {
            int col = cell.getX();
            int row = cell.getY();
            Board.darkenCell(g, row, col, cellWidth, cellHeight, Color.BLACK);
        }
    }

    public interface InGameBoardListener {
        /**
         * Wird aufgerufen, wenn im Spielfeld eine Zelle angeklickt wird.
         */
        void onCellClick(int row, int col);

        /**
         * Wird aufgerufen, wenn der Mauszeiger über eine Zelle bewegt wird.
         */
        void onHover(int row, int col, ArrayList<Cell> affectedCells);

        /**
         * Wird aufgerufen, wenn sich das aktuell ausgewählte Item ändert (z.B. durch Toggle).
         */
        void onSelectedItemChanged(Item newItem);
    }
}