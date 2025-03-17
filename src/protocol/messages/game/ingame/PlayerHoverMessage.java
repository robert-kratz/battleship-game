package protocol.messages.game.ingame;

import protocol.game.Cell;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This message is sent from the client to the server when a player hovers over a cell.
 * This message is also received by the client from the server when the opponent hovers over a cell.
 * The client distinguishes between the two by checking if the userId is equal to its own.
 */

public class PlayerHoverMessage extends Message {

    private static final long serialVersionUID = 1L;
    
    private final UUID userId; // 1P Für Deklaration + Initialisierung (2x) + Gettet Methode
    private final int x; // 1P Für Deklaration + Initialisierung (2x) + Gettet Methode
    private final int y; // 1P Für Deklaration + Initialisierung (2x) + Gettet Methode
    private ArrayList<Cell> affectedFields; // 1P Für Deklaration + Initialisierung (2x) + Gettet Methode

    /**
     * Creates a new PlayerHoverMessage instance.
     * @param message The PlayerHoverMessage to copy.
     */
    public PlayerHoverMessage(PlayerHoverMessage message) {
        super(MessageType.PLAYER_HOVER); // 0.5P Für Übergabe an Message
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
        super(MessageType.PLAYER_HOVER); // 0.5P Für Übergabe an Message
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

    @Override
    public String toString() {
        return "PlayerHoverMessage{" +
                "userId=" + userId +
                ", x=" + x +
                ", y=" + y +
                ", affectedFields=" + affectedFields +
                '}';
    }
}
