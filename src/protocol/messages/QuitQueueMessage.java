package protocol.messages;

public class QuitQueueMessage extends Message {

    public QuitQueueMessage() {
        super(MessageType.JOIN_QUEUE);
    }
}
