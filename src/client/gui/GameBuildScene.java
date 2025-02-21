package client.gui;

import client.GameHandler;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Date;

public class GameBuildScene extends JPanel implements Runnable {

    private int height, width;

    private GameHandler gameHandler;

    private final BattleshipBoard battleshipBoard;
    private final JPanel leftPanel;
    private final JPanel rightPanel;

    private JLabel timerLabel;

    public GameBuildScene(GameHandler gameHandler) {
        setLayout(new BorderLayout());

        this.gameHandler = gameHandler;

        // Linke Seitenleiste (200px breit)
        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.add(new JLabel("GAMEBOARD"));

        timerLabel = new JLabel("Timer: ");

        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(timerLabel);

        // Rechte Seitenleiste (200px breit)
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.add(new JLabel("Test-Items"));

        ArrayList<Ship> ships = this.gameHandler.getGameState().getShipsA();

        // Spielfeld (BattleshipBoard in der Mitte)
        battleshipBoard = new BattleshipBoard(gameHandler.getGameState().getBoardSize(), gameHandler.getGameState().getBoardSize(), "resource/background.png", this.gameHandler.getStageManager().getWindowsWidth(), ships);

        // Panels zum Layout hinzufügen
        add(leftPanel, BorderLayout.WEST);
        add(battleshipBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                System.out.println("GameBuildScene: Resized: " + getWidth() + "x" + getHeight());
            }
        });
    }

    public void updateDimensions(int width, int height) {
        this.width = width;
        this.height = height;

        System.out.println("GameBuildScene: Updated dimensions: " + width + "x" + height);

        // Setze die Preferred Sizes der Komponenten vor dem Neulayout
        battleshipBoard.setPreferredSize(new Dimension(width - 400, height));
        leftPanel.setPreferredSize(new Dimension(200, height));
        rightPanel.setPreferredSize(new Dimension(200, height));

        // Layout neu berechnen und neu zeichnen
        revalidate();
        repaint();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Date start = gameHandler.getGameState().getBuildGameBoardStarted();
            Date end = gameHandler.getGameState().getBuildGameBoardFinished();

            if (start != null && end != null) {
                long currentTime = System.currentTimeMillis();
                long remainingMillis = end.getTime() - currentTime;

                if (remainingMillis <= 0) {
                    // Zeit abgelaufen
                    SwingUtilities.invokeLater(() -> timerLabel.setText("Time over"));
                    // Optional: Schleife beenden oder weitere Aktionen triggern

                    //HERE SERVER ÜBERTRAGUNG

                    break;
                } else {
                    long minutes = (remainingMillis / (60 * 1000)) % 60;
                    long seconds = (remainingMillis / 1000) % 60;
                    String timeStr = String.format("%02d:%02d", minutes, seconds);
                    SwingUtilities.invokeLater(() -> timerLabel.setText(timeStr));
                }
            }
        }
    }
}
