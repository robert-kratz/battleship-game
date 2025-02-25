package client;

import client.gui.*;
import protocol.GameState;

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

    public GameWaitingScene gameWaitingScene;

    public GameBuildScene gameBuildScene;
    public Thread gameBuildSceneThread;

    public GameInGameScene gameIngameScene;
    public Thread gameIngameSceneThread;

    public GameOverScene gameOverScene;

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final Map<Stage, JPanel> scenes = new HashMap<>();
    private final GameHandler gameHandler;

    private Stage currentStage = Stage.LOBBY_SCENE;
    private boolean isGameRunning = false;

    private final Dimension BIG_DIMENSION = new Dimension(900, 530);
    private final Dimension SMALL_DIMENSION = new Dimension(430, 270);

    public StageManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        lobbyScene = new LobbyScene(gameHandler);
        lobbyScene.setUsername(gameHandler.getUsername());
        addScene(Stage.LOBBY_SCENE, lobbyScene);

        createGameScene = new CreateGameScene(this.gameHandler);
        addScene(Stage.CREATE_GAME_SCENE, createGameScene);

        switchScene(Stage.LOBBY_SCENE);

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

        setVisible(true);
    }

    public void startWaitingLobbyScene() {
        if(!this.gameHandler.getGameState().getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        if(this.gameWaitingScene != null) return;

        this.isGameRunning = true;
        this.gameWaitingScene = new GameWaitingScene(gameHandler);

        addScene(Stage.GAME_WAITING_SCENE, gameWaitingScene);
        switchScene(Stage.GAME_WAITING_SCENE);
    }

    public void startBuildScene() {
        if(!this.gameHandler.getGameState().getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        if(this.gameBuildScene != null) return; //Allow only one build scene being initialized

        this.gameBuildScene = new GameBuildScene(gameHandler);
        this.gameBuildSceneThread = new Thread(gameBuildScene);

        addScene(Stage.GAME_BUILD_SCENE, gameBuildScene);
        switchScene(Stage.GAME_BUILD_SCENE);
        this.gameBuildSceneThread.start();
    }

    public void startInGameScene() {
        if(!this.gameHandler.getGameState().getStatus().equals(GameState.GameStatus.IN_GAME)) return;

        if(this.gameIngameScene != null) return;

        this.gameBuildSceneThread.interrupt();

        this.gameIngameScene = new GameInGameScene(gameHandler);
        this.gameIngameSceneThread = new Thread(gameIngameScene);

        addScene(Stage.GAME_IN_GAME_SCENE, gameIngameScene);
        switchScene(Stage.GAME_IN_GAME_SCENE);

        this.gameIngameSceneThread.start();
    }

    private void addScene(Stage stage, JPanel panel) {
        scenes.put(stage, panel);
        mainPanel.add(panel, stage.toString());
    }

    private void removeScene(Stage stage) {
        scenes.remove(stage);
        mainPanel.remove(scenes.get(stage));
    }

    /**
     * Call This method to adapt the screen size depending on the current stage
     */
    public void adaptScreenSize() {

        System.out.println("Adapting screen size for stage: " + currentStage);

        switch (currentStage) {
            case LOBBY_SCENE:
            case CREATE_GAME_SCENE:
                setSize(SMALL_DIMENSION);
                setMinimumSize(SMALL_DIMENSION);
                setResizable(true);
                break;
            case GAME_WAITING_SCENE:
            case GAME_BUILD_SCENE:
            case GAME_IN_GAME_SCENE:
            case GAME_END_SCENE:
                setSize(BIG_DIMENSION);
                setMinimumSize(BIG_DIMENSION);
                setResizable(false);
                break;
        }
    }

    public void switchScene(Stage stage) {
        if (scenes.containsKey(stage)) {
            cardLayout.show(mainPanel, stage.toString());

            this.currentStage = stage;

            setTitle(stage.toString());
            adaptScreenSize();
        } else {
            System.err.println("Szene nicht gefunden: " + stage.toString());
        }
    }

    public boolean isGameRunning() {
        return isGameRunning;
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