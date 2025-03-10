package protocol.messages;

import java.util.UUID;

public class RegisterMessage extends Message {
    private final String username;
    private final UUID userId;

    public RegisterMessage(String username, UUID userId) {
        super(MessageType.REGISTER);
        this.username = username;
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Willkommen, " + username + "!";
    }
}