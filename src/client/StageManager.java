package client;

import client.gui.*;
import protocol.GameState;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Date;
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

    private final ClientHandler clientHandler;

    private Stage currentStage = Stage.LOBBY_SCENE;

    private final Dimension BIG_DIMENSION = new Dimension(900, 530);
    private final Dimension SMALL_DIMENSION = new Dimension(430, 270);
    private final Dimension GAME_OVER_DIMENSION = new Dimension(510, 590);

    public StageManager(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        lobbyScene = new LobbyScene(clientHandler);
        lobbyScene.setUsername(clientHandler.getUsername());
        addScene(Stage.LOBBY_SCENE, lobbyScene);

        createGameScene = new CreateGameScene(clientHandler);
        addScene(Stage.CREATE_GAME_SCENE, createGameScene);

        switchScene(Stage.LOBBY_SCENE);

        setVisible(true);
    }

    public void startWaitingLobbyScene() {
        if(!this.clientHandler.getGameHandler().getGameState().getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        if(this.gameWaitingScene != null) return;

        this.gameWaitingScene = new GameWaitingScene(this.clientHandler.getGameHandler());

        addScene(Stage.GAME_WAITING_SCENE, gameWaitingScene);
        switchScene(Stage.GAME_WAITING_SCENE);
    }

    public void startBuildScene() {
        if(!this.clientHandler.getGameHandler().getGameState().getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        if(this.gameBuildScene != null) return; //Allow only one build scene being initialized

        this.gameBuildScene = new GameBuildScene(this.clientHandler.getGameHandler());
        this.gameBuildSceneThread = new Thread(gameBuildScene);

        addScene(Stage.GAME_BUILD_SCENE, gameBuildScene);
        switchScene(Stage.GAME_BUILD_SCENE);
        this.gameBuildSceneThread.start();
    }

    public void startInGameScene(ArrayList<Ship> playerShips) {

        System.out.println("c");

        if(this.gameIngameScene != null) return;

        System.out.println("d");

        this.gameBuildSceneThread.interrupt();

        this.gameIngameScene = new GameInGameScene(this.clientHandler.getGameHandler(), playerShips);
        this.gameIngameSceneThread = new Thread(gameIngameScene);

        addScene(Stage.GAME_IN_GAME_SCENE, gameIngameScene);
        switchScene(Stage.GAME_IN_GAME_SCENE);

        this.gameIngameSceneThread.start();
    }

    public void startGameOverScene() {

        if(this.gameOverScene != null) return;

        try {
            this.gameIngameSceneThread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.gameOverScene = new GameOverScene(this.clientHandler.getGameHandler());

        adaptScreenSize();

        addScene(Stage.GAME_END_SCENE, gameOverScene);
        switchScene(Stage.GAME_END_SCENE);
    }

    /**
     * Call this method to exit the game scene and return to the lobby, assuming the
     */
    public void exitGameScene() {
        if(this.gameWaitingScene != null) {
            removeScene(Stage.GAME_WAITING_SCENE);
            this.gameWaitingScene = null;
        }

        if(this.gameBuildScene != null) {
            this.gameBuildSceneThread.interrupt();
            removeScene(Stage.GAME_BUILD_SCENE);
            this.gameBuildScene = null;
        }

        if(this.gameIngameScene != null) {
            this.gameIngameSceneThread.interrupt();
            removeScene(Stage.GAME_IN_GAME_SCENE);
            this.gameIngameScene = null;
        }

        this.lobbyScene.updateQueueButton();

        switchScene(Stage.LOBBY_SCENE);

        adaptScreenSize();
    }

    private void addScene(Stage stage, JPanel panel) {
        scenes.put(stage, panel);
        mainPanel.add(panel, stage.toString());
    }

    private void removeScene(Stage stage) {
        try {
            scenes.remove(stage);
            mainPanel.remove(scenes.get(stage));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                setResizable(false);
                break;
            case GAME_WAITING_SCENE:
            case GAME_BUILD_SCENE:
            case GAME_IN_GAME_SCENE:
                setSize(BIG_DIMENSION);
                setMinimumSize(BIG_DIMENSION);
                setResizable(false);
                break;
            case GAME_END_SCENE:
                setSize(GAME_OVER_DIMENSION);
                setMinimumSize(GAME_OVER_DIMENSION);
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
            System.err.println("Unable to switch to scene: " + stage);
        }
    }

    public int getWindowsWidth() {
        return getWidth();
    }

    public int getWindowsHeight() {
        return getHeight();
    }
}