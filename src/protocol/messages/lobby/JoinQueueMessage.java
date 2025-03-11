package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This message is sent from the client to the server when a player wants to join the queue.
 * A response to this message from the server will be a
 * - QueueUpdateMessage; which contains the size of the queue and whether the player is in the queue or not.
 * or
 * - ErrorMessage(ErrorType.ALREADY_IN_GAME); if the player is already in a game.
 */

public class JoinQueueMessage extends Message {

    /**
     * Creates a new JoinQueueMessage instance.
     */
    public JoinQueueMessage() {
        super(MessageType.JOIN_QUEUE);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
