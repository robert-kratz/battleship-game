package protocol.messages;

public class QueueUpdateMessage extends Message {

    private final int queueSize;

    public QueueUpdateMessage(int queueSize) {
        super(MessageType.QUEUE_UPDATE);

        this.queueSize = queueSize;
    }

    public int getQueueSize() {
        return queueSize;
    }
}
