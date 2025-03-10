package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class JoinQueueMessage extends Message {

    /**
     * Creates a new JoinQueueMessage instance.
     */
    public JoinQueueMessage() {
        super(MessageType.JOIN_QUEUE);
    }
}
