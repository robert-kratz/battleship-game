package protocol.messages.game.ingame;

import protocol.game.Cell;
import protocol.messages.Message;
import protocol.messages.MessageType;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Aufgabe 1
 */

public class PlayerHoverMessage { // TODO

    private static final long serialVersionUID = 1L; // Sie können diese Zeile ignorieren. Nicht Löschen!

    // TODO

    /**
     * Creates a new PlayerHoverMessage instance.
     * @param message The PlayerHoverMessage to copy.
     */
    public PlayerHoverMessage(PlayerHoverMessage message) {
        // TODO
    }

    /**
     * Creates a new PlayerHoverMessage instance.
     * @param userId The user id of the player that made the move.
     * @param x the x coordinate of the cell.
     * @param y the y coordinate of the cell.
     * @param affectedFields the list of affected fields.
     */
    public PlayerHoverMessage(UUID userId, int x, int y, ArrayList<Cell> affectedFields) {
        // TODO
    }
}
