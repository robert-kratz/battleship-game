package client.gui;

import server.GameState;

import javax.swing.*;
import java.awt.*;

public class GameBoard extends JPanel {

    private GameState gameState;

    private final BattleshipBoard battleshipBoard;
    private final JPanel leftPanel;
    private final JPanel rightPanel;

    public GameBoard(int rows, int cols) {
        setLayout(new BorderLayout());

        // Linke Seitenleiste (200px breit)
        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.add(new JLabel("GAMEBOARD"));

        // Rechte Seitenleiste (200px breit)
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.add(new JLabel("Test-Items"));

        // Spielfeld (BattleshipBoard in der Mitte)
        battleshipBoard = new BattleshipBoard(rows, cols, "resource/background.png");
        battleshipBoard.setPreferredSize(new Dimension(804, 804)); // Spielfeld ist immer quadratisch
        battleshipBoard.setListener(new BattleshipBoard.BattleShipBoardListener() {
            @Override
            public void onCellHover(int row, int col) {
                System.out.println("Hover: " + row + ", " + col);
            }

            @Override
            public void onCellClick(int row, int col) {
                System.out.println("Click: " + row + ", " + col);
            }
        });

        // Panels zum Layout hinzufügen
        add(leftPanel, BorderLayout.WEST);
        add(battleshipBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}