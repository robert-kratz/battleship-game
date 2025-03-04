package protocol.messages;

import protocol.game.Move;

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
