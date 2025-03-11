package client;

/**
 * Enum representing the different sound types used in the game.
 */
public enum SoundType {

    CLICK("sounds/mouse-click.wav"),
    ERROR("sounds/error.wav"),
    CLOCK_TICK("sounds/clock-tick.wav");

    private final String filePath;

    SoundType(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}