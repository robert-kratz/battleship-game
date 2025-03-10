package protocol.messages.game.building;

import protocol.messages.Message;
import protocol.messages.MessageType;

public class PlayerReadyMessage extends Message {

    private static final long serialVersionUID = 1L;

    public final boolean ready;

    /**
     * Creates a new PlayerReadyMessage instance.
     * @param ready True if the player is ready, false otherwise.
     */
    public PlayerReadyMessage(boolean ready) {
        super(MessageType.PLAYER_READY);
        this.ready = ready;
    }

    public boolean isReady() {
        return ready;
    }
}
