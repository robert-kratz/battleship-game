package client.gui;

import client.ClientHandler;
import client.GameHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameGUI extends JPanel {

    public enum GameGuiState {
        GAME_WAITING_SCENE,
        GAME_BUILD_SCENE,
        GAME_IN_GAME_SCENE,
    }

    private GameGuiState state = GameGuiState.GAME_WAITING_SCENE;
    private GameBuildScene gameBuildScene;
    private GameIngameScene gameIngameScene;
    private GameHandler gameHandler;
    private final int size;

    public GameWaitingScene gameWaitingScene;

    public GameGUI(GameHandler gameHandler, int size) {
        this.size = size;
        this.gameHandler = gameHandler;

        setVisible(true);

        this.gameWaitingScene = new GameWaitingScene(gameHandler);
        //this.gameIngameScene = new GameIngameScene(gameHandler, size);

        add(gameWaitingScene);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                //force repaint for the build
                if (gameBuildScene != null) {
                    gameBuildScene.updateDimensions(getWidth(), getHeight());
                }

                System.out.println("GameGui: Resized: " + getWidth() + "x" + getHeight());
            }
        });
    }

    public void startGame() {
        switchScene(GameGuiState.GAME_BUILD_SCENE);
    }

    public void switchScene(GameGuiState state) {
        this.state = state;

        removeAll();

        switch (state) {
            case GAME_WAITING_SCENE:
                add(gameWaitingScene);
                break;
            case GAME_BUILD_SCENE:
                gameBuildScene = new GameBuildScene(gameHandler);
                add(gameBuildScene);
                this.gameHandler.getStageManager().setDimensions(1204, 816);
                break;
            case GAME_IN_GAME_SCENE:
                //gameIngameScene = new GameIngameScene(gameHandler, size);
                //add(gameIngameScene);
                break;
        }

        revalidate();
        repaint();

        gameBuildScene.run();
    }
}
