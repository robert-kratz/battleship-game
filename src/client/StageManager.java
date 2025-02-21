package client;

import client.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

public class StageManager extends JFrame {

    //These are persistent for the whole game
    public LobbyScene lobbyScene;
    public CreateGameScene createGameScene;

    public GameGUI gameGUI; //This is dynamic for the current Game

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Map<Stages, JPanel> scenes;

    private final GameHandler gameHandler;

    public enum Stages {
        LOBBY_SCENE,
        CREATE_GAME_SCENE,
        GAME_SCENE
    }

    public StageManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.scenes = new HashMap<>();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(430, 270));
        setSize(430, 270);
        setResizable(true);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                System.out.println("StageManager: Resized: " + getWidth() + "x" + getHeight());
            }
        });

        initScenes();
        setVisible(true);
    }

    public void initScenes() {
        //LobbyScene
        lobbyScene = new LobbyScene(gameHandler);
        lobbyScene.setUsername(gameHandler.getUsername());

        addScene(Stages.LOBBY_SCENE, lobbyScene);

        //CreateGameScene
        createGameScene = new CreateGameScene(this.gameHandler);
        addScene(Stages.CREATE_GAME_SCENE, createGameScene);

        switchScene(Stages.LOBBY_SCENE);
    }

    /**
     * ONLY BE CALLED WHEN THE USER STARTS A GAME
     * @param size The size of the game board
     */
    public void initGameScene(int size) {
        gameGUI = new GameGUI(this.gameHandler, size);
        addScene(Stages.GAME_SCENE, gameGUI);
        switchScene(Stages.GAME_SCENE);

        setMinimumSize(new Dimension(900, 530));

        System.out.println("GameScene initialized");
    }

    public void destroyGameScene() {
        if(gameGUI != null) {
            mainPanel.remove(gameGUI);
            gameGUI = null;
        }
    }

    /**
     * ONLY BE CALLED WHEN THE USER LEAVES THE GAME
     */
    public void removeGameScene() {
        if(gameGUI != null) {
            mainPanel.remove(gameGUI);
            gameGUI = null;
        }
    }

    private void addScene(Stages stage, JPanel panel) {
        scenes.put(stage, panel);
        mainPanel.add(panel, stage.toString());

        //Set the size of the panel depending on the stage
        if(stage.equals(Stages.LOBBY_SCENE) || stage.equals(Stages.CREATE_GAME_SCENE)) {
            panel.setSize(430, 270);
            panel.setMinimumSize(new Dimension(430, 270));
            setLocationRelativeTo(null);
        } else {
            panel.setSize(900, 530);
            setMinimumSize(new Dimension(900, 530));
            setLocationRelativeTo(null);
        }
    }

    public void switchScene(Stages stage) {
        if (scenes.containsKey(stage)) {
            cardLayout.show(mainPanel, stage.toString());

            setTitle(stage.toString());
        } else {
            System.err.println("Szene nicht gefunden: " + stage.toString());
        }
    }

    public void setDimensions(int width, int height) {
        setSize(width, height);
        setLocationRelativeTo(null);
    }

    public int getWindowsWidth() {
        return getWidth();
    }

    public int getWindowsHeight() {
        return getHeight();
    }

    public GameHandler getGameHandler() {
        return gameHandler;
    }
}