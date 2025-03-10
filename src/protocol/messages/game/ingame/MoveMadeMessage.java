package protocol.messages.game.ingame;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the MoveMadeMessage which is sent from the server to the client when a player makes a move.
 * This Message is in response to a PlayerMoveMessage; and triggers the client to update the game state and board.
 * It contains the game state of the game that the player is a part of.
 * This message is broadcast to all players in the game.
 */

public class MoveMadeMessage extends Message {

    public static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new MoveMadeMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public MoveMadeMessage(GameState gameState) {
        super(MessageType.MOVE_MADE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }
}
