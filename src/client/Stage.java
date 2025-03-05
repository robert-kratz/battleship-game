package client;

public enum Stage {
    LOBBY_SCENE(""),
    CREATE_GAME_SCENE("Create Game"),
    GAME_WAITING_SCENE("Waiting for game to start"),
    GAME_BUILD_SCENE("Build your ship"),
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
