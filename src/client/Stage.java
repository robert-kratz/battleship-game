package client;

/**
 * Enum representing the different stages of the game.
 */
public enum Stage {

    LOBBY_SCENE(""),
    CREATE_GAME_SCENE("Create Game"),
    GAME_WAITING_SCENE("Waiting for game to start"),
    GAME_BUILD_SCENE("Build your board"),
    GAME_IN_GAME_SCENE("Game"),
    GAME_END_SCENE("Game Over"),;

    private final String windowTitle;

    Stage(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public String getWindowTitle() {
        return windowTitle;
    }
}
