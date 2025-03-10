package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class QuitQueueMessage extends Message {

    /**
     * Creates a new QuitQueueMessage instance.
     */
    public QuitQueueMessage() {
        super(MessageType.LEAVE_QUEUE);
    }
}
