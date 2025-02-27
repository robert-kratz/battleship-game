package protocol;

public enum ErrorType {
    SERVER_CLOSED("Server closed"),
    INVALID_GAME_SIZE("Invalid game size"),
    INVALID_SESSION_CODE("Invalid session code"),
    INVALID_PLACEMENT("Invalid placement"),
    INVALID_SECRET("Invalid secret"),
    NO_GAME_IN_PROGRESS("No game in progress"),
    INVALID_PLAYER("Invalid player"),
    ALREADY_IN_QUEUE("Already in queue"),
    NOT_IN_QUEUE("Not in queue");

    private final String message;

    ErrorType(String message) {
        this.message = "An error occurred";
    }

    public String getMessage() {
        return message;
    }
}
