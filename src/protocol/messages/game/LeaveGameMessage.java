package protocol.messages.game;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class LeaveGameMessage extends Message {

    private static final long serialVersionUID = 1L;

    public LeaveGameMessage() {
        super(MessageType.LEAVE_GAME);
    }
}
