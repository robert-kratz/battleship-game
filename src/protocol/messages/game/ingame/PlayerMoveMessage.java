package protocol.messages.game.ingame;

import protocol.game.Move;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This message is sent from the client to the server when a player makes a move.
 * A response to this message from the server will be a:
 * - MoveMadeMessage; which contains the game state of the game that has started.
 * or
 * - ErrorMessage(ErrorType.NO_GAME_IN_PROGRESS); if the player is not in a game.
 * - ErrorMessage(ErrorType.INVALID_MOVE); if the move is invalid.
 * - ErrorMessage(ErrorType.NOT_ENOUGH_ENERGY); if the player does not have enough energy to make the move.
 */

public class PlayerMoveMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final Move move;

    /**
     * Creates a new PlayerMoveMessage instance.
     * @param move The move made by the player.
     */
    public PlayerMoveMessage(Move move) {
        super(MessageType.PLAYER_MOVE);
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public String toString() {
        return "PlayerMoveMessage{" +
                "move=" + move +
                '}';
    }
}
