package client.gui;

import java.awt.*;

public class Board {

    /**
     * Zeichnet den Hintergrund des Spielfeldes.
     * @param g Graphics-Objekt
     * @param backgroundImage Hintergrundbild
     * @param boardSize Größe (in Pixeln) des quadratischen Spielfeldes
     * @param rows Anzahl der Reihen
     * @param cols Anzahl der Spalten
     */
    public static void drawLines(Graphics g, Image backgroundImage, int boardSize, int rows, int cols) {
        double cellWidth = boardSize / (double) cols;
        double cellHeight = boardSize / (double) rows;
        // Zeichne in jeder Zelle das Hintergrundbild
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = (int) Math.round(col * cellWidth);
                int y = (int) Math.round(row * cellHeight);
                g.drawImage(backgroundImage, x, y, (int) cellWidth, (int) cellHeight, null);
            }
        }
        // Zeichne die Gitterlinien
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
     * Zeichnet die Legende (Zeilen- und Spaltenbezeichnungen) des Spielfeldes.
     * @param g Graphics-Objekt
     * @param boardSize Größe (in Pixeln) des quadratischen Spielfeldes
     * @param rows Anzahl der Reihen
     * @param cols Anzahl der Spalten
     */
    public static void drawLegend(Graphics g, int boardSize, int rows, int cols) {
        double cellWidth = boardSize / (double) cols;
        double cellHeight = boardSize / (double) rows;
        // Draw row labels (numbers) on the left
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

        // Draw column labels (letters) at the top
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

}
