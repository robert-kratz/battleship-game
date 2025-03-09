package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class JoinQueueMessage extends Message {

    public JoinQueueMessage() {
        super(MessageType.JOIN_QUEUE);
    }
}
