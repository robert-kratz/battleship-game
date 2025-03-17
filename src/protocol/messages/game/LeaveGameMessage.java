package protocol.messages.game;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the LeaveGameMessage which is sent from the client to the server when a player leaves a game.
 * It is sent from the client to the server to confirm that the player has left the game.
 * This client does not await a response from the server; It will terminate the game session and game thread.
 * A response to this message from the server could be:
 * - ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS); if the player is not in a game.
 */
public class LeaveGameMessage extends Message {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new LeaveGameMessage instance.
     */
    public LeaveGameMessage() {
        super(MessageType.LEAVE_GAME); // 0.5P Für Übergabe an Message
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
