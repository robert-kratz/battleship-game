package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class QuitQueueMessage extends Message {

    public QuitQueueMessage() {
        super(MessageType.LEAVE_QUEUE);
    }
}
