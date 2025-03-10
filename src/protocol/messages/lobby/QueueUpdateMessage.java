package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This class represents a message that updates the player about the queue size and their status in the queue.
 * It contains the size of the queue and whether the player is in the queue or not.
 * It will be sent from the server to the client to inform the player about their queue status and a queue change.
 */

public class QueueUpdateMessage extends Message {

    private final int queueSize;
    private final boolean playerInQueue;

    /**
     * Creates a new QueueUpdateMessage instance.
     * @param queueSize The size of the queue.
     * @param playerInQueue True if the player is in the queue, false otherwise.
     */
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
