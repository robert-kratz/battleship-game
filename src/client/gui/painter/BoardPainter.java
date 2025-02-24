package client.gui.painter;

import java.awt.Graphics;

public interface BoardPainter {
    /**
     * Zeichnet den Hintergrund des Spielfeldes.
     *
     * @param g         Graphics-Objekt
     * @param boardSize Größe (in Pixeln) des quadratischen Spielfeldes
     * @param rows      Anzahl der Reihen
     * @param cols      Anzahl der Spalten
     */
    void paintBackground(Graphics g, int boardSize, int rows, int cols);
}