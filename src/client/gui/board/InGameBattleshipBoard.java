package client.gui.board;

import client.gui.painter.BoardPainter;
import protocol.Ship;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class InGameBattleshipBoard extends AbstractBattleshipBoard {
    private InGameBoardListener listener;

    public InGameBattleshipBoard(int boardSize, List<Ship> placedShips, BoardPainter boardPainter,
                                 InGameBoardListener listener) {
        super(boardSize, placedShips, boardPainter);
        this.listener = listener;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point cell = getCellFromMouseEvent(e);
                if (cell != null && listener != null) {
                    listener.onCellClick(cell.y, cell.x);
                }
            }
        });
    }

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

    public interface InGameBoardListener {
        /**
         * Wird aufgerufen, wenn im Spielbereich (z. B. zum Angreifen) eine Zelle angeklickt wird.
         *
         * @param row Zeile
         * @param col Spalte
         */
        void onCellClick(int row, int col);
    }
}