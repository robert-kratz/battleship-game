package protocol.messages;

public class QueueUpdateMessage extends Message {

    private final int queueSize;
    private final boolean playerInQueue;

    public QueueUpdateMessage(int queueSize, boolean playerInQueue) {
        super(MessageType.QUEUE_UPDATE);

        this.queueSize = queueSize;
        this.playerInQueue = playerInQueue;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public boolean isPlayerInQueue() {
        return playerInQueue;
    }
}
