package client;

import client.gui.*;
import protocol.GameState;
import protocol.Ship;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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

    /**
     * Constructor for StageManager.
     * @param clientHandler The client handler instance to be used for communication with the server.
     */
    public StageManager(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout) {
            @Override
            public Dimension getPreferredSize() {
                for (Component comp : getComponents()) {
                    if (comp.isVisible()) {
                        return comp.getPreferredSize();
                    }
                }
                return super.getPreferredSize();
            }
        };

        add(mainPanel);

        lobbyScene = new LobbyScene(clientHandler);
        lobbyScene.setUsername(clientHandler.getUsername());
        addScene(Stage.LOBBY_SCENE, lobbyScene);

        createGameScene = new CreateGameScene(clientHandler);
        addScene(Stage.CREATE_GAME_SCENE, createGameScene);

        switchScene(Stage.LOBBY_SCENE);

        adaptScreenSize();

        setVisible(true);
    }

    /**
     * Call this method to start the waiting lobby scene.
     */
    public void startWaitingLobbyScene() {
        if(!this.clientHandler.getGameHandler().getGameState().getStatus().equals(GameState.GameStatus.LOBBY_WAITING)) return;

        if(this.gameWaitingScene != null) return;

        this.gameWaitingScene = new GameWaitingScene(this.clientHandler.getGameHandler());

        addScene(Stage.GAME_WAITING_SCENE, gameWaitingScene);
        switchScene(Stage.GAME_WAITING_SCENE);
    }

    /**
     * Call this method to start the build scene.
     */
    public void startBuildScene() {

        if(this.clientHandler.getGameHandler() == null) return; // In case a player leaves the game before the build scene is created

        if(!this.clientHandler.getGameHandler().getGameState().getStatus().equals(GameState.GameStatus.BUILD_GAME_BOARD)) return;

        if(this.gameBuildScene != null) return; // Allow only one build scene being initialized

        this.gameBuildScene = new GameBuildScene(this.clientHandler.getGameHandler());
        this.gameBuildSceneThread = new Thread(gameBuildScene);

        addScene(Stage.GAME_BUILD_SCENE, gameBuildScene);
        switchScene(Stage.GAME_BUILD_SCENE);
        this.gameBuildSceneThread.start();
    }

    /**
     * Call this method to start the in-game scene.
     * @param playerShips The ships of the player.
     */
    public void startInGameScene(ArrayList<Ship> playerShips) {

        if(this.gameIngameScene != null) return;

        this.gameBuildSceneThread.interrupt();

        this.gameIngameScene = new GameInGameScene(this.clientHandler.getGameHandler(), playerShips);
        this.gameIngameSceneThread = new Thread(gameIngameScene);

        addScene(Stage.GAME_IN_GAME_SCENE, gameIngameScene);
        switchScene(Stage.GAME_IN_GAME_SCENE);

        this.gameIngameSceneThread.start();
    }

    /**
     * Call this method to start the game over scene.
     */
    public void startGameOverScene() {

        try {
            if(this.gameBuildSceneThread != null) gameBuildSceneThread.interrupt();
            if(this.gameIngameSceneThread != null) gameIngameSceneThread.interrupt();
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

    /**
     * Adds a scene to the stage manager.
     * @param stage The stage to be added.
     * @param panel The panel to be added.
     */
    private void addScene(Stage stage, JPanel panel) {
        scenes.put(stage, panel);
        mainPanel.add(panel, stage.toString());
    }

    /**
     * Removes a scene from the stage manager.
     * @param stage The stage to be removed.
     */
    private void removeScene(Stage stage) {
        try {
            scenes.remove(stage);
            if(scenes.get(stage) != null) mainPanel.remove(scenes.get(stage));
        } catch (Exception e) {
            System.err.println("Unable to remove scene: " + stage);
            e.printStackTrace();
        }
    }

    /**
     * Call This method to adapt the screen size depending on the current stage
     */
    public void adaptScreenSize() {

        setResizable(false);

        switch (currentStage) {
            case LOBBY_SCENE:
                this.lobbyScene.setPreferredSize(this.lobbyScene.getWindowSize());

                setSize(this.lobbyScene.getWindowSize());
                setMinimumSize(this.lobbyScene.getWindowSize());
                break;
            case CREATE_GAME_SCENE:
                this.createGameScene.setPreferredSize(this.createGameScene.getWindowSize());

                setSize(this.createGameScene.getWindowSize());
                setMinimumSize(this.createGameScene.getWindowSize());
                break;
            case GAME_WAITING_SCENE:
                this.gameWaitingScene.setPreferredSize(this.gameWaitingScene.getWindowSize());

                setSize(this.gameWaitingScene.getWindowSize());
                setMinimumSize(this.gameWaitingScene.getWindowSize());
                break;
            case GAME_BUILD_SCENE:
                this.gameBuildScene.setPreferredSize(this.gameBuildScene.getWindowSize());

                setSize(this.gameBuildScene.getWindowSize());
                setMinimumSize(this.gameBuildScene.getWindowSize());
                break;
            case GAME_IN_GAME_SCENE:
                this.gameIngameScene.setPreferredSize(this.gameIngameScene.getWindowSize());

                setSize(this.gameIngameScene.getWindowSize());
                setMinimumSize(this.gameIngameScene.getWindowSize());
                break;
            case GAME_END_SCENE:
                this.gameOverScene.setPreferredSize(this.gameOverScene.getWindowSize());

                setSize(this.gameOverScene.getWindowSize());
                setMinimumSize(this.gameOverScene.getWindowSize());
                break;
        }
    }

    /**
     * Switches the current scene to the specified stage.
     * @param stage The stage to switch to.
     */
    public void switchScene(Stage stage) {
        if (scenes.containsKey(stage)) {
            cardLayout.show(mainPanel, stage.toString());

            this.currentStage = stage;

            setTitle(stage.getWindowTitle());
            adaptScreenSize();
            pack();
        } else {
            System.err.println("Unable to switch to scene: " + stage);
        }
    }
}