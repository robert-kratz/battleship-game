package protocol.messages.lobby;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class CreateGameMessage extends Message {

    private int size;

    public CreateGameMessage(int size) {
        super(MessageType.CREATE_GAME);

        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
