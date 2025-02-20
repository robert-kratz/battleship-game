package client;

import client.gui.BattleshipBoard;
import client.gui.GameBoard;
import client.gui.HomeScreen;
import client.gui.MapBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class StageManager extends JFrame {

    public HomeScreen homeScreen;
    public GameBoard gameBoard;
    public MapBuilder mapBuilder;
    public BattleshipBoard battleshipBoard;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Map<Stages, JPanel> scenes;

    private final ClientHandler clientHandler;

    public enum Stages {
        MAIN_MENU,
        GAME,
        BOARD,
        MAP_BUILDER
    }

    public StageManager(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.scenes = new HashMap<>();
        setTitle("Stage Manager - Scene Handling");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1204, 816);
        setResizable(false);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        initializeScenes();
        setVisible(true);
    }

    private void initializeScenes() {
        // Szenen erstellen
        homeScreen = new HomeScreen(clientHandler);
        homeScreen.setUsername(clientHandler.getUsername());
        gameBoard = new GameBoard(12, 12);

        mapBuilder = new MapBuilder(clientHandler, 12, 12);

        // Szenen hinzufügen
        addScene(Stages.MAIN_MENU, homeScreen);
        addScene(Stages.BOARD, gameBoard);
        addScene(Stages.MAP_BUILDER, mapBuilder);

        switchScene(Stages.MAIN_MENU);
    }

    public void addScene(Stages stage, JPanel panel) {
        scenes.put(stage, panel);
        mainPanel.add(panel, stage.toString());  // Hier wird kein JFrame mehr verwendet!
    }

    public void switchScene(Stages stage) {
        if (scenes.containsKey(stage)) {
            cardLayout.show(mainPanel, stage.toString());
            setTitle(stage.toString());
        } else {
            System.err.println("Szene nicht gefunden: " + stage.toString());
        }
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }
}