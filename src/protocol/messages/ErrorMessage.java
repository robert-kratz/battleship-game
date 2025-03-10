package protocol.messages;

import protocol.ErrorType;

/**
 * This is the ErrorMessage which is sent from the server to the client when an error occurs.
 * It contains the error type.
 * It will be sent from the server to the client to inform the player about the error.
 */

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