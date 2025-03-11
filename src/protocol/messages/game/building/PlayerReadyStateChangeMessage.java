package protocol.messages.game.building;

import protocol.GameState;
import protocol.messages.Message;
import protocol.messages.MessageType;

/**
 * This is the PlayerReadyStateChangeMessage which is sent from the server to the client when the player is ready.
 * This message is broadcast to all players in the game in response to a PlayerReadyMessage
 * to inform them that the player is ready.
 */

public class PlayerReadyStateChangeMessage extends Message {

    private static final long serialVersionUID = 1L;

    private final GameState gameState;

    /**
     * Creates a new PlayerReadyStateChangeMessage instance.
     * @param gameState The game state of the game that has started.
     */
    public PlayerReadyStateChangeMessage(GameState gameState) {
        super(MessageType.BUILD_READY_STATE_CHANGE);
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String toString() {
        return "PlayerReadyStateChangeMessage{" +
                "gameState=" + gameState +
                '}';
    }
}
