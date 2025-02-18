package protocol;

public class ErrorMessage extends Message {
    private final ErrorType error;

    public ErrorMessage(ErrorType error) {
        super(MessageType.ERROR);
        this.error = error;
    }

    public ErrorType getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Error: " + error;
    }
}