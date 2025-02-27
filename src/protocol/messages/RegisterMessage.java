package protocol.messages;

import java.util.UUID;

public class RegisterMessage extends Message {
    private final String username;
    private final UUID userId;
    private final int queueLength;

    public RegisterMessage(String username, UUID userId, int queueLength) {
        super(MessageType.REGISTER);
        this.username = username;
        this.userId = userId;
        this.queueLength = queueLength;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getQueueLength() {
        return queueLength;
    }

    @Override
    public String toString() {
        return "Willkommen, " + username + "!";
    }
}