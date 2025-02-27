package protocol.messages;

import java.util.UUID;

public class PlayerHoverMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private int x;
    private int y;

    public PlayerHoverMessage(UUID userId, int x, int y) {
        super(MessageType.PLAYER_HOVER);
        this.userId = userId;
        this.x = x;
        this.y = y;
    }

    public PlayerHoverMessage(UUID userId) {
        super(MessageType.PLAYER_HOVER);
        this.userId = userId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public UUID getUserId() {
        return userId;
    }
}
