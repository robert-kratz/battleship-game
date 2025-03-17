package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This message is sent from the client to the server when a player wants to leave the queue.
 * A response to this message from the server will be a
 * - QueueUpdateMessage; which contains the size of the queue and whether the player is in the queue or not.
 * or
 * - ErrorMessage(ErrorType.ALREADY_IN_GAME); if the player is already in a game.
 */

public class LeaveQueueMessage extends Message {

	private static final long serialVersionUID = 1L;

	
    /**
     * Creates a new LeaveQueueMessage instance.
     */
    public LeaveQueueMessage() {
        super(MessageType.LEAVE_QUEUE);  // 0.5P Für Übergabe an Message
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
