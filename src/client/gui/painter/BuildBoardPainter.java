package client.gui.painter;

import client.gui.Board;

import java.awt.*;

public class BuildBoardPainter implements BoardPainter {
    private final Image backgroundImage;

    public BuildBoardPainter(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    public void paintBackground(Graphics g, int boardSize, int rows, int cols) {
        Board.drawLines(g, backgroundImage, boardSize, rows, cols);
    }
}