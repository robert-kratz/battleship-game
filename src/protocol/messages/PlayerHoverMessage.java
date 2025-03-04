package protocol.messages;

import protocol.game.Cell;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerHoverMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private int x;
    private int y;
    private ArrayList<Cell> affectedFields;

    public PlayerHoverMessage(PlayerHoverMessage message) {
        super(MessageType.PLAYER_HOVER);
        this.userId = message.userId;
        this.x = message.x;
        this.y = message.y;
        this.affectedFields = message.affectedFields;
    }

    public PlayerHoverMessage(UUID userId, int x, int y, ArrayList<Cell> affectedFields) {
        super(MessageType.PLAYER_HOVER);
        this.userId = userId;
        this.x = x;
        this.y = y;
        this.affectedFields = affectedFields;

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ArrayList<Cell> getAffectedFields() {
        return affectedFields;
    }

    public UUID getUserId() {
        return userId;
    }
}
