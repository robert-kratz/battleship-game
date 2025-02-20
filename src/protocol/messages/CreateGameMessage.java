package protocol.messages;

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
