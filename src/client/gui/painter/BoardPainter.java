package client.gui.painter;

import java.awt.Graphics;

public interface BoardPainter {
    /**
     * Paints the background of the board.
     * @param g the Graphics object to draw on
     * @param boardSize the size of the board (number of rows and columns)
     * @param rows the number of rows
     * @param cols the number of columns
     */
    void paintBackground(Graphics g, int boardSize, int rows, int cols);
}