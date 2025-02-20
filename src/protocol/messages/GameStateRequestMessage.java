package protocol.messages;

import java.util.UUID;

public class GameStateRequestMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final UUID gameId, secret, userId;

    public GameStateRequestMessage(UUID gameID, UUID secret, UUID userId) {
        super(MessageType.GAME_STATE_REQUEST);

        this.gameId = gameID;
        this.secret = secret;
        this.userId = userId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSecret() {
        return secret;
    }
}
