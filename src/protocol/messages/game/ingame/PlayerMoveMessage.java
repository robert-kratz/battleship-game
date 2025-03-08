package protocol.messages.game.ingame;

import protocol.game.Move;
import protocol.messages.Message;
import protocol.messages.MessageType;

public class PlayerMoveMessage extends Message {

    private static final long serialVersionUID = 1L;

    private Move move;

    public PlayerMoveMessage(Move move) {
        super(MessageType.PLAYER_MOVE);
        this.move = move;
    }

    public Move getMove() {
        return move;
    }
}
