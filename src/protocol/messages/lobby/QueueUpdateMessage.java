package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This class represents a message that updates the player about the queue size and their status in the queue.
 * It contains the size of the queue and whether the player is in the queue or not.
 * It will be sent from the server to the client to inform the player about their queue status and a queue change.
 */

public class QueueUpdateMessage extends Message {

	private static final long serialVersionUID = 1L;

	
    private final int queueSize; // 0.5P Für Deklaration + Initialisierung + Gettet Methode
    private final boolean playerInQueue; // 0.5P Für Deklaration + Initialisierung + Gettet Methode

    /**
     * Creates a new QueueUpdateMessage instance.
     * @param queueSize The size of the queue.
     * @param playerInQueue True if the player is in the queue, false otherwise.
     */
    public QueueUpdateMessage(int queueSize, boolean playerInQueue) {
        super(MessageType.QUEUE_UPDATE); // 0.5P Für Übergabe an Message

        this.queueSize = queueSize;
        this.playerInQueue = playerInQueue;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public boolean isPlayerInQueue() {
        return playerInQueue;
    }

    @Override
    public String toString() {
        return "QueueUpdateMessage{" +
                "queueSize=" + queueSize +
                ", playerInQueue=" + playerInQueue +
                '}';
    }
}
