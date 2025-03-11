package client.gui;

import java.awt.*;

public class Board {

    /**
     * Draws the grid lines and fills each cell with the specified background image.
     * @param g Graphics object
     * @param backgroundImage The image to be used as the background for each cell
     * @param boardSize Size (in pixels) of the square board
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public static void drawLines(Graphics g, Image backgroundImage, int boardSize, int rows, int cols) {
        double cellWidth = boardSize / (double) cols;
        double cellHeight = boardSize / (double) rows;
        // Paints the background image in each cell
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = (int) Math.round(col * cellWidth);
                int y = (int) Math.round(row * cellHeight);
                g.drawImage(backgroundImage, x, y, (int) cellWidth, (int) cellHeight, null);
            }
        }
        // Paints the grid lines
        g.setColor(Color.WHITE);
        for (int i = 0; i <= cols; i++) {
            int x = (int) Math.round(i * cellWidth);
            g.drawLine(x, 0, x, boardSize);
        }
        for (int i = 0; i <= rows; i++) {
            int y = (int) Math.round(i * cellHeight);
            g.drawLine(0, y, boardSize, y);
        }
    }

    /**
     * Paints the legend (row and column labels) on the board.
     * @param g Graphics object
     * @param boardSize Size of the board (in pixels)
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public static void drawLegend(Graphics g, int boardSize, int rows, int cols) {
        double cellWidth = boardSize / (double) cols;
        double cellHeight = boardSize / (double) rows;
        // Paints the column labels (left)
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        for (int row = 0; row < rows; row++) {
            String label = String.valueOf(row + 1);
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getAscent();
            int x = 2;
            int y = (int) (row * cellHeight + cellHeight / 2 + textHeight / 2);
            g.drawString(label, x, y);
        }

        // Paints the row labels (top)
        for (int col = 0; col < cols; col++) {
            char letter = (char) ('A' + col);
            String label = String.valueOf(letter);
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getAscent();
            int x = (int) (col * cellWidth + cellWidth / 2 - textWidth / 2);
            int y = textHeight;
            g.drawString(label, x, y);
        }
    }

    /**
     * Darkens a cell with a specified color.
     * @param g Graphics object
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth width of the cell
     * @param cellHeight height of the cell
     * @param color Color to darken the cell with
     */
    public static void darkenCell(Graphics g, int row, int col, double cellWidth, double cellHeight, Color color) {
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);

        g.setColor(color);
        int x = (int) Math.round(col * cellWidth);
        int y = (int) Math.round(row * cellHeight);
        int fillWidth = (int) Math.ceil(cellWidth);
        int fillHeight = (int) Math.ceil(cellHeight);
        g.fillRect(x, y, fillWidth, fillHeight);
    }

    /**
     * Draws a hit cell (red marking) in the cell.
     * @param g Graphics object
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth width of the cell
     * @param cellHeight height of the cell
     */
    public static void drawShipHitCell(Graphics g, int row, int col, double cellWidth, double cellHeight) {
        g.setColor(Color.RED);
        int centerX = (int) Math.round(col * cellWidth + cellWidth / 2);
        int centerY = (int) Math.round(row * cellHeight + cellHeight / 2);
        int radius = 7;
        g.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    /**
     * Draws a miss cell (light gray marking) in the cell.
     * @param g Graphics object
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth width of the cell
     * @param cellHeight height of the cell
     */
    public static void drawShipMissCell(Graphics g, int row, int col, double cellWidth, double cellHeight) {
        g.setColor(Color.LIGHT_GRAY);
        int centerX = (int) Math.round(col * cellWidth + cellWidth / 2);
        int centerY = (int) Math.round(row * cellHeight + cellHeight / 2);
        int radius = 7;
        g.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    /**
     * Draws a radar number in the cell.
     * @param g Graphics object
     * @param row row
     * @param col col
     * @param cellWidth width of the cell
     * @param cellHeight height of the cell
     * @param number The number to be displayed in the cell
     */
    public static void drawRadar(Graphics g, int row, int col, double cellWidth, double cellHeight, int number, boolean isHit) {
        Graphics2D g2d = (Graphics2D) g.create();
        // Setze den Strich auf 2 Pixel Dicke
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(38, 255, 0));

        if(!isHit) darkenCell(g, row, col, cellWidth, cellHeight, new Color(38, 255, 0));

        int startCol = col - 1;
        int startRow = row - 1;
        int topLeftX = (int) Math.round(startCol * cellWidth);
        int topLeftY = (int) Math.round(startRow * cellHeight);
        int blockWidth = (int) Math.round(3 * cellWidth);
        int blockHeight = (int) Math.round(3 * cellHeight);

        g2d.drawRect(topLeftX, topLeftY, blockWidth, blockHeight);
        g2d.dispose();

        int centerX = (int) Math.round(col * cellWidth + cellWidth / 2);
        int centerY = (int) Math.round(row * cellHeight + cellHeight / 2);
        FontMetrics fm = g.getFontMetrics();
        String label = String.valueOf(number);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        if(number != 0) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.LIGHT_GRAY);
        }
        int textWidth = fm.stringWidth(label);
        int y = centerY + (fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(label, centerX - textWidth / 2, y);
    }
}