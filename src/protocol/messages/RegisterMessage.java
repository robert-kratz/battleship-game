package protocol.messages;

import java.util.UUID;

public class RegisterMessage extends Message {
    private final String username;
    private final UUID userId, secret;
    private final int queueLength;

    public RegisterMessage(String username, UUID userId, UUID secret, int queueLength) {
        super(MessageType.REGISTER);
        this.username = username;
        this.userId = userId;
        this.secret = secret;
        this.queueLength = queueLength;
    }

    public String getUsername() {
        return username;
    }

    public UUID getSecret() {
        return secret;
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