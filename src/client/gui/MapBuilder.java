package client.gui;

import client.ClientHandler;
import client.GameHandler;
import client.Scene;
import protocol.Board;
import protocol.Ship;
import protocol.messages.GameStateRequestMessage;
import server.GameState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MapBuilder extends JPanel implements Scene {

    private final JLabel infoLabel;
    private final JPanel leftPanel;
    private final JPanel rightPanel;

    //private final BattleshipBoard battleshipBoard;
    private GameState gameState;

    private Ship selectedShip;

    private GameHandler gameHandler;

    private Thread timerThread;

    @Override
    public String getTitle() {
        return "Prepare your board";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1204, 816);
    }

    public MapBuilder(GameHandler gameHandler, int rows, int cols) {

        this.gameHandler = gameHandler;

        setLayout(new BorderLayout());

        infoLabel = new JLabel("Map Builder");

        // Linke Seitenleiste (200px breit)
        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, getHeight()));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.add(new JLabel("Shiffe"));
        //show all 1x1, 2x1, 2x2, 3x1, 4x1 ship png in leftPanel, all as buttons

        // Rechte Seitenleiste (200px breit)
        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, getHeight()));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.add(new JLabel("Test-Items"));
        rightPanel.add(infoLabel);

        // Spielfeld (BattleshipBoard in der Mitte)
        //battleshipBoard = new BattleshipBoard(rows, cols, "resource/background.png", this.gameHandler.getStageManager().getWindowsWidth());
       /* battleshipBoard.setListener(new BattleshipBoard.BattleShipBoardListener() {
            @Override
            public void onCellHover(int row, int col) {
                System.out.println("Hover: " + row + ", " + col);

            }

            @Override
            public void onCellClick(int row, int col) {
                System.out.println("Click: " + row + ", " + col);
                Ship ship = new Ship(1, row, col, Ship.Orientation.HORIZONTAL, 3, 1, "resource/ship.png");

                if(gameState.playerA.equals(gameHandler.getUserId())) {
                    gameState.getBoardA().addShip(ship);
                } else {
                    gameState.getBoardB().addShip(ship);
                }
                battleshipBoard.repaint();
            }
        });*/
        add(leftPanel, BorderLayout.WEST);
        //add(battleshipBoard, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;

        ArrayList<Ship> ships = new ArrayList<>();

        if(gameState.playerA.equals(gameHandler.getUserId())) {
            ships = gameState.getBoardA().getShips();
        } else {
            ships = gameState.getBoardB().getShips();
        }

        //print a button with full width for each ship in the leftPanel
        for (Ship ship : ships) {
            JButton shipButton = new JButton();
            shipButton.setSize(200, 50);
            shipButton.setText("Ship " + ship.getId());
            shipButton.addActionListener(e -> {
                System.out.println("Selected ship: " + ship.getId());
                selectedShip = ship;
            });
            leftPanel.add(shipButton);
        }

        //this.battleshipBoard.repaint();

        if (gameState.getStatus() == GameState.GameStatus.BUILD_GAME_BOARD) {
            this.timerThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);

                        if (this.gameState.getBuildGameBoardFinished().getTime() < System.currentTimeMillis()) {
                            //stop this thread
                            System.out.println("Time is up!");
                            this.gameHandler.getClientHandler().sendMessage(new GameStateRequestMessage(this.gameHandler.getGameId(), this.gameHandler.getSecret(), this.gameHandler.getUserId()));
                            this.timerThread.interrupt();
                            this.timerThread = null;
                            return;
                        }

                        // Update the info label with the remaining time in 00:00 format
                        long remainingTime = gameState.getBuildGameBoardFinished().getTime() - System.currentTimeMillis();
                        long minutes = (remainingTime / 1000) / 60;
                        long seconds = (remainingTime / 1000) % 60;
                        setInfoText(String.format("Remaining time: %02d:%02d", minutes, seconds));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            this.timerThread.start();
        }
    }

    public void setInfoText(String text) {
        this.infoLabel.setText(text);
    }
}
