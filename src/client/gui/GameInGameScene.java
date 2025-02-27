package client.gui;

import client.GameHandler;
import client.gui.board.InGameBattleshipBoard;
import client.gui.painter.BoardPainter;
import client.gui.painter.InGameBoardPainter;
import protocol.GameState;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameInGameScene extends JPanel implements Runnable {

    private Point lastHoveredTile = new Point(-1, -1);

    private GameHandler gameHandler;

    private JPanel leftPanel;
    private JPanel rightPanel;
    private InGameBattleshipBoard inGameBoard;

    private UUID playersTurn;

    public GameInGameScene(GameHandler gameHandler, ArrayList<Ship> placedShips) {
        this.gameHandler = gameHandler;

        // Board-Painter für InGame-Phase (verwende hier ggf. ein anderes Bild)
        Image inGameBackgroundImage = new ImageIcon("resource/background-2.png").getImage();
        BoardPainter inGamePainter = new InGameBoardPainter(inGameBackgroundImage);

        System.out.println("GameInGameScene created");
        for (Ship ship : placedShips) {
            System.out.println("Ship: " + ship);
        }

        inGameBoard = new InGameBattleshipBoard(gameHandler.getGameState().getBoardSize(), placedShips, inGamePainter,
                (row, col) -> {
                    // Hier wird ein Klick im InGame-Board verarbeitet (z.B. zum Angreifen)
                    System.out.println("InGameBoard clicked at: " + row + ", " + col);
                });

        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.add(new JLabel("GAMEBOARD"));
        leftPanel.add(new JLabel("INGAME"));

        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.add(new JLabel("Test-Items"));

        setLayout(new BorderLayout());
        add(leftPanel, BorderLayout.WEST);
        add(inGameBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    public void setOpponentHover(Point lastHoveredTile) {
        this.lastHoveredTile = lastHoveredTile;
    }

    @Override
    public void run() {
        System.out.println("GameInGameScene started");
    }
}