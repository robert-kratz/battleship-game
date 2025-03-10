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
        // Zeichne Zeilenbeschriftung (Nummern) links
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

        // Zeichne Spaltenbeschriftung (Buchstaben) oben
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
     * Verdunkelt eine Zelle im Spielfeld.
     * @param g Graphics-Objekt
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth Breite der Zelle
     * @param cellHeight Höhe der Zelle
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
     * Zeichnet einen Treffer (rote Markierung) in der Zelle.
     * @param g Graphics-Objekt
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth Breite der Zelle
     * @param cellHeight Höhe der Zelle
     */
    public static void drawShipHitCell(Graphics g, int row, int col, double cellWidth, double cellHeight) {
        g.setColor(Color.RED);
        int centerX = (int) Math.round(col * cellWidth + cellWidth / 2);
        int centerY = (int) Math.round(row * cellHeight + cellHeight / 2);
        int radius = 7;
        g.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    /**
     * Zeichnet einen Fehlschuss (graue Markierung) in der Zelle.
     * @param g Graphics-Objekt
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth Breite der Zelle
     * @param cellHeight Höhe der Zelle
     */
    public static void drawShipMissCell(Graphics g, int row, int col, double cellWidth, double cellHeight) {
        g.setColor(Color.LIGHT_GRAY);
        int centerX = (int) Math.round(col * cellWidth + cellWidth / 2);
        int centerY = (int) Math.round(row * cellHeight + cellHeight / 2);
        int radius = 7;
        g.fillOval(centerX - radius, centerY - radius, 2 * radius, 2 * radius);
    }

    /**
     * Zeichnet eine Radarzahl in der Zelle.
     * @param g Graphics-Objekt
     * @param row Zeile (Zellenindex)
     * @param col Spalte (Zellenindex)
     * @param cellWidth Breite der Zelle
     * @param cellHeight Höhe der Zelle
     * @param number Die Radarzahl
     */
    public static void drawRadar(Graphics g, int row, int col, double cellWidth, double cellHeight, int number) {
        int centerX = (int) Math.round(col * cellWidth + cellWidth / 2);
        int centerY = (int) Math.round(row * cellHeight + cellHeight / 2);
        FontMetrics fm = g.getFontMetrics();
        String label = String.valueOf(number);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        if(number != 0) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.LIGHT_GRAY);
        }
        int textWidth = fm.stringWidth(label);
        // Berechne die Basislinie, sodass der Text vertikal zentriert ist
        int y = centerY + (fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(label, centerX - textWidth / 2, y);
    }
}