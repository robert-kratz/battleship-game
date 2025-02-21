package protocol.messages;

public class LeaveGameMessage extends Message {

    private static final long serialVersionUID = 1L;

    public LeaveGameMessage() {
        super(MessageType.LEAVE_GAME);
    }
}
