package protocol.messages.game.ingame;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the PlayerTurnChangeMessage which is sent from the server to the client when the player turn changes.
 * It contains the game state of the game that the player is a part of.
 * This message is broadcast to all players in the game in response to:
 * - PlayerMoveMessage; A Player miss;
 * OR
 * - The turn Timer expired; And a force move was made.
 */

public class PlayerTurnChangeMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new PlayerTurnChangeMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public PlayerTurnChangeMessage(GameState gameState) {
        super(MessageType.TURN_CHANGE);

        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String toString() {
        return "PlayerTurnChangeMessage{" +
                "gameState=" + gameState +
                '}';
    }
}
