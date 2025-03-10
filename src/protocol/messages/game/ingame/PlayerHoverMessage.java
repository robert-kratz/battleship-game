package protocol.messages.game.ingame;

import protocol.game.Cell;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerHoverMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private int x;
    private int y;
    private ArrayList<Cell> affectedFields;

    /**
     * Creates a new PlayerHoverMessage instance.
     * @param message The PlayerHoverMessage to copy.
     */
    public PlayerHoverMessage(PlayerHoverMessage message) {
        super(MessageType.PLAYER_HOVER);
        this.userId = message.userId;
        this.x = message.x;
        this.y = message.y;
        this.affectedFields = message.affectedFields;
    }

    /**
     * Creates a new PlayerHoverMessage instance.
     * @param userId The user id of the player that made the move.
     * @param x the x coordinate of the cell.
     * @param y the y coordinate of the cell.
     * @param affectedFields the list of affected fields.
     */
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
