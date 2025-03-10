package client.gui.painter;

import client.gui.Board;

import java.awt.*;

public class InGameBoardPainter implements BoardPainter {
    private final Image backgroundImage;

    /**
     * Creates a new InGameBoardPainter with the specified background image.
     * @param backgroundImage The image to be used as the background.
     */
    public InGameBoardPainter(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     * Paints the background of the board using the specified image.
     * @param g The Graphics object to draw on.
     * @param boardSize The size of the board (number of rows and columns).
     * @param rows The number of rows.
     * @param cols The number of columns.
     */
    @Override
    public void paintBackground(Graphics g, int boardSize, int rows, int cols) {
        Board.drawLines(g, backgroundImage, boardSize, rows, cols);
    }
}