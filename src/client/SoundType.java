package client;

public enum SoundType {
    CLICK("sounds/mouse-click.wav"),
    ERROR("sounds/error.wav");

    private final String filePath;

    SoundType(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
}