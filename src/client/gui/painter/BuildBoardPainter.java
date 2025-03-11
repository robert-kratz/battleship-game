package client.gui.painter;

import client.gui.Board;

import java.awt.*;

public class BuildBoardPainter implements BoardPainter {
    private final Image backgroundImage;

    /**
     * Creates a new BuildBoardPainter with the specified background image.
     * @param backgroundImage the image to use as the background
     */
    public BuildBoardPainter(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     * Paints the background of the board using the specified image.
     * @param g the Graphics object to draw on
     * @param boardSize the size of the board (number of rows and columns)
     * @param rows the number of rows
     * @param cols the number of columns
     */
    @Override
    public void paintBackground(Graphics g, int boardSize, int rows, int cols) {
        Board.drawLines(g, backgroundImage, boardSize, rows, cols);
    }
}