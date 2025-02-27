package protocol.messages;

import protocol.game.Hit;

public class PlayerMoveMessage extends Message {

    private static final long serialVersionUID = 1L;

    public PlayerMoveMessage(Hit hit) {
        super(MessageType.PLAYER_MOVE);
    }

    public Hit getHit() {
        return null;
    }
}
