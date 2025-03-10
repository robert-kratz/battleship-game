package protocol.messages;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private final MessageType type;

    /**
     * Creates a new Message instance.
     * @param type The type of the message.
     */
    protected Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }
}