package protocol.messages;

import protocol.ErrorType;

public class ErrorMessage extends Message {

    private final ErrorType error;

    /**
     * Creates a new ErrorMessage instance.
     * @param error The error type.
     */
    public ErrorMessage(ErrorType error) {
        super(MessageType.ERROR);
        System.err.println("Error: " + error);
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