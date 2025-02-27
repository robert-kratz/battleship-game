package protocol.messages;

public class JoinGameWithCodeMessage extends Message {

    private int sessionCode;

    public JoinGameWithCodeMessage(int sessionCode) {
        super(MessageType.JOIN_GAME_WITH_CODE);

        this.sessionCode = sessionCode;

    }

    public int getSessionCode() {
        return sessionCode;
    }
}
