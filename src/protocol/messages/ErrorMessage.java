package protocol.messages;

import protocol.ErrorType;

public class ErrorMessage extends Message {
    private final ErrorType error;

    public ErrorMessage(ErrorType error) {
        super(MessageType.ERROR);
        System.out.println("Error: " + error);
        this.error = error;

        try {
            throw new Error(error.toString());
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    public ErrorType getError() {
        return error;
    }

    @Override
    public String toString() {
        return "Error: " + error;
    }
}