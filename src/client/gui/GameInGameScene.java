package client.gui;

import client.GameHandler;

import javax.swing.*;
import java.awt.*;

public class GameInGameScene extends JPanel implements Runnable {

    private int height, width;

    private GameHandler gameHandler;

    private JPanel leftPanel;
    private JPanel rightPanel;

    public GameInGameScene(GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        leftPanel = new JPanel();

        rightPanel = new JPanel();

        leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 804));
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.add(new JLabel("GAMEBOARD"));

        leftPanel.add(new JLabel("INGAME"));

        rightPanel = new JPanel();
        rightPanel.setPreferredSize(new Dimension(200, 804));
        rightPanel.setBackground(Color.LIGHT_GRAY);
        rightPanel.add(new JLabel("Test-Items"));

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    @Override
    public void run() {
        System.out.println("GameIngameScene started");
    }
}
