package protocol.messages;

import java.util.UUID;

/**
 * This class represents a message sent to the server when a player registers.
 * It contains the player's username and UUID.
 * It will be sent from the server to the client to confirm the registration.
 */

public class RegisterMessage extends Message {
    private final String username;
    private final UUID userId;

    /**
     * Constructor for RegisterMessage
     * @param username The username of the player
     * @param userId The UUID of the player
     */
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
        return "RegisterMessage{" +
                "username='" + username + '\'' +
                ", userId=" + userId +
                '}';
    }
}