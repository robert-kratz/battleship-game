package protocol.messages.game.building;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class PlayerReadyMessage extends Message {

    private static final long serialVersionUID = 1L;

    public final boolean ready;

    public PlayerReadyMessage(boolean ready) {
        super(MessageType.PLAYER_READY);
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }
}
