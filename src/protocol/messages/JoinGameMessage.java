package protocol.messages;

public class JoinGameMessage extends Message {

    private int sessionCode;

    public JoinGameMessage(int sessionCode) {
        super(MessageType.JOIN_GAME);

        this.sessionCode = sessionCode;

    }

    public int getSessionCode() {
        return sessionCode;
    }
}
